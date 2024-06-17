package rocks.blackblock.bib.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

/**
 * Library class for working with worlds
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibWorld {

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
     * Create an explosion
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Explosion createExplosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, Vec3d pos, float power, boolean createFire, World.ExplosionSourceType explosionSourceType) {
        Explosion explosion = world.createExplosion(entity, damageSource, behavior, pos, power, createFire, explosionSourceType);

        // The `affectWorld` only adds particles on the client-side
        BibWorld.spawnParticles(world, ParticleTypes.EXPLOSION_EMITTER, pos);

        return explosion;
    }

    /**
     * Create an explosion with a predefined destruction type
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static Explosion createExplosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, Vec3d pos, float power, boolean createFire, Explosion.DestructionType destruction_type) {
        boolean particles = true;

        Explosion explosion = new Explosion(world, entity, damageSource, behavior, pos.getX(), pos.getY(), pos.getZ(), power, createFire, destruction_type, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.ENTITY_GENERIC_EXPLODE);
        explosion.collectBlocksAndDamageEntities();
        explosion.affectWorld(particles);

        // The `affectWorld` only adds particles on the client-side
        BibWorld.spawnParticles(world, ParticleTypes.EXPLOSION_EMITTER, pos);

        return explosion;
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
