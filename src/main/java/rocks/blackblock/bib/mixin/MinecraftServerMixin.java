package rocks.blackblock.bib.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rocks.blackblock.bib.augment.AugmentManager;
import rocks.blackblock.bib.runnable.TickRunnable;
import rocks.blackblock.bib.util.BibPerf;
import rocks.blackblock.bib.util.BibServer;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Shadow private int ticks;

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

    @Inject(method="tickWorlds", at=@At("TAIL"))
    private void checkQueuedRunnables(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        TickRunnable.checkQueuedRunnables();
    }

    @Inject(method="save", at=@At("RETURN"))
    private void onSave(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        try {
            AugmentManager.saveAll();
        } catch (Throwable e) {
            BibServer.registerThrowable(e, "Failed to save Blackblock augments");
        }
    }

    /**
     * We're about to tick all the worlds
     * @since    0.1.0
     */
    @Inject(
        method = "tickWorlds",
        at = @At("HEAD")
    )
    public void onTickWorlds(BooleanSupplier should_keep_ticking, CallbackInfo ci) {
        BibPerf.registerPreTick(this.ticks);

        if (BibPerf.ON_TENTH_SECOND) {
            AugmentManager.collectGarbage();
        }
    }
}
