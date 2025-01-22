package rocks.blackblock.bib.mixin.network;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.bib.interfaces.HasDisconnectionInfo;

/**
 * Implement the HasDisconnectionInfo interface
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@Mixin(ServerCommonNetworkHandler.class)
public class ServerCommonNetworkHandlerMixinForDisconnectInfo implements HasDisconnectionInfo {

    @Shadow
    @Final protected ClientConnection connection;

    @Unique
    private boolean bb$is_disconnecting = false;

    /**
     * Mark this handler as disconnecting
     */
    @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionInfo;)V", at = @At("HEAD"))
    private void onDisconnect(DisconnectionInfo disconnectionInfo, CallbackInfo ci) {
        this.bb$is_disconnecting = true;
    }

    /**
     * Is this thing in the process of disconnecting?
     * (If it already has disconnected it should also return true)
     */
    @Override
    public boolean bb$isDisconnecting() {

        if (this.bb$is_disconnecting) {
            return true;
        }

        return this.connection.bb$isDisconnecting();
    }
}
