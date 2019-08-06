package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.TileNetworkList;
import mekanism.common.MekanismBlock;
import mekanism.common.base.IActiveState;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.HeatUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityFuelwoodHeater extends TileEntityMekanism implements IHeatTransfer, IActiveState {

    public double temperature;
    public double heatToAbsorb = 0;

    public int burnTime;
    public int maxBurnTime;

    public double lastEnvironmentLoss;

    public TileEntityFuelwoodHeater() {
        super(MekanismBlock.FUELWOOD_HEATER);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            boolean burning = false;
            if (burnTime > 0) {
                burnTime--;
                burning = true;
            } else {
                if (!getInventory().get(0).isEmpty()) {
                    maxBurnTime = burnTime = TileEntityFurnace.getItemBurnTime(getInventory().get(0)) / 2;
                    if (burnTime > 0) {
                        ItemStack preShrunk = getInventory().get(0).copy();
                        getInventory().get(0).shrink(1);
                        if (getInventory().get(0).getCount() == 0) {
                            getInventory().set(0, preShrunk.getItem().getContainerItem(preShrunk));
                        }
                        burning = true;
                    }
                }
            }
            if (burning) {
                heatToAbsorb += MekanismConfig.current().general.heatPerFuelTick.val();
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
        burnTime = nbtTags.getInteger("burnTime");
        maxBurnTime = nbtTags.getInteger("maxBurnTime");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setDouble("temperature", temperature);
        nbtTags.setInteger("burnTime", burnTime);
        nbtTags.setInteger("maxBurnTime", maxBurnTime);
        return nbtTags;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            temperature = dataStream.readDouble();
            burnTime = dataStream.readInt();
            maxBurnTime = dataStream.readInt();
            lastEnvironmentLoss = dataStream.readDouble();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(temperature);
        data.add(burnTime);
        data.add(maxBurnTime);
        data.add(lastEnvironmentLoss);
        return data;
    }

    @Override
    public boolean canSetFacing(@Nonnull EnumFacing facing) {
        return facing != EnumFacing.DOWN && facing != EnumFacing.UP;
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
}