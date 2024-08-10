package rocks.blackblock.bib.mixin.player;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.bib.player.BlackblockPlayer;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin extends PlayerEntityMixin implements BlackblockPlayer {

    @Unique
    private byte bb$tick_count = 0;

    @Unique
    private int bb$seconds_online = 0;

    @Unique
    private int bb$ticks_since_last_movement = 0;

    @Unique
    private boolean bb$is_stationary = false;

    /**
     * Catch setPos changes.
     * (This overrides the bookmark mixin in EntityMixin)
     * @since    0.2.0
     */
    @Override
    protected void onCustomPositionChange(double x, double y, double z, CallbackInfo ci) {
        this.bb$ticks_since_last_movement = 0;
    }

    /**
     * Handle ticks
     * @since    0.2.0
     */
    @Inject(method = "tick", at = @At("HEAD"))
    public void bb$OnTick(CallbackInfo ci) {
        this.bb$tick_count++;
        this.bb$ticks_since_last_movement++;

        if (this.bb$tick_count % 20 == 0) {
            this.bb$perSecond();
            this.bb$tick_count = 0;
        }
    }

    /**
     * Do something per second
     * @since    0.2.0
     */
    @Unique
    public void bb$perSecond() {
        this.bb$seconds_online++;

        if (this.bb$is_stationary) {
            if (this.bb$ticks_since_last_movement < 100) {
                this.bb$setIsStationary(false);
            }
        } else if (this.bb$ticks_since_last_movement > 6000) {
            // Make stationary after 5 minutes of no movement
            this.bb$setIsStationary(true);
        }
    }

    /**
     * Get the ticks since the last movement
     * @since    0.2.0
     */
    @Unique
    @Override
    public int bb$getTicksSinceLastMovement() {
        return this.bb$ticks_since_last_movement;
    }

    /**
     * Get the amount of seconds this player has been online
     * @since    0.2.0
     */
    @Unique
    @Override
    public int bb$getSecondsOnline() {
        return this.bb$seconds_online;
    }

    @Unique
    @Override
    public void bb$setIsStationary(boolean stationary) {
        this.bb$is_stationary = stationary;
    }

    @Unique
    @Override
    public boolean bb$isStationary() {
        return this.bb$is_stationary;
    }

    @Unique
    @Override
    public boolean bb$isAfk() {
        return this.bb$isStationary();
    }
}
