package rocks.blackblock.bib.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.BibMod;
import rocks.blackblock.bib.collection.RollingAverage;
import rocks.blackblock.bib.interop.InteropServerCore;
import rocks.blackblock.bib.monitor.GlitchGuru;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Keep an eye on the performance of the server
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public class BibPerf {

    // Performance update listeners (for the global info)
    private static final List<Consumer<Info>> GLOBAL_PERF_LISTENERS = new ArrayList<>();

    // The server is "overloaded" when the MSPT goes over 50ms
    public static boolean SERVER_IS_OVERLOADED = false;

    // The server is "busy" when the MSPT goes over 35ms
    public static boolean SERVER_IS_BUSY = false;

    // The server is "normal" when the MSPT is between 15ms and 35ms
    public static boolean SERVER_IS_NORMAL = true;

    // The server is "idle" when the MSPT is between 0ms and 15ms
    public static boolean SERVER_IS_IDLE = true;

    // This has a chance of being true when the server is busy
    public static boolean RANDOMLY_DISABLED_WHEN_BUSY = false;

    // The current MSPT
    public static float MSPT = 0.0f;

    // The current TPS
    public static float TPS = 0.0f;

    // The current mobcap modifier
    public static double MOBCAP_MODIFIER = 1.0;

    // The current load percentage
    // Between 0 and 100 when not overloaded, else it can go higher
    public static int LOAD = 0;

    // Has the monitor been started?
    private static boolean started = false;

    // The timer that controls the main check task
    private static Timer task_timer = null;

    // The global performance info
    private static Info global_info = new Info();

    // The function that will get the perf info for the given world
    // (Will use the global info by default)
    private static Function<World, Info> world_info_getter = world -> global_info;

    /**
     * Register a performance update listener for the global info
     *
     * @since    0.2.0
     */
    public static void registerGlobalPerfListener(Consumer<Info> listener) {
        GLOBAL_PERF_LISTENERS.add(listener);
    }

    /**
     * The actual task that will be run
     *
     * @since    0.3.1
     */
    private static void performHealthChecks() {

        MinecraftServer server = BibServer.getServer();

        if (server == null) {
            return;
        }

        global_info.processAverageMspt(server.getAverageTickTime());
        int pct = global_info.load;

        if (BibMod.PLATFORM.isModLoaded("servercore")) {
            MOBCAP_MODIFIER = InteropServerCore.getMobcapModifier();

            if (MOBCAP_MODIFIER < 1) {
                if (pct < 100) {
                    pct = 100;
                } else {
                    pct = pct + (int) (100 - (100 * (MOBCAP_MODIFIER / 2)));

                    if (global_info.mspt < 50 && pct > 100) {
                        pct = 100;
                    }
                }
            }
        }

        global_info.load = pct;

        BibPerf.MSPT = global_info.mspt;
        BibPerf.TPS = global_info.tps;
        BibPerf.LOAD = pct;

        SERVER_IS_OVERLOADED = global_info.is_overloaded;
        SERVER_IS_BUSY = global_info.is_busy;
        SERVER_IS_NORMAL = global_info.is_normal;
        SERVER_IS_IDLE = global_info.is_idle;
        RANDOMLY_DISABLED_WHEN_BUSY = global_info.randomly_disabled;

        for (Consumer<Info> listener : GLOBAL_PERF_LISTENERS) {
            try {
                listener.accept(global_info);
            } catch (Throwable t) {
                GlitchGuru.registerThrowable(t, "Failed to run performance update listener");
            }
        }
    }

    /**
     * Set a new world info getter
     *
     * @since    0.2.0
     */
    public static void setWorldInfoGetter(Function<World, Info> getter) {

        if (getter == null) {
            world_info_getter = world -> global_info;
            return;
        }

        world_info_getter = getter;
    }

    /**
     * Get the perf info for the given world
     *
     * @since    0.2.0
     */
    public static Info getWorldInfo(World world) {
        return world_info_getter.apply(world);
    }

    /**
     * Ease In sine method
     *
     * @since    0.2.0
     */
    public static float easeInSine(float value) {
        return (float) (1 - Math.cos((value * Math.PI) / 2));
    }

    /**
     * Linear interpolation between 2 values
     *
     * @since    0.2.0
     */
    public static float linearInterpolation(float start_value, float end_value, float progress) {
        return start_value + (end_value - start_value) * progress;
    }

    /**
     * Start the monitor
     *
     * @since    0.2.0
     */
    @ApiStatus.Internal
    public static void start() {

        if (started) {
            return;
        }

        started = true;

        task_timer = new Timer(true);

        // Start the monitor
        task_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                performHealthChecks();
            }
        }, 2500, 2500);
    }

    /**
     * Performance information class
     * @since    0.2.0
     */
    public static class Info implements BibLog.Argable {

        // The server is "overloaded" when the MSPT goes over 50ms
        private boolean is_overloaded = false;

        // The server is "busy" when the MSPT goes over 35ms
        private boolean is_busy = false;

        // The server is "normal" when the MSPT is between 15ms and 35ms
        private boolean is_normal = true;

        // The server is "idle" when the MSPT is between 0ms and 15ms
        private boolean is_idle = true;

        // This has a chance of being true when the server is busy
        private boolean randomly_disabled = false;

        // The current MSPT
        private float mspt = 0.0f;

        // The current TPS
        private float tps = 0.0f;

        // The current load percentage
        // Between 0 and 100 when not overloaded, else it can go higher
        private int load = 0;

        // The rolling average calculator for MSPT
        private RollingAverage<Float> rolling_average = new RollingAverage<>();

        // The world this info is for
        private final World world;

        /**
         * Create a new Info instance
         * @since    0.2.0
         */
        public Info() {
            this.world = null;
        }

        /**
         * Create a new Info instance for the given world
         * @since    0.2.0
         */
        public Info(World world) {
            this.world = world;
        }

        /**
         * Get the world
         * @since    0.2.0
         */
        public World getWorld() {
            return this.world;
        }

        /**
         * Aggregate the given MSPT
         * This is called when the MSPT is not averaged.
         * @since    0.2.0
         */
        public void aggregateMspt(float mspt) {
            this.rolling_average.addValue(mspt);

            if (this.rolling_average.getCurrentIndex() == 0) {
                this.processAverageMspt((float) this.rolling_average.getAverage());
            }
        }

        /**
         * Process the given MSPT
         * @since    0.2.0
         */
        public void processAverageMspt(float mspt) {

            float tps = 1000/Math.max(mspt, 50);
            int pct = (int) ((mspt / 50) * 100);

            if (mspt < 50) {
                // Make the percentage ramp up a bit slower
                pct = (int) linearInterpolation(0, 100, easeInSine(pct / 100f));
            }

            this.tps = tps;
            this.mspt = mspt;
            this.load = pct;

            this.is_overloaded = false;
            this.is_busy = false;
            this.is_normal = false;
            this.is_idle = false;
            this.randomly_disabled = false;

            if (mspt >= 49) {
                this.is_overloaded = true;
                this.is_busy = true;
                this.randomly_disabled = BibRandom.hasChance(80);
            } else if (mspt >= 35) {
                this.is_busy = true;
                this.randomly_disabled = BibRandom.hasChance(20);
            } else if (mspt >= 15) {
                this.is_normal = true;
            } else {
                this.is_normal = true;
                this.is_idle = true;
            }
        }

        /**
         * Get the current TPS
         * @since    0.2.0
         */
        public float getTps() {
            return this.tps;
        }

        /**
         * Get the current MSPT
         * @since    0.2.0
         */
        public float getMspt() {
            return this.mspt;
        }

        /**
         * Get the current load percentage
         * @since    0.2.0
         */
        public int getLoad() {
            return this.load;
        }

        /**
         * Is the server overloaded?
         * @since    0.2.0
         */
        public boolean isOverloaded() {
            return this.is_overloaded;
        }

        /**
         * Is the server busy?
         * @since    0.2.0
         */
        public boolean isBusy() {
            return this.is_busy;
        }

        /**
         * Is the server normal?
         * @since    0.2.0
         */
        public boolean isNormal() {
            return this.is_normal;
        }

        /**
         * Is the server idle?
         * @since    0.2.0
         */
        public boolean isIdle() {
            return this.is_idle;
        }

        /**
         * Is the random disabler enabled?
         * @since    0.2.0
         */
        public boolean isRandomlyDisabled() {
            return this.randomly_disabled;
        }

        /**
         * Get the Arg representation for this instance
         * @since    0.2.0
         */
        @Override
        public BibLog.Arg toBBLogArg() {
            var result = BibLog.createArg(this)
                    .add("is_overloaded", this.is_overloaded)
                    .add("is_busy", this.is_busy)
                    .add("is_normal", this.is_normal)
                    .add("is_idle", this.is_idle)
                    .add("randomly_disabled", this.randomly_disabled)
                    .add("mspt", this.mspt)
                    .add("tps", this.tps)
                    .add("load", this.load);

            if (this.world != null) {
                result.add("world", this.world);
            }

            return result;
        }

        /**
         * Return a string representation of this instance
         * @since    0.2.0
         */
        @Override
        public String toString() {
            return this.toBBLogArg().toString();
        }
    }
}
