package xyz.destiall.sgames.match;

import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.lobby.Lobby;
import xyz.destiall.sgames.map.Map;
import xyz.destiall.sgames.map.MapInfo;
import xyz.destiall.sgames.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;

public class MatchFactory implements Callable<Match> {
    private final Stack<Stage> stages;
    private final Future<Match> future;
    private final AtomicBoolean timedOut;

    public MatchFactory(Lobby lobby, MapInfo map) {
        this.stages = new Stack<>();
        this.stages.push(new DownloadMapStage(lobby, map));
        this.timedOut = new AtomicBoolean(false);
        this.future = Executors.newSingleThreadExecutor().submit(this);
    }

    public Match call() {
        try {
            return run();
        } catch (Exception e) {
            if (e.getCause() instanceof InterruptedException) throw e;

            Throwable err = e.getCause();
            SGames.INSTANCE.getLogger().log(Level.SEVERE, err.getMessage(), err.getCause());
            throw e;
        }
    }

    private Match run() {
        Stage stage;
        Future<? extends Stage> next;

        while (!stages.empty()) {
            stage = stages.peek();
            next = stage.advance();

            // Only wait if the next stage is not done, or
            // the entire factory is not timed out.
            final long delay = stage.delay().toMillis();
            while (!next.isDone() && !timedOut.get()) {
                try {
                    Thread.sleep(Math.max(100, delay));
                } catch (InterruptedException e) {
                    return revert(e);
                }
            }

            // Get the next stage and capture its exception
            final Stage done;
            try {
                done = next.get();
            } catch (ExecutionException | InterruptedException e) {
                return revert(e);
            }

            // If there is no other stage, commit the match.
            if (done == null) {
                stages.clear();
                if (stage instanceof Commitable) {
                    return ((Commitable) stage).commit();
                }
                return revert(new IllegalStateException("Unable to load match with an incomplete stage"));
            } else {
                stages.push(done);
            }
        }

        return revert(new IllegalStateException("Unable to load match without an initial stage"));
    }

    private Match revert(Exception err) {
        while (!stages.empty()) {
            final Stage stage = stages.pop();
            if (stage instanceof Revertable) {
                ((Revertable) stage).revert();
            } else {
                throw new IllegalStateException("Unable to revert a loaded match");
            }
        }

        throw new RuntimeException(err);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        if (isDone()) return false;
        return future.cancel(mayInterruptIfRunning);
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean isDone() {
        return future.isDone();
    }

    public Match get() throws InterruptedException, ExecutionException {
        return get(0, TimeUnit.MILLISECONDS);
    }

    public Match get(long duration, TimeUnit unit) throws InterruptedException, ExecutionException {
        return future.get();
    }

    public void await() {
        timedOut.set(true); // Will disable all delays from any stage
    }

    private interface Stage {
        Future<? extends Stage> advance();
        default Duration delay() {
            return Duration.ZERO;
        }
    }

    private interface Revertable {
        void revert();
    }

    private interface Commitable {
        Match commit();
    }

    private static class DownloadMapStage implements Stage, Revertable {
        private final Lobby lobby;
        private final MapInfo map;
        private File dir;

        private DownloadMapStage(Lobby lobby, MapInfo map) {
            this.lobby = lobby;
            this.map = checkNotNull(map);
        }

        private File getDirectory() {
            if (dir == null) {
                dir = new File(SGames.INSTANCE.getServer().getWorldContainer(), "match" + File.separator);
            }
            return dir;
        }

        private InitWorldStage advanceSync() {
            final File dir = getDirectory();
            FileUtils.delete(dir);

            if (dir.mkdirs()) {
                try {
                    FileUtils.copy(map.getMapFolder(), dir, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return new InitWorldStage(lobby, map, dir.getName());
        }

        @Override
        public Future<InitWorldStage> advance() {
            return runAsyncThread(this::advanceSync);
        }

        @Override
        public void revert() {
            FileUtils.delete(getDirectory());
        }
    }

    private static class InitWorldStage implements Stage, Revertable {
        private final MapInfo map;
        private final Lobby lobby;
        private final String worldName;

        private InitWorldStage(Lobby lobby, MapInfo map, String worldName) {
            this.lobby = lobby;
            this.map = checkNotNull(map);
            this.worldName = checkNotNull(worldName);
        }

        private Stage advanceSync() throws IllegalStateException {
            WorldCreator creator = new WorldCreator(worldName);

            final World world =
                    SGames.INSTANCE
                            .getServer()
                            .createWorld(creator
                                            .environment(World.Environment.NORMAL)
                                            .seed(creator.seed()));
            if (world == null) throw new IllegalStateException("Unable to load a null world");

            world.setPVP(true);
            world.setSpawnFlags(false, false);
            world.setAutoSave(false);
            world.setDifficulty(Difficulty.NORMAL);
            world.setKeepSpawnInMemory(true);

            Map map = new Map(this.map, world);

            return new InitMatchStage(lobby, map);
        }

        @Override
        public Future<Stage> advance() {
            return runMainThread(this::advanceSync);
        }

        private boolean revertSync() {
            return SGames.INSTANCE.getServer().unloadWorld(worldName, false);
        }

        @Override
        public void revert() {
            runMainThread(this::revertSync);
        }

        @Override
        public Duration delay() {
            return Duration.ofSeconds(3);
        }
    }

    private static class InitMatchStage implements Stage, Revertable, Commitable {
        private final Match match;
        private final Lobby lobby;

        private InitMatchStage(Lobby lobby, Map map) {
            this.lobby = lobby;
            this.match = new Match(map);
        }

        private MoveMatchStage advanceSync() {
            final boolean move = SGames.INSTANCE.getMatchManager().getMatch() != null;
            match.load();
            return move ? new MoveMatchStage(lobby, match) : null;
        }

        @Override
        public Future<? extends Stage> advance() {
            return runMainThread(this::advanceSync);
        }

        private boolean revertSync() {
            match.unload();
            return true;
        }

        @Override
        public void revert() {
            runMainThread(this::revertSync);
        }

        @Override
        public Match commit() {
            return match;
        }
    }

    private static class MoveMatchStage implements Stage, Commitable {
        private final Match match;
        private final Duration delay;
        private final Lobby lobby;

        private MoveMatchStage(Lobby lobby, Match match) {
            this.match = checkNotNull(match);
            this.delay = Duration.ZERO;
            this.lobby = lobby;
        }

        private Stage advanceSync() {
            match.addCompetitors(lobby.queue());

            return null;
        }

        @Override
        public Future<? extends Stage> advance() {
            return runMainThread(this::advanceSync);
        }

        @Override
        public Duration delay() {
            return delay;
        }

        @Override
        public Match commit() {
            return match;
        }
    }

    private static <V> Future<V> runMainThread(Callable<V> task) {
        return SGames.INSTANCE.getServer().getScheduler().callSyncMethod(SGames.INSTANCE, task);
    }

    private static <V> CompletableFuture<V> runAsyncThread(Callable<V> task) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return task.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
