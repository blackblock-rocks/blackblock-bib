package rocks.blackblock.bib.debug.rendering.shapes;


import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.debug.rendering.RenderLayer;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public record SplineShape(
        @NotNull Type type,
        @NotNull List<Vec3d> points,
        boolean loop,
        int color,
        @NotNull RenderLayer layer,
        float lineWidth
) implements Shape {
    public enum Type {
        CATMULL_ROM,
        BEZIER
    }

    @Override
    public Shape.Type getType() {
        return Shape.Type.SPLINE;
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
        buf.writeBoolean(loop);
        buf.writeInt(color);
        buf.writeEnumConstant(layer);
        buf.writeFloat(lineWidth);
    }

    public SplineShape(PacketByteBuf buf) {
        this(
                buf.readEnumConstant(Type.class),
                readPoints(buf),
                buf.readBoolean(),
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
        private Type type = Type.CATMULL_ROM;
        private final List<Vec3d> points = new ArrayList<>();
        private boolean loop = false;
        private int color = 0xFFFFFFFF;
        private RenderLayer layer = RenderLayer.INLINE;
        private float lineWidth = 3.0f;

        public @NotNull Builder type(@NotNull Type type) {
            this.type = type;
            return this;
        }

        public @NotNull Builder point(@NotNull Vec3d point) {
            this.points.add(point);
            return this;
        }

        public @NotNull Builder loop(boolean loop) {
            this.loop = loop;
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

        public @NotNull SplineShape build() {
            return new SplineShape(type, points, loop, color, layer, lineWidth);
        }
    }
}