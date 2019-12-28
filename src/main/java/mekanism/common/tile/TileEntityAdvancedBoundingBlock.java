package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.Mekanism;
import mekanism.common.base.IAdvancedBoundingBlock;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.registries.MekanismTileEntityTypes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityAdvancedBoundingBlock extends TileEntityBoundingBlock implements IStrictEnergyAcceptor, IComputerIntegration, ISpecialConfigData {

    public TileEntityAdvancedBoundingBlock() {
        super(MekanismTileEntityTypes.ADVANCED_BOUNDING_BLOCK.getTileEntityType());
    }

    @Override
    public double acceptEnergy(Direction side, double amount, boolean simulate) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null || !canReceiveEnergy(side)) {
            return 0;
        }
        return inv.acceptEnergy(side, amount, simulate);
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return false;
        }
        return inv.canBoundReceiveEnergy(getPos(), side);
    }

    public IAdvancedBoundingBlock getInv() {
        // Return the inventory/main tile; note that it's possible, esp. when chunks are
        // loading that the inventory/main tile has not yet loaded and thus is null.
        final TileEntity tile = getMainTile();
        if (tile == null) {
            return null;
        }
        if (!(tile instanceof IAdvancedBoundingBlock)) {
            // On the off chance that another block got placed there (which seems only likely with corruption,
            // go ahead and log what we found.
            Mekanism.logger.error("Found tile {} instead of an IAdvancedBoundingBlock, at {}. Multiblock cannot function", tile, getMainPos());
            //world.removeBlock(mainPos, false);
            return null;
        }
        return (IAdvancedBoundingBlock) tile;
    }

    @Override
    public void onPower() {
        super.onPower();
        IAdvancedBoundingBlock inv = getInv();
        if (inv != null) {
            inv.onPower();
        }
    }

    @Override
    public void onNoPower() {
        super.onNoPower();
        IAdvancedBoundingBlock inv = getInv();
        if (inv != null) {
            inv.onNoPower();
        }
    }

    @Override
    public String[] getMethods() {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return new String[]{};
        }
        return inv.getMethods();
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return new Object[]{};
        }
        return inv.invoke(method, arguments);
    }

    @Override
    public CompoundNBT getConfigurationData(CompoundNBT nbtTags) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return new CompoundNBT();
        }
        return inv.getConfigurationData(nbtTags);
    }

    @Override
    public void setConfigurationData(CompoundNBT nbtTags) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return;
        }
        inv.setConfigurationData(nbtTags);
    }

    @Override
    public String getDataType() {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return "null";
        }
        return inv.getDataType();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.TILE_NETWORK_CAPABILITY) {
            return super.getCapability(capability, side);
        }
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return super.getCapability(capability, side);
        }
        return inv.getOffsetCapability(capability, side, pos.subtract(getMainPos()));
    }
}