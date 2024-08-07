package rocks.blackblock.bib.mixin.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends EntityMixin {

    /**
     * Bookmark mixin which ServerPlayerEntityMixin will override
     * @since 0.2.0
     */
    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;setUuid(Ljava/util/UUID;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = false
    )
    protected void afterSettingUUIDInInit(World world, BlockPos pos, float yaw, GameProfile gameProfile, CallbackInfo ci) {
        // Ignore
    }
}