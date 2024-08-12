package rocks.blackblock.bib.debug.rendering.shapes;


import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.debug.rendering.RenderLayer;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public record LineShape(
        @NotNull Type type,
        @NotNull List<Vec3d> points,
        int color,
        @NotNull RenderLayer layer,
        float lineWidth
) implements Shape {

    public enum Type {
        SINGLE,
        STRIP,
        LOOP
    }

    @Override
    public Shape.Type getType() {
        return Shape.Type.LINE;
    }

    @Override
    public void serialize(@NotNull PacketByteBuf buf) {
        buf.writeEnumConstant(type);
        buf.writeVarInt(points.size());
        for (Vec3d point : points) {
            buf.writeDouble(point.x);
            buf.writeDouble(point.y);
            buf.writeDouble(point.z);
        }
        buf.writeInt(color);
        buf.writeEnumConstant(layer);
        buf.writeFloat(lineWidth);
    }

    public LineShape(PacketByteBuf buf) {
        this(
                buf.readEnumConstant(Type.class),
                readPoints(buf),
                buf.readInt(),
                buf.readEnumConstant(RenderLayer.class),
                buf.readFloat()
        );
    }

    private static List<Vec3d> readPoints(PacketByteBuf buf) {
        int size = buf.readVarInt();
        List<Vec3d> points = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            points.add(new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }
        return points;
    }

    public static class Builder {

        private Type type = Type.SINGLE;
        private final List<Vec3d> points = new ArrayList<>();
        private float lineWidth = 4f;
        private int color = 0xFFFFFFFF;
        private RenderLayer layer = RenderLayer.INLINE;

        public @NotNull Builder type(@NotNull Type type) {
            this.type = type;
            return this;
        }

        public @NotNull Builder point(@NotNull Vec3d point) {
            points.add(point);
            return this;
        }

        public @NotNull Builder color(int color) {
            this.color = color;
            return this;
        }

        public @NotNull Builder layer(@NotNull RenderLayer layer) {
            this.layer = layer;
            return this;
        }

        public @NotNull Builder lineWidth(float lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        public @NotNull LineShape build() {
            if (points.size() < 2) {
                throw new IllegalArgumentException("Line must have at least 2 points");
            }
            return new LineShape(type, points, color, layer, lineWidth);
        }
    }
}