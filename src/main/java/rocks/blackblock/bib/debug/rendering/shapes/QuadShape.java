package rocks.blackblock.bib.debug.rendering.shapes;


import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import rocks.blackblock.bib.debug.rendering.RenderLayer;

@SuppressWarnings("unused")
public record QuadShape(
        @NotNull Vec3d a,
        @NotNull Vec3d b,
        @NotNull Vec3d c,
        @NotNull Vec3d d,
        int color,
        @NotNull RenderLayer renderLayer
) implements Shape {

    @Override
    public Shape.Type getType() {
        return Shape.Type.QUAD;
    }

    @Override
    public void serialize(@NotNull PacketByteBuf buf) {
        writeVec3d(buf, a);
        writeVec3d(buf, b);
        writeVec3d(buf, c);
        writeVec3d(buf, d);
        buf.writeInt(color);
        buf.writeEnumConstant(renderLayer);
    }

    private static void writeVec3d(PacketByteBuf buf, Vec3d vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }

    public QuadShape(PacketByteBuf buf) {
        this(
                readVec3d(buf),
                readVec3d(buf),
                readVec3d(buf),
                readVec3d(buf),
                buf.readInt(),
                buf.readEnumConstant(RenderLayer.class)
        );
    }

    private static Vec3d readVec3d(PacketByteBuf buf) {
        return new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static class Builder {
        private Vec3d a;
        private Vec3d b;
        private Vec3d c;
        private Vec3d d;
        private int color = 0xFFFFFFFF;
        private RenderLayer renderLayer = RenderLayer.INLINE;

        public @NotNull Builder a(@NotNull Vec3d a) {
            this.a = a;
            return this;
        }

        public @NotNull Builder b(@NotNull Vec3d b) {
            this.b = b;
            return this;
        }

        public @NotNull Builder c(@NotNull Vec3d c) {
            this.c = c;
            return this;
        }

        public @NotNull Builder d(@NotNull Vec3d d) {
            this.d = d;
            return this;
        }

        public @NotNull Builder color(int color) {
            this.color = color;
            return this;
        }

        public @NotNull Builder renderLayer(@NotNull RenderLayer renderLayer) {
            this.renderLayer = renderLayer;
            return this;
        }

        public @NotNull QuadShape build() {
            if (a == null || b == null || c == null || d == null) {
                throw new IllegalStateException("All four points (a, b, c, d) must be set");
            }
            return new QuadShape(a, b, c, d, color, renderLayer);
        }
    }
}