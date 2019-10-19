package mekanism.common.tile;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.LaserManager;
import mekanism.common.LaserManager.LaserInfo;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.IInventorySlotHolder;
import mekanism.common.inventory.InventorySlotHelper;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntityLaserTractorBeam extends TileEntityMekanism implements ILaserReceptor, IComparatorSupport {

    public static final double MAX_ENERGY = 5E9;
    public double collectedEnergy = 0;
    public double lastFired = 0;
    public boolean on = false;
    public Coord4D digging;
    public double diggingProgress;

    public TileEntityLaserTractorBeam() {
        super(MekanismBlock.LASER_TRACTOR_BEAM);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper.Builder builder = InventorySlotHelper.Builder.forSide(this::getDirection);
        for (int slotX = 0; slotX < 9; slotX++) {
            for (int slotY = 0; slotY < 3; slotY++) {
                //TODO: We probably want it to create normal looking slots instead of "output" slots
                builder.addSlot(OutputInventorySlot.at(this, 8 + slotX * 18, 16 + slotY * 18));
            }
        }
        return builder.build();
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
        if (isRemote()) {
            if (on) {
                BlockRayTraceResult mop = LaserManager.fireLaserClient(this, getDirection(), lastFired, world);
                Coord4D hitCoord = new Coord4D(mop, world);
                if (!hitCoord.equals(digging)) {
                    digging = mop.getType() == Type.MISS ? null : hitCoord;
                    diggingProgress = 0;
                }

                if (mop.getType() != Type.MISS) {
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
            Coord4D hitCoord = new Coord4D(info.movingPos, world);
            if (!hitCoord.equals(digging)) {
                digging = info.movingPos.getType() == Type.MISS ? null : hitCoord;
                diggingProgress = 0;
            }
            if (info.movingPos.getType() != Type.MISS) {
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
                    //TODO: Else tell client to spawn hit effect, instead of having there be client side onUpdate code for TileEntityLaser
                }
            }
            setEnergy(getEnergy() - firing);
        } else if (on) {
            on = false;
            diggingProgress = 0;
            Mekanism.packetHandler.sendUpdatePacket(this);
        }
    }

    @Override
    public double getEnergy() {
        return collectedEnergy;
    }

    @Override
    public void setEnergy(double energy) {
        collectedEnergy = Math.max(0, Math.min(energy, MAX_ENERGY));
    }

    public void receiveDrops(List<ItemStack> drops) {
        List<IInventorySlot> inventorySlots = getInventorySlots(null);
        for (ItemStack drop : drops) {
            for (IInventorySlot slot : inventorySlots) {
                drop = slot.insertItem(drop, Action.EXECUTE);
                if (drop.isEmpty()) {
                    //If we inserted it all, then break otherwise try to insert the remainder into another slot
                    break;
                }
            }
            if (!drop.isEmpty()) {
                //If we have some drop left over that we couldn't fit, then spawn it into the world
                Block.spawnAsEntity(getWorld(), pos, drop);
            }
        }
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
        if (isRemote()) {
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
        return ItemHandlerHelper.calcRedstoneFromInventory(this);
    }
}