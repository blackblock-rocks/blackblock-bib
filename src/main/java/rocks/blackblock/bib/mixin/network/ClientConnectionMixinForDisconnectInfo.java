package rocks.blackblock.bib.mixin.network;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import rocks.blackblock.bib.interfaces.HasDisconnectionInfo;

/**
 * Implement the HasDisconnectionInfo interface
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixinForDisconnectInfo implements HasDisconnectionInfo {

    @Shadow
    private boolean disconnected;

    @Shadow
    private volatile @Nullable DisconnectionInfo pendingDisconnectionInfo;

    @Shadow
    private @Nullable DisconnectionInfo disconnectionInfo;

    /**
     * Is this thing in the process of disconnecting?
     * (If it already has disconnected it should also return true)
     */
    @Override
    public boolean bb$isDisconnecting() {

        if (this.disconnected) {
            return true;
        }

        if (this.pendingDisconnectionInfo != null) {
            return true;
        }

        if (this.disconnectionInfo != null) {
            return true;
        }

        return false;
    }
}
