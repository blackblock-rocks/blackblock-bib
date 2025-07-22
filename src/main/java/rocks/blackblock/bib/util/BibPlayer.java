package rocks.blackblock.bib.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import rocks.blackblock.bib.BibMod;
import rocks.blackblock.bib.interop.BibInterop;

import java.util.List;

/**
 * Library class for working with player instances
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public final class BibPlayer {

    /**
     * Don't let anyone instantiate this class
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private BibPlayer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Does this player have an explicit permission node set?
     * This will not always be true for an operator.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean hasExplicitPermission(PlayerEntity player, String permission) {

        if (BibInterop.LUCKPERMS != null) {
            return BibInterop.LUCKPERMS.doesPlayerHaveExplicitPermission(player, permission);
        }

        return false;
    }

    /**
     * Does the given player have the given permission?
     * This will always be true for operators.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static boolean hasPermission(PlayerEntity player, String permission) {

        if (player != null && player.hasPermissionLevel(2)) {
            return true;
        }

        if (BibInterop.LUCKPERMS != null) {
            return BibInterop.LUCKPERMS.doesPlayerHavePermission(player, permission);
        }

        return false;
    }

    /**
     * Get the given player's username
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static String getUsername(PlayerEntity player) {

        if (player == null) {
            return "player-null";
        }

        return player.getNameForScoreboard();
    }

    /**
     * Spawn particles only the given player can see
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.3.0
     */
    public static void spawnParticles(ServerPlayerEntity player, ParticleEffect particle, Vec3d pos) {
        spawnParticles(player, particle, pos.x, pos.y, pos.z);
    }

    /**
     * Spawn particles only the given player can see
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.3.0
     */
    public static void spawnParticles(ServerPlayerEntity player, ParticleEffect particle, double x, double y, double z) {
        spawnParticles(player, particle, x, y, z, 1);
    }

    /**
     * Spawn particles only the given player can see
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.3.0
     */
    public static void spawnParticles(ServerPlayerEntity player, ParticleEffect particle, double x, double y, double z, int count) {
        player.getWorld().spawnParticles(player, particle, true, true, x, y, z, count, 0, 0, 0, 0);
    }

    /**
     * Get all the active players in a world
     *
     * @since    0.4.1
     */
    public static List<ServerPlayerEntity> getActivePlayers(ServerWorld world) {
        return world.getPlayers().stream().filter(p -> !p.bb$isAfk() && !p.isSpectator()).toList();
    }

    /**
     * Trigger a simple named criteria
     *
     * @since    0.4.1
     */
    public static void triggerNamedCriteria(ServerPlayerEntity player, String name) {
        BibMod.NAMED_CRITERION.trigger(player, name);
    }
}
