package rocks.blackblock.bib.debug.rendering;

/**
 * How layers should be rendered
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public enum RenderLayer {
    /**
     * Objects should be rendered normally.
     */
    INLINE,

    /**
     * Objects should be rendered on-top of everything else.
     */
    TOP,

    /**
     * Objects are rendered on top with lower opacity when behind another object
     */
    MIXED
}
