package rocks.blackblock.bib.mixin.player;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    /**
     * Bookmark mixin which ServerPlayerEntityMixin will override
     * @since 0.2.0
     */
    @Inject(
            method = "setPos",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/Vec3d;<init>(DDD)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = false
    )
    protected void onCustomPositionChange(double x, double y, double z, CallbackInfo ci) {
        // Ignore
    }
}