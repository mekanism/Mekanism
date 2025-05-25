package mekanism.common.lib.radiation;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

public class Meltdown {

    private static final int DURATION = 5 * SharedConstants.TICKS_PER_SECOND;

    private final BlockPos minPos, maxPos;
    private final double magnitude, chance;
    private final UUID multiblockID;
    private final float radius;

    private int ticksExisted;

    public Meltdown(BlockPos minPos, BlockPos maxPos, double magnitude, double chance, float radius, UUID multiblockID) {
        this(minPos, maxPos, magnitude, chance, radius, multiblockID, 0);
    }

    private Meltdown(BlockPos minPos, BlockPos maxPos, double magnitude, double chance, float radius, UUID multiblockID, int ticksExisted) {
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.magnitude = magnitude;
        this.chance = chance;
        this.radius = radius;
        this.multiblockID = multiblockID;
        this.ticksExisted = ticksExisted;
    }

    @Nullable
    public static Meltdown load(CompoundTag tag) {
        Optional<BlockPos> minPos = NbtUtils.readBlockPos(tag, SerializationConstants.MIN);
        Optional<BlockPos> maxPos = NbtUtils.readBlockPos(tag, SerializationConstants.MAX);
        if (minPos.isEmpty() || maxPos.isEmpty()) {
            return null;
        }
        return new Meltdown(
              minPos.get(),
              maxPos.get(),
              tag.getDouble(SerializationConstants.MAGNITUDE),
              tag.getDouble(SerializationConstants.CHANCE),
              tag.getFloat(SerializationConstants.RADIUS),
              tag.getUUID(SerializationConstants.INVENTORY_ID),
              tag.getInt(SerializationConstants.AGE)
        );
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put(SerializationConstants.MIN, NbtUtils.writeBlockPos(minPos));
        tag.put(SerializationConstants.MAX, NbtUtils.writeBlockPos(maxPos));
        tag.putDouble(SerializationConstants.MAGNITUDE, magnitude);
        tag.putDouble(SerializationConstants.CHANCE, chance);
        tag.putFloat(SerializationConstants.RADIUS, radius);
        tag.putUUID(SerializationConstants.INVENTORY_ID, multiblockID);
        tag.putInt(SerializationConstants.AGE, ticksExisted);
        return tag;
    }

    public boolean update(ServerLevel world) {
        ticksExisted++;

        if (world.random.nextInt() % MekanismUtils.TICKS_PER_HALF_SECOND == 0 && world.random.nextDouble() < magnitude * chance) {
            int x = Mth.nextInt(world.random, minPos.getX(), maxPos.getX());
            int y = Mth.nextInt(world.random, minPos.getY(), maxPos.getY());
            int z = Mth.nextInt(world.random, minPos.getZ(), maxPos.getZ());
            Explosion.BlockInteraction mode = world.getGameRules().getBoolean(GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY) ? Explosion.BlockInteraction.DESTROY_WITH_DECAY
                                                                                                                         : Explosion.BlockInteraction.DESTROY;
            createExplosion(world, x, y, z, radius, true, mode);
        }

        if (!WorldUtils.isBlockLoaded(world, minPos) || !WorldUtils.isBlockLoaded(world, maxPos)) {
            return true;
        }

        return ticksExisted >= DURATION;
    }

    /**
     * Creates an explosion and ensures all blocks that are inside our meltdown radius actually get destroyed
     */
    private void createExplosion(ServerLevel world, double x, double y, double z, float radius, boolean causesFire, Explosion.BlockInteraction mode) {
        Explosion explosion = new MeltdownExplosion(world, x, y, z, radius, causesFire, mode, multiblockID);
        //Calculate which block positions should get broken based on the logic that would happen in Explosion#explode
        ObjectArrayList<BlockPos> toBlow = new ObjectArrayList<>();
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = j / 7.5 - 1.0;
                        double d1 = k / 7.5 - 1.0;
                        double d2 = l / 7.5 - 1.0;
                        double d3 = Mth.length(d0, d1, d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float f = radius * (0.7F + world.random.nextFloat() * 0.6F);
                        double d4 = x;
                        double d6 = y;
                        double d8 = z;

                        for (; f > 0.0F; f -= 0.22500001F) {
                            BlockPos pos = BlockPos.containing(d4, d6, d8);
                            BlockState blockstate = world.getBlockState(pos);
                            FluidState fluidstate = blockstate.getFluidState();
                            if (!blockstate.isAir() || !fluidstate.isEmpty()) {
                                f -= (Math.max(blockstate.getExplosionResistance(world, pos, explosion),
                                      fluidstate.getExplosionResistance(world, pos, explosion)) + 0.3F) * 0.3F;
                            }
                            if (f > 0.0F && minPos.getX() <= d4 && minPos.getY() <= d6 && minPos.getZ() <= d8 && d4 <= maxPos.getX() && d6 <= maxPos.getY() &&
                                d8 <= maxPos.getZ()) {
                                //Only add it as a spot to break if it is inside the bounds of the reactor
                                toBlow.add(pos);
                            }
                            d4 += d0 * 0.3;
                            d6 += d1 * 0.3;
                            d8 += d2 * 0.3;
                        }
                    }
                }
            }
        }
        //Try to make the explosion actually happen
        if (!EventHooks.onExplosionStart(world, explosion)) {
            explosion.explode();
            explosion.finalizeExplosion(false);
        }
        //Note: Regardless of if the event got canceled vanilla syncs it to the client so that sounds and the like can play
        syncExplosionToClient(world, explosion);

        //Next go through the different locations that were inside our reactor that should have exploded and make sure
        // that if they didn't explode that we manually run the logic to make them "explode" so that the reactor stops
        //Note: Shuffle so that the drops don't end up all in one corner of an explosion
        Util.shuffle(toBlow, world.random);
        List<Pair<ItemStack, BlockPos>> drops = new ArrayList<>();
        for (BlockPos toExplode : toBlow) {
            world.getBlockState(toExplode)
                  .onExplosionHit(world, toExplode, explosion, (stack, position) -> Explosion.addOrAppendStack(drops, stack, position));
        }
        for (Pair<ItemStack, BlockPos> pair : drops) {
            Block.popResource(world, pair.getSecond(), pair.getFirst());
        }
    }

    private static void syncExplosionToClient(ServerLevel level, Explosion explosion) {
        //Note: We can just sync the explosion the same way vanilla does (ServerLevel#explode) after setting it off
        // as the client doesn't need to know about the multiblock's uuid that caused the meltdown
        if (!explosion.interactsWithBlocks()) {
            explosion.clearToBlow();
        }
        Vec3 center = explosion.center();
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(center.x, center.y, center.z) < 4096.0) {
                player.connection.send(new ClientboundExplodePacket(center.x, center.y, center.z,
                      explosion.radius(),
                      explosion.getToBlow(),
                      explosion.getHitPlayers().get(player),
                      explosion.getBlockInteraction(),
                      explosion.getSmallExplosionParticles(),
                      explosion.getLargeExplosionParticles(),
                      explosion.getExplosionSound()
                ));
            }
        }
    }

    public static class MeltdownExplosion extends Explosion {

        private final UUID multiblockID;

        private MeltdownExplosion(Level world, double x, double y, double z, float radius, boolean causesFire, BlockInteraction mode, UUID multiblockID) {
            super(world, null, x, y, z, radius, causesFire, mode);
            this.multiblockID = multiblockID;
        }

        public UUID getMultiblockID() {
            return multiblockID;
        }
    }
}