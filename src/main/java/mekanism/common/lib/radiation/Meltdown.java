package mekanism.common.lib.radiation;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

public class Meltdown {

    private static final int DURATION = 100;

    private final BlockPos minPos, maxPos;
    private final double magnitude, chance;
    private final UUID multiblockID;

    private int ticksExisted;

    public Meltdown(BlockPos minPos, BlockPos maxPos, double magnitude, double chance, UUID multiblockID) {
        this(minPos, maxPos, magnitude, chance, multiblockID, 0);
    }

    private Meltdown(BlockPos minPos, BlockPos maxPos, double magnitude, double chance, UUID multiblockID, int ticksExisted) {
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.magnitude = magnitude;
        this.chance = chance;
        this.multiblockID = multiblockID;
        this.ticksExisted = ticksExisted;
    }

    public static Meltdown load(CompoundNBT tag) {
        return new Meltdown(
              NBTUtil.readBlockPos(tag.getCompound(NBTConstants.MIN)),
              NBTUtil.readBlockPos(tag.getCompound(NBTConstants.MAX)),
              tag.getDouble(NBTConstants.MAGNITUDE),
              tag.getDouble(NBTConstants.CHANCE),
              tag.getUUID(NBTConstants.INVENTORY_ID),
              tag.getInt(NBTConstants.AGE)
        );
    }

    public void write(CompoundNBT tag) {
        tag.put(NBTConstants.MIN, NBTUtil.writeBlockPos(minPos));
        tag.put(NBTConstants.MAX, NBTUtil.writeBlockPos(maxPos));
        tag.putDouble(NBTConstants.MAGNITUDE, magnitude);
        tag.putDouble(NBTConstants.CHANCE, chance);
        tag.putUUID(NBTConstants.INVENTORY_ID, multiblockID);
        tag.putInt(NBTConstants.AGE, ticksExisted);
    }

    public boolean update(World world) {
        ticksExisted++;

        if (world.random.nextInt() % 10 == 0 && world.random.nextDouble() < magnitude * chance) {
            int x = MathHelper.nextInt(world.random, minPos.getX(), maxPos.getX());
            int y = MathHelper.nextInt(world.random, minPos.getY(), maxPos.getY());
            int z = MathHelper.nextInt(world.random, minPos.getZ(), maxPos.getZ());
            createExplosion(world, x, y, z, 8, true, Explosion.Mode.DESTROY);
        }

        if (!WorldUtils.isBlockLoaded(world, minPos) || !WorldUtils.isBlockLoaded(world, maxPos)) {
            return true;
        }

        return ticksExisted >= DURATION;
    }

    /**
     * Creates an explosion and ensures all blocks that are inside our meltdown radius actually get destroyed
     */
    private void createExplosion(World world, double x, double y, double z, float radius, boolean causesFire, Explosion.Mode mode) {
        Explosion explosion = new MeltdownExplosion(world, x, y, z, radius, causesFire, mode, multiblockID);
        //Calculate which block positions should get broken based on the logic that would happen in Explosion#explode
        List<BlockPos> toBlow = new ArrayList<>();
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = j / 7.5 - 1.0;
                        double d1 = k / 7.5 - 1.0;
                        double d2 = l / 7.5 - 1.0;
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float f = radius * (0.7F + world.random.nextFloat() * 0.6F);
                        double d4 = x;
                        double d6 = y;
                        double d8 = z;

                        for (; f > 0.0F; f -= 0.22500001F) {
                            BlockPos pos = new BlockPos(d4, d6, d8);
                            BlockState blockstate = world.getBlockState(pos);
                            FluidState fluidstate = blockstate.getFluidState();
                            if (!blockstate.isAir(world, pos) || !fluidstate.isEmpty()) {
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
        if (!ForgeEventFactory.onExplosionStart(world, explosion)) {
            explosion.explode();
            explosion.finalizeExplosion(true);
        }
        //Next go through the different locations that were inside our reactor that should have exploded and make sure
        // that if they didn't explode that we manually run the logic to make them "explode" so that the reactor stops
        //Note: Shuffle so that the drops don't end up all in one corner of an explosion
        Collections.shuffle(toBlow, world.random);
        List<Pair<ItemStack, BlockPos>> drops = new ArrayList<>();
        for (BlockPos toExplode : toBlow) {
            BlockState state = world.getBlockState(toExplode);
            //If the block didn't already get broken when running the normal explosion
            if (!state.isAir(world, toExplode)) {
                if (state.canDropFromExplosion(world, toExplode, explosion) && world instanceof ServerWorld) {
                    TileEntity tileentity = state.hasTileEntity() ? world.getBlockEntity(toExplode) : null;
                    LootContext.Builder lootContextBuilder = new LootContext.Builder((ServerWorld) world)
                          .withRandom(world.random)
                          .withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(toExplode))
                          .withParameter(LootParameters.TOOL, ItemStack.EMPTY)
                          .withOptionalParameter(LootParameters.BLOCK_ENTITY, tileentity)
                          .withOptionalParameter(LootParameters.THIS_ENTITY, null);
                    if (mode == Explosion.Mode.DESTROY) {
                        lootContextBuilder.withParameter(LootParameters.EXPLOSION_RADIUS, radius);
                    }
                    state.getDrops(lootContextBuilder).forEach(stack -> addBlockDrops(drops, stack, toExplode));
                }
                state.onBlockExploded(world, toExplode, explosion);
            }
        }
        for (Pair<ItemStack, BlockPos> pair : drops) {
            Block.popResource(world, pair.getSecond(), pair.getFirst());
        }
    }

    //Copy of Explosion#addBlockDrops
    private static void addBlockDrops(List<Pair<ItemStack, BlockPos>> dropPositions, ItemStack stack, BlockPos pos) {
        for (int i = 0, size = dropPositions.size(); i < size; ++i) {
            Pair<ItemStack, BlockPos> pair = dropPositions.get(i);
            ItemStack itemstack = pair.getFirst();
            if (ItemEntity.areMergable(itemstack, stack)) {
                ItemStack itemstack1 = ItemEntity.merge(itemstack, stack, 16);
                dropPositions.set(i, Pair.of(itemstack1, pair.getSecond()));
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
        dropPositions.add(Pair.of(stack, pos));
    }

    public static class MeltdownExplosion extends Explosion {

        private final UUID multiblockID;

        private MeltdownExplosion(World world, double x, double y, double z, float radius, boolean causesFire, Mode mode, UUID multiblockID) {
            super(world, null, null, null, x, y, z, radius, causesFire, mode);
            this.multiblockID = multiblockID;
        }

        public UUID getMultiblockID() {
            return multiblockID;
        }
    }
}