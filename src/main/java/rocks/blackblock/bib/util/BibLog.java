package rocks.blackblock.bib.util;


import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import org.apache.logging.log4j.Level;
import rocks.blackblock.bib.BibMod;

import java.util.*;

import static com.diogonunes.jcolor.Attribute.*;

/**
 * A colourful logger class.
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
public class BibLog {

    // Log colours & formats
    private static AnsiFormat YellowText = new AnsiFormat(BRIGHT_YELLOW_TEXT());
    private static AnsiFormat RedText = new AnsiFormat(BRIGHT_RED_TEXT());
    private static AnsiFormat GreenText = new AnsiFormat(BRIGHT_GREEN_TEXT());
    private static AnsiFormat BlueText = new AnsiFormat(BRIGHT_BLUE_TEXT());
    private static AnsiFormat MagentaText = new AnsiFormat(BRIGHT_MAGENTA_TEXT());
    private static AnsiFormat WhiteText = new AnsiFormat(WHITE_TEXT());

    private static Attribute BLACK_BACK = BACK_COLOR(0, 0, 0);
    private static AnsiFormat BoldYellowOnRed = new AnsiFormat(YELLOW_TEXT(), RED_BACK(), BOLD());
    private static AnsiFormat BoldGrayOnBlack = new AnsiFormat(TEXT_COLOR(120, 120, 120), BLACK_BACK, BOLD());
    private static AnsiFormat CyanOnBlack = new AnsiFormat(CYAN_TEXT(), BLACK_BACK);
    private static AnsiFormat RedOnBlack = new AnsiFormat(RED_TEXT(), BLACK_BACK);

    private static long last_log = 0;

    // All notification handlers
    private static List<NotificationHandler> notification_handlers = new ArrayList<>();

    // See if debug mode is on (environment variable)
    public static boolean DEBUG = getEnvBoolean("DEBUG");

    // Get the debug level
    public static int DEBUG_LEVEL = getEnvInt("DEBUG_LEVEL", 0);

    // The logging categories that are enabled
    public static Set<String> enabled_categories = new HashSet<>();

    // Is logging enabled?
    public static final boolean VERBOSE_LOGGING;

    static {
        String categories = getEnv("LOGGING_CATEGORIES");
        boolean enable_verbose_logging = false;

        if (!categories.isEmpty()) {
            String[] parts = categories.split(",");
            enabled_categories.addAll(Arrays.asList(parts));
            enable_verbose_logging = true;
        }

        if (DEBUG) {
            enabled_categories.add("debug");
            enable_verbose_logging = true;
        }

        if (!enabled_categories.isEmpty()) {
            BibLog.attention("Enabled categories: " + enabled_categories);
        }

        VERBOSE_LOGGING = enable_verbose_logging;
    }

    /**
     * Returns true if the given category is enabled
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean hasEnabledCategory(String category) {
        if (!VERBOSE_LOGGING) {
            return false;
        }

        return enabled_categories.contains(category);
    }

    /**
     * Returns true if one of the given categories is enabled
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean hasEnabledCategory(String... categories) {

        if (!VERBOSE_LOGGING) {
            return false;
        }

        for (String category : categories) {
            if (enabled_categories.contains(category)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Execute the runnable if the log category is enabled
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean onEnabledCategory(String category, Runnable runnable) {

        if (!VERBOSE_LOGGING) {
            return false;
        }

        if (!enabled_categories.contains(category)) {
            return false;
        }

        runnable.run();

        return true;
    }

    /**
     * Execute the runnable if one of the categories is enabled
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean onEnabledCategory(String[] categories, Runnable runnable) {

        if (!VERBOSE_LOGGING) {
            return false;
        }

        for (String category : categories) {
            if (enabled_categories.contains(category)) {
                runnable.run();
                return true;
            }
        }

        return false;
    }

    /**
     * Get a categorised logger
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Categorised getCategorised(String category) {

        if (!VERBOSE_LOGGING) {
            return new Categorised();
        }

        return new Categorised(category);
    }

    /**
     * Get a categorised logger
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Categorised getCategorised(String... categories) {

        if (!VERBOSE_LOGGING) {
            return new Categorised();
        }

        return new Categorised(categories);
    }

    /**
     * Get an environment variable
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static String getEnv(String name) {
        String result = System.getenv(name);

        if (result == null) {
            return "";
        }

        return result;
    }

    /**
     * Get an environment variable as a boolean
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean getEnvBoolean(String name) {

        String result = getEnv(name);

        if (result.isEmpty()) {
            return false;
        }

        return result.equals("1") || result.equalsIgnoreCase("true");
    }

    /**
     * Get an environment variable as an integer
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static int getEnvInt(String name, int default_value) {

        String result = getEnv(name);

        if (result.isEmpty()) {
            return default_value;
        }

        try {
            return Integer.parseInt(result);
        } catch (NumberFormatException nfe) {
            return default_value;
        }
    }

    /**
     * Get the BB prefix
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private static String getPrefix(Level level) {

        String result = BoldGrayOnBlack.format("[");

        if (level == Level.DEBUG) {
            result += RedOnBlack.format("BB");
        } else {
            result += CyanOnBlack.format("BB");
        }

        result += BoldGrayOnBlack.format("]") + " ";

        return result;
    }

    /**
     * Output
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    message  The actual message
     */
    public static void outputLevel(Level level, String message) {

        if (DEBUG) {
            long now = System.currentTimeMillis();

            if (now - last_log > 500) {
                BibMod.LOGGER.log(level, "\n\n\n");
            }

            last_log = now;
        }

        BibMod.LOGGER.log(level, message);
    }

    /**
     * Output
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    builder  The actual message in a string builder
     */
    public static void outputLevel(Level level, StringBuilder builder) {
        outputLevel(level, getPrefix(level) + builder.toString());
    }

    /**
     * Output
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void outputLevel(Level level, Object[] args) {
        outputLevel(level, concatenateArguments(args));
    }

    /**
     * Output
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void outputLevel(Level level, Collection<Object> args) {
        outputLevel(level, concatenateArguments(args));
    }

    /**
     * Output to the Blackblock logger using the info level
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    args  Multiple arguments
     */
    public static void log(Object... args) {
        outputLevel(Level.INFO, concatenateArguments(args));
    }

    /**
     * Output to the Blackblock logger using the warning level
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    args  Multiple arguments
     */
    public static void warn(Object... args) {
        StringBuilder builder = concatenateArguments(args);
        outputLevel(Level.WARN, builder);

        String message = builder.toString() ;

        for (NotificationHandler handler : notification_handlers) {
            handler.receiveNotification(message, Level.WARN);
        }
    }

    /**
     * Output to the Blackblock logger using the debug level
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    args  Multiple arguments
     */
    public static void debug(Object... args) {
        outputLevel(Level.DEBUG, concatenateArguments(args));
    }

    /**
     * Output to the Blackblock logger using the error level
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    args  Multiple arguments
     */
    public static void error(Object... args) {
        StringBuilder builder = concatenateArguments(args);
        outputLevel(Level.ERROR, builder);

        String message = builder.toString();

        for (NotificationHandler handler : notification_handlers) {
            handler.receiveNotification(message, Level.WARN);
        }
    }

    /**
     * Output to the Blackblock logger by grabbing some attention
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    message  The actual message
     */
    public static void attention(Object message) {
        log("");
        log(BoldYellowOnRed.format("»»»»»»»»»» Attention ««««««««««"));
        log(Level.WARN, message);
        log(BoldYellowOnRed.format("==============================="));
        log("");
    }

    /**
     * Get a string builder for the given arguments
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    args  Multiple arguments
     */
    private static StringBuilder concatenateArguments(Collection<Object> args) {
        return concatenateArguments(args.toArray());
    }

    /**
     * Get a string builder for the given arguments
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    args  Multiple arguments
     */
    private static StringBuilder concatenateArguments(Object[] args) {

        StringBuilder builder = new StringBuilder();
        int i = 0;

        for (Object arg : args) {

            if (i > 0) {
                builder.append(" ");
            }

            String entry;

            if (arg == null) {
                entry = "null";
            } else {
                try {
                    entry = arg.toString();

                    if (arg instanceof Number) {
                        entry = BlueText.format(entry);
                    } else if (arg instanceof Boolean bool) {
                        if (bool) {
                            entry = GreenText.format(entry);
                        } else {
                            entry = RedText.format(entry);
                        }
                    } else if (arg instanceof String) {
                        entry = YellowText.format(entry);
                    } else if (arg instanceof BlockPos pos) {
                        entry = "BlockPos{" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "}";
                        entry = MagentaText.format(entry);
                    } else {

                        if (arg instanceof WrapperProtoChunk roc) {
                            entry = "WrapperProtoChunk{" + roc.getPos().x + ", " + roc.getPos().z + "}";
                        } else if (arg instanceof ProtoChunk proto) {
                            entry = "ProtoChunk{" + proto.getPos().x + ", " + proto.getPos().z + "}";
                        } else if (arg instanceof WorldChunk chunk) {
                            entry = "WorldChunk{" + chunk.getPos().x + ", " + chunk.getPos().z + "}";
                        } else if (arg instanceof Chunk chunk) {
                            entry = "Chunk{" + chunk.getPos().x + ", " + chunk.getPos().z + "}";
                        } else if (arg instanceof ServerWorld world) {
                            entry = "ServerWorld{" + world.getRegistryKey().getValue() + "}";
                        } else if (arg instanceof ServerWorldAccess world) {
                            entry = "ServerWorldAccess{" + world.toServerWorld().getRegistryKey().getValue() + "}";
                        } else if (arg instanceof Identifier id) {
                            entry = "Identifier{" + id.getNamespace() + ":" + id.getPath() + "}";
                        } else if (arg instanceof UUID uuid) {
                            entry = "UUID{" + uuid + "}";
                        } else if (arg instanceof PlayerEntity player) {
                            String world = player.getWorld() == null ? "~NULL~" : player.getWorld().toString();
                            BlockPos pos = player.getBlockPos();
                            entry = "Player{" + player.getName().getString() + ",l=" + world + ",x=" + pos.getX() + ",y=" + pos.getY() + ",z=" + pos.getZ() + "}";
                        }

                        entry = MagentaText.format(entry);
                    }
                } catch (Throwable t) {
                    entry = "Error formatting argument: " + t;
                    entry = RedText.format(entry);
                }
            }

            builder.append(entry);
            i++;
        }

        return builder;
    }

    public static Arg createArg(Object value) {
        return new Arg(value);
    }

    private static Arg createArgInternal(Object value) {

        if (value instanceof Argable argable) {
            return argable.toBBLogArg();
        }

        return createArg(value);
    }

    /**
     * Argument log helper
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static class Arg {
        public final Object value;
        public final Map<String, Object> properties = new HashMap<>();
        private String class_name = null;
        private String content = null;

        public Arg(Object value) {
            this.value = value;

            if (value instanceof String) {
                this.class_name = (String) value;
            } else if (value != null) {
                String name;

                if (value instanceof WrapperProtoChunk roc) {
                    name = "WrapperProtoChunk";
                    this.add("pos", roc.getPos());
                } else if (value instanceof ProtoChunk proto) {
                    name = "ProtoChunk";
                    this.add("pos", proto.getPos());
                } else if (value instanceof WorldChunk chunk) {
                    name = "WorldChunk";
                    this.add("pos", chunk.getPos());
                    this.add("world", chunk.getWorld());
                } else if (value instanceof Chunk chunk) {
                    name = "Chunk";
                    this.add("pos", chunk.getPos());
                } else if (value instanceof ServerWorld world) {
                    name = "ServerWorld";
                    this.add("name", world.getRegistryKey().getValue().toString());
                } else if (value instanceof ServerWorldAccess world) {
                    name = "ServerWorldAccess";
                    this.add("name", world.toServerWorld().getRegistryKey().getValue().toString());
                } else if (value instanceof BlockPos pos) {
                    name = "BlockPos";
                    this.add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ());
                } else if (value instanceof ChunkPos pos) {
                    name = "ChunkPos";
                    this.add("x", pos.x).add("z", pos.z);
                } else if (value instanceof Identifier id) {
                    name = "Identifier";
                    this.add("namespace", id.getNamespace()).add("path", id.getPath());
                } else if (value instanceof UUID uuid) {
                    name = "UUID";
                    this.add("uuid", uuid.toString());
                } else if (value instanceof Entity entity) {

                    if (entity instanceof PlayerEntity) {
                        name = "Player";
                    } else if (entity instanceof LivingEntity) {
                        name = "LivingEntity";
                    } else {
                        name = "Entity";
                    }

                    this.add("name", entity.getName().getString());

                    String world = entity.getWorld() == null ? "~NULL~" : entity.getWorld().toString();
                    BlockPos pos = entity.getBlockPos();

                    this.add("l", world);
                    this.add("x", pos.getX());
                    this.add("y", pos.getY());
                    this.add("z", pos.getZ());
                } else if (value instanceof PropertyMap pmap) {

                    name = "PropertyMap";

                    pmap.forEach((key, property) -> {
                        this.add(key, property.value());
                    });

                } else {
                    name = value.getClass().getSimpleName();

                    if (name.isEmpty() || name.startsWith("class_")) {
                        this.content = value + "";
                    }
                }

                this.class_name = name;
            }
        }

        public Arg add(String key, Object value) {

            if (value instanceof String || value instanceof Number || value instanceof Boolean || value == null) {
                // Ok
            } else {
                value = createArgInternal(value);
            }

            this.properties.put(key, value);
            return this;
        }

        public String toIndentedStringWithStart(int level) {

            StringBuilder builder = new StringBuilder();

            builder.append("  ".repeat(level));

            return builder.append(this.toIndentedString(level)).toString();
        }

        public String toIndentedString(int level) {

            StringBuilder builder = new StringBuilder();

            if (this.content != null) {
                builder.append(MagentaText.format(this.content));
                return builder.toString();
            }

            builder.append(MagentaText.format(class_name));

            builder.append("{");

            int i = 0;

            for (Map.Entry<String, Object> entry : properties.entrySet()) {

                if (i > 0) {
                    builder.append(",");
                }

                builder.append("\n");
                builder.append("  ".repeat(level + 1));

                builder.append(WhiteText.format(entry.getKey()));
                builder.append("=");

                if (entry.getValue() instanceof Arg arg) {
                    builder.append(arg.toIndentedString(level + 1));
                } else {
                    builder.append(entry.getValue());
                }

                i++;
            }

            if (i > 0) {
                builder.append("\n");
                builder.append("  ".repeat(level));
            }

            builder.append("}");

            return builder.toString();
        }

        @Override
        public String toString() {
            return this.toIndentedString(1);
        }
    }

    /**
     * A class for logging to a specific category
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static class Categorised {

        private final boolean enabled;

        public Categorised() {
            this.enabled = false;
        }

        public Categorised(String category) {
            this.enabled = BibLog.hasEnabledCategory(category);
        }

        public Categorised(String... categories) {
            this.enabled = BibLog.hasEnabledCategory(categories);
        }

        /**
         * Is this category enabled?
         *
         * @since    0.1.0
         */
        public boolean isEnabled() {
            return this.enabled;
        }

        /**
         * Do the runnable if the category is enabled
         *
         * @since    0.1.0
         */
        public boolean ifEnabled(Runnable runnable) {

            if (!this.enabled) {
                return false;
            }

            runnable.run();
            return true;
        }

        /**
         * Output to the Blackblock logger using the info level
         *
         * @since    0.1.0
         *
         * @param    args  Multiple arguments
         */
        public void log(Object... args) {

            if (!this.enabled) {
                return;
            }

            outputLevel(Level.INFO, concatenateArguments(args));
        }
    }

    /**
     * An interface to let instances return useful info
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public interface Argable {

        /**
         * Get the Arg representation for this instance
         */
        Arg toBBLogArg();
    }

    /**
     * An interface to let other instances be notified of an error
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @FunctionalInterface
    public interface NotificationHandler {
        void receiveNotification(String message, Level level);
    }
}
