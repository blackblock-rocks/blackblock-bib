package rocks.blackblock.bib.mixin.dfu;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixerBuilder;
import net.minecraft.datafixer.Schemas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rocks.blackblock.bib.util.BibData;

@Mixin(Schemas.class)
public abstract class SchemasMixin {

    @Inject(
        method = "create",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/datafixer/Schemas;build(Lcom/mojang/datafixers/DataFixerBuilder;)V",
            shift = At.Shift.AFTER
        )
    )
    private static void bb$afterDataFixerBuilderCreate(CallbackInfoReturnable<DataFixerBuilder.Result> cir, @Local DataFixerBuilder builder) {
        BibData.setDataFixerBuilder(builder, true);
    }

    @Inject(
            method = "create",
            at = @At("RETURN")
    )
    private static void bb$afterDataFixerBuilderIsBuilt(CallbackInfoReturnable<DataFixerBuilder.Result> cir, @Local DataFixerBuilder builder) {
        BibData.setDataFixerBuilder(builder, false);
    }
}
