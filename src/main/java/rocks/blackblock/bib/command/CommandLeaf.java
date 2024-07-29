package rocks.blackblock.bib.command;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rocks.blackblock.bib.interop.BibInterop;
import rocks.blackblock.bib.util.BibLog;
import rocks.blackblock.bib.util.BibPlayer;
import rocks.blackblock.bib.util.BibServer;

import java.util.*;
import java.util.function.Predicate;

/**
 * Represents a single command leaf
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class CommandLeaf {

    protected String name;
    protected CommandLeaf parent;
    protected Map<String, CommandLeaf> children = new HashMap<>();
    protected Command<ServerCommandSource> main_command = null;
    protected ArgumentType<?> argument_type = null;
    protected List<Predicate<ServerCommandSource>> requirements = null;
    protected SuggestionProvider<ServerCommandSource> suggestion_provider = null;
    protected Set<String> required_permissions = null;
    protected boolean protect_direct_children = false;

    /**
     * Create the new lea finstance
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public CommandLeaf(String name, CommandLeaf parent) {
        this.name = name;
        this.parent = parent;
    }

    /**
     * Get our permission string
     *
     * @since    0.2.0
     */
    private String constructPermissionString(boolean as_parent) {

        String result;

        if (this.parent != null) {
            result = this.parent.constructPermissionString(true);
            result += ".";
            result += this.name;
        } else {
            result = this.name + ".command";

            if (!as_parent) {
                result = result + ".root";
            }
        }

        return result;
    }

    /**
     * Should the children be protected by default?
     *
     * @since    0.2.0
     */
    public boolean getProtectDirectChildren() {
        return this.protect_direct_children;
    }

    /**
     * Set if the children should be protected by default
     *
     * @since    0.2.0
     */
    public void setProtectDirectChildren(boolean protect_direct_children) {
        this.protect_direct_children = protect_direct_children;
    }

    /**
     * Get a child leaf
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public CommandLeaf getChild(String name) {

        if (this.protect_direct_children) {
            return this.getProtectedChild(name);
        }

        return this.getUnprotectedChild(name);
    }

    /**
     * Get a child leaf without adding new protections.
     *
     * @since    0.2.0
     */
    public CommandLeaf getUnprotectedChild(String name) {
        CommandLeaf child = this.children.get(name);

        if (child == null) {
            child = new CommandLeaf(name, this);
            this.children.put(name, child);
        }

        return child;
    }

    /**
     * Get a child leaf, but make sure it is protected
     *
     * @since    0.2.0
     */
    public CommandLeaf getProtectedChild(String name) {
        CommandLeaf child = this.getUnprotectedChild(name);
        child.requires(child.constructPermissionString(false));
        return child;
    }

    /**
     * Require a permission
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public CommandLeaf requires(String permission) {

        if (this.required_permissions == null) {
            this.required_permissions = new HashSet<>();
        } else {
            if (this.required_permissions.contains(permission)) {
                return this;
            }
        }

        if (BibInterop.LUCKPERMS != null) {
            BibInterop.LUCKPERMS.registerPermission(permission);
        } else {
            BibServer.withReadyServer(minecraftServer -> {
                if (BibInterop.LUCKPERMS != null) {
                    BibInterop.LUCKPERMS.registerPermission(permission);
                }
            });
        }

        this.required_permissions.add(permission);

        return this.requires(source -> {
            ServerPlayerEntity player = source.getPlayer();

            if (player == null) {
                return source.hasPermissionLevel(2);
            }

            var result = BibPlayer.hasPermission(player, permission);

            //BibLog.log("Checking permission", permission, "for", player.getNameForScoreboard(), "result:", result);

            return result;
        });
    }

    /**
     * Add requirement check
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public CommandLeaf requires(Predicate<ServerCommandSource> requirement) {

        if (this.requirements == null) {
            this.requirements = new ArrayList<>();
        }

        this.requirements.add(requirement);

        return this;
    }

    /**
     * Add an executor
     * (Makes this leaf executable)
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public CommandLeaf onExecute(Command<ServerCommandSource> command) {
        this.main_command = command;
        return this;
    }

    /**
     * Set the type of the argument
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public CommandLeaf setType(ArgumentType<?> argument_type) {
        this.argument_type = argument_type;
        return this;
    }

    /**
     * Suggest a bunch of strings
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public CommandLeaf suggests(SuggestionProvider<ServerCommandSource> suggestion_provider) {
        this.suggestion_provider = suggestion_provider;
        return this;
    }

    /**
     * Suggest a bunch of strings
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public CommandLeaf suggests(Collection<String> strings) {

        this.suggestion_provider = (command_source, suggestions_builder) -> {
            for (String str : strings) {
                suggestions_builder.suggest(str);
            }

            return suggestions_builder.buildFuture();
        };

        return this;
    }

    /**
     * Register this leaf and its children
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    protected ArgumentBuilder register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {

        ArgumentBuilder result = null;

        ArgumentType argument_type = this.argument_type;

        // Default to a string argument type if none is set but there is a providesuggestion
        if (argument_type == null && this.suggestion_provider != null) {
            argument_type = StringArgumentType.string();
        }

        if (argument_type == null) {
            result = CommandManager.literal(this.name);
        } else {
            var argument_builder = CommandManager.argument(this.name, argument_type);

            if (this.suggestion_provider != null) {
                result = argument_builder.suggests(this.suggestion_provider);
            } else {
                result = argument_builder;
            }
        }

        if (this.requirements != null) {
            Predicate<ServerCommandSource> root_requirement;

            if (this.requirements.size() == 1) {
                root_requirement = this.requirements.get(0);
            } else {
                root_requirement = serverCommandSource -> {
                    for (Predicate<ServerCommandSource> requirement : this.requirements) {
                        if (!requirement.test(serverCommandSource)) {
                            return false;
                        }
                    }

                    return true;
                };
            }

            result = result.requires(root_requirement);
        }

        if (this.main_command != null) {
            result.executes(context -> {

                int command_result = 0;

                try {
                    command_result = this.main_command.run(context);
                } catch (Exception e) {
                    BibLog.error("Error executing the Blackblock command at leaf", this.name, e);
                    e.printStackTrace();
                }

                return command_result;
            });
        }

        for (String child_name : this.children.keySet()) {
            CommandLeaf child_leaf = this.children.get(child_name);

            ArgumentBuilder child_ab = child_leaf.register(dispatcher, registryAccess, environment);
            result.then(child_ab);
        }

        return result;
    }

    /**
     * Add a player selection leaf
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static CommandLeaf addOnlinePlayerSelection(CommandLeaf parent, String leaf_name, PlayerSelection selection) {

        CommandLeaf player_leaf = parent.getChild(leaf_name);
        player_leaf.setType(EntityArgumentType.player());

        if (selection == null) {
            return player_leaf;
        }

        player_leaf.onExecute(context -> {

            ServerCommandSource source = context.getSource();

            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, leaf_name);

            if (player == null) {
                source.sendFeedback(() -> Text.literal("Player not found!"), false);
                return 0;
            }

            return selection.executeWithPlayer(context, player);
        });

        return player_leaf;
    }

    /**
     * The player selection callback interface
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @FunctionalInterface
    public interface PlayerSelection {
        int executeWithPlayer(CommandContext<ServerCommandSource> context, ServerPlayerEntity player);
    }
}
