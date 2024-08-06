package rocks.blackblock.bib.tweaks;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.bib.augment.Augment;
import rocks.blackblock.bib.augment.AugmentKey;
import rocks.blackblock.bib.bv.parameter.MapParameter;
import rocks.blackblock.bib.bv.parameter.TweakParameter;
import rocks.blackblock.bib.bv.value.AbstractBvType;
import rocks.blackblock.bib.bv.value.BvElement;
import rocks.blackblock.bib.bv.value.BvMap;
import rocks.blackblock.bib.command.CommandLeaf;
import rocks.blackblock.bib.monitor.GlitchGuru;

import java.util.function.Function;

/**
 * Tweak configuration registry
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class TweaksConfiguration extends MapParameter<AbstractBvType<?, ?>> {

    protected Function<CommandContext<ServerCommandSource>, BvMap> context_resolver = null;

    /**
     * Create a new tweak registry
     *
     * @since    0.2.0
     */
    public TweaksConfiguration(String name) {
        super(name);
    }

    /**
     * Set the context resolver
     *
     * @since    0.2.0
     */
    public TweaksConfiguration setContextResolver(Function<CommandContext<ServerCommandSource>, BvMap> resolver) {
        this.context_resolver = resolver;
        return this;
    }

    /**
     * Get the root Map of the given CommandSource
     *
     * @since    0.2.0
     */
    @Nullable
    @Override
    public BvMap getDataContext(CommandContext<ServerCommandSource> command_context) {

        if (command_context == null || this.context_resolver == null) {
            return null;
        }

        return this.context_resolver.apply(command_context);
    }

    /**
     * Add this parameter to the given command leaf.
     * Because this is the root map parameter,
     * the children will be injected into the given leaf directly.
     *
     * @since    0.2.0
     */
    @Override
    public CommandLeaf addToCommandLeaf(CommandLeaf parent_leaf) {

        for (TweakParameter<?> param : this.contained_parameters.values()) {
            param.addToCommandLeaf(parent_leaf);
        }

        return parent_leaf;
    }

    /**
     * Root parameters can't be stored in a root map:
     * they are the root itself.
     *
     * @since    0.2.0
     */
    @Override
    public BvMap getFromRootMap(BvMap map) {
        return map;
    }

    /**
     * Augmented versions of tweak configurations
     *
     * @since    0.2.0
     */
    public abstract static class Augmented extends TweaksConfiguration {


        /**
         * Create a new tweak registry
         *
         * @since 0.2.0
         */
        public Augmented(String name) {
            super(name);
        }

        /**
         * Child classes should override this again
         *
         * @since    0.2.0
         */
        @Nullable
        @Override
        public BvMap getDataContext(CommandContext<ServerCommandSource> command_context) {
            return null;
        }

        /**
         * There is no need for context resolvers
         *
         * @since    0.2.0
         */
        @Override
        public TweaksConfiguration setContextResolver(Function<CommandContext<ServerCommandSource>, BvMap> resolver) {
            return this;
        }
    }

    /**
     * Global tweaks configuration.
     * Automatically registers as an augment.
     *
     * @since    0.2.0
     */
    public static class Global extends TweaksConfiguration.Augmented {

        private final AugmentKey.Global<TweaksAugment.Global> augment_key;

        /**
         * Create a new tweak registry
         *
         * @since 0.2.0
         */
        public Global(Identifier id) {
            super(id.toString());

            this.augment_key = Augment.Global.register(id, TweaksAugment.Global.class, () -> {
                return new TweaksAugment.Global(this);
            });
        }

        /**
         * Get the context
         *
         * @since    0.2.0
         */
        @Nullable
        @Override
        public BvMap getDataContext(CommandContext<ServerCommandSource> command_context) {
            return this.augment_key.get().getDataContext();
        }

        /**
         * Get the value
         *
         * @since    0.2.0
         */
        @Nullable
        public <T extends BvElement<?, ?>> T get(TweakParameter<T> param) {
            var result = param.getFromRootMap(this.augment_key.get().getDataContext());

            if (result == null) {
                return param.getDefaultValue();
            }

            return result;
        }
    }

    /**
     * PerPlayer tweaks configuration.
     * Automatically registers as an augment.
     *
     * @since    0.2.0
     */
    public static class PerPlayer extends TweaksConfiguration.Augmented {

        private final AugmentKey.PerPlayer<TweaksAugment.PerPlayer> augment_key;

        /**
         * Create a new tweak registry
         *
         * @since 0.2.0
         */
        public PerPlayer(Identifier id) {
            super(id.toString());

            this.augment_key = Augment.PerPlayer.register(id, TweaksAugment.PerPlayer.class, false, player -> {
                return new TweaksAugment.PerPlayer(this, player);
            });
        }

        /**
         * Get the context
         *
         * @since    0.2.0
         */
        @Nullable
        @Override
        public BvMap getDataContext(CommandContext<ServerCommandSource> command_context) {

            ServerPlayerEntity player = null;
            boolean tried_arg = false;

            try {
                player = EntityArgumentType.getPlayer(command_context, "player");
                tried_arg = true;
            } catch (IllegalArgumentException e_arg) {
                // There is no "player" arg, and that's ok!
            } catch (Exception e) {
                GlitchGuru.registerThrowable(e);
                return null;
            }

            if (player == null && !tried_arg) {
                ServerCommandSource source = command_context.getSource();

                if (source != null) {
                    player = source.getPlayer();
                }
            }

            if (player == null) {
                return null;
            }

            return this.augment_key.get(player).getDataContext();
        }

        /**
         * Get the value of the given player
         *
         * @since    0.2.0
         */
        @Nullable
        public <T extends BvElement<?, ?>> T get(TweakParameter<T> param, ServerPlayerEntity player) {

            if (player == null) {
                return null;
            }

            var result = param.getFromRootMap(this.augment_key.get(player).getDataContext());

            if (result == null) {
                return param.getDefaultValue();
            }

            return result;
        }
    }

    /**
     * PerWorld tweaks configuration.
     * Automatically registers as an augment.
     *
     * @since    0.2.0
     */
    public static class PerWorld extends TweaksConfiguration.Augmented {

        private final AugmentKey.PerWorld<TweaksAugment.PerWorld> augment_key;

        /**
         * Create a new tweak registry
         *
         * @since 0.2.0
         */
        public PerWorld(Identifier id) {
            super(id.toString());

            this.augment_key = Augment.PerWorld.register(id, TweaksAugment.PerWorld.class, world -> {
                return new TweaksAugment.PerWorld(this, world);
            });
        }

        /**
         * Get the context
         *
         * @since    0.2.0
         */
        @Nullable
        @Override
        public BvMap getDataContext(CommandContext<ServerCommandSource> command_context) {

            World world = null;
            boolean tried_arg = false;

            try {
                world = DimensionArgumentType.getDimensionArgument(command_context, "world");
                tried_arg = true;
            } catch (IllegalArgumentException e_arg) {
                // There is no "world" arg, and that's ok!
            } catch (Exception e) {
                GlitchGuru.registerThrowable(e);
                return null;
            }

            if (world == null && !tried_arg) {
                ServerCommandSource source = command_context.getSource();

                if (source != null) {
                    ServerPlayerEntity player = source.getPlayer();

                    if (player != null) {
                        world = player.getServerWorld();
                    }
                }
            }

            if (world == null) {
                return null;
            }

            return this.augment_key.get(world).getDataContext();
        }

        /**
         * Get the value of the given world
         *
         * @since    0.2.0
         */
        @Nullable
        public <T extends BvElement<?, ?>> T get(TweakParameter<T> param, World world) {

            if (world == null) {
                return null;
            }

            var result = param.getFromRootMap(this.augment_key.get(world).getDataContext());

            if (result == null) {
                return param.getDefaultValue();
            }

            return result;
        }
    }

    /**
     * PerChunk tweaks configuration.
     * Automatically registers as an augment.
     *
     * @since    0.2.0
     */
    public static class PerChunk extends TweaksConfiguration.Augmented {

        private final AugmentKey.PerChunk<TweaksAugment.PerChunk> augment_key;

        /**
         * Create a new tweak registry
         *
         * @since 0.2.0
         */
        public PerChunk(Identifier id) {
            super(id.toString());

            this.augment_key = Augment.PerChunk.register(id, TweaksAugment.PerChunk.class, (world, chunk) -> {
                return new TweaksAugment.PerChunk(this, world, chunk);
            });
        }

        /**
         * Get the context
         *
         * @since    0.2.0
         */
        @Nullable
        @Override
        public BvMap getDataContext(CommandContext<ServerCommandSource> command_context) {

            World world = null;
            Chunk chunk = null;
            BlockPos chunk_pos = null;
            boolean tried_arg = false;

            try {
                world = DimensionArgumentType.getDimensionArgument(command_context, "world");
                tried_arg = true;
            } catch (IllegalArgumentException e_arg) {
                // There is no "world" arg, and that's ok!
            } catch (Exception e) {
                GlitchGuru.registerThrowable(e);
                return null;
            }

            if (world == null && !tried_arg) {
                ServerCommandSource source = command_context.getSource();

                if (source != null) {
                    ServerPlayerEntity player = source.getPlayer();

                    if (player != null) {
                        world = player.getServerWorld();
                    }
                }
            }

            if (world == null) {
                return null;
            }

            tried_arg = false;

            try {
                chunk_pos = BlockPosArgumentType.getBlockPos(command_context, "chunk_pos");
                tried_arg = true;
            } catch (IllegalArgumentException e_arg) {
                // There is no "chunk_pos" arg, and that's ok!
            } catch (Exception e) {
                GlitchGuru.registerThrowable(e);
                return null;
            }

            if (chunk_pos == null && !tried_arg) {
                ServerCommandSource source = command_context.getSource();

                if (source != null) {
                    ServerPlayerEntity player = source.getPlayer();

                    if (player != null) {
                        chunk_pos = player.getBlockPos();
                    }
                }
            }

            if (chunk_pos != null) {
                chunk = world.getChunk(chunk_pos);
            }

            if (chunk == null) {
                return null;
            }

            return this.augment_key.get(chunk).getDataContext();
        }

        /**
         * Get the value of the given chunk
         *
         * @since    0.2.0
         */
        @Nullable
        public <T extends BvElement<?, ?>> T get(TweakParameter<T> param, Chunk chunk) {

            if (chunk == null) {
                return null;
            }

            var result = param.getFromRootMap(this.augment_key.get(chunk).getDataContext());

            if (result == null) {
                return param.getDefaultValue();
            }

            return result;
        }
    }
}
