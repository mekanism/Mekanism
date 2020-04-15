package mekanism.common.multiblock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.util.StackUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public class MultiblockCache<T extends SynchronizedData<T>> implements IMekanismInventory, IMekanismFluidHandler, IMekanismGasHandler,
      IMekanismStrictEnergyHandler, IMekanismHeatHandler {

    protected final List<IInventorySlot> inventorySlots = Arrays.asList(BasicInventorySlot.at(this, 0, 0), BasicInventorySlot.at(this, 0, 0));
    private final List<IExtendedFluidTank> fluidTanks = Collections.singletonList(BasicFluidTank.create(Integer.MAX_VALUE, this));
    private final List<IChemicalTank<Gas, GasStack>> gasTanks = Collections.singletonList(BasicGasTank.create(Long.MAX_VALUE, this));
    private final List<IEnergyContainer> energyContainers = Collections.singletonList(BasicEnergyContainer.create(FloatingLong.MAX_VALUE, this));
    public final List<IHeatCapacitor> heatCapacitors = Collections.singletonList(BasicHeatCapacitor.create(HeatAPI.DEFAULT_HEAT_CAPACITY, this));

    public Set<Coord4D> locations = new ObjectOpenHashSet<>();

    public void apply(T data) {
        applyInventoryData(data);
        applyFluidData(data);
        applyGasData(data);
        applyEnergyData(data);
        applyHeatData(data);
    }

    public void sync(T data) {
        syncInventoryData(data);
        syncFluidData(data);
        syncGasData(data);
        syncEnergyData(data);
        syncHeatData(data);
    }

    public void load(CompoundNBT nbtTags) {
        DataHandlerUtils.readSlots(getInventorySlots(null), nbtTags.getList(NBTConstants.ITEMS, NBT.TAG_COMPOUND));
        DataHandlerUtils.readTanks(getFluidTanks(null), nbtTags.getList(NBTConstants.FLUID_TANKS, NBT.TAG_COMPOUND));
        DataHandlerUtils.readTanks(getGasTanks(null), nbtTags.getList(NBTConstants.GAS_TANKS, NBT.TAG_COMPOUND));
        DataHandlerUtils.readContainers(getEnergyContainers(null), nbtTags.getList(NBTConstants.ENERGY_CONTAINERS, NBT.TAG_COMPOUND));
        DataHandlerUtils.readContainers(getHeatCapacitors(null), nbtTags.getList(NBTConstants.HEAT_CAPACITORS, NBT.TAG_COMPOUND));
    }

    public void save(CompoundNBT nbtTags) {
        nbtTags.put(NBTConstants.ITEMS, DataHandlerUtils.writeSlots(getInventorySlots(null)));
        nbtTags.put(NBTConstants.FLUID_TANKS, DataHandlerUtils.writeTanks(getFluidTanks(null)));
        nbtTags.put(NBTConstants.GAS_TANKS, DataHandlerUtils.writeTanks(getGasTanks(null)));
        nbtTags.put(NBTConstants.ENERGY_CONTAINERS, DataHandlerUtils.writeContainers(getEnergyContainers(null)));
        nbtTags.put(NBTConstants.HEAT_CAPACITORS, DataHandlerUtils.writeContainers(getHeatCapacitors(null)));
    }

    public void merge(MultiblockCache<T> mergeCache, List<ItemStack> rejectedItems) {
        // Items
        rejectedItems.addAll(StackUtils.getMergeRejects(getInventorySlots(null), ((IMekanismInventory) mergeCache).getInventorySlots(null)));
        StackUtils.merge(getInventorySlots(null), ((IMekanismInventory) mergeCache).getInventorySlots(null));
        // Fluid
        List<IExtendedFluidTank> cacheFluidTanks = getFluidTanks(null);
        for (int i = 0; i < cacheFluidTanks.size(); i++) {
            StorageUtils.mergeTanks(cacheFluidTanks.get(i), ((IMekanismFluidHandler) mergeCache).getFluidTanks(null).get(i));
        }
        // Gas
        List<? extends IChemicalTank<Gas, GasStack>> cacheGasTanks = getGasTanks(null);
        for (int i = 0; i < cacheGasTanks.size(); i++) {
            StorageUtils.mergeTanks(cacheGasTanks.get(i), ((IMekanismGasHandler) mergeCache).getGasTanks(null).get(i));
        }
        // Energy
        List<IEnergyContainer> cacheContainers = getEnergyContainers(null);
        for (int i = 0; i < cacheContainers.size(); i++) {
            StorageUtils.mergeContainers(cacheContainers.get(i), ((IMekanismStrictEnergyHandler) mergeCache).getEnergyContainers(null).get(i));
        }
        // Heat
        List<IHeatCapacitor> cacheCapacitors = getHeatCapacitors(null);
        for (int i = 0; i < cacheCapacitors.size(); i++) {
            StorageUtils.mergeContainers(cacheCapacitors.get(i), ((IMekanismHeatHandler) mergeCache).getHeatCapacitors(null).get(i));
        }
    }

    public void syncInventoryData(SynchronizedData<T> data) {
        if (data instanceof IMekanismInventory) {
            List<IInventorySlot> slotsToCopy = data.getInventorySlots(null);
            List<IInventorySlot> cacheSlots = ((IMekanismInventory) this).getInventorySlots(null);
            for (int i = 0; i < slotsToCopy.size(); i++) {
                if (i < cacheSlots.size()) {
                    //Just directly set it as we don't have any restrictions on our slots here
                    cacheSlots.get(i).setStack(slotsToCopy.get(i).getStack());
                }
            }
        }
    }

    public void syncFluidData(SynchronizedData<T> data) {
        if (data instanceof IMekanismFluidHandler) {
            List<IExtendedFluidTank> fluidTanksToCopy = ((IMekanismFluidHandler) data).getFluidTanks(null);
            List<IExtendedFluidTank> cacheTanks = ((IMekanismFluidHandler) this).getFluidTanks(null);
            for (int i = 0; i < fluidTanksToCopy.size(); i++) {
                if (i < cacheTanks.size()) {
                    //Just directly set it as we don't have any restrictions on our tanks here
                    cacheTanks.get(i).setStack(fluidTanksToCopy.get(i).getFluid());
                }
            }
        }
    }

    public void syncGasData(SynchronizedData<T> data) {
        if (data instanceof IMekanismGasHandler) {
            List<? extends IChemicalTank<Gas, GasStack>> gasTanksToCopy = ((IMekanismGasHandler) data).getGasTanks(null);
            List<? extends IChemicalTank<Gas, GasStack>> cacheTanks = ((IMekanismGasHandler) this).getGasTanks(null);
            for (int i = 0; i < gasTanksToCopy.size(); i++) {
                if (i < cacheTanks.size()) {
                    //Just directly set it as we don't have any restrictions on our tanks here
                    cacheTanks.get(i).setStack(gasTanksToCopy.get(i).getStack());
                }
            }
        }
    }

    public void syncEnergyData(SynchronizedData<T> data) {
        if (data instanceof IMekanismStrictEnergyHandler) {
            List<IEnergyContainer> containersToCopy = ((IMekanismStrictEnergyHandler) data).getEnergyContainers(null);
            List<IEnergyContainer> cacheContainers = ((IMekanismStrictEnergyHandler) this).getEnergyContainers(null);
            for (int i = 0; i < containersToCopy.size(); i++) {
                if (i < cacheContainers.size()) {
                    //Just directly set it as we don't have any restrictions on our containers here
                    cacheContainers.get(i).setEnergy(containersToCopy.get(i).getEnergy());
                }
            }
        }
    }

    public void syncHeatData(SynchronizedData<T> data) {
        if (data instanceof IMekanismHeatHandler) {
            List<IHeatCapacitor> heatCapacitorsToCopy = ((IMekanismHeatHandler) data).getHeatCapacitors(null);
            List<IHeatCapacitor> cacheCapacitors = ((IMekanismHeatHandler) this).getHeatCapacitors(null);
            for (int i = 0; i < heatCapacitorsToCopy.size(); i++) {
                if (i < cacheCapacitors.size()) {
                    //Just directly set it as we don't have any restrictions on our tanks here
                    cacheCapacitors.get(i).setHeat(heatCapacitorsToCopy.get(i).getHeat());
                    if (cacheCapacitors.get(i) instanceof BasicHeatCapacitor) {
                        ((BasicHeatCapacitor) cacheCapacitors.get(i)).setHeatCapacity(heatCapacitorsToCopy.get(i).getHeatCapacity(), false);
                    }
                }
            }
        }
    }

    public void applyInventoryData(SynchronizedData<T> data) {
        if (data instanceof IMekanismInventory) {
            List<IInventorySlot> inventorySlots = ((IMekanismInventory) data).getInventorySlots(null);
            List<IInventorySlot> cacheSlots = ((IMekanismInventory) this).getInventorySlots(null);
            for (int i = 0; i < cacheSlots.size(); i++) {
                if (i < inventorySlots.size()) {
                    //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                    inventorySlots.get(i).deserializeNBT(cacheSlots.get(i).serializeNBT());
                }
            }
        }
    }

    public void applyFluidData(SynchronizedData<T> data) {
        if (data instanceof IMekanismFluidHandler) {
            List<IExtendedFluidTank> fluidTanks = ((IMekanismFluidHandler) data).getFluidTanks(null);
            List<IExtendedFluidTank> cacheTanks = ((IMekanismFluidHandler) this).getFluidTanks(null);
            for (int i = 0; i < cacheTanks.size(); i++) {
                if (i < fluidTanks.size()) {
                    //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                    fluidTanks.get(i).deserializeNBT(cacheTanks.get(i).serializeNBT());
                }
            }
        }
    }

    public void applyGasData(SynchronizedData<T> data) {
        if (data instanceof IMekanismGasHandler) {
            List<? extends IChemicalTank<Gas, GasStack>> gasTanks = ((IMekanismGasHandler) data).getGasTanks(null);
            List<? extends IChemicalTank<Gas, GasStack>> cacheTanks = ((IMekanismGasHandler) this).getGasTanks(null);
            for (int i = 0; i < cacheTanks.size(); i++) {
                if (i < gasTanks.size()) {
                    //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                    gasTanks.get(i).deserializeNBT(cacheTanks.get(i).serializeNBT());
                }
            }
        }
    }

    public void applyEnergyData(SynchronizedData<T> data) {
        if (data instanceof IMekanismStrictEnergyHandler) {
            List<IEnergyContainer> energyContainers = ((IMekanismStrictEnergyHandler) data).getEnergyContainers(null);
            List<IEnergyContainer> cacheContainers = ((IMekanismStrictEnergyHandler) this).getEnergyContainers(null);
            for (int i = 0; i < cacheContainers.size(); i++) {
                if (i < energyContainers.size()) {
                    //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                    energyContainers.get(i).deserializeNBT(cacheContainers.get(i).serializeNBT());
                }
            }
        }
    }

    public void applyHeatData(SynchronizedData<T> data) {
        if (data instanceof IMekanismHeatHandler) {
            List<IHeatCapacitor> heatCapacitors = ((IMekanismHeatHandler) data).getHeatCapacitors(null);
            List<IHeatCapacitor> cacheCapacitors = ((IMekanismHeatHandler) this).getHeatCapacitors(null);
            for (int i = 0; i < cacheCapacitors.size(); i++) {
                if (i < heatCapacitors.size()) {
                    //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                    heatCapacitors.get(i).deserializeNBT(cacheCapacitors.get(i).serializeNBT());
                }
            }
        }
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return inventorySlots;
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyContainers;
    }

    @Nonnull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(Direction side) {
        return heatCapacitors;
    }

    @Override
    public void onContentsChanged() {}
}