package rocks.blackblock.bib.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rocks.blackblock.bib.augment.AugmentManager;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {

    // Serialize chunk components together with the chunk
    @Inject(method="serialize", at=@At("RETURN"))
    private static void onSerialize(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir) {
        AugmentManager.serializeChunkAugments(world, chunk, cir.getReturnValue());
    }

    // Deserialize chunk components together with the chunk
    // (Only called when loading a chunk from disk, for new chunks see TACS mixin)
    @Inject(method="deserialize", at=@At("RETURN"))
    private static void onDeserialize(ServerWorld world, PointOfInterestStorage poiStorage, StorageKey key, ChunkPos chunkPos, NbtCompound nbt, CallbackInfoReturnable<ProtoChunk> cir) {
        AugmentManager.deserializeChunkAugments(world, cir.getReturnValue(), nbt);
    }
}
