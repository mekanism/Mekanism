package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntitySeismicVibrator extends TileEntityMekanism implements IActiveState, IBoundingBlock {

    public int clientPiston;

    private EnergyInventorySlot energySlot;

    public TileEntitySeismicVibrator() {
        super(MekanismBlocks.SEISMIC_VIBRATOR);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 143, 35));
        return builder.build();
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (getActive()) {
            clientPiston++;
        }
        updateActiveVibrators();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.discharge(this);
        if (MekanismUtils.canFunction(this) && getEnergy() >= getBaseUsage()) {
            setActive(true);
            pullEnergy(null, getBaseUsage(), false);
        } else {
            setActive(false);
        }
        updateActiveVibrators();
    }

    private void updateActiveVibrators() {
        if (getActive()) {
            Mekanism.activeVibrators.add(Coord4D.get(this));
        } else {
            Mekanism.activeVibrators.remove(Coord4D.get(this));
        }
    }

    @Override
    public void remove() {
        super.remove();
        Mekanism.activeVibrators.remove(Coord4D.get(this));
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return side == getOppositeDirection();
    }

    @Override
    public void onPlace() {
        MekanismUtils.makeBoundingBlock(getWorld(), getPos().up(), getPos());
    }

    @Override
    public void onBreak() {
        World world = getWorld();
        if (world != null) {
            world.removeBlock(getPos().up(), false);
            world.removeBlock(getPos(), false);
        }
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}