package rocks.blackblock.bib.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.PalettesFactory;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.SerializedChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rocks.blackblock.bib.augment.AugmentManager;
import rocks.blackblock.bib.util.BibData;

@Mixin(SerializedChunk.class)
public class SerializedChunk_ChunkAugmentsMixin {

    @Unique
    private NbtCompound bb$augmentData = null;

    @Inject(
        method = "fromNbt",
        at = @At("RETURN")
    )
    private static void onFromNbt(HeightLimitView world, PalettesFactory palettesFactory, NbtCompound source_nbt, CallbackInfoReturnable<SerializedChunk> cir) {

        SerializedChunk result = cir.getReturnValue();

        if (result == null) {
            return;
        }

        NbtCompound chunk_augments_nbt = BibData.getCompound(source_nbt, "BlackBlockAugments");

        if (chunk_augments_nbt == null) {
            chunk_augments_nbt = BibData.getCompound(source_nbt, "BlackBlockComponents");
        }

        if (chunk_augments_nbt == null || chunk_augments_nbt.isEmpty()) {
            return;
        }

        // Not sure if this will work, or if it needs another interface hack
        ((SerializedChunk_ChunkAugmentsMixin) (Object) result).bb$augmentData = chunk_augments_nbt;
    }

    // Deserialize chunk components together with the chunk
    // (Only called when loading a chunk from disk, for new chunks see TACS mixin)
    @Inject(method="convert", at=@At("RETURN"))
    private void onDeserialize(ServerWorld world, PointOfInterestStorage poiStorage, StorageKey key, ChunkPos expectedPos, CallbackInfoReturnable<ProtoChunk> cir) {

        if (this.bb$augmentData == null) {
            return;
        }

        AugmentManager.deserializeChunkAugments(world, cir.getReturnValue(), this.bb$augmentData);
    }
}
