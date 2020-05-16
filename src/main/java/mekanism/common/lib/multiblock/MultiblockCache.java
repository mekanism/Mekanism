package mekanism.common.lib.multiblock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.IGasTank;
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
import net.minecraftforge.common.util.INBTSerializable;

public class MultiblockCache<T extends MultiblockData> implements IMekanismInventory, IMekanismFluidHandler, IMekanismGasHandler,
      IMekanismStrictEnergyHandler, IMekanismHeatHandler {

    private List<IInventorySlot> inventorySlots = new ArrayList<>();
    private List<IExtendedFluidTank> fluidTanks = new ArrayList<>();
    private List<IGasTank> gasTanks = new ArrayList<>();
    private List<IEnergyContainer> energyContainers = new ArrayList<>();
    private List<IHeatCapacitor> heatCapacitors = new ArrayList<>();

    public Set<Coord4D> locations = new ObjectOpenHashSet<>();

    public void apply(T data) {
        for (CacheSubstance type : CacheSubstance.values()) {
            List<? extends INBTSerializable<CompoundNBT>> containers = type.getContainerList(data);
            if (containers != null) {
                List<? extends INBTSerializable<CompoundNBT>> cacheContainers = type.getContainerList(this);
                for (int i = 0; i < cacheContainers.size(); i++) {
                    if (i < containers.size()) {
                        //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                        containers.get(i).deserializeNBT(cacheContainers.get(i).serializeNBT());
                    }
                }
            }
        }
    }

    public void sync(T data) {
        for (CacheSubstance type : CacheSubstance.values()) {
            List<? extends INBTSerializable<CompoundNBT>> containersToCopy = type.getContainerList(data);
            if (containersToCopy != null) {
                List<? extends INBTSerializable<CompoundNBT>> cacheContainers = type.getContainerList(this);
                if (cacheContainers.isEmpty()) type.prefab(this, containersToCopy.size());
                for (int i = 0; i < containersToCopy.size(); i++) {
                    type.sync(cacheContainers.get(i), containersToCopy.get(i));
                }
            }
        }
    }

    public void load(CompoundNBT nbtTags) {
        for (CacheSubstance type : CacheSubstance.values()) {
            type.prefab(this, nbtTags.getInt(type.getTagKey() + "_stored"));
            DataHandlerUtils.readContainers(type.getContainerList(this), nbtTags.getList(type.getTagKey(), NBT.TAG_COMPOUND));
        }
    }

    public void save(CompoundNBT nbtTags) {
        for (CacheSubstance type : CacheSubstance.values()) {
            nbtTags.putInt(type.getTagKey() + "_stored", type.getContainerList(this).size());
            nbtTags.put(type.getTagKey(), DataHandlerUtils.writeContainers(type.getContainerList(this)));
        }
    }

    public void merge(MultiblockCache<T> mergeCache, List<ItemStack> rejectedItems) {
        // prefab enough containers for each substance type to support the merge cache
        for (CacheSubstance type : CacheSubstance.values())
            type.preHandleMerge(this, mergeCache);

        // Items
        rejectedItems.addAll(StackUtils.getMergeRejects(getInventorySlots(null), mergeCache.getInventorySlots(null)));
        StackUtils.merge(getInventorySlots(null), mergeCache.getInventorySlots(null));
        // Fluid
        List<IExtendedFluidTank> cacheFluidTanks = getFluidTanks(null);
        for (int i = 0; i < cacheFluidTanks.size(); i++) {
            StorageUtils.mergeTanks(cacheFluidTanks.get(i), mergeCache.getFluidTanks(null).get(i));
        }
        // Gas
        List<IGasTank> cacheGasTanks = getGasTanks(null);
        for (int i = 0; i < cacheGasTanks.size(); i++) {
            StorageUtils.mergeTanks(cacheGasTanks.get(i), mergeCache.getGasTanks(null).get(i));
        }
        // Energy
        List<IEnergyContainer> cacheContainers = getEnergyContainers(null);
        for (int i = 0; i < cacheContainers.size(); i++) {
            StorageUtils.mergeContainers(cacheContainers.get(i), mergeCache.getEnergyContainers(null).get(i));
        }
        // Heat
        List<IHeatCapacitor> cacheCapacitors = getHeatCapacitors(null);
        for (int i = 0; i < cacheCapacitors.size(); i++) {
            StorageUtils.mergeContainers(cacheCapacitors.get(i), mergeCache.getHeatCapacitors(null).get(i));
        }
    }

    @Override
    public void onContentsChanged() {}

    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) { return inventorySlots; }
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) { return fluidTanks; }
    @Override
    public List<IGasTank> getGasTanks(@Nullable Direction side) { return gasTanks; }
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) { return energyContainers; }
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(Direction side) { return heatCapacitors; }

    public enum CacheSubstance {
        ITEMS(NBTConstants.ITEMS, (cache) -> cache.inventorySlots.add(BasicInventorySlot.at(cache, 0, 0)),
            (holder) -> ((IMekanismInventory) holder).getInventorySlots(null)),

        FLUID(NBTConstants.FLUID_TANKS, (cache) -> cache.fluidTanks.add(BasicFluidTank.create(Integer.MAX_VALUE, cache)),
            (holder) -> ((IMekanismFluidHandler) holder).getFluidTanks(null)),

        GAS(NBTConstants.GAS_TANKS, (cache) -> cache.gasTanks.add(BasicGasTank.create(Long.MAX_VALUE, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrueBi,
              BasicGasTank.alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, cache)),
            (holder) -> ((IMekanismGasHandler) holder).getGasTanks(null)),

        ENERGY(NBTConstants.ENERGY_CONTAINERS, (cache) -> cache.energyContainers.add(BasicEnergyContainer.create(FloatingLong.MAX_VALUE, cache)),
            (holder) -> ((IMekanismStrictEnergyHandler) holder).getEnergyContainers(null)),

        HEAT(NBTConstants.HEAT_CAPACITORS, (cache) -> cache.heatCapacitors.add(BasicHeatCapacitor.create(HeatAPI.DEFAULT_HEAT_CAPACITY, cache)),
            (holder) -> ((IMekanismHeatHandler) holder).getHeatCapacitors(null));

        private String tagKey;
        private Consumer<MultiblockCache<?>> defaultPrefab;
        private Function<Object, List<? extends INBTSerializable<CompoundNBT>>> containerList;

        private CacheSubstance(String tagKey, Consumer<MultiblockCache<?>> defaultPrefab, Function<Object, List<? extends INBTSerializable<CompoundNBT>>> containerList) {
            this.tagKey = tagKey;
            this.defaultPrefab = defaultPrefab;
            this.containerList = containerList;
        }

        private void prefab(MultiblockCache<?> cache, int count) {
            for (int i = 0; i < count; i++) {
                defaultPrefab.accept(cache);
            }
        }

        public List<? extends INBTSerializable<CompoundNBT>> getContainerList(Object holder) {
            return containerList.apply(holder);
        }

        public void sync(INBTSerializable<CompoundNBT> cache, INBTSerializable<CompoundNBT> data) {
            switch (this) {
                case ITEMS: ((IInventorySlot) cache).setStack(((IInventorySlot) data).getStack()); break;
                case FLUID: ((IExtendedFluidTank) cache).setStack(((IExtendedFluidTank) data).getFluid()); break;
                case GAS: ((IGasTank) cache).setStack(((IGasTank) data).getStack()); break;
                case ENERGY: ((IEnergyContainer) cache).setEnergy(((IEnergyContainer) data).getEnergy()); break;
                case HEAT: {
                    ((IHeatCapacitor) cache).setHeat(((IHeatCapacitor) data).getHeat());
                    if (cache instanceof BasicHeatCapacitor) {
                        ((BasicHeatCapacitor) cache).setHeatCapacity(((IHeatCapacitor) data).getHeatCapacity(), false);
                    }
                    break;
                }
            }
        }

        public void preHandleMerge(MultiblockCache<?> cache, MultiblockCache<?> merge) {
            int diff = getContainerList(merge).size() - getContainerList(cache).size();
            if (diff > 0) {
                prefab(cache, diff);
            }
        }

        public String getTagKey() {
            return tagKey;
        }
    }
}