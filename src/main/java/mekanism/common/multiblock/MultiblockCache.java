package mekanism.common.multiblock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
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

// need to clean this up eventually
public class MultiblockCache<T extends SynchronizedData<T>> implements IMekanismInventory, IMekanismFluidHandler, IMekanismGasHandler,
      IMekanismStrictEnergyHandler, IMekanismHeatHandler {

    private List<IInventorySlot> inventorySlots = new ArrayList<>();
    private List<IExtendedFluidTank> fluidTanks = new ArrayList<>();
    private List<IChemicalTank<Gas, GasStack>> gasTanks = new ArrayList<>();
    private List<IEnergyContainer> energyContainers = new ArrayList<>();
    private List<IHeatCapacitor> heatCapacitors = new ArrayList<>();

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
        int stored = nbtTags.getInt(NBTConstants.ITEMS + "_stored");
        prefabItems(stored);
        DataHandlerUtils.readSlots(getInventorySlots(null), nbtTags.getList(NBTConstants.ITEMS, NBT.TAG_COMPOUND));

        stored = nbtTags.getInt(NBTConstants.FLUID_TANKS + "_stored");
        prefabFluid(stored);
        DataHandlerUtils.readTanks(getFluidTanks(null), nbtTags.getList(NBTConstants.FLUID_TANKS, NBT.TAG_COMPOUND));

        stored = nbtTags.getInt(NBTConstants.GAS_TANKS + "_stored");
        prefabGas(stored);
        DataHandlerUtils.readTanks(getGasTanks(null), nbtTags.getList(NBTConstants.GAS_TANKS, NBT.TAG_COMPOUND));

        stored = nbtTags.getInt(NBTConstants.ENERGY_CONTAINERS + "_stored");
        prefabEnergy(stored);
        DataHandlerUtils.readContainers(getEnergyContainers(null), nbtTags.getList(NBTConstants.ENERGY_CONTAINERS, NBT.TAG_COMPOUND));

        stored = nbtTags.getInt(NBTConstants.HEAT_CAPACITORS + "_stored");
        prefabHeat(stored);
        DataHandlerUtils.readContainers(getHeatCapacitors(null), nbtTags.getList(NBTConstants.HEAT_CAPACITORS, NBT.TAG_COMPOUND));
    }

    public void save(CompoundNBT nbtTags) {
        nbtTags.putInt(NBTConstants.ITEMS + "_stored", inventorySlots.size());
        nbtTags.put(NBTConstants.ITEMS, DataHandlerUtils.writeSlots(getInventorySlots(null)));

        nbtTags.putInt(NBTConstants.FLUID_TANKS + "_stored", fluidTanks.size());
        nbtTags.put(NBTConstants.FLUID_TANKS, DataHandlerUtils.writeTanks(getFluidTanks(null)));

        nbtTags.putInt(NBTConstants.GAS_TANKS + "_stored", gasTanks.size());
        nbtTags.put(NBTConstants.GAS_TANKS, DataHandlerUtils.writeTanks(getGasTanks(null)));

        nbtTags.putInt(NBTConstants.ENERGY_CONTAINERS + "_stored", energyContainers.size());
        nbtTags.put(NBTConstants.ENERGY_CONTAINERS, DataHandlerUtils.writeContainers(getEnergyContainers(null)));

        nbtTags.putInt(NBTConstants.HEAT_CAPACITORS + "_stored", heatCapacitors.size());
        nbtTags.put(NBTConstants.HEAT_CAPACITORS, DataHandlerUtils.writeContainers(getHeatCapacitors(null)));
    }

    public void merge(MultiblockCache<T> mergeCache, List<ItemStack> rejectedItems) {
        // Items
        if (inventorySlots.isEmpty()) prefabItems(mergeCache.inventorySlots.size());
        rejectedItems.addAll(StackUtils.getMergeRejects(getInventorySlots(null), mergeCache.getInventorySlots(null)));
        StackUtils.merge(getInventorySlots(null), mergeCache.getInventorySlots(null));
        // Fluid
        if (fluidTanks.isEmpty()) prefabFluid(mergeCache.fluidTanks.size());
        List<IExtendedFluidTank> cacheFluidTanks = getFluidTanks(null);
        for (int i = 0; i < cacheFluidTanks.size(); i++) {
            StorageUtils.mergeTanks(cacheFluidTanks.get(i), mergeCache.getFluidTanks(null).get(i));
        }
        // Gas
        if (gasTanks.isEmpty()) prefabGas(mergeCache.gasTanks.size());
        List<? extends IChemicalTank<Gas, GasStack>> cacheGasTanks = getGasTanks(null);
        for (int i = 0; i < cacheGasTanks.size(); i++) {
            StorageUtils.mergeTanks(cacheGasTanks.get(i), mergeCache.getGasTanks(null).get(i));
        }
        // Energy
        if (energyContainers.isEmpty()) prefabEnergy(mergeCache.energyContainers.size());
        List<IEnergyContainer> cacheContainers = getEnergyContainers(null);
        for (int i = 0; i < cacheContainers.size(); i++) {
            StorageUtils.mergeContainers(cacheContainers.get(i), mergeCache.getEnergyContainers(null).get(i));
        }
        // Heat
        if (heatCapacitors.isEmpty()) prefabHeat(mergeCache.heatCapacitors.size());
        List<IHeatCapacitor> cacheCapacitors = getHeatCapacitors(null);
        for (int i = 0; i < cacheCapacitors.size(); i++) {
            StorageUtils.mergeContainers(cacheCapacitors.get(i), mergeCache.getHeatCapacitors(null).get(i));
        }
    }

    public void syncInventoryData(SynchronizedData<T> data) {
        if (data instanceof IMekanismInventory) {
            List<IInventorySlot> slotsToCopy = data.getInventorySlots(null);
            if (inventorySlots.isEmpty()) prefabItems(slotsToCopy.size());
            List<IInventorySlot> cacheSlots = getInventorySlots(null);
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
            if (fluidTanks.isEmpty()) prefabFluid(fluidTanksToCopy.size());
            List<IExtendedFluidTank> cacheTanks = getFluidTanks(null);
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
            if (gasTanks.isEmpty()) prefabGas(gasTanksToCopy.size());
            List<? extends IChemicalTank<Gas, GasStack>> cacheTanks = getGasTanks(null);
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
            if (energyContainers.isEmpty()) prefabEnergy(containersToCopy.size());
            List<IEnergyContainer> cacheContainers = getEnergyContainers(null);
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
            if (heatCapacitors.isEmpty()) prefabHeat(heatCapacitorsToCopy.size());
            List<IHeatCapacitor> cacheCapacitors = getHeatCapacitors(null);
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
            List<IInventorySlot> cacheSlots = getInventorySlots(null);
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
            List<IExtendedFluidTank> cacheTanks = getFluidTanks(null);
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
            List<? extends IChemicalTank<Gas, GasStack>> cacheTanks = getGasTanks(null);
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
            List<IEnergyContainer> cacheContainers = getEnergyContainers(null);
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
            List<IHeatCapacitor> cacheCapacitors = getHeatCapacitors(null);
            for (int i = 0; i < cacheCapacitors.size(); i++) {
                if (i < heatCapacitors.size()) {
                    //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                    heatCapacitors.get(i).deserializeNBT(cacheCapacitors.get(i).serializeNBT());
                }
            }
        }
    }

    public void prefabItems(int count) {
        inventorySlots = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            inventorySlots.add(BasicInventorySlot.at(this, 0, 0));
        }
    }

    public void prefabFluid(int count) {
        fluidTanks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            fluidTanks.add(BasicFluidTank.create(Integer.MAX_VALUE, this));
        }
    }

    public void prefabGas(int count) {
        gasTanks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            gasTanks.add(BasicGasTank.create(Long.MAX_VALUE, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, this));
        }
    }

    public void prefabEnergy(int count) {
        energyContainers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            energyContainers.add(BasicEnergyContainer.create(FloatingLong.MAX_VALUE, this));
        }
    }

    public void prefabHeat(int count) {
        heatCapacitors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            heatCapacitors.add(BasicHeatCapacitor.create(HeatAPI.DEFAULT_HEAT_CAPACITY, this));
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