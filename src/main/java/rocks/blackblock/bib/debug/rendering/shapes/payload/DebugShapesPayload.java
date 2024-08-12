package rocks.blackblock.bib.debug.rendering.shapes.payload;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import rocks.blackblock.bib.debug.rendering.shapes.Shape;
import rocks.blackblock.bib.monitor.GlitchGuru;

import java.util.ArrayList;
import java.util.List;

/**
 * Instructions for the client to add, remove, or clear shapes
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
public record DebugShapesPayload(List<Operation> operations) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, DebugShapesPayload> CODEC = CustomPayload.codecOf(DebugShapesPayload::write, DebugShapesPayload::new);
    public static final CustomPayload.Id<DebugShapesPayload> ID = new Id<>(Identifier.of("debug", "shapes"));

    /**
     * Send the given operations to the given player
     *
     * @since    0.2.0
     */
    public static void sendToPlayer(ServerPlayerEntity player, List<Operation> operations) {
        DebugShapesPayload payload = new DebugShapesPayload(operations);
        ServerPlayNetworking.send(player, payload);
    }

    /**
     * Remove all shapes from the given player
     *
     * @since    0.2.0
     */
    public static void clearAllShapes(ServerPlayerEntity player) {
        List<DebugShapesPayload.Operation> operations = List.of(new DebugShapesPayload.Clear());
        DebugShapesPayload payload = new DebugShapesPayload(operations);
        ServerPlayNetworking.send(player, payload);
    }

    private DebugShapesPayload(PacketByteBuf buf) {
        this(readOperations(buf));
    }

    private static List<Operation> readOperations(PacketByteBuf buf) {
        int size = buf.readVarInt();
        List<Operation> operations = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            operations.add(readOperation(buf));
        }
        return operations;
    }

    private static Operation readOperation(PacketByteBuf buf) {
        int type = buf.readVarInt();
        return switch (type) {
            case 0 -> new Set(buf.readIdentifier(), buf.readEnumConstant(Shape.Type.class).deserialize(buf));
            case 1 -> new Remove(buf.readIdentifier());
            case 2 -> new ClearNamespace(buf.readString());
            case 3 -> new Clear();
            default -> throw new IllegalArgumentException("Unknown operation type: " + type);
        };
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(operations.size());
        for (Operation op : operations) {
            if (op instanceof Set set) {
                buf.writeVarInt(0);
                buf.writeIdentifier(set.namespaceId());
                buf.writeEnumConstant(set.shape().getType());
                set.shape().serialize(buf);
            } else if (op instanceof Remove remove) {
                buf.writeVarInt(1);
                buf.writeIdentifier(remove.namespaceId());
            } else if (op instanceof ClearNamespace clearNs) {
                buf.writeVarInt(2);
                buf.writeString(clearNs.namespace());
            } else if (op instanceof Clear) {
                buf.writeVarInt(3);
            }
        }
    }

    @Override
    public Id<DebugShapesPayload> getId() {
        return ID;
    }

    public sealed interface Operation permits Set, Remove, ClearNamespace, Clear {}
    public record Set(Identifier namespaceId, Shape shape) implements Operation {}
    public record Remove(Identifier namespaceId) implements Operation {}
    public record ClearNamespace(String namespace) implements Operation {}
    public record Clear() implements Operation {}

    /**
     * Register the payload
     *
     * @since    0.2.0
     */
    @ApiStatus.Internal
    public static void register() {
        try {
            PayloadTypeRegistry.playS2C().register(DebugShapesPayload.ID, DebugShapesPayload.CODEC);
        } catch (Throwable t) {
            GlitchGuru.registerThrowable(t, "Failed to register DebugShapesPayload");
        }
    }
}