package mekanism.common.lib.multiblock;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.util.StackUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiblockCache<T extends MultiblockData> implements IMekanismInventory, IMekanismFluidHandler, IMekanismStrictEnergyHandler, IMekanismHeatHandler,
      IMekanismChemicalHandler {

    private final List<IInventorySlot> inventorySlots = new ArrayList<>();
    private final List<IExtendedFluidTank> fluidTanks = new ArrayList<>();
    private final List<IChemicalTank> chemicalTanks = new ArrayList<>();
    private final List<IEnergyContainer> energyContainers = new ArrayList<>();
    private final List<IHeatCapacitor> heatCapacitors = new ArrayList<>();

    public void apply(HolderLookup.Provider provider, T data) {
        for (CacheSubstance<?, INBTSerializable<CompoundTag>> type : CacheSubstance.VALUES) {
            List<? extends INBTSerializable<CompoundTag>> containers = type.getContainerList(data);
            if (containers != null) {
                List<? extends INBTSerializable<CompoundTag>> cacheContainers = type.getContainerList(this);
                for (int i = 0; i < cacheContainers.size(); i++) {
                    if (i < containers.size()) {
                        //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                        containers.get(i).deserializeNBT(provider, cacheContainers.get(i).serializeNBT(provider));
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

    public void load(HolderLookup.Provider provider, CompoundTag nbtTags) {
        for (CacheSubstance<?, INBTSerializable<CompoundTag>> type : CacheSubstance.VALUES) {
            type.readFrom(provider, nbtTags, this);
        }
    }

    public void save(HolderLookup.Provider provider, CompoundTag nbtTags) {
        for (CacheSubstance<?, INBTSerializable<CompoundTag>> type : CacheSubstance.VALUES) {
            type.saveTo(provider, nbtTags, this);
        }
    }

    public void merge(MultiblockCache<T> mergeCache, RejectContents rejectContents) {
        // prefab enough containers for each substance type to support the merge cache
        for (CacheSubstance<?, INBTSerializable<CompoundTag>> type : CacheSubstance.VALUES) {
            type.preHandleMerge(this, mergeCache);
        }

        // Items
        StackUtils.merge(getInventorySlots(null), mergeCache.getInventorySlots(null), rejectContents.rejectedItems);
        // Fluid
        StorageUtils.mergeFluidTanks(getFluidTanks(null), mergeCache.getFluidTanks(null), rejectContents.rejectedFluids);
        // Chemical
        StorageUtils.mergeTanks(getChemicalTanks(null), mergeCache.getChemicalTanks(null), rejectContents.rejectedChemicals);
        // Energy
        StorageUtils.mergeEnergyContainers(getEnergyContainers(null), mergeCache.getEnergyContainers(null));
        // Heat
        StorageUtils.mergeHeatCapacitors(getHeatCapacitors(null), mergeCache.getHeatCapacitors(null));
    }

    @Override
    public void onContentsChanged() {
    }

    @NotNull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return inventorySlots;
    }

    @NotNull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @NotNull
    @Override
    public List<IChemicalTank> getChemicalTanks(@Nullable Direction side) {
        return chemicalTanks;
    }

    @NotNull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyContainers;
    }

    @NotNull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(Direction side) {
        return heatCapacitors;
    }

    public static class RejectContents {

        public final List<ItemStack> rejectedItems = new ArrayList<>();
        public final List<FluidStack> rejectedFluids = new ArrayList<>();
        public final List<ChemicalStack> rejectedChemicals = new ArrayList<>();
    }

    public abstract static class CacheSubstance<HANDLER, ELEMENT extends INBTSerializable<CompoundTag>> {

        public static final CacheSubstance<IMekanismInventory, IInventorySlot> ITEMS = new CacheSubstance<>(ContainerType.ITEM) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.inventorySlots.add(BasicInventorySlot.at(cache, 0, 0));
            }

            @Override
            protected List<IInventorySlot> containerList(IMekanismInventory inventory) {
                return inventory.getInventorySlots(null);
            }

            @Override
            public void sync(IInventorySlot cache, IInventorySlot data) {
                cache.setStack(data.getStack());
            }
        };

        public static final CacheSubstance<IMekanismFluidHandler, IExtendedFluidTank> FLUID = new CacheSubstance<>(ContainerType.FLUID) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.fluidTanks.add(BasicFluidTank.create(Integer.MAX_VALUE, cache));
            }

            @Override
            protected List<IExtendedFluidTank> containerList(IMekanismFluidHandler fluidHandler) {
                return fluidHandler.getFluidTanks(null);
            }

            @Override
            public void sync(IExtendedFluidTank cache, IExtendedFluidTank data) {
                cache.setStack(data.getFluid());
            }
        };

        public static final CacheSubstance<IMekanismChemicalHandler, IChemicalTank> CHEMICAL = new CacheSubstance<>(ContainerType.CHEMICAL) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.chemicalTanks.add(BasicChemicalTank.createAllValid(Long.MAX_VALUE, cache));
            }

            @Override
            protected List<IChemicalTank> containerList(IMekanismChemicalHandler tracker) {
                return tracker.getChemicalTanks(null);
            }

            @Override
            public void sync(IChemicalTank cache, IChemicalTank data) {
                cache.setStack(data.getStack());
            }

            @Override//TODO - 1.22: remove backcompat
            public void readFrom(HolderLookup.Provider provider, CompoundTag tag, MultiblockCache<?> cache) {
                int stored = Math.max(
                      tag.getInt("gas_tanks_stored"),
                      Math.max(
                            tag.getInt("infusion_tanks_stored"),
                            Math.max(
                                  tag.getInt("pigment_tanks_stored"),
                                  tag.getInt("slurry_tanks_stored")
                            )
                      )
                );
                if (stored > 0) {
                    tag.putInt(getStoredTagKey(), Math.max(stored, tag.getInt(getStoredTagKey())));
                }
                super.readFrom(provider, tag, cache);
            }
        };

        public static final CacheSubstance<IMekanismStrictEnergyHandler, IEnergyContainer> ENERGY = new CacheSubstance<>(ContainerType.ENERGY) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.energyContainers.add(BasicEnergyContainer.create(Long.MAX_VALUE, cache));
            }

            @Override
            protected List<IEnergyContainer> containerList(IMekanismStrictEnergyHandler handler) {
                return handler.getEnergyContainers(null);
            }

            @Override
            public void sync(IEnergyContainer cache, IEnergyContainer data) {
                cache.setEnergy(data.getEnergy());
            }
        };

        public static final CacheSubstance<IMekanismHeatHandler, IHeatCapacitor> HEAT = new CacheSubstance<>(ContainerType.HEAT) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.heatCapacitors.add(BasicHeatCapacitor.create(HeatAPI.DEFAULT_HEAT_CAPACITY, null, cache));
            }

            @Override
            protected List<IHeatCapacitor> containerList(IMekanismHeatHandler handler) {
                return handler.getHeatCapacitors(null);
            }

            @Override
            public void sync(IHeatCapacitor cache, IHeatCapacitor data) {
                cache.setHeat(data.getHeat());
                if (cache instanceof BasicHeatCapacitor heatCapacitor) {
                    heatCapacitor.setHeatCapacity(data.getHeatCapacity(), false);
                }
            }
        };

        @SuppressWarnings("unchecked")
        public static final CacheSubstance<?, INBTSerializable<CompoundTag>>[] VALUES = new CacheSubstance[]{
              CHEMICAL,
              ITEMS,
              FLUID,
              ENERGY,
              HEAT
        };

        private final ContainerType<ELEMENT, ?, ?> containerType;

        public CacheSubstance(ContainerType<ELEMENT, ?, ?> containerType) {
            this.containerType = containerType;
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

        protected String getStoredTagKey() {
            return containerType.getTag() + "_stored";
        }

        public void readFrom(HolderLookup.Provider provider, CompoundTag tag, MultiblockCache<?> cache) {
            int stored = tag.getInt(getStoredTagKey());
            if (stored > 0) {
                prefab(cache, stored);
                containerType.readFrom(provider, tag, getContainerList(cache));
            }
        }

        public void saveTo(HolderLookup.Provider provider, CompoundTag tag, MultiblockCache<?> holder) {
            List<ELEMENT> containers = getContainerList(holder);
            if (!containers.isEmpty()) {
                //Note: We can skip putting stored at zero if containers is empty (in addition to skipping actually writing the containers)
                // because getInt will default to 0 for keys that aren't present
                tag.putInt(getStoredTagKey(), containers.size());
                containerType.saveTo(provider, tag, getContainerList(holder));
            }
        }
    }
}