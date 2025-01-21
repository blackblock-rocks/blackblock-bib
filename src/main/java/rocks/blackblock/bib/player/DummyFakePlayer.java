package rocks.blackblock.bib.player;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * A fake player that doesn't do anything.
 * To be used in places where some kind of player instance is required.
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.2.0
 */
@SuppressWarnings("unused")
public class DummyFakePlayer extends FakePlayer {

    private static final Map<String, List<DummyPair>> DUMMY_MAP = new HashMap<>();

    /**
     * Get a dummy by name in the given world.
     * It will be reserved as long as the given reference exists.
     * After that, it can be reused.
     */
    public static DummyFakePlayer lendFor(String name, ServerWorld world, Object ref) {

        var dummies = DUMMY_MAP.computeIfAbsent(name, k -> new ArrayList<>());
        DummyPair pair = null;

        for (DummyPair p : dummies) {
            if (!p.isReferenced()) {
                pair = p;
                break;
            }
        }

        if (pair == null) {
            pair = new DummyPair(name, UUID.randomUUID(), new DummyFakePlayer(world, new GameProfile(UUID.randomUUID(), name)));
            dummies.add(pair);
        }

        pair.setServerWorld(world);
        pair.setReference(ref);

        return pair.getPlayer();
    }

    /**
     * Initialize the dummy player
     */
    protected DummyFakePlayer(ServerWorld world, GameProfile profile) {
        super(world, profile);
    }

    /**
     * Skip expensive chunk logic and just return 0,0,0
     */
    @Override
    public BlockPos getWorldSpawnPos(ServerWorld world, BlockPos basePos) {
        return BlockPos.ORIGIN;
    }

    /**
     * Keep info on a dummy player
     */
    private static class DummyPair {

        private final String name;
        private final UUID uuid;
        private final DummyFakePlayer player;
        private WeakReference<Object> reference = null;

        public DummyPair(String name, UUID uuid, DummyFakePlayer player) {
            this.name = name;
            this.uuid = uuid;
            this.player = player;
        }

        public void setServerWorld(ServerWorld world) {
            this.player.setServerWorld(world);
        }

        public ServerWorld getServerWorld() {
            return this.player.getServerWorld();
        }

        public DummyFakePlayer getPlayer() {
            return this.player;
        }

        public void setReference(Object value) {

            if (value == null) {
                this.reference = null;
                return;
            }

            this.reference = new WeakReference<>(value);
        }

        public boolean isReferenced() {

            if (this.reference == null) {
                return false;
            }

            return this.reference.get() != null;
        }
    }
}
