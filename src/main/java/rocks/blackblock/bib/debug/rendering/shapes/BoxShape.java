package rocks.blackblock.bib.debug.rendering.shapes;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import rocks.blackblock.bib.debug.rendering.RenderLayer;

/**
 * Represent a simple box shape
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class BoxShape implements Shape {
    private final Vec3d min;
    private final Vec3d max;
    private final int faceColor;
    private final RenderLayer faceRenderLayer;
    private final int edgeColor;
    private final RenderLayer edgeRenderLayer;
    private final float edgeWidth;

    public BoxShape(Vec3d min, Vec3d max, int faceColor, RenderLayer faceRenderLayer,
                    int edgeColor, RenderLayer edgeRenderLayer, float edgeWidth) {
        this.min = min;
        this.max = max;
        this.faceColor = faceColor;
        this.faceRenderLayer = faceRenderLayer;
        this.edgeColor = edgeColor;
        this.edgeRenderLayer = edgeRenderLayer;
        this.edgeWidth = edgeWidth;
    }

    public BoxShape(PacketByteBuf buf) {
        this.min = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.max = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.faceColor = buf.readInt();
        this.faceRenderLayer = buf.readEnumConstant(RenderLayer.class);
        this.edgeColor = buf.readInt();
        this.edgeRenderLayer = buf.readEnumConstant(RenderLayer.class);
        this.edgeWidth = buf.readFloat();
    }

    @Override
    public Type getType() {
        return Type.BOX;
    }

    @Override
    public void serialize(PacketByteBuf buf) {
        buf.writeDouble(min.x).writeDouble(min.y).writeDouble(min.z);
        buf.writeDouble(max.x).writeDouble(max.y).writeDouble(max.z);
        buf.writeInt(faceColor);
        buf.writeEnumConstant(faceRenderLayer);
        buf.writeInt(edgeColor);
        buf.writeEnumConstant(edgeRenderLayer);
        buf.writeFloat(edgeWidth);
    }
}
