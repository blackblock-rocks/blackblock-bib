package rocks.blackblock.bib.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Library class for working with sounds
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.3.0
 */
@SuppressWarnings("unused")
public abstract class BibSound {

    /**
     * Create and register a sound event
     */
    public static SoundEvent register(Identifier id) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    /**
     * Play a sound coming from the given BlockEntity
     */
    public static boolean playSound(BlockEntity be, SoundEvent sound, float volume) {

        if (be == null || sound == null) {
            return false;
        }

        World world = be.getWorld();

        if (world == null || world.isClient) {
            return false;
        }

        BlockPos pos = be.getPos();

        world.playSound(null, pos, sound, SoundCategory.BLOCKS, volume, 1f);

        return true;
    }
}
