package rocks.blackblock.bib.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.monitor.GlitchGuru;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Library class for working with the main server instance
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibServer {

    // The actual server instance
    private static MinecraftServer SERVER = null;

    // Is the server fully ready?
    private static boolean SERVER_IS_READY = false;

    // A list of things to do when the `MinecraftServer` object is available
    private static final List<Consumer<MinecraftServer>> SERVER_STARTING_HANDLERS = new ArrayList<>();

    // A list of things to do when the server has started
    private static final List<Consumer<MinecraftServer>> SERVER_STARTED_HANDLERS = new ArrayList<>();

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibServer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Is the server ready? (Are all worlds loaded?)
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean isReady() {
        return SERVER_IS_READY;
    }

    /**
     * Get the server instance, which is available after the server has started.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Nullable
    public static MinecraftServer getServer() {
        return SERVER;
    }

    /**
     * The Minecraft server is starting
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @ApiStatus.Internal()
    public static void setServerWhenStarting(MinecraftServer server) {
        SERVER = server;

        // Call all server start handlers
        for (Consumer<MinecraftServer> handler : SERVER_STARTING_HANDLERS) {
            handler.accept(server);
        }

        SERVER_STARTING_HANDLERS.clear();
    }

    /**
     * The Minecraft server has started
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @ApiStatus.Internal()
    public static void setServerWhenStarted(MinecraftServer server) {

        SERVER_IS_READY = true;

        // Make sure any other "starting" callbacks are called
        setServerWhenStarting(server);

        // Call all server start handlers
        for (Consumer<MinecraftServer> handler : SERVER_STARTED_HANDLERS) {
            handler.accept(server);
        }

        SERVER_STARTED_HANDLERS.clear();
    }

    /**
     * Perform the consumer once we have a server.
     * The server might not be ready yet.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void withServer(Consumer<MinecraftServer> consumer) {
        if (SERVER != null) {
            consumer.accept(SERVER);
        } else {
            SERVER_STARTING_HANDLERS.add(consumer);
        }
    }

    /**
     * Perform the consumer once we have a server that is fully ready.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void withReadyServer(Consumer<MinecraftServer> consumer) {
        if (SERVER_IS_READY) {
            consumer.accept(SERVER);
        } else {
            SERVER_STARTED_HANDLERS.add(consumer);
        }
    }

    /**
     * Get the current tick count
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static int getTick() {

        if (SERVER == null) {
            return -1;
        }

        return SERVER.getTicks();
    }

    /**
     * Register a throwable/error
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void registerThrowable(Throwable t, String message) {
        GlitchGuru.registerThrowable(t, message);
    }

    /**
     * Get the main world directory
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Path getMainWorldDirectory() {
        return FabricLoader.getInstance().getGameDir().resolve("world");
    }

    /**
     * Get the main world data directory
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Path getMainDataDirectory() {
        return getMainWorldDirectory().resolve("data");
    }

    /**
     * Get the server's dynamic registry
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static DynamicRegistryManager getDynamicRegistry() {

        if (SERVER != null) {
            return SERVER.getRegistryManager();
        }

        return DynamicRegistryManager.EMPTY;
    }
}
