package rocks.blackblock.bib.debug.rendering.shapes;

import net.minecraft.network.PacketByteBuf;

/**
 * The base Shape interface
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public interface Shape {

    enum Type {
        LINE,
        SPLINE,
        QUAD,
        BOX;

        public Shape deserialize(PacketByteBuf buf) {
            return switch (this) {
                case LINE -> new LineShape(buf);
                case SPLINE -> new SplineShape(buf);
                case QUAD -> new QuadShape(buf);
                case BOX -> new BoxShape(buf);
            };
        }
    }

    Type getType();
    void serialize(PacketByteBuf buf);
}
