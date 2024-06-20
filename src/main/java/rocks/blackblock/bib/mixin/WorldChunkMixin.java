package rocks.blackblock.bib.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.bib.augment.AugmentManager;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {

    @Inject(method = "<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/ProtoChunk;Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;)V", at = @At("RETURN"))
    public void onInit(ServerWorld world, ProtoChunk protoChunk, WorldChunk.EntityLoader entityLoader, CallbackInfo ci) {
        AugmentManager.handleUpgradedProtoChunk(protoChunk, (WorldChunk) (Object) this);
    }
}