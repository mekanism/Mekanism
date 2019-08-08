package mekanism.common.tile.induction_cell;

import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.base.IBlockProvider;
import mekanism.common.block.basic.BlockInductionCell;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class TileEntityInductionCell extends TileEntityMekanism implements IStrictEnergyStorage {

    public InductionCellTier tier;

    public TileEntityInductionCell(IBlockProvider blockProvider) {
        super(blockProvider);
        this.tier = ((BlockInductionCell) blockProvider.getBlock()).getTier();
    }

    public double electricityStored;

    @Override
    public void onUpdate() {
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (world.isRemote) {
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
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        electricityStored = nbtTags.getDouble("electricityStored");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble("electricityStored", electricityStored);
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
    public boolean hasCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<?> capability, Direction facing) {
        return capability == Capabilities.ENERGY_STORAGE_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY) {
            return Capabilities.ENERGY_STORAGE_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, facing);
    }
}
