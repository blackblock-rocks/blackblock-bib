package rocks.blackblock.bib.util;


import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.ContainerLootComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.BibMod;
import rocks.blackblock.bib.debug.logging.BibYarn;

import java.util.*;
import java.util.stream.Collectors;

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
    private static AnsiFormat BrightWhiteText = new AnsiFormat(BRIGHT_WHITE_TEXT());

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

    // Log memory addresses?
    public static boolean LOG_MEMORY_ADDRESSES;

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

        LOG_MEMORY_ADDRESSES = getEnvBoolean("LOG_MEMORY_ADDRESSES") || hasEnabledCategory("identity-hash-code");
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
    @NotNull
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
     * Log a TODO message
     *
     * @since    0.2.0
     *
     * @param    args  Multiple arguments
     */
    public static void todo(Object... args) {
        outputLevel(Level.WARN, concatenateArguments(args));
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
        Object[] as_args = {message};
        attention(as_args);
    }

    /**
     * Output to the Blackblock logger by grabbing some attention
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    message  The actual message
     */
    public static void attention(Object... message) {

        String output = "\n"
                + BoldYellowOnRed.format("»»»»»»»»»»»»»»»»»»»»»»»»»»»» Attention ««««««««««««««««««««««««««««") + "\n"
                + concatenateArguments(message) + "\n"
                + BoldYellowOnRed.format("===================================================================") + "\n";

        outputLevel(Level.WARN, output);
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

            if (arg instanceof Argable argable) {
                Arg temp = argable.toBBLogArg();

                if (temp != null) {
                    arg = temp;
                }
            }

            if (arg == null) {
                entry = "null";
            } else if (arg instanceof Arg) {
                entry = arg.toString();
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

                        Arg sarg = createArg(arg);
                        sarg.setFallbackContent(entry);
                        entry = sarg.toIndentedString(0);

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
     * Deobfuscate a string (if mappings are loaded)
     *
     * @since    0.2.0
     */
    public static String deobfuscateString(String input) {

        if (BibYarn.INSTANCE != null) {
            return BibYarn.INSTANCE.deobfuscateStackTrace(input);
        }

        return input;
    }

    /**
     * Print a (deobfuscated) stack trace
     *
     * @since    0.2.0
     */
    public static void printStackTrace() {
        Exception dummy = new Exception("Stack trace");
        String result = stringifyStackTrace(dummy);
        BibLog.log(result);
    }

    /**
     * Create a (deobfuscated) stack trace string
     *
     * @since    0.2.0
     */
    public static String createStackTrace() {
        Exception dummy = new Exception("Stack trace");
        return stringifyStackTrace(dummy);
    }

    /**
     * Create a (deobfuscated) stack trace string
     *
     * @since    0.2.0
     */
    private static String stringifyStackTrace(Throwable throwable) {

        StringBuilder builder = new StringBuilder(20);
        StackTraceElement[] trace = throwable.getStackTrace();
        int length = trace.length;

        builder.append(throwable).append("\n");

        for (int i = 0; i < length; ++i) {
            StackTraceElement traceElement = trace[i];
            builder.append("\tat ").append(traceElement).append("\n");
        }

        String result = builder.toString();

        if (BibYarn.INSTANCE != null) {
            result = BibYarn.INSTANCE.deobfuscateStackTrace(result);
        }

        return result;
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

        // When this is set, only this string will be used in `toString`
        private String full_override = null;

        // When this is set, this will be printed after the class name
        private String body_content = null;

        // When this is set, this will be printed when no properties are added
        private String fallback_body_content = null;

        public Arg(Object value) {
            this.value = value;

            if (LOG_MEMORY_ADDRESSES) {
                this.add("@address", Integer.toHexString(System.identityHashCode(value)));
            }

            boolean is_argable = value instanceof Argable;

            if (value instanceof String) {
                this.class_name = (String) value;
            } else if (value != null) {
                String name;

                if (value instanceof ItemStack stack) {
                    name = "ItemStack";
                    String item = stack.getItem().toString();

                    this.add("item", item);
                    this.add("count", stack.getCount());

                    ComponentMap components = stack.getComponents();

                    for (Component<?> component : components) {
                        this.add(String.valueOf(component.type()), component.value());
                    }
                } else if (value instanceof WrapperProtoChunk roc) {
                    name = "WrapperProtoChunk";
                    this.add("pos", roc.getPos());
                    this.add("status", roc.getStatus());
                } else if (value instanceof ProtoChunk proto) {
                    name = "ProtoChunk";
                    this.add("pos", proto.getPos());
                    this.add("status", proto.getStatus());
                } else if (value instanceof WorldChunk chunk) {
                    name = "WorldChunk";
                    this.add("pos", chunk.getPos());
                    this.add("world", chunk.getWorld());
                    this.add("status", chunk.getStatus());
                } else if (value instanceof Chunk chunk) {
                    name = "Chunk";
                    this.add("pos", chunk.getPos());
                    this.add("status", chunk.getStatus());
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
                    this.setContent(id.getNamespace() + ":" + BrightWhiteText.format(id.getPath()));
                } else if (value instanceof UUID uuid) {
                    name = "UUID";
                    this.add("uuid", uuid.toString());
                } else if (value instanceof ChunkStatus status) {
                    name = "ChunkStatus";
                    this.setContent(status.getId());
                } else if (value instanceof Packet packet) {

                    if (BibYarn.INSTANCE != null) {
                        name = BibYarn.INSTANCE.deobfuscateStackTrace(value.getClass().getSimpleName());
                    } else {
                        name = "Packet";
                    }

                    this.add("packet_id", packet.getPacketType());

                    if (packet instanceof EntityTrackerUpdateS2CPacket et) {
                        this.add("entity_id", et.id());
                        this.add("tracked_values", et.trackedValues());
                    }

                } else if (value instanceof PacketType<?> packet_type) {
                    name = "PacketType";

                    this.add("side", packet_type.side());
                    this.add("id", packet_type.id());
                } else if (value instanceof List list) {

                    name = list.getClass().getSimpleName();

                    int size = list.size();
                    this.add("size", size);

                    for (int i = 0; i < size; i++) {
                        if (i > 10) {
                            break;
                        }

                        this.add("" + i, list.get(i));
                    }

                } else if (value instanceof NetworkSide side) {
                    name = "NetworkSide";
                    this.setContent(side.name());
                } else if (value instanceof DataTracker.SerializedEntry<?> serialized) {

                    if (BibYarn.INSTANCE != null) {
                        name = BibYarn.INSTANCE.deobfuscateStackTrace(value.getClass().getSimpleName());
                    } else {
                        name = "DataTracker.SerializedEntry";
                    }

                    Object serialized_value = serialized.value();
                    String value_string = "" + serialized_value;

                    if (serialized_value instanceof Integer nr) {
                        value_string = nr + " (int)";
                    } else if (serialized_value instanceof Byte nr) {
                        value_string = nr + " (byte)";
                    } else if (serialized_value instanceof Float nr) {
                        value_string = nr + " (float)";
                    } else if (serialized_value instanceof Number nr) {
                        value_string = nr + " (" + nr.getClass().getSimpleName() + ")";
                    } else if (serialized_value != null) {
                        value_string = value_string + " (" + value.getClass().getSimpleName() + ")";
                    }

                    this.add("id", serialized.id());
                    this.add("value", value_string);
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

                } else if (value instanceof Map mmap) {

                    name = mmap.getClass().getSimpleName();

                    mmap.forEach((key, map_value) -> {

                        if (key instanceof String str) {
                            this.add(str, map_value);
                        }
                    });

                } else if (value instanceof ContainerComponent containerComponent) {

                    name = "ContainerComponent";

                    var items = containerComponent.stream().collect(Collectors.toSet());

                    this.add("size", items.size());
                    this.add("stacks", items);
                } else if (value instanceof ContainerLootComponent containerLoot) {

                    name = "ContainerLootComponent";
                    this.add("loot_table", containerLoot.lootTable());
                } else if (value instanceof LoreComponent loreComponent) {
                    name = "LoreComponent";
                    this.add("lines", loreComponent.lines());
                } else if (value instanceof RegistryKey<?> key) {
                    name = "RegistryKey";
                    this.add("registry", key.getRegistry());
                    this.add("identifier", key.getValue());
                } else if (value instanceof StructureStart structureStart) {
                    name = "StructureStart";

                    this.add("pos", structureStart.getPos());
                    this.add("structure", structureStart.getStructure());
                } else if (value instanceof Structure structure) {
                    name = "Structure";
                    this.add("type", structure.getType());
                    this.add("id", BibServer.getDynamicRegistry().getOrThrow(RegistryKeys.STRUCTURE).getId(structure));
                } else if (value instanceof StructureType<?> type) {
                    name = "StructureType";
                    this.add("id", Registries.STRUCTURE_TYPE.getId(type));
                } else if (value instanceof NbtComponent nbt_component) {
                    name = "NbtComponent";
                    this.add("nbt", nbt_component.getNbt());
                    this.add("size", nbt_component.getSize());
                } else if (value instanceof NbtCompound nbt_compound) {
                    name = "NbtCompound";

                    for (String key : nbt_compound.getKeys()) {
                        this.add(key, nbt_compound.get(key));
                    }
                } else if (value instanceof AbstractNbtNumber nbt_nr) {
                    name = nbt_nr.getClass().getSimpleName();
                    this.setContent(nbt_nr.numberValue() + "");
                } else if (value instanceof NbtString nbt_string) {
                    name = "NbtString";
                    this.setContent(nbt_string.asString());
                } else if (value instanceof SpawnGroup spawnGroup) {
                    name = "SpawnGroup";
                    this.add("name", spawnGroup.getName());
                    this.add("capacity", spawnGroup.getCapacity());
                    this.add("isPeaceful", spawnGroup.isPeaceful());
                    this.add("isRare", spawnGroup.isRare());
                    this.add("despawnStartRange", spawnGroup.getDespawnStartRange());
                } else if (value instanceof SpawnSettings.SpawnEntry entry) {
                    name = "SpawnSettings.SpawnEntry";
                    this.add("type", entry.type);
                    this.add("weight", entry.getWeight());
                    this.add("minGroupSize", entry.minGroupSize);
                    this.add("maxGroupSize", entry.maxGroupSize);
                } else if (value instanceof Weight weight) {
                    name = "Weight";
                    this.setContent(weight.getValue() + "");
                } else if (value instanceof EntityType entity_type) {
                    name = "EntityType";
                    this.add("key", entity_type.getTranslationKey());
                    this.add("group", entity_type.getSpawnGroup());
                    this.add("height", entity_type.getHeight());
                    this.add("width", entity_type.getWidth());
                } else if (value instanceof Collection collection) {
                    name = collection.getClass().getSimpleName();
                    this.add("size", collection.size());

                    int i = 0;

                    for (Object entry : collection) {
                        this.add("" + i, entry);
                        i++;

                        if (i > 10) {
                            break;
                        }
                    }

                } else if (value instanceof ChunkSection section) {
                    name = "ChunkSection";
                    this.add("is_empty", section.isEmpty());
                    this.add("has_random_ticks", section.hasRandomTicks());
                    this.add("block_state_container", section.getBlockStateContainer());
                    this.add("biome_container", section.getBiomeContainer());
                } else if (value instanceof PalettedContainer<?> container) {
                    name = "PalettedContainer";
                    this.add("packet_size", container.getPacketSize());
                } else if (value instanceof ReadableContainer<?> container) {
                    name = "ReadableContainer";
                    this.add("packet_size", container.getPacketSize());
                } else if (value instanceof Box box) {
                    name = "Box";
                    this.add("min_x", (int) box.minX);
                    this.add("min_y", (int) box.minY);
                    this.add("min_z", (int) box.minZ);
                    this.add("max_x", (int) box.maxX);
                    this.add("max_y", (int) box.maxY);
                    this.add("max_z", (int) box.maxZ);
                } else if (value instanceof Thread thread) {
                    name = thread.getClass().getSimpleName();
                    this.add("name", thread.getName());
                    this.add("state", thread.getState().toString());
                    this.add("priority", thread.getPriority());
                    this.add("is_daemon", thread.isDaemon());
                } else if (value instanceof TeleportTarget target) {
                    name = "TeleportTarget";
                    this.add("world", target.world());
                    this.add("pos", target.position());
                    this.add("missingRespawnBlock", target.missingRespawnBlock());
                    this.add("postTeleportTransition", target.postTeleportTransition());
                } else {
                    name = value.getClass().getSimpleName();

                    if (BibYarn.INSTANCE != null) {

                        if (name.startsWith("class_")) {
                            String deobfuscated = BibYarn.INSTANCE.deobfuscateSimpleClassName(name);

                            if (deobfuscated != null) {
                                name = deobfuscated;
                            }
                        }

                        if (name.contains("class_")) {
                            name = BibYarn.INSTANCE.deobfuscateStackTrace(name);
                        }
                    }

                    if (!is_argable && (name.isEmpty() || name.startsWith("class_"))) {
                        this.full_override = value + "";
                    }
                }

                this.class_name = name;
            }
        }

        /**
         * Set fallback content when no properties are available
         *
         * @since    0.2.0
         */
        public Arg setFallbackContent(String message) {
            this.fallback_body_content = this.cleanupContent(message);
            return this;
        }

        /**
         * Force the main content to display
         *
         * @since    0.2.0
         */
        public Arg setContent(String message) {
            this.body_content = this.cleanupContent(message);
            return this;
        }

        /**
         * Clean up the body content
         *
         * @since    0.2.0
         */
        private String cleanupContent(String message) {

            if (BibYarn.INSTANCE != null) {
                message = BibYarn.INSTANCE.deobfuscateStackTrace(message);
            }

            char first_char = message.charAt(0);

            if (first_char == '{' || first_char == '[') {
                message = message.substring(1, message.length() - 1);
            }

            if (this.class_name != null && !this.class_name.isEmpty()) {
                if (message.startsWith(this.class_name)) {
                    message = message.substring(this.class_name.length());
                    return this.cleanupContent(message);
                }

                String full_path = "net.minecraft." + this.class_name;
                if (message.startsWith(full_path)) {
                    message = message.substring(full_path.length());
                    return this.cleanupContent(message);
                }
            }

            return message;
        }

        /**
         * Add a property to output
         *
         * @since    0.1.0
         *
         * @param    key   The property key to display
         * @param    value The actual value to display
         */
        public Arg add(String key, Object value) {

            if (value instanceof String || value instanceof Number || value instanceof Boolean || value == null) {
                // Ok
            } else if (value instanceof Arg) {
                // Do nothing
            } else {
                value = createArgInternal(value);
            }

            this.properties.put(key, value);
            return this;
        }

        /**
         * Return this Arg instance to a serialized string
         * for debug purposes.
         * Also add an indentation of the given level at the beginning.
         *
         * @since    0.1.0
         *
         * @param    level   The current indentation level
         */
        public String toIndentedStringWithStart(int level) {

            StringBuilder builder = new StringBuilder();

            builder.append("  ".repeat(level));

            return builder.append(this.toIndentedString(level)).toString();
        }

        /**
         * Return this Arg instance to a serialized string
         * for debug purposes.
         *
         * @since    0.1.0
         *
         * @param    level   The current indentation level
         */
        public String toIndentedString(int level) {
            return this.toIndentedString(level, new WeakHashMap<>());
        }

        /**
         * Return this Arg instance to a serialized string
         * for debug purposes.
         *
         * @since    0.1.0
         *
         * @param    level   The current indentation level
         */
        public String toIndentedString(int level, WeakHashMap<Object, Boolean> seen) {

            // If there is a full_override string, return that
            if (this.full_override != null) {
                return MagentaText.format(this.full_override);
            }

            if (seen.containsKey(this)) {
                return "[circular]";
            }

            seen.put(this, true);

            if (this.value != null) {
                if (seen.containsKey(this.value)) {
                    return "[circular]";
                }

                seen.put(this.value, true);
            }

            StringBuilder builder = new StringBuilder();

            builder.append(MagentaText.format(class_name));

            String body_content = this.body_content;

            if (body_content == null && this.fallback_body_content != null && this.properties.isEmpty()) {
                body_content = this.fallback_body_content;
            }

            // If we have an explicit body_content string, use that
            if (body_content != null) {
                builder.append('{');
                builder.append(body_content);
                builder.append('}');

                return builder.toString();
            }

            builder.append("{");

            int i = 0;
            int indent_count = 0;
            int property_count = this.properties.size();
            boolean print_newlines = property_count > 1;
            int full_char_length = 0;

            Map<String, String> converted = new HashMap<>(property_count);

            for (Map.Entry<String, Object> entry : this.properties.entrySet()) {
                String key = entry.getKey();
                full_char_length += key.length();

                Object value = entry.getValue();
                String value_string;

                if (value instanceof Arg arg) {
                    // Indent with no level
                    value_string = arg.toIndentedString(0);
                } else {
                    value_string = "" + value;
                }

                full_char_length += value_string.length();
                converted.put(key, value_string);

                if (full_char_length > 60) {
                    break;
                }
            }

            if (full_char_length <= 60) {
                print_newlines = false;

                for (Map.Entry<String, String> entry : converted.entrySet()) {

                    if (i > 0) {
                        builder.append(",");
                    }

                    builder.append("  ".repeat(indent_count));

                    builder.append(WhiteText.format(entry.getKey()));
                    builder.append("=");
                    builder.append(entry.getValue());

                    i++;
                }

            } else {
                // Iterate again, this time stringifying with the correct indent size
                for (Map.Entry<String, Object> entry : this.properties.entrySet()) {

                    if (i > 0) {
                        builder.append(",");
                    }

                    if (print_newlines) {
                        builder.append("\n");
                        indent_count = level + 1;
                    }

                    builder.append("  ".repeat(indent_count));

                    builder.append(WhiteText.format(entry.getKey()));
                    builder.append("=");

                    if (entry.getValue() instanceof Arg arg) {
                        builder.append(arg.toIndentedString(indent_count));
                    } else {
                        builder.append(entry.getValue());
                    }

                    i++;
                }
            }

            if (print_newlines) {
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
