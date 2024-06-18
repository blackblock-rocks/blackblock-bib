package rocks.blackblock.bib.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.bib.util.BibServer;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        BibServer.setServerWhenStarting((MinecraftServer) (Object) this);
    }

    @Inject(method = "runServer", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z",
            shift = At.Shift.AFTER
    ))
    private void afterSetup(CallbackInfo ci) {
        BibServer.setServerWhenStarted((MinecraftServer) (Object) this);
    }
}
