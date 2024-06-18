package rocks.blackblock.bib.monitor;


import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import rocks.blackblock.bib.util.BibLog;
import rocks.blackblock.bib.util.BibPlayer;
import rocks.blackblock.bib.util.BibServer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Gather information about an issue.
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings("unused")
public class IssueContext {

    private PlayerEntity player = null;
    private Entity other_entity = null;
    private Type type = null;
    private Set<String> categories = null;
    private String message = null;
    private Map<String, Object> data = null;
    private Throwable throwable = null;
    private boolean submitted = false;

    /**
     * Create a new one for the given player
     *
     * @since    0.1.0
     */
    public static IssueContext create() {
        return new IssueContext();
    }

    /**
     * Create a new one for the given player
     *
     * @since    0.1.0
     */
    public static IssueContext create(PlayerEntity player) {
        return create().setPlayer(player);
    }

    /**
     * Set the player that experienced this error
     *
     * @since    0.1.0
     */
    public IssueContext setPlayer(PlayerEntity player) {
        this.player = player;
        return this;
    }

    /**
     * Set the other entity that experienced this error
     *
     * @since    0.1.0
     */
    public IssueContext setOtherEntity(Entity other_entity) {
        this.other_entity = other_entity;
        return this;
    }

    /**
     * Set the type of issue
     *
     * @since    0.1.0
     */
    public IssueContext setType(Type type) {
        this.type = type;
        return this;
    }

    /**
     * Add a category
     *
     * @since    0.1.0
     */
    public IssueContext addCategory(String category) {
        if (this.categories == null) {
            this.categories = new HashSet<>();
        }

        this.categories.add(category);
        return this;
    }

    /**
     * Set a description message
     *
     * @since    0.1.0
     */
    public IssueContext setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Set extra data pairs
     *
     * @since    0.1.0
     */
    public IssueContext setData(String key, Object value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }

        this.data.put(key, value);
        return this;
    }

    /**
     * Set the throwable
     *
     * @since    0.1.0
     */
    public IssueContext setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    /**
     * Turn this into a nice message
     *
     * @since    0.1.0
     */
    public String toMessage() {

        StringBuilder result = new StringBuilder("[STAFF] ");

        if (this.type != null) {
            result.append(this.type.name()).append(" ");
        }

        if (this.player != null) {
            result.append("(").append(BibPlayer.getUsername(this.player)).append(") ");
        }

        if (this.other_entity != null) {
            result.append("(").append(this.other_entity.getType().getName().getString()).append(") ");
        }

        if (this.message != null) {
            result.append(this.message);
        }

        if (this.categories != null) {
            result.append(" [");

            for (String category : this.categories) {
                result.append(category).append(", ");
            }

            result.append("]");
        }

        if (this.data != null) {
            result.append("\nData: ");

            for (Map.Entry<String, Object> entry : this.data.entrySet()) {
                result.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
            }
        }

        if (this.throwable != null) {
            result.append("\nError: ");
            result.append(this.throwable.getMessage());
        }

        return result.toString();
    }

    /**
     * Inform the monitor of this issue
     *
     * @since    0.1.0
     */
    public void submit() {

        if (this.submitted) {
            return;
        }

        this.submitted = true;

        try {
            String message = this.toMessage();

            BibLog.log(message);
            BibServer.getServer().getCommandSource().sendFeedback(() -> Text.literal(message).setStyle(Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.RED))), true);
        } catch (Throwable e) {
            BibLog.error("Failed to submit issue: " + e.getMessage());
        }
    }

    /**
     * The type of issues
     *
     * @since    0.1.0
     */
    public enum Type {
        DEGRADED_PLAYER_EXPERIENCE,
        CHEATING,
        ERROR
    }
}

