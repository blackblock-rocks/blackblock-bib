package rocks.blackblock.bib.mixin;

import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rocks.blackblock.bib.augment.AugmentManager;

@Mixin(ServerChunkLoadingManager.class)
public class TACSMixin {

    @Shadow
    @Final
    ServerWorld world;

    @Inject(method="getProtoChunk", at=@At("RETURN"))
    private void onGetProtoChunk(ChunkPos chunkPos, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();
        AugmentManager.createProtoChunkAugments(this.world, chunk);
    }
}