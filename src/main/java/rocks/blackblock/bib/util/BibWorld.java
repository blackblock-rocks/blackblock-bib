package rocks.blackblock.bib.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.explosion.ExplosionImpl;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Library class for working with worlds
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibWorld {

    private static final List<Function<World, Integer>> WORLD_BORDER_RADIUS_CALCULATOR = new ArrayList<>();

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibWorld() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Get all the worlds
     * @since    0.2.0
     */
    public static Iterable<ServerWorld> getWorlds() {

        var server = BibServer.getServer();

        if (server == null) {
            return Collections.emptyList();
        }

        return server.getWorlds();
    }

    /**
     * Get a world by its identifier
     * @since    0.2.0
     */
    public static ServerWorld getWorld(Identifier id) {

        if (id == null) {
            return null;
        }

        var server = BibServer.getServer();

        if (server == null) {
            return null;
        }

        for (ServerWorld world : server.getWorlds()) {
            if (world.getRegistryKey().getValue().equals(id)) {
                return world;
            }
        }

        return null;
    }

    /**
     * Register a world border radius calculator
     * @since    0.2.0
     */
    public static void registerWorldBorderRadiusCalculator(Function<World, Integer> calculator) {
        WORLD_BORDER_RADIUS_CALCULATOR.add(calculator);
    }

    /**
     * Get the border radius of the world
     * @since    0.2.0
     */
    public static int getWorldBorderRadius(World world) {

        if (world == null) {
            return 0;
        }

        for (Function<World, Integer> calculator : WORLD_BORDER_RADIUS_CALCULATOR) {
            int result = calculator.apply(world);

            if (result != 0) {
                return result;
            }
        }

        WorldBorder vanilla_border = world.getWorldBorder();

        double center_x = vanilla_border.getCenterX();
        double center_z = vanilla_border.getCenterZ();
        double radius = vanilla_border.getSize() / 2;

        return (int) Math.floor(radius);
    }

    /**
     * Create an explosion
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void createExplosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, Vec3d pos, float power, boolean createFire, World.ExplosionSourceType explosionSourceType) {
        world.createExplosion(entity, damageSource, behavior, pos, power, createFire, explosionSourceType);

        // The `affectWorld` only adds particles on the client-side
        BibWorld.spawnParticles(world, ParticleTypes.EXPLOSION_EMITTER, pos);
    }

    /**
     * Create an explosion with a predefined destruction type
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void createExplosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, Vec3d pos, float power, boolean createFire, Explosion.DestructionType destruction_type) {

        if (!(world instanceof ServerWorld serverWorld)) {
            BibLog.log("Not creating explosion at", pos, ", world is not a ServerWorld");
            return;
        }

        Vec3d vec3d = new Vec3d(pos.x, pos.y, pos.z);
        ExplosionImpl explosionImpl = new ExplosionImpl(serverWorld, entity, damageSource, behavior, vec3d, power, createFire, destruction_type);
        explosionImpl.explode();
        ParticleEffect particleEffect = explosionImpl.isSmall() ? ParticleTypes.EXPLOSION : ParticleTypes.EXPLOSION_EMITTER;

        for (ServerPlayerEntity serverPlayerEntity : serverWorld.getPlayers()) {
            if (serverPlayerEntity.squaredDistanceTo(vec3d) < 4096.0) {
                Optional<Vec3d> optional = Optional.ofNullable(explosionImpl.getKnockbackByPlayer().get(serverPlayerEntity));
                serverPlayerEntity.networkHandler.sendPacket(new ExplosionS2CPacket(vec3d, optional, particleEffect, SoundEvents.ENTITY_GENERIC_EXPLODE));
            }
        }
    }

    /**
     * Emit particles
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void spawnParticles(World world, ParticleEffect particle, Vec3d pos) {
        // The `affectWorld` only adds particles on the client-side,
        // So we have to spawn them manually
        if (world instanceof ServerWorld server_world) {
            server_world.spawnParticles(particle, pos.getX(), pos.getY(), pos.getZ(), 1, 1.0, 0.0, 0.0, 0.0);
        }
    }
}
