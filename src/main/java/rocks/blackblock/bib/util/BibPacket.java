package rocks.blackblock.bib.util;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;

/**
 * Library class for working with Packets
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public final class BibPacket {

    /**
     * Don't let anyone instantiate this class
     *
     * @since    0.2.0
     */
    private BibPacket() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Is the given packet an entity packet?
     *
     * @since    0.2.0
     */
    public static boolean isEntityPacket(Packet<?> packet) {

        if (packet instanceof EntityS2CPacket) {
            return true;
        }

        if (packet instanceof EntityAnimationS2CPacket) {
            return true;
        }

        if (packet instanceof EntityAttachS2CPacket) {
            return true;
        }

        if (packet instanceof EntityAttributesS2CPacket) {
            return true;
        }

        if (packet instanceof EntityDamageS2CPacket) {
            return true;
        }

        if (packet instanceof EntitiesDestroyS2CPacket) {
            return true;
        }

        if (packet instanceof EntityEquipmentUpdateS2CPacket) {
            return true;
        }

        if (packet instanceof EntityPassengersSetS2CPacket) {
            return true;
        }

        if (packet instanceof EntityPositionS2CPacket) {
            return true;
        }

        if (packet instanceof EntitySetHeadYawS2CPacket) {
            return true;
        }

        if (packet instanceof EntitySpawnS2CPacket) {
            return true;
        }

        if (packet instanceof EntityStatusS2CPacket) {
            return true;
        }

        if (packet instanceof EntityStatusEffectS2CPacket) {
            return true;
        }

        if (packet instanceof EntityTrackerUpdateS2CPacket) {
            return true;
        }

        if (packet instanceof EntityVelocityUpdateS2CPacket) {
            return true;
        }

        return false;
    }

}
