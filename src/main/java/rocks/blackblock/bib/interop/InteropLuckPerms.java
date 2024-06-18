package rocks.blackblock.bib.interop;


import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.ApiStatus;

/**
 * Simple class to work with LuckPerms
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@ApiStatus.Internal()
public class InteropLuckPerms {

    private static LuckPerms LUCKPERMS_INSTANCE = null;
    private static UserManager USER_MANAGER = null;

    /**
     * Create the instance & load luckperms
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public InteropLuckPerms() {}

    /**
     * Get the luckperms instance
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private LuckPerms getLuckperms() {
        if (LUCKPERMS_INSTANCE == null) {
            LUCKPERMS_INSTANCE = LuckPermsProvider.get();
        }

        return LUCKPERMS_INSTANCE;
    }

    /**
     * Get the luckperms user manager
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    private UserManager getUserManager() {
        if (USER_MANAGER == null) {
            USER_MANAGER = this.getLuckperms().getUserManager();
        }

        return USER_MANAGER;
    }

    /**
     * Check if the given player has an explicit permission
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public boolean doesPlayerHaveExplicitPermission(PlayerEntity player, String permission) {

        UserManager manager = this.getUserManager();

        if (manager == null) {
            return false;
        }

        boolean result = false;

        User user = manager.getUser(player.getUuid());

        if (user != null) {
            result = user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        }

        return result;
    }

    /**
     * Check if the given player has a permission
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public boolean doesPlayerHavePermission(PlayerEntity player, String permission) {

        if (player == null) {
            return false;
        }

        if (player.hasPermissionLevel(2)) {
            return true;
        }

        return this.doesPlayerHaveExplicitPermission(player, permission);
    }

}
