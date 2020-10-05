package mekanism.common.lib.multiblock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
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
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;

public class MultiblockCache<T extends MultiblockData> implements IMekanismInventory, IMekanismFluidHandler, IMekanismStrictEnergyHandler, IMekanismHeatHandler,
      IGasTracker, IInfusionTracker, IPigmentTracker, ISlurryTracker {

    private final List<IInventorySlot> inventorySlots = new ArrayList<>();
    private final List<IExtendedFluidTank> fluidTanks = new ArrayList<>();
    private final List<IGasTank> gasTanks = new ArrayList<>();
    private final List<IInfusionTank> infusionTanks = new ArrayList<>();
    private final List<IPigmentTank> pigmentTanks = new ArrayList<>();
    private final List<ISlurryTank> slurryTanks = new ArrayList<>();
    private final List<IEnergyContainer> energyContainers = new ArrayList<>();
    private final List<IHeatCapacitor> heatCapacitors = new ArrayList<>();

    public void apply(T data) {
        for (CacheSubstance type : EnumUtils.CACHE_SUBSTANCES) {
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
        for (CacheSubstance type : EnumUtils.CACHE_SUBSTANCES) {
            List<? extends INBTSerializable<CompoundNBT>> containersToCopy = type.getContainerList(data);
            if (containersToCopy != null) {
                List<? extends INBTSerializable<CompoundNBT>> cacheContainers = type.getContainerList(this);
                if (cacheContainers.isEmpty()) {
                    type.prefab(this, containersToCopy.size());
                }
                for (int i = 0; i < containersToCopy.size(); i++) {
                    type.sync(cacheContainers.get(i), containersToCopy.get(i));
                }
            }
        }
    }

    public void load(CompoundNBT nbtTags) {
        for (CacheSubstance type : EnumUtils.CACHE_SUBSTANCES) {
            type.prefab(this, nbtTags.getInt(type.getTagKey() + "_stored"));
            DataHandlerUtils.readContainers(type.getContainerList(this), nbtTags.getList(type.getTagKey(), NBT.TAG_COMPOUND));
        }
    }

    public void save(CompoundNBT nbtTags) {
        for (CacheSubstance type : EnumUtils.CACHE_SUBSTANCES) {
            nbtTags.putInt(type.getTagKey() + "_stored", type.getContainerList(this).size());
            nbtTags.put(type.getTagKey(), DataHandlerUtils.writeContainers(type.getContainerList(this)));
        }
    }

    public void merge(MultiblockCache<T> mergeCache, List<ItemStack> rejectedItems) {
        // prefab enough containers for each substance type to support the merge cache
        for (CacheSubstance type : EnumUtils.CACHE_SUBSTANCES) {
            type.preHandleMerge(this, mergeCache);
        }

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
        // Infusion
        List<IInfusionTank> cacheInfusionTanks = getInfusionTanks(null);
        for (int i = 0; i < cacheInfusionTanks.size(); i++) {
            StorageUtils.mergeTanks(cacheInfusionTanks.get(i), mergeCache.getInfusionTanks(null).get(i));
        }
        // Pigment
        List<IPigmentTank> cachePigmentTanks = getPigmentTanks(null);
        for (int i = 0; i < cachePigmentTanks.size(); i++) {
            StorageUtils.mergeTanks(cachePigmentTanks.get(i), mergeCache.getPigmentTanks(null).get(i));
        }
        // Slurry
        List<ISlurryTank> cacheSlurryTanks = getSlurryTanks(null);
        for (int i = 0; i < cacheSlurryTanks.size(); i++) {
            StorageUtils.mergeTanks(cacheSlurryTanks.get(i), mergeCache.getSlurryTanks(null).get(i));
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
    public void onContentsChanged() {
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
    public List<IGasTank> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }

    @Nonnull
    @Override
    public List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
        return infusionTanks;
    }

    @Nonnull
    @Override
    public List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
        return pigmentTanks;
    }

    @Nonnull
    @Override
    public List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
        return slurryTanks;
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

    public enum CacheSubstance {
        ITEMS(NBTConstants.ITEMS, cache -> cache.inventorySlots.add(BasicInventorySlot.at(cache, 0, 0)),
              holder -> ((IMekanismInventory) holder).getInventorySlots(null)),
        FLUID(NBTConstants.FLUID_TANKS, cache -> cache.fluidTanks.add(BasicFluidTank.create(Integer.MAX_VALUE, cache)),
              holder -> ((IMekanismFluidHandler) holder).getFluidTanks(null)),
        GAS(NBTConstants.GAS_TANKS, cache -> cache.gasTanks.add(ChemicalTankBuilder.GAS.createAllValid(Long.MAX_VALUE, cache)),
              holder -> ((IGasTracker) holder).getGasTanks(null)),
        INFUSION(NBTConstants.INFUSION_TANKS, cache -> cache.infusionTanks.add(ChemicalTankBuilder.INFUSION.createAllValid(Long.MAX_VALUE, cache)),
              holder -> ((IInfusionTracker) holder).getInfusionTanks(null)),
        PIGMENT(NBTConstants.PIGMENT_TANKS, cache -> cache.pigmentTanks.add(ChemicalTankBuilder.PIGMENT.createAllValid(Long.MAX_VALUE, cache)),
              holder -> ((IPigmentTracker) holder).getPigmentTanks(null)),
        SLURRY(NBTConstants.SLURRY_TANKS, cache -> cache.slurryTanks.add(ChemicalTankBuilder.SLURRY.createAllValid(Long.MAX_VALUE, cache)),
              holder -> ((ISlurryTracker) holder).getSlurryTanks(null)),
        ENERGY(NBTConstants.ENERGY_CONTAINERS, cache -> cache.energyContainers.add(BasicEnergyContainer.create(FloatingLong.MAX_VALUE, cache)),
              holder -> ((IMekanismStrictEnergyHandler) holder).getEnergyContainers(null)),
        HEAT(NBTConstants.HEAT_CAPACITORS, cache -> cache.heatCapacitors.add(BasicHeatCapacitor.create(HeatAPI.DEFAULT_HEAT_CAPACITY, cache)),
              holder -> ((IMekanismHeatHandler) holder).getHeatCapacitors(null));

        private final String tagKey;
        private final Consumer<MultiblockCache<?>> defaultPrefab;
        private final Function<Object, List<? extends INBTSerializable<CompoundNBT>>> containerList;

        CacheSubstance(String tagKey, Consumer<MultiblockCache<?>> defaultPrefab, Function<Object, List<? extends INBTSerializable<CompoundNBT>>> containerList) {
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
                case ITEMS:
                    ((IInventorySlot) cache).setStack(((IInventorySlot) data).getStack());
                    break;
                case FLUID:
                    ((IExtendedFluidTank) cache).setStack(((IExtendedFluidTank) data).getFluid());
                    break;
                case GAS:
                    ((IGasTank) cache).setStack(((IGasTank) data).getStack());
                    break;
                case INFUSION:
                    ((IInfusionTank) cache).setStack(((IInfusionTank) data).getStack());
                    break;
                case PIGMENT:
                    ((IPigmentTank) cache).setStack(((IPigmentTank) data).getStack());
                    break;
                case SLURRY:
                    ((ISlurryTank) cache).setStack(((ISlurryTank) data).getStack());
                    break;
                case ENERGY:
                    ((IEnergyContainer) cache).setEnergy(((IEnergyContainer) data).getEnergy());
                    break;
                case HEAT:
                    ((IHeatCapacitor) cache).setHeat(((IHeatCapacitor) data).getHeat());
                    if (cache instanceof BasicHeatCapacitor) {
                        ((BasicHeatCapacitor) cache).setHeatCapacity(((IHeatCapacitor) data).getHeatCapacity(), false);
                    }
                    break;
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