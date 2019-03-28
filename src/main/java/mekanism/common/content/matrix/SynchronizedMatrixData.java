package mekanism.common.content.matrix;

import java.util.HashSet;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.tile.TileEntityInductionCell;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class SynchronizedMatrixData extends SynchronizedData<SynchronizedMatrixData> {

    public NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);

    public Set<Coord4D> cells = new HashSet<>();

    public Set<Coord4D> providers = new HashSet<>();

    public double remainingInput;
    public double lastInput;

    public double remainingOutput;
    public double lastOutput;

    public double clientEnergy;
    public double storageCap;
    public double transferCap;

    @Override
    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    public double getEnergy(World world) {
        double ret = 0;

        for (Coord4D coord : cells) {
            TileEntity tile = coord.getTileEntity(world);

            if (tile instanceof TileEntityInductionCell) {
                ret += ((TileEntityInductionCell) tile).getEnergy();
            }
        }

        return ret;
    }

    public void setEnergy(World world, double energy) {
        for (Coord4D coord : cells) {
            TileEntity tile = coord.getTileEntity(world);

            if (tile instanceof TileEntityInductionCell) {
                TileEntityInductionCell cell = (TileEntityInductionCell) tile;

                cell.setEnergy(0);

                double toAdd = Math.min(cell.getMaxEnergy(), energy);
                cell.setEnergy(toAdd);
                energy -= toAdd;
            }
        }
    }
}
