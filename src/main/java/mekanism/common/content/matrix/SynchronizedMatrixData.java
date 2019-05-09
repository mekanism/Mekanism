package mekanism.common.content.matrix;

import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

//TODO: Do something better for purposes of double precision such as BigInt
public class SynchronizedMatrixData extends SynchronizedData<SynchronizedMatrixData> {

    private NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    private Set<Coord4D> providers = new HashSet<>();
    private Set<Coord4D> cells = new HashSet<>();
    private double remainingOutput;
    private double remainingInput;
    private double lastOutput;
    private double lastInput;

    private double cachedTotal;
    private double transferCap;
    private double storageCap;

    private int clientProviders;
    private int clientCells;

    @Override
    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    public void setInventory(NonNullList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    public void addCell(Coord4D coord, TileEntityInductionCell cell) {
        //As we already have the two different variables just pass them instead of accessing world to get tile again
        cells.add(coord);
        storageCap += cell.tier.getMaxEnergy();
        cachedTotal += cell.getEnergy();
    }

    public void addProvider(Coord4D coord, TileEntityInductionProvider provider) {
        providers.add(coord);
        transferCap += provider.tier.getOutput();
    }

    public double getEnergyPostQueue() {
        //The reason we do remainingOutput - remainingInput when it logically appears that
        // it should be the other way around, is because in reality our value is
        // (transferCap - remainingInput) - (transferCap - remainingOutput)
        // which simplifies to remainingOutput - remainingInput
        // This is because remainingInput and remainingOutput go down if we have
        // the corresponding one queued.
        return cachedTotal - remainingInput + remainingOutput;
    }

    public void tick(World world) {
        //See comment in getEnergyPostQueue for explanation of how lastChange is calculated.
        double lastChange = remainingOutput - remainingInput;
        if (lastChange < 0) {
            //We are removing energy
            removeEnergy(world, -lastChange);
        } else if (lastChange > 0) {
            //we are adding energy
            addEnergy(world, lastChange);
        }
        cachedTotal += lastChange;

        lastInput = transferCap - remainingInput;
        remainingInput = transferCap;
        lastOutput = transferCap - remainingOutput;
        remainingOutput = transferCap;
    }

    public double queueEnergyAddition(double energy, boolean simulate) {
        if (energy < 0) {
            //Ensure that the correct queue type gets called
            return queueEnergyRemoval(-energy, simulate);
        }
        if (energy > remainingInput) {
            energy = remainingInput;
        }
        //Check to see if we are trying to add more energy than we have room for,
        // as we want to be as accurate as possible with the values we return
        // It is possible that the energy we have space for is a lot less than the amount we
        // can input at once such as if the matrix is almost full.
        double availableEnergy = storageCap - getEnergyPostQueue();
        if (energy > availableEnergy) {
            //Only allow addition of
            energy = availableEnergy;
        }
        if (!simulate) {
            //Lower amount remaining input rate by the amount we accepted
            remainingInput -= energy;
        }
        return energy;
    }

    public double queueEnergyRemoval(double energy, boolean simulate) {
        if (energy < 0) {
            //Ensure that the correct queue type gets called
            return queueEnergyAddition(-energy, simulate);
        }
        if (energy > remainingOutput) {
            //If it is more than we can output lower it further
            energy = remainingOutput;
        }
        //Check to see if we are trying to remove more energy than we have to remove,
        // as we want to be as accurate as possible with the values we return
        // It is possible that the energy we have stored is a lot less than the amount we
        // can output at once such as if the matrix is almost empty.
        double availableEnergy = getEnergyPostQueue();
        if (energy > availableEnergy) {
            //If it is more than we have lower it further
            energy = availableEnergy;
        }
        if (!simulate) {
            //Lower amount remaining output rate by the amount we accepted
            remainingOutput -= energy;
        }
        return energy;
    }

    public void queueSetEnergy(double energy) {
        if (energy > storageCap) {
            energy = storageCap;
        }
        //TODO: Potentially should allow setting it directly to something bypassing rate limit
        // API wise that makes sense, however this is only *really* used by IC2's setStored
        double difference = energy - getEnergyPostQueue();
        if (difference != 0) {
            //We call addition as values greater than zero are for addition
            // The queue methods also ensure that it is using the correct method
            // as IC2 changes energy by passing negative values for removal.
            // This also adds extra safety in case a mod ends up using the wrong
            // method for adding/removing energy. So when it is negative
            // queueEnergyAddition will pass it to queueEnergyRemoval
            queueEnergyAddition(difference, false);
        }
    }

    private void addEnergy(World world, double energy) {
        for (Coord4D coord : cells) {
            TileEntity tile = coord.getTileEntity(world);
            if (tile instanceof TileEntityInductionCell) {
                TileEntityInductionCell cell = (TileEntityInductionCell) tile;
                double cellEnergy = cell.getEnergy();
                double cellMax = cell.getMaxEnergy();
                if (cellEnergy >= cellMax) {
                    //Is full
                    //Should this just be ==
                    continue;
                }
                double cellSpace = cellMax - cellEnergy;
                if (cellSpace >= energy) {
                    //All fits
                    cell.setEnergy(cellEnergy + energy);
                    //This cells data changed, so mark it for saving
                    MekanismUtils.saveChunk(cell);
                    break;
                } else {
                    //We have left over so
                    cell.setEnergy(cellMax);
                    energy -= cellSpace;
                    //This cells data changed, so mark it for saving
                    MekanismUtils.saveChunk(cell);
                }
            }
        }
    }

    private void removeEnergy(World world, double energy) {
        for (Coord4D coord : cells) {
            TileEntity tile = coord.getTileEntity(world);
            if (tile instanceof TileEntityInductionCell) {
                TileEntityInductionCell cell = (TileEntityInductionCell) tile;
                double cellEnergy = cell.getEnergy();
                if (cellEnergy == 0) {
                    //It is already empty
                    continue;
                }
                if (cellEnergy >= energy) {
                    //Can supply it all
                    cell.setEnergy(cellEnergy - energy);
                    //This cells data changed, so mark it for saving
                    MekanismUtils.saveChunk(cell);
                    break;
                } else {
                    //We need to keep removing from other ones
                    cell.setEnergy(0);
                    energy -= cellEnergy;
                    //This cells data changed, so mark it for saving
                    MekanismUtils.saveChunk(cell);
                }
            }
        }
    }

    public TileNetworkList addStructureData(TileNetworkList data) {
        data.add(cachedTotal);
        data.add(storageCap);
        data.add(transferCap);
        data.add(lastInput);
        data.add(lastOutput);

        data.add(volWidth);
        data.add(volHeight);
        data.add(volLength);

        data.add(cells.size());
        data.add(providers.size());
        return data;
    }

    public void readStructureData(ByteBuf dataStream) {
        cachedTotal = dataStream.readDouble();
        storageCap = dataStream.readDouble();
        transferCap = dataStream.readDouble();
        lastInput = dataStream.readDouble();
        lastOutput = dataStream.readDouble();

        volWidth = dataStream.readInt();
        volHeight = dataStream.readInt();
        volLength = dataStream.readInt();

        clientCells = dataStream.readInt();
        clientProviders = dataStream.readInt();
    }

    public double getStorageCap() {
        return storageCap;
    }

    public double getTransferCap() {
        return transferCap;
    }

    public double getEnergy() {
        return cachedTotal;
    }

    public double getLastInput() {
        return lastInput;
    }

    public double getLastOutput() {
        return lastOutput;
    }

    public double getRemainingInput() {
        return remainingInput;
    }

    public double getRemainingOutput() {
        return remainingOutput;
    }

    public int getCellCount() {
        return cells.isEmpty() ? clientCells : cells.size();
    }

    public int getProviderCount() {
        return providers.isEmpty() ? clientProviders : providers.size();
    }
}