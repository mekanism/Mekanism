package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.LaserManager;
import mekanism.common.LaserManager.LaserInfo;
import mekanism.common.Mekanism;
import mekanism.api.TileNetworkList;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityLaserTractorBeam extends TileEntityContainerBlock implements ILaserReceptor, ISecurityTile {

    public static final double MAX_ENERGY = 5E9;
    public static int[] availableSlotIDs = InventoryUtils.getIntRange(0, 26);
    public double collectedEnergy = 0;
    public double lastFired = 0;
    public boolean on = false;
    public Coord4D digging;
    public double diggingProgress;
    public TileComponentSecurity securityComponent = new TileComponentSecurity(this);

    public TileEntityLaserTractorBeam() {
        super("LaserTractorBeam");
        inventory = NonNullList.withSize(27, ItemStack.EMPTY);
    }

    @Override
    public void receiveLaserEnergy(double energy, EnumFacing side) {
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
                RayTraceResult mop = LaserManager.fireLaserClient(this, facing, lastFired, world);
                Coord4D hitCoord = mop == null ? null : new Coord4D(mop, world);

                if (hitCoord == null || !hitCoord.equals(digging)) {
                    digging = hitCoord;
                    diggingProgress = 0;
                }

                if (hitCoord != null) {
                    IBlockState blockHit = hitCoord.getBlockState(world);
                    TileEntity tileHit = hitCoord.getTileEntity(world);
                    float hardness = blockHit.getBlockHardness(world, hitCoord.getPos());

                    if (!(hardness < 0 || (LaserManager.isReceptor(tileHit, mop.sideHit) && !(LaserManager
                          .getReceptor(tileHit, mop.sideHit).canLasersDig())))) {
                        diggingProgress += lastFired;

                        if (diggingProgress < hardness * general.laserEnergyNeededPerHardness) {
                            Mekanism.proxy.addHitEffects(hitCoord, mop);
                        }
                    }
                }

            }
        } else {
            if (collectedEnergy > 0) {
                double firing = collectedEnergy;

                if (!on || firing != lastFired) {
                    on = true;
                    lastFired = firing;
                    Mekanism.packetHandler.sendToAllAround(
                          new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                          Coord4D.get(this).getTargetPoint(50D));
                }

                LaserInfo info = LaserManager.fireLaser(this, facing, firing, world);
                Coord4D hitCoord = info.movingPos == null ? null : new Coord4D(info.movingPos, world);

                if (hitCoord == null || !hitCoord.equals(digging)) {
                    digging = hitCoord;
                    diggingProgress = 0;
                }

                if (hitCoord != null) {
                    IBlockState blockHit = hitCoord.getBlockState(world);
                    TileEntity tileHit = hitCoord.getTileEntity(world);
                    float hardness = blockHit.getBlockHardness(world, hitCoord.getPos());

                    if (!(hardness < 0 || (LaserManager.isReceptor(tileHit, info.movingPos.sideHit) && !(LaserManager
                          .getReceptor(tileHit, info.movingPos.sideHit).canLasersDig())))) {
                        diggingProgress += firing;

                        if (diggingProgress >= hardness * general.laserEnergyNeededPerHardness) {
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
                Mekanism.packetHandler.sendToAllAround(
                      new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                      Coord4D.get(this).getTargetPoint(50D));
            }
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
            for (int i = 0; i < inventory.size(); i++) {
                if (inventory.get(i).isEmpty()) {
                    inventory.set(i, drop);
                    continue outer;
                }

                ItemStack slot = inventory.get(i);

                if (StackUtils.equalsWildcardWithNBT(slot, drop)) {
                    int change = Math.min(drop.getCount(), slot.getMaxStackSize() - slot.getCount());
                    slot.grow(change);
                    drop.shrink(change);
                    if (drop.getCount() <= 0) {
                        continue outer;
                    }
                }
            }

            dropItem(drop);
        }
    }

    public void dropItem(ItemStack stack) {
        EntityItem item = new EntityItem(world, getPos().getX() + 0.5, getPos().getY() + 1, getPos().getZ() + 0.5,
              stack);
        item.motionX = world.rand.nextGaussian() * 0.05;
        item.motionY = world.rand.nextGaussian() * 0.05 + 0.2;
        item.motionZ = world.rand.nextGaussian() * 0.05;
        item.setPickupDelay(10);
        world.spawnEntity(item);
    }

    @Override
    public boolean canInsertItem(int i, @Nonnull ItemStack itemStack, @Nonnull EnumFacing side) {
        return false;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
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
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            on = dataStream.readBoolean();
            collectedEnergy = dataStream.readDouble();
            lastFired = dataStream.readDouble();
        }
    }

    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.LASER_RECEPTOR_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.LASER_RECEPTOR_CAPABILITY) {
            return Capabilities.LASER_RECEPTOR_CAPABILITY.cast(this);
        }

        return super.getCapability(capability, side);
    }
}
