package rocks.blackblock.bib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import rocks.blackblock.bib.util.BibLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Something to easily create commands
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class CommandCreator {

    private static CommandLeaf BLACKBLOCK_ROOT = null;
    private static Map<String, CommandLeaf> ROOTS = new HashMap<>();

    /**
     * Get a root command leaf
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static CommandLeaf getRoot(String name) {

        CommandLeaf leaf = ROOTS.get(name);

        if (leaf == null) {
            leaf = new CommandLeaf(name, null);
            ROOTS.put(name, leaf);
        }

        return leaf;
    }

    /**
     * Get the Blackblock root.
     * This will automatically require the "commands.blackblock.root" permission.
     * @since    0.2.0
     */
    public static CommandLeaf getBlackblockRoot() {

        if (BLACKBLOCK_ROOT == null) {
            BLACKBLOCK_ROOT = CommandCreator.getPermissionRoot("blackblock", "commands.blackblock.root");
        }

        return BLACKBLOCK_ROOT;
    }

    /**
     * Get a custom root with the given required permission
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static CommandLeaf getPermissionRoot(String name, String permission) {
        CommandLeaf root = getRoot(name);
        root.requires(permission);
        root.setProtectDirectChildren(true);
        return root;
    }

    /**
     * Register all our command leafs
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void registerAll(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        BibLog.log("Registering all Blackblock commands!");

        for (String root_name : ROOTS.keySet()) {
            CommandLeaf root_leaf = ROOTS.get(root_name);
            var built_root = root_leaf.register(dispatcher, registryAccess, environment);

            if (built_root instanceof LiteralArgumentBuilder literal_root) {
                dispatcher.register(literal_root);
            } else {
                BibLog.error("Failed to register Blackblock command '" + root_name + "', not a LiteralArgument:", built_root);
            }
        }
    }
}