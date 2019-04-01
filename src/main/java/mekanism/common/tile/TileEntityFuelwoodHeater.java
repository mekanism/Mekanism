package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.api.TileNetworkList;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityFuelwoodHeater extends TileEntityContainerBlock implements IHeatTransfer, ISecurityTile,
      IActiveState {

    public double temperature;
    public double heatToAbsorb = 0;

    public int burnTime;
    public int maxBurnTime;

    /**
     * Whether or not this machine is in it's active state.
     */
    public boolean isActive;

    /**
     * The client's current active state.
     */
    public boolean clientActive;

    /**
     * How many ticks must pass until this block's active state can sync with the client.
     */
    public int updateDelay;

    public double lastEnvironmentLoss;

    public TileComponentSecurity securityComponent = new TileComponentSecurity(this);

    public TileEntityFuelwoodHeater() {
        super("FuelwoodHeater");
        inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        if (world.isRemote && updateDelay > 0) {
            updateDelay--;

            if (updateDelay == 0 && clientActive != isActive) {
                isActive = clientActive;
                MekanismUtils.updateBlock(world, getPos());
            }
        }

        if (!world.isRemote) {
            if (updateDelay > 0) {
                updateDelay--;

                if (updateDelay == 0 && clientActive != isActive) {
                    Mekanism.packetHandler.sendToReceivers(
                          new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                          new Range4D(Coord4D.get(this)));
                }
            }

            boolean burning = false;

            if (burnTime > 0) {
                burnTime--;
                burning = true;
            } else {
                if (!inventory.get(0).isEmpty()) {
                    maxBurnTime = burnTime = TileEntityFurnace.getItemBurnTime(inventory.get(0)) / 2;

                    if (burnTime > 0) {
                        ItemStack preShrunk = inventory.get(0).copy();
                        inventory.get(0).shrink(1);

                        if (inventory.get(0).getCount() == 0) {
                            inventory.set(0, preShrunk.getItem().getContainerItem(preShrunk));
                        }

                        burning = true;
                    }
                }
            }

            if (burning) {
                heatToAbsorb += general.heatPerFuelTick;
            }

            double[] loss = simulateHeat();
            applyTemperatureChange();

            lastEnvironmentLoss = loss[1];

            setActive(burning);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        temperature = nbtTags.getDouble("temperature");
        clientActive = isActive = nbtTags.getBoolean("isActive");
        burnTime = nbtTags.getInteger("burnTime");
        maxBurnTime = nbtTags.getInteger("maxBurnTime");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setDouble("temperature", temperature);
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("burnTime", burnTime);
        nbtTags.setInteger("maxBurnTime", maxBurnTime);

        return nbtTags;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            temperature = dataStream.readDouble();
            clientActive = dataStream.readBoolean();
            burnTime = dataStream.readInt();
            maxBurnTime = dataStream.readInt();

            lastEnvironmentLoss = dataStream.readDouble();

            if (updateDelay == 0 && clientActive != isActive) {
                updateDelay = general.UPDATE_DELAY;
                isActive = clientActive;
                MekanismUtils.updateBlock(world, getPos());
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(temperature);
        data.add(isActive);
        data.add(burnTime);
        data.add(maxBurnTime);

        data.add(lastEnvironmentLoss);

        return data;
    }

    @Override
    public boolean canSetFacing(int side) {
        return side != 0 && side != 1;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return new int[]{0};
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack stack) {
        return TileEntityFurnace.getItemBurnTime(stack) > 0;
    }

    @Override
    public boolean getActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        isActive = active;

        if (clientActive != active && updateDelay == 0) {
            Mekanism.packetHandler
                  .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));

            updateDelay = 10;
            clientActive = active;
        }
    }

    @Override
    public boolean renderUpdate() {
        return false;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    public double getTemp() {
        return temperature;
    }

    @Override
    public double getInverseConductionCoefficient() {
        return 5;
    }

    @Override
    public double getInsulationCoefficient(EnumFacing side) {
        return 1000;
    }

    @Override
    public void transferHeatTo(double heat) {
        heatToAbsorb += heat;
    }

    @Override
    public double[] simulateHeat() {
        return HeatUtils.simulate(this);
    }

    @Override
    public double applyTemperatureChange() {
        temperature += heatToAbsorb;
        heatToAbsorb = 0;

        return temperature;
    }

    @Override
    public boolean canConnectHeat(EnumFacing side) {
        return true;
    }

    @Override
    public IHeatTransfer getAdjacent(EnumFacing side) {
        TileEntity adj = Coord4D.get(this).offset(side).getTileEntity(world);

        if (CapabilityUtils.hasCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite())) {
            return CapabilityUtils.getCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite());
        }

        return null;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.HEAT_TRANSFER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.cast(this);
        }

        return super.getCapability(capability, side);
    }

    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }
}
