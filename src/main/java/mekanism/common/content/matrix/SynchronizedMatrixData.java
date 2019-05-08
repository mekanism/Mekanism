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

    private double getEnergyPostQueue() {
        //TODO: Does this need to have a Math.max(0, ); So that if a cell is removed midway through it can never be negative
        return cachedTotal - remainingOutput + remainingInput;
    }

    //TODO: Rename to reset and send or something like that
    public void resetRemaining(World world) {
        double lastChange = remainingInput - remainingOutput;
        if (lastChange < 0) {
            //We are removing energy
            removeEnergy(world, -lastChange);
        } else if (lastChange > 0) {
            //we are adding energy
            addEnergy(world, lastChange);
        }

        lastInput = transferCap - remainingInput;
        remainingInput = transferCap;
        lastOutput = transferCap - remainingOutput;
        remainingOutput = transferCap;
    }

    public double queueEnergyAddition(double energy) {
        if (energy > remainingInput) {
            energy = remainingInput;
        }

        double availableEnergy = storageCap - getEnergyPostQueue();
        if (energy > availableEnergy) {
            energy = availableEnergy;
            //TODO: write description of why checking just the > remainingInput is not enough
        }
        //Lower amount remaining input rate by the amount we accepted
        remainingInput -= energy;
        return energy;
    }

    public double queueEnergyRemoval(double energy) {
        if (energy > remainingOutput) {
            //If it is more than we can output lower it further
            energy = remainingOutput;
        }

        double availableEnergy = getEnergyPostQueue();
        if (energy > availableEnergy) {
            //If it is more than we have lower it further
            energy = availableEnergy;
            //TODO: write description of why checking just the > remainingOutput is not enough
        }
        //Lower amount remaining output rate by the amount we accepted
        remainingOutput -= energy;
        return energy;
    }

    public void queueSetEnergy(double energy) {
        if (energy > storageCap) {
            energy = storageCap;
        }
        double difference = energy - getEnergyPostQueue();
        if (difference < 0) {
            //We are removing energy
            queueEnergyRemoval(-difference);
        } else if (difference > 0) {
            //we are adding energy
            queueEnergyAddition(difference);
        }
    }

    private double addEnergy(World world, double energy) {
        if (energy > storageCap - cachedTotal) {
            energy = storageCap - cachedTotal;
            //TODO: Is this needed anymore? Maybe because of the if cells change
        }
        double energyToAdd = energy;
        Set<Coord4D> invalidCells = new HashSet<>();
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
                if (cellSpace >= energyToAdd) {
                    //All fits
                    cell.setEnergy(cellEnergy + energyToAdd);
                    energyToAdd = 0;
                    //This cells data changed, so mark it for saving
                    MekanismUtils.saveChunk(cell);
                    break;
                } else {
                    //We have left over so
                    cell.setEnergy(cellMax);
                    energyToAdd -= cellSpace;
                    //This cells data changed, so mark it for saving
                    MekanismUtils.saveChunk(cell);
                }
            } else {
                invalidCells.add(coord);
            }
        }
        cells.removeAll(invalidCells);
        //Amount actually added
        energy = energy - energyToAdd;
        cachedTotal += energy;
        return energy;
    }

    private double removeEnergy(World world, double energy) {
        if (energy > cachedTotal) {
            energy = cachedTotal;
            //TODO: Is this needed anymore? Maybe because of the if cells change
        }
        double energyToRemove = energy;
        Set<Coord4D> invalidCells = new HashSet<>();
        for (Coord4D coord : cells) {
            TileEntity tile = coord.getTileEntity(world);

            if (tile instanceof TileEntityInductionCell) {
                TileEntityInductionCell cell = (TileEntityInductionCell) tile;
                double cellEnergy = cell.getEnergy();
                if (cellEnergy == 0) {
                    //Is empty
                    continue;
                }
                if (cellEnergy >= energyToRemove) {
                    //Can supply it all
                    cell.setEnergy(cellEnergy - energyToRemove);
                    energyToRemove = 0;
                    //This cells data changed, so mark it for saving
                    MekanismUtils.saveChunk(cell);
                    break;
                } else {
                    //We need to keep removing from other ones
                    cell.setEnergy(0);
                    energyToRemove -= cellEnergy;
                    //This cells data changed, so mark it for saving
                    MekanismUtils.saveChunk(cell);
                }
            } else {
                invalidCells.add(coord);
            }
        }
        cells.removeAll(invalidCells);
        //Amount actually removed
        energy = energy - energyToRemove;
        cachedTotal -= energy;
        return energy;
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