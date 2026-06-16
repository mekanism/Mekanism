package mekanism.common.lib.radiation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidRelative;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

public class Meltdown {

    //TODO - 26.2: Should we have validation bounds on any of these things?
    public static final Codec<Meltdown> CODEC = RecordCodecBuilder.create(in -> in.group(
          VoxelCuboid.MAP_CODEC.forGetter(meltdown -> meltdown.bounds),
          Codec.DOUBLE.fieldOf(SerializationConstants.MAGNITUDE).forGetter(meltdown -> meltdown.magnitude),
          Codec.DOUBLE.fieldOf(SerializationConstants.CHANCE).forGetter(meltdown -> meltdown.chance),
          Codec.FLOAT.fieldOf(SerializationConstants.RADIUS).forGetter(meltdown -> meltdown.radius),
          UUIDUtil.CODEC.fieldOf(SerializationConstants.INVENTORY_ID).forGetter(meltdown -> meltdown.multiblockID),
          ExtraCodecs.NON_NEGATIVE_INT.fieldOf(SerializationConstants.AGE).forGetter(meltdown -> meltdown.ticksExisted),
          Codec.BOOL.optionalFieldOf(SerializationConstants.EXPLODED, false).forGetter(meltdown -> meltdown.damageCalculator.wallExploded)
    ).apply(in, Meltdown::new));

    private static final int DURATION = 5 * SharedConstants.TICKS_PER_SECOND;

    private final MeltdownDamageCalculator damageCalculator;
    private final VoxelCuboid bounds;
    private final double magnitude, chance;
    private final UUID multiblockID;
    private final float radius;

    private int ticksExisted;

    public Meltdown(VoxelCuboid bounds, double magnitude, double chance, float radius, UUID multiblockID) {
        this(bounds, magnitude, chance, radius, multiblockID, 0, false);
    }

    private Meltdown(VoxelCuboid bounds, double magnitude, double chance, float radius, UUID multiblockID, int ticksExisted, boolean wallExploded) {
        this.bounds = bounds;
        this.magnitude = magnitude;
        this.chance = chance;
        this.radius = radius;
        this.multiblockID = multiblockID;
        this.ticksExisted = ticksExisted;
        this.damageCalculator = new MeltdownDamageCalculator(bounds, wallExploded);
    }

    public boolean update(ServerLevel world) {
        ticksExisted++;

        BlockPos minPos = bounds.getMinPos();
        BlockPos maxPos = bounds.getMaxPos();
        RandomSource random = world.getRandom();
        if (random.nextInt() % MekanismUtils.TICKS_PER_HALF_SECOND == 0 && random.nextDouble() < magnitude * chance) {
            int x = Mth.nextInt(random, minPos.getX(), maxPos.getX());
            int y = Mth.nextInt(random, minPos.getY(), maxPos.getY());
            int z = Mth.nextInt(random, minPos.getZ(), maxPos.getZ());
            Explosion.BlockInteraction mode = world.getGameRules().get(GameRules.BLOCK_EXPLOSION_DROP_DECAY) ? Explosion.BlockInteraction.DESTROY_WITH_DECAY
                                                                                                             : Explosion.BlockInteraction.DESTROY;
            createExplosion(world, x, y, z, radius, true, mode);
        }

        if (!WorldUtils.isBlockLoaded(world, minPos) || !WorldUtils.isBlockLoaded(world, maxPos)) {
            return true;
        }

        return ticksExisted >= DURATION;
    }

    /// Creates an explosion and ensures all blocks that are inside our meltdown radius actually get destroyed
    private void createExplosion(ServerLevel world, double x, double y, double z, float radius, boolean causesFire, Explosion.BlockInteraction mode) {
        //nb damage source is defaulted in ServerExplosion when null
        MeltdownExplosion explosion = new MeltdownExplosion(world, null, damageCalculator, new Vec3(x, y, z), radius, causesFire, mode, multiblockID);

        //mark if we need to restrict blocks or not
        damageCalculator.setCancelled(EventHooks.onExplosionStart(world, explosion));
        int countExploded = explosion.explode();

        //Note: Regardless of if the event got canceled vanilla syncs it to the client so that sounds and the like can play
        syncExplosionToClient(world, explosion, countExploded);
    }

    private static void syncExplosionToClient(ServerLevel level, ServerExplosion explosion, int blockCount) {
        //Note: We can just sync the explosion the same way vanilla does (ServerLevel#explode) after setting it off
        // as the client doesn't need to know about the multiblock's uuid that caused the meltdown
        ParticleOptions explosionParticle = ParticleTypes.EXPLOSION_EMITTER;//"Large"

        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(explosion.center()) < 4096.0) {
                Optional<Vec3> playerKnockback = Optional.ofNullable(explosion.getHitPlayers().get(player));
                player.connection.send(new ClientboundExplodePacket(explosion.center(), explosion.radius(), blockCount, playerKnockback, explosionParticle,
                      SoundEvents.GENERIC_EXPLODE, Level.DEFAULT_EXPLOSION_BLOCK_PARTICLES));
            }
        }
    }

    public static class MeltdownExplosion extends ServerExplosion {

        private final MeltdownDamageCalculator damageCalculator;
        private final UUID multiblockID;

        private MeltdownExplosion(ServerLevel level,
              @Nullable DamageSource damageSource,
              MeltdownDamageCalculator damageCalculator,
              Vec3 center,
              float radius,
              boolean causesFire,
              Explosion.BlockInteraction blockInteraction,
              UUID multiblockID
        ) {
            super(level, null, damageSource, damageCalculator, center, radius, causesFire, blockInteraction);
            this.damageCalculator = damageCalculator;
            this.multiblockID = multiblockID;
        }

        public UUID getMultiblockID() {
            return multiblockID;
        }

        /// Override explode to ensure we always contain one wall block
        @Override
        public int explode() {
            level().gameEvent(getDirectSourceEntity(), GameEvent.EXPLODE, center());
            List<BlockPos> toBlow = calculateExplodedPositions();
            if (!damageCalculator.wallExploded) {
                //If a wall hasn't been blown up yet, add one to the positions that need to be destroyed
                BlockPos minPos = damageCalculator.bounds.getMinPos();
                BlockPos maxPos = damageCalculator.bounds.getMaxPos();
                Vec3 center = center();
                int xWall = getCloserWall(center.x(), minPos.getX(), maxPos.getX());
                int yWall = getCloserWall(center.y(), minPos.getY(), maxPos.getY());
                int zWall = getCloserWall(center.z(), minPos.getZ(), maxPos.getZ());
                BlockPos wallPos = new BlockPos(xWall, yWall, zWall);
                assert damageCalculator.bounds.isWall(wallPos) : "Position " + wallPos + " is not a wall";
                toBlow.add(wallPos);
                damageCalculator.wallExploded = true;
            }
            hurtEntities(toBlow);
            if (interactsWithBlocks()) {
                ProfilerFiller profiler = Profiler.get();
                profiler.push("explosion_blocks");
                interactWithBlocks(toBlow);
                profiler.pop();
            }

            if (this.fire) {
                createFire(toBlow);
            }

            return toBlow.size();
        }

        private int getCloserWall(double center, int min, int max) {
            if (Math.abs(center - min) <= Math.abs(max - center)) {
                return min;
            }
            return max;
        }
    }

    @NullMarked
    private static class MeltdownDamageCalculator extends ExplosionDamageCalculator {

        private final VoxelCuboid bounds;
        private boolean wasCanceled = false;
        protected boolean wallExploded;

        private MeltdownDamageCalculator(VoxelCuboid bounds, boolean wallExploded) {
            this.bounds = bounds;
            this.wallExploded = wallExploded;
        }

        public void setCancelled(boolean wasCanceled) {
            this.wasCanceled = wasCanceled;
        }

        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float power) {
            if (!super.shouldBlockExplode(explosion, level, pos, state, power)) {
                //Note: Super currently always returns false from this, but in case that changes at some point, check it
                return false;
            }
            CuboidRelative relative = bounds.getRelativeLocation(pos);
            if (relative == CuboidRelative.WALLS) {
                wallExploded = true;
            }
            //restrict it to the multiblock if it was cancelled
            return !wasCanceled || relative != CuboidRelative.OUTSIDE;
        }
    }
}