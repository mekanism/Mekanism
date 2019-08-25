package mekanism.common.tile;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.LaserManager;
import mekanism.common.LaserManager.LaserInfo;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityLaserTractorBeam extends TileEntityMekanism implements ILaserReceptor, IComparatorSupport {

    public static final double MAX_ENERGY = 5E9;
    public static int[] availableSlotIDs = InventoryUtils.getIntRange(0, 26);
    public double collectedEnergy = 0;
    public double lastFired = 0;
    public boolean on = false;
    public Coord4D digging;
    public double diggingProgress;

    public TileEntityLaserTractorBeam() {
        super(MekanismBlock.LASER_TRACTOR_BEAM);
    }

    @Override
    public void receiveLaserEnergy(double energy, Direction side) {
        setEnergy(getEnergy() + energy);
    }

    @Override
    public boolean canLasersDig() {
        return false;
    }

    @Override
    public void onUpdate() {
        if (world.isRemote) {
            if (on) {
                BlockRayTraceResult mop = LaserManager.fireLaserClient(this, getDirection(), lastFired, world);
                Coord4D hitCoord = mop == null ? null : new Coord4D(mop, world);
                if (hitCoord == null || !hitCoord.equals(digging)) {
                    digging = hitCoord;
                    diggingProgress = 0;
                }

                if (hitCoord != null) {
                    BlockState blockHit = hitCoord.getBlockState(world);
                    TileEntity tileHit = hitCoord.getTileEntity(world);
                    float hardness = blockHit.getBlockHardness(world, hitCoord.getPos());
                    if (hardness >= 0 && !CapabilityUtils.getCapabilityHelper(tileHit, Capabilities.LASER_RECEPTOR_CAPABILITY, mop.getFace())
                          .matches(receptor -> !receptor.canLasersDig())) {
                        diggingProgress += lastFired;
                        if (diggingProgress < hardness * MekanismConfig.general.laserEnergyNeededPerHardness.get()) {
                            Mekanism.proxy.addHitEffects(hitCoord, mop);
                        }
                    }
                }

            }
        } else if (collectedEnergy > 0) {
            double firing = collectedEnergy;
            if (!on || firing != lastFired) {
                on = true;
                lastFired = firing;
                Mekanism.packetHandler.sendUpdatePacket(this);
            }

            LaserInfo info = LaserManager.fireLaser(this, getDirection(), firing, world);
            Coord4D hitCoord = info.movingPos == null ? null : new Coord4D(info.movingPos, world);

            if (hitCoord == null || !hitCoord.equals(digging)) {
                digging = hitCoord;
                diggingProgress = 0;
            }

            if (hitCoord != null) {
                BlockState blockHit = hitCoord.getBlockState(world);
                TileEntity tileHit = hitCoord.getTileEntity(world);
                float hardness = blockHit.getBlockHardness(world, hitCoord.getPos());

                if (hardness >= 0 && !CapabilityUtils.getCapabilityHelper(tileHit, Capabilities.LASER_RECEPTOR_CAPABILITY, info.movingPos.getFace())
                      .matches(receptor -> !receptor.canLasersDig())) {
                    diggingProgress += firing;
                    if (diggingProgress >= hardness * MekanismConfig.general.laserEnergyNeededPerHardness.get()) {
                        List<ItemStack> drops = LaserManager.breakBlock(hitCoord, false, world, pos);
                        if (drops != null) {
                            receiveDrops(drops);
                        }
                        diggingProgress = 0;
                    }
                }
            }
            setEnergy(getEnergy() - firing);
        } else if (on) {
            on = false;
            diggingProgress = 0;
            Mekanism.packetHandler.sendUpdatePacket(this);
        }
    }

    public double getEnergy() {
        return collectedEnergy;
    }

    public void setEnergy(double energy) {
        collectedEnergy = Math.max(0, Math.min(energy, MAX_ENERGY));
    }

    public void receiveDrops(List<ItemStack> drops) {
        outer:
        for (ItemStack drop : drops) {
            for (int i = 0; i < getInventory().size(); i++) {
                if (getInventory().get(i).isEmpty()) {
                    getInventory().set(i, drop);
                    continue outer;
                }
                ItemStack slot = getInventory().get(i);
                if (StackUtils.equalsWildcardWithNBT(slot, drop)) {
                    int change = Math.min(drop.getCount(), slot.getMaxStackSize() - slot.getCount());
                    slot.grow(change);
                    drop.shrink(change);
                    if (drop.getCount() <= 0) {
                        continue outer;
                    }
                }
            }
            Block.spawnAsEntity(world, pos, drop);
        }
    }

    @Override
    public boolean canInsertItem(int i, @Nonnull ItemStack itemStack, @Nonnull Direction side) {
        return false;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return availableSlotIDs;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(on);
        data.add(collectedEnergy);
        data.add(lastFired);
        return data;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (world.isRemote) {
            on = dataStream.readBoolean();
            collectedEnergy = dataStream.readDouble();
            lastFired = dataStream.readDouble();
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.LASER_RECEPTOR_CAPABILITY) {
            return Capabilities.LASER_RECEPTOR_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public int getRedstoneLevel() {
        return Container.calcRedstoneFromInventory(this);
    }
}