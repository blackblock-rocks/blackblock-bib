package rocks.blackblock.bib.mixin;

import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.bib.util.BibLog;

@Mixin(ServerCommonNetworkHandler.class)
public class PacketLoggerMixin {
    // Can be used to debug packets quickly. Just enable it in the mixin file
    @Inject(method="send", at = @At("HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
        if (packet instanceof EntityTrackerUpdateS2CPacket et) {
            BibLog.log("Sending Packet:", et);
            BibLog.printStackTrace();
        }
    }
}
