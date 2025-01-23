package rocks.blackblock.bib.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rocks.blackblock.bib.augment.AugmentManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerChunkLoadingManager.class)
public class ServerChunkLoadingManager_ChunkAugmentMixin {

    @Shadow
    @Final ServerWorld world;

    // Serialize chunk components together with the chunk
    @Redirect(
        method = "save(Lnet/minecraft/world/chunk/Chunk;)Z",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
        )
    )
    private CompletableFuture<NbtCompound> onChunkSerialize(Supplier<NbtCompound> supplier, Executor executor, Chunk chunk) {
        return CompletableFuture.supplyAsync(() -> {
            NbtCompound result = supplier.get();
            AugmentManager.serializeChunkAugments(this.world, chunk, result);
            return result;
        }, executor);
    }

}
