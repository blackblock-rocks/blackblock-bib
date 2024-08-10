package rocks.blackblock.bib.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
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
@SuppressWarnings("unused")
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
     * The current state of the server/world
     * @since    0.2.0
     */
    public enum State {
        CRITICAL(5, 2, 10, 1.0f, 90),
        OVERLOADED(4, 3, 15, 0.8f, 70),
        VERY_BUSY(3, 4, 10, 0.7f, 40),
        BUSY(2, 4, 8, 0.5f, 25),
        NORMAL(1, 2, 4, 0.0f, 0),
        IDLE(0, 0, 0, 0.0f, 0);

        private final int severity;
        private final int ramp_up_period;
        private final int recovery_period;
        private final float performance_modifier;
        private final int randomly_disabled_chance;

        State(int severity, int ramp_up_period, int recovery_period, float performance_modifier, int randomly_disabled_chance) {
            this.severity = severity;
            this.ramp_up_period = ramp_up_period;
            this.recovery_period = recovery_period;
            this.performance_modifier = performance_modifier;
            this.randomly_disabled_chance = randomly_disabled_chance;
        }

        /**
         * Get the severity of this state
         * @since    0.2.0
         */
        public int getSeverity() {
            return severity;
        }

        /**
         * Get the number of seconds to wait before ramping up
         * @since    0.2.0
         */
        public int getRampUpPeriod() {
            return ramp_up_period;
        }

        /**
         * Get the number of seconds to wait before recovering from one state to another
         * @since    0.2.0
         */
        public int getRecoveryPeriod() {
            return recovery_period;
        }

        /**
         * Get the performance modifier
         * @since    0.2.0
         */
        public float getPerformanceModifier() {
            return performance_modifier;
        }

        /**
         * Get the next higher state
         * @since    0.2.0
         */
        public State getNextLowerState() {
            if (this.ordinal() < State.values().length - 1) {
                return State.values()[this.ordinal() + 1];
            }
            return this;
        }

        /**
         * Get the next lower state
         * @since    0.2.0
         */
        public State getNextHigherState() {
            if (this.ordinal() > 0) {
                return State.values()[this.ordinal() - 1];
            }

            return this;
        }

        /**
         * Return the chance of enabling the random disabler
         * @since    0.2.0
         */
        public int getRandomlyDisabledChance() {
            return this.randomly_disabled_chance;
        }

        /**
         * Should the random disabler be enabled?
         * @since    0.2.0
         */
        public boolean isRandomlyDisabled() {
            if (this.randomly_disabled_chance == 0) {
                return false;
            }

            return BibRandom.hasChance(this.randomly_disabled_chance);
        }
    }

    /**
     * Performance information class
     * @since    0.2.0
     */
    public static class Info implements BibLog.Argable {

        // The current state of the server/world
        private State current_state = State.NORMAL;

        // The target state of the server/world
        private State target_state = State.NORMAL;

        // The number of seconds since the server was recovering
        private int recovery_seconds = 0;

        // The number of seconds since the server was ramping up
        private int ramp_up_seconds = 0;

        // The server is "critical"
        private boolean is_critical = false;

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

        // The current recovery progress (0.0 to 1.0)
        private float recovery_progress = 0.0f;

        // The current ramp-up progress (0.0 to 1.0)
        private float ramp_up_progress = 0.0f;

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

            // Determine the target state based on MSPT
            State new_target_state;
            if (mspt >= 52) {
                new_target_state = State.CRITICAL;
            } else if (mspt >= 45) {
                new_target_state = State.OVERLOADED;
            } else if (mspt >= 40) {
                new_target_state = State.VERY_BUSY;
            } else if (mspt >= 32) {
                new_target_state = State.BUSY;
            } else if (mspt >= 15) {
                new_target_state = State.NORMAL;
            } else {
                new_target_state = State.IDLE;
            }

            this.target_state = new_target_state;
            int new_target_state_severity = new_target_state.getSeverity();
            int current_state_severity = this.current_state.getSeverity();

            // Update current_state based on waterfall recovery and stepped ramp-up system
            if (new_target_state_severity > current_state_severity) {
                State next_state = this.current_state.getNextHigherState();
                int ramp_up_period = next_state.getRampUpPeriod();

                this.ramp_up_seconds++;

                this.recovery_progress = 0;
                this.ramp_up_progress = (float) this.ramp_up_seconds / (float) ramp_up_period;

                if (this.ramp_up_seconds >= ramp_up_period) {
                    this.current_state = next_state;
                    this.ramp_up_seconds = 0;
                    this.ramp_up_progress = 0;
                }
            } else if (new_target_state_severity < current_state_severity) {
                int recovery_period = this.current_state.getRecoveryPeriod();

                this.recovery_seconds++;

                this.ramp_up_progress = 0;
                this.recovery_progress = (float) this.recovery_seconds / (float) recovery_period;

                if (this.recovery_seconds >= recovery_period) {
                    this.current_state = this.current_state.getNextLowerState();
                    this.recovery_seconds = 0;
                    this.recovery_progress = 0;
                }
            } else {
                this.ramp_up_seconds = 0;
                this.recovery_seconds = 0;
                this.ramp_up_progress = 0;
                this.recovery_progress = 0;
            }

            // Update boolean flags based on current state
            this.is_critical = (this.current_state == State.CRITICAL);
            this.is_overloaded = (this.current_state == State.CRITICAL || this.current_state == State.OVERLOADED);
            this.is_busy = (this.current_state == State.VERY_BUSY || this.current_state == State.BUSY);
            this.is_normal = (this.current_state == State.NORMAL);
            this.is_idle = (this.current_state == State.IDLE);

            // Update random disabling
            this.randomly_disabled = this.current_state.isRandomlyDisabled();
        }

        /**
         * Create a MutableText representation of this instance
         * @since    0.2.0
         */
        public MutableText toTextLine() {

            MutableText line = Text.literal("");

            if (this.world != null) {
                // Loaded chunks (white)
                line.append(Text.literal(this.world.getChunkManager().getLoadedChunkCount() + "").formatted(Formatting.WHITE))
                        .append(" / ");
            } else {
                line.append(Text.literal("? / ").formatted(Formatting.WHITE));
            }

            // MSPT (color based on value)
            Formatting msptColor;
            if (this.mspt >= 50) {
                msptColor = Formatting.RED;
            } else if (this.mspt >= 35) {
                msptColor = Formatting.GOLD;
            } else {
                msptColor = Formatting.GREEN;
            }
            line.append(Text.literal(String.format("%.0f", this.mspt)).formatted(msptColor))
                    .append(" / ");

            // TPS (color based on value)
            Formatting tpsColor;
            if (this.tps < 15) {
                tpsColor = Formatting.RED;
            } else if (this.tps < 20) {
                tpsColor = Formatting.GOLD;
            } else {
                tpsColor = Formatting.GREEN;
            }
            line.append(Text.literal(String.format("%.0f", this.tps)).formatted(tpsColor))
                    .append(" / ");

            // Load %
            line.append(Text.literal(this.load + "%").formatted(Formatting.AQUA))
                    .append(" / ");

            // Current performance state with direction indicator
            Formatting stateColor = switch (this.current_state) {
                case CRITICAL, OVERLOADED -> Formatting.RED;
                case VERY_BUSY, BUSY -> Formatting.GOLD;
                case NORMAL -> Formatting.GREEN;
                case IDLE -> Formatting.AQUA;
            };

            String directionIndicator;
            Formatting directionColor;
            if (this.current_state.getSeverity() < this.target_state.getSeverity()) {
                directionIndicator = " »»»";
                directionColor = Formatting.RED;
            } else if (this.current_state.getSeverity() > this.target_state.getSeverity()) {
                directionIndicator = " «««";
                directionColor = Formatting.GREEN;
            } else {
                directionIndicator = " ===";
                directionColor = Formatting.YELLOW;
            }

            MutableText stateText = Text.literal(this.current_state.name()).formatted(stateColor)
                    .append(Text.literal(directionIndicator).formatted(directionColor));

            // Add hover tooltip for state change info
            if (this.current_state != this.target_state) {
                String stateChangeInfo;
                if (this.current_state.getSeverity() > this.target_state.getSeverity()) {
                    stateChangeInfo = "Recovering to " + this.target_state.name();
                } else {
                    stateChangeInfo = "Ramping up to " + this.target_state.name();
                }
                stateText.setStyle(stateText.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(stateChangeInfo))));
            }

            line.append(stateText);

            return line;
        }

        /**
         * Get the current state
         * @since    0.2.0
         */
        public State getCurrentState() {
            return this.current_state;
        }

        /**
         * Get the target state
         * @since    0.2.0
         */
        public State getTargetState() {
            return this.target_state;
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
         * Is the state critical?
         * @since    0.2.0
         */
        public boolean isCritical() {
            return this.is_critical;
        }

        /**
         * Is the state overloaded?
         * @since    0.2.0
         */
        public boolean isOverloaded() {
            return this.is_overloaded;
        }

        /**
         * Is the state busy?
         * @since    0.2.0
         */
        public boolean isBusy() {
            return this.is_busy;
        }

        /**
         * Is the state normal?
         * @since    0.2.0
         */
        public boolean isNormal() {
            return this.is_normal;
        }

        /**
         * Is the state idle?
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
         * Get the progress towards recovery to the next lower state (0.0 to 1.0)
         * @since    0.2.0
         */
        public float getRecoveryProgress() {
            return this.recovery_progress;
        }

        /**
         * Get the progress towards ramping up to the next higher state (0.0 to 1.0)
         * @since    0.2.0
         */
        public float getRampUpProgress() {
            return this.ramp_up_progress;
        }

        /**
         * Get the Arg representation for this instance
         * @since    0.2.0
         */
        @Override
        public BibLog.Arg toBBLogArg() {
            var result = BibLog.createArg(this)
                    .add("current_state", this.current_state)
                    .add("target_state", this.target_state)
                    .add("randomly_disabled", this.randomly_disabled)
                    .add("recovery_ticks", this.recovery_seconds)
                    .add("mspt", this.mspt)
                    .add("tps", this.tps)
                    .add("load", this.load)
                    .add("recovery_progress", this.getRecoveryProgress());

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
