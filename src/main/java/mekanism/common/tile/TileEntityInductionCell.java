package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityInductionCell extends TileEntityMekanism implements IStrictEnergyStorage {

    public double electricityStored;

    @Override
    public void onUpdate() {
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            electricityStored = dataStream.readDouble();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(electricityStored);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        electricityStored = nbtTags.getDouble("electricityStored");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setDouble("electricityStored", electricityStored);
        return nbtTags;
    }

    @Override
    public double getEnergy() {
        return electricityStored;
    }

    @Override
    public void setEnergy(double energy) {
        electricityStored = Math.min(energy, getMaxEnergy());
    }

    @Override
    public double getMaxEnergy() {
        return tier.getMaxEnergy();
    }

    @Override
    public boolean hasCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<?> capability, EnumFacing facing) {
        return capability == Capabilities.ENERGY_STORAGE_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY) {
            return Capabilities.ENERGY_STORAGE_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, facing);
    }
}
