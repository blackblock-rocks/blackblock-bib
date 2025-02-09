package rocks.blackblock.bib.mixin.dfu;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixerUpper;
import com.mojang.serialization.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rocks.blackblock.bib.util.BibData;

@Mixin(DataFixerUpper.class)
public class DataFixerUpperMixin {

    @Inject(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/datafixers/DataFixerUpper;getType(Lcom/mojang/datafixers/DSL$TypeReference;I)Lcom/mojang/datafixers/types/Type;",
            ordinal = 0,
            shift = At.Shift.AFTER
        ),
        remap = false
    )
    private <T> void bb$onUpdate(DSL.TypeReference type, Dynamic<T> input, int version, int newVersion, CallbackInfoReturnable<Dynamic<T>> cir) {
        BibData.handleTriggeredUpdate(type, input, version, newVersion);
    }
}
