package rocks.blackblock.bib.util;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.jetbrains.annotations.Nullable;

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
    private static MinecraftDedicatedServer SERVER = null;

    // A list of things to do when the `MinecraftDedicatedServer` object is available
    private static final List<Consumer<MinecraftDedicatedServer>> SERVER_START_HANDLERS = new ArrayList<>();

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
     * Get the server instance, which is available after the server has started.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Nullable
    public static MinecraftDedicatedServer getServer() {
        return SERVER;
    }

    /**
     * Set the server instance.
     * This is automatically called by the `ServerMixin` class upon server start.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void setServer(MinecraftDedicatedServer server) {
        SERVER = server;

        // Call all server start handlers
        for (Consumer<MinecraftDedicatedServer> handler : SERVER_START_HANDLERS) {
            handler.accept(server);
        }

        SERVER_START_HANDLERS.clear();
    }

    /**
     * Perform the consumer once we have a server
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void withServer(Consumer<MinecraftDedicatedServer> consumer) {
        if (SERVER != null) {
            consumer.accept(SERVER);
        } else {
            SERVER_START_HANDLERS.add(consumer);
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

}
