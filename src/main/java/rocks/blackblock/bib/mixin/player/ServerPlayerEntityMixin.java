package rocks.blackblock.bib.mixin.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.blackblock.bib.player.PlayerActivityInfo;
import rocks.blackblock.bib.util.BibPerf;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntityMixin implements PlayerActivityInfo {

    @Shadow
    public abstract ServerWorld getServerWorld();

    @Unique
    private int bb$seconds_online = 0;

    @Unique
    private int bb$ticks_since_last_movement = 0;

    @Unique
    private boolean bb$is_stationary = false;

    @Unique
    private boolean bb$is_afk = false;

    @Unique
    private boolean bb$is_ignored = false;

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
        this.bb$ticks_since_last_movement++;

        if (BibPerf.ON_FULL_SECOND) {
            this.bb$perSecond();
        }
    }

    /**
     * Do something per second
     * @since    0.2.0
     */
    @Unique
    public void bb$perSecond() {
        this.bb$seconds_online++;

        if (this.bb$is_stationary || this.bb$is_afk) {
            if (this.bb$ticks_since_last_movement < 100) {
                this.bb$setIsStationary(false);
            }
        } else if (this.bb$ticks_since_last_movement > 6000) {
            // Make stationary after 5 minutes of no movement
            this.bb$setIsStationary(true);
        }

        this.bb$updateIgnoredStatus();
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

        if (stationary) {
            this.bb$is_afk = true;
            this.bb$updateIgnoredStatus();
        } else {
            this.bb$is_afk = false;
            this.bb$is_ignored = false;
        }
    }

    @Unique
    private void bb$updateIgnoredStatus() {

        if (!this.bb$is_afk) {
            this.bb$is_ignored = false;
            return;
        }

        BibPerf.Info info = BibPerf.getWorldInfo(this.getServerWorld());

        if (info.isCritical()) {
            this.bb$is_ignored = true;
        } else if (info.isRandomlyDisabled()) {
            this.bb$is_ignored = true;
        } else {
            this.bb$is_ignored = false;
        }
    }

    @Unique
    @Override
    public boolean bb$isStationary() {
        return this.bb$is_stationary;
    }

    @Unique
    @Override
    public boolean bb$isAfk() {
        return this.bb$is_afk;
    }

    @Unique
    @Override
    public boolean bb$ignoreDueToSystemLoad() {
        return this.bb$is_ignored;
    }
}
