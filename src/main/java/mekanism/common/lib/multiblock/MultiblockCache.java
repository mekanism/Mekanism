package mekanism.common.lib.multiblock;

import java.util.ArrayList;
import java.util.List;
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
import mekanism.common.util.StackUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
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
        for (CacheSubstance<?, INBTSerializable<CompoundTag>> type : CacheSubstance.VALUES) {
            List<? extends INBTSerializable<CompoundTag>> containers = type.getContainerList(data);
            if (containers != null) {
                List<? extends INBTSerializable<CompoundTag>> cacheContainers = type.getContainerList(this);
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
        for (CacheSubstance<?, INBTSerializable<CompoundTag>> type : CacheSubstance.VALUES) {
            List<? extends INBTSerializable<CompoundTag>> containersToCopy = type.getContainerList(data);
            if (containersToCopy != null) {
                List<? extends INBTSerializable<CompoundTag>> cacheContainers = type.getContainerList(this);
                if (cacheContainers.isEmpty()) {
                    type.prefab(this, containersToCopy.size());
                }
                for (int i = 0; i < containersToCopy.size(); i++) {
                    type.sync(cacheContainers.get(i), containersToCopy.get(i));
                }
            }
        }
    }

    public void load(CompoundTag nbtTags) {
        for (CacheSubstance<?, INBTSerializable<CompoundTag>> type : CacheSubstance.VALUES) {
            type.prefab(this, nbtTags.getInt(type.getTagKey() + "_stored"));
            DataHandlerUtils.readContainers(type.getContainerList(this), nbtTags.getList(type.getTagKey(), Tag.TAG_COMPOUND));
        }
    }

    public void save(CompoundTag nbtTags) {
        for (CacheSubstance<?, INBTSerializable<CompoundTag>> type : CacheSubstance.VALUES) {
            nbtTags.putInt(type.getTagKey() + "_stored", type.getContainerList(this).size());
            nbtTags.put(type.getTagKey(), DataHandlerUtils.writeContainers(type.getContainerList(this)));
        }
    }

    public void merge(MultiblockCache<T> mergeCache, List<ItemStack> rejectedItems) {
        // prefab enough containers for each substance type to support the merge cache
        for (CacheSubstance<?, INBTSerializable<CompoundTag>> type : CacheSubstance.VALUES) {
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

    public abstract static class CacheSubstance<HANDLER, ELEMENT> {

        public static final CacheSubstance<IMekanismInventory, IInventorySlot> ITEMS = new CacheSubstance<>(NBTConstants.ITEMS) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.inventorySlots.add(BasicInventorySlot.at(cache, 0, 0));
            }

            @Override
            protected List<IInventorySlot> containerList(IMekanismInventory iMekanismInventory) {
                return iMekanismInventory.getInventorySlots(null);
            }

            @Override
            public void sync(IInventorySlot cache, IInventorySlot data) {
                cache.setStack(data.getStack());
            }
        };

        public static final CacheSubstance<IMekanismFluidHandler, IExtendedFluidTank> FLUID = new CacheSubstance<>(NBTConstants.FLUID_TANKS) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.fluidTanks.add(BasicFluidTank.create(Integer.MAX_VALUE, cache));
            }

            @Override
            protected List<IExtendedFluidTank> containerList(IMekanismFluidHandler iMekanismFluidHandler) {
                return iMekanismFluidHandler.getFluidTanks(null);
            }

            @Override
            public void sync(IExtendedFluidTank cache, IExtendedFluidTank data) {
                cache.setStack(data.getFluid());
            }
        };

        public static final CacheSubstance<IGasTracker, IGasTank> GAS = new CacheSubstance<>(NBTConstants.GAS_TANKS) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.gasTanks.add(ChemicalTankBuilder.GAS.createAllValid(Long.MAX_VALUE, cache));
            }

            @Override
            protected List<IGasTank> containerList(IGasTracker holder) {
                return holder.getGasTanks(null);
            }

            @Override
            public void sync(IGasTank cache, IGasTank data) {
                cache.setStack(data.getStack());
            }
        };

        public static final CacheSubstance<IInfusionTracker, IInfusionTank> INFUSION = new CacheSubstance<>(NBTConstants.INFUSION_TANKS) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.infusionTanks.add(ChemicalTankBuilder.INFUSION.createAllValid(Long.MAX_VALUE, cache));
            }

            @Override
            protected List<IInfusionTank> containerList(IInfusionTracker holder) {
                return holder.getInfusionTanks(null);
            }

            @Override
            public void sync(IInfusionTank cache, IInfusionTank data) {
                cache.setStack(data.getStack());
            }
        };

        public static final CacheSubstance<IPigmentTracker, IPigmentTank> PIGMENT = new CacheSubstance<>(NBTConstants.PIGMENT_TANKS) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.pigmentTanks.add(ChemicalTankBuilder.PIGMENT.createAllValid(Long.MAX_VALUE, cache));
            }

            @Override
            protected List<IPigmentTank> containerList(IPigmentTracker holder) {
                return holder.getPigmentTanks(null);
            }

            @Override
            public void sync(IPigmentTank cache, IPigmentTank data) {
                cache.setStack(data.getStack());
            }
        };

        public static final CacheSubstance<ISlurryTracker, ISlurryTank> SLURRY = new CacheSubstance<>(NBTConstants.SLURRY_TANKS) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.slurryTanks.add(ChemicalTankBuilder.SLURRY.createAllValid(Long.MAX_VALUE, cache));
            }

            @Override
            protected List<ISlurryTank> containerList(ISlurryTracker holder) {
                return holder.getSlurryTanks(null);
            }

            @Override
            public void sync(ISlurryTank cache, ISlurryTank data) {
                cache.setStack(data.getStack());
            }
        };

        public static final CacheSubstance<IMekanismStrictEnergyHandler, IEnergyContainer> ENERGY = new CacheSubstance<>(NBTConstants.ENERGY_CONTAINERS) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.energyContainers.add(BasicEnergyContainer.create(FloatingLong.MAX_VALUE, cache));
            }

            @Override
            protected List<IEnergyContainer> containerList(IMekanismStrictEnergyHandler holder) {
                return holder.getEnergyContainers(null);
            }

            @Override
            public void sync(IEnergyContainer cache, IEnergyContainer data) {
                cache.setEnergy(data.getEnergy());
            }
        };

        public static final CacheSubstance<IMekanismHeatHandler, IHeatCapacitor> HEAT = new CacheSubstance<>(NBTConstants.HEAT_CAPACITORS) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.heatCapacitors.add(BasicHeatCapacitor.create(HeatAPI.DEFAULT_HEAT_CAPACITY, null, cache));
            }

            @Override
            protected List<IHeatCapacitor> containerList(IMekanismHeatHandler holder) {
                return holder.getHeatCapacitors(null);
            }

            @Override
            public void sync(IHeatCapacitor cache, IHeatCapacitor data) {
                cache.setHeat(data.getHeat());
                if (cache instanceof BasicHeatCapacitor heatCapacitor) {
                    heatCapacitor.setHeatCapacity(data.getHeatCapacity(), false);
                }
            }
        };

        @SuppressWarnings({"unchecked"})
        public static final CacheSubstance<?, INBTSerializable<CompoundTag>>[] VALUES = new CacheSubstance[]{
              ITEMS,
              FLUID,
              GAS,
              INFUSION,
              PIGMENT,
              SLURRY,
              ENERGY,
              HEAT
        };

        private final String tagKey;

        public CacheSubstance(String tagKey) {
            this.tagKey = tagKey;
        }

        protected abstract void defaultPrefab(MultiblockCache<?> cache);

        protected abstract List<ELEMENT> containerList(HANDLER handler);

        private void prefab(MultiblockCache<?> cache, int count) {
            for (int i = 0; i < count; i++) {
                defaultPrefab(cache);
            }
        }

        public List<ELEMENT> getContainerList(Object holder) {
            return containerList((HANDLER) holder);
        }

        public abstract void sync(ELEMENT cache, ELEMENT data);

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