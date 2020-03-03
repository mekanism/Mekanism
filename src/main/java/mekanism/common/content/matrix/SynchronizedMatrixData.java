package mekanism.common.content.matrix;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.world.World;

//TODO: Do something better for purposes of double precision such as BigInt
public class SynchronizedMatrixData extends SynchronizedData<SynchronizedMatrixData> {

    private Set<Coord4D> providers = new ObjectOpenHashSet<>();
    private Set<Coord4D> cells = new ObjectOpenHashSet<>();
    private double queuedOutput;
    private double queuedInput;
    private double lastOutput;
    private double lastInput;

    private double cachedTotal;
    private double transferCap;
    private double storageCap;

    private int clientProviders;
    private int clientCells;

    @Nonnull
    private List<IInventorySlot> inventorySlots;

    public SynchronizedMatrixData() {
        inventorySlots = createBaseInventorySlots();
    }

    private List<IInventorySlot> createBaseInventorySlots() {
        List<IInventorySlot> inventorySlots = new ArrayList<>();
        EnergyInventorySlot energyInputSlot;
        EnergyInventorySlot energyOutputSlot;
        inventorySlots.add(energyInputSlot = EnergyInventorySlot.charge(this, 146, 20));
        inventorySlots.add(energyOutputSlot = EnergyInventorySlot.discharge(this, 146, 51));
        energyInputSlot.setSlotOverlay(SlotOverlay.PLUS);
        energyOutputSlot.setSlotOverlay(SlotOverlay.MINUS);
        return inventorySlots;
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return inventorySlots;
    }

    public void setInventoryData(@Nonnull List<IInventorySlot> toCopy) {
        for (int i = 0; i < toCopy.size(); i++) {
            if (i < inventorySlots.size()) {
                //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                inventorySlots.get(i).deserializeNBT(toCopy.get(i).serializeNBT());
            }
        }
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
        return cachedTotal + queuedInput - queuedOutput;
    }

    public void tick(World world) {
        //See comment in getEnergyPostQueue for explanation of how lastChange is calculated.
        double lastChange = queuedInput - queuedOutput;
        if (lastChange < 0) {
            //We are removing energy
            removeEnergy(world, -lastChange);
        } else if (lastChange > 0) {
            //we are adding energy
            addEnergy(world, lastChange);
        }
        cachedTotal += lastChange;

        lastInput = queuedInput;
        queuedInput = 0;
        lastOutput = queuedOutput;
        queuedOutput = 0;
    }

    public double queueEnergyAddition(double energy, boolean simulate) {
        if (energy < 0) {
            //Ensure that the correct queue type gets called
            return queueEnergyRemoval(-energy, simulate);
        }
        double remainingInput = getRemainingInput();
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
            //Increase how much we are inputting
            queuedInput += energy;
        }
        return energy;
    }

    public double queueEnergyRemoval(double energy, boolean simulate) {
        if (energy < 0) {
            //Ensure that the correct queue type gets called
            return queueEnergyAddition(-energy, simulate);
        }
        double remainingOutput = getRemainingOutput();
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
            //Increase how much we are outputting by the amount we accepted
            queuedOutput += energy;
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
            TileEntityInductionCell cell = MekanismUtils.getTileEntity(TileEntityInductionCell.class, world, coord.getPos());
            if (cell != null) {
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
            TileEntityInductionCell cell = MekanismUtils.getTileEntity(TileEntityInductionCell.class, world, coord.getPos());
            if (cell != null) {
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

    public void readStructureData(PacketBuffer dataStream) {
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
        return transferCap - queuedInput;
    }

    public double getRemainingOutput() {
        return transferCap - queuedOutput;
    }

    public int getCellCount() {
        return cells.isEmpty() ? clientCells : cells.size();
    }

    public int getProviderCount() {
        return providers.isEmpty() ? clientProviders : providers.size();
    }
}