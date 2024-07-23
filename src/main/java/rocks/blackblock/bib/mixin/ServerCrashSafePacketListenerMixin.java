package rocks.blackblock.bib.mixin;

import net.minecraft.network.listener.ServerCrashSafePacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.bib.monitor.GlitchGuru;

@Mixin(ServerCrashSafePacketListener.class)
public interface ServerCrashSafePacketListenerMixin {

    @Inject(method = "onPacketException", at = @At("HEAD"), cancellable = true)
    private void onOnPacketException(Packet packet, Exception exception, CallbackInfo ci) {
        GlitchGuru.registerThrowable(exception, "Failed to handle packet " + packet.getClass().getSimpleName());
        ci.cancel();
    }
}
