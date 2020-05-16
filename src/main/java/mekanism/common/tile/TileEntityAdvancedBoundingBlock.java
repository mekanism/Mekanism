package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.Upgrade;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IAdvancedBoundingBlock;
import mekanism.common.tile.interfaces.IUpgradeTile;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

//TODO: Comparator handling?
public class TileEntityAdvancedBoundingBlock extends TileEntityBoundingBlock implements ISpecialConfigData, IUpgradeTile {

    public TileEntityAdvancedBoundingBlock() {
        super(MekanismTileEntityTypes.ADVANCED_BOUNDING_BLOCK.getTileEntityType());
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
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return super.getCapability(capability, side);
        }
        return inv.getOffsetCapability(capability, side, pos.subtract(getMainPos()));
    }

    @Override
    public boolean supportsUpgrades() {
        IAdvancedBoundingBlock inv = getInv();
        return inv instanceof IUpgradeTile && ((IUpgradeTile) inv).supportsUpgrades();
    }

    @Override
    public TileComponentUpgrade getComponent() {
        IAdvancedBoundingBlock inv = getInv();
        if (inv instanceof IUpgradeTile) {
            IUpgradeTile upgradeTile = (IUpgradeTile) inv;
            if (upgradeTile.supportsUpgrades()) {
                return upgradeTile.getComponent();
            }
        }
        return null;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgradeType) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv instanceof IUpgradeTile) {
            IUpgradeTile upgradeTile = (IUpgradeTile) inv;
            if (upgradeTile.supportsUpgrades()) {
                upgradeTile.recalculateUpgrades(upgradeType);
            }
        }
    }
}