package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.util.ResourceUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public class MultiblockCache<T extends MultiblockData> implements IMultiblockContents {

    private final List<IInventorySlot> inventorySlots = new ArrayList<>();
    private final List<IFluidTank> fluidTanks = new ArrayList<>();
    private final List<IChemicalTank> chemicalTanks = new ArrayList<>();
    private final List<IEnergyContainer> energyContainers = new ArrayList<>();
    private final List<IHeatCapacitor> heatCapacitors = new ArrayList<>();

    public void apply(T data) {
        for (CacheSubstance<ValueIOSerializable> type : CacheSubstance.VALUES) {
            List<? extends ValueIOSerializable> containers = type.containerList(data);
            if (containers != null) {
                List<? extends ValueIOSerializable> cacheContainers = type.containerList(this);
                for (int i = 0; i < cacheContainers.size(); i++) {
                    if (i < containers.size()) {
                        type.copy(cacheContainers.get(i), containers.get(i));
                    }
                }
            }
        }
    }

    public void sync(T data) {
        for (CacheSubstance<ValueIOSerializable> type : CacheSubstance.VALUES) {
            List<? extends ValueIOSerializable> containersToCopy = type.containerList(data);
            if (containersToCopy != null) {
                List<? extends ValueIOSerializable> cacheContainers = type.containerList(this);
                if (cacheContainers.isEmpty()) {
                    type.prefab(this, containersToCopy.size());
                }
                for (int i = 0; i < containersToCopy.size(); i++) {
                    type.sync(cacheContainers.get(i), containersToCopy.get(i));
                }
            }
        }
    }

    public void load(@NotNull ValueInput input) {
        for (CacheSubstance<ValueIOSerializable> type : CacheSubstance.VALUES) {
            type.readFrom(input, this);
        }
    }

    public void save(@NotNull ValueOutput output) {
        for (CacheSubstance<ValueIOSerializable> type : CacheSubstance.VALUES) {
            type.saveTo(output, this);
        }
    }

    public void merge(MultiblockCache<T> mergeCache, RejectContents rejectContents) {
        // prefab enough containers for each substance type to support the merge cache
        for (CacheSubstance<ValueIOSerializable> type : CacheSubstance.VALUES) {
            type.preHandleMerge(this, mergeCache);
        }

        try (Transaction transaction = Transaction.openRoot()) {
            // Items
            ResourceUtils.merge(getInventorySlots(), mergeCache.getInventorySlots(), rejectContents.rejectedItems, transaction);
            // Fluid
            ResourceUtils.merge(getFluidTanks(), mergeCache.getFluidTanks(), rejectContents.rejectedFluids, transaction);
            // Chemical
            ResourceUtils.merge(getChemicalTanks(), mergeCache.getChemicalTanks(), rejectContents.rejectedChemicals, transaction);
            // Energy
            StorageUtils.mergeEnergyContainers(getEnergyContainers(), mergeCache.getEnergyContainers(), transaction);
            // Heat
            StorageUtils.mergeHeatCapacitors(getHeatCapacitors(), mergeCache.getHeatCapacitors());
            transaction.commit();
        }
    }

    @Override
    public void onContentsChanged() {
    }

    @NotNull
    @Override
    public List<IInventorySlot> getInventorySlots() {
        return inventorySlots;
    }

    @NotNull
    @Override
    public List<IFluidTank> getFluidTanks() {
        return fluidTanks;
    }

    @NotNull
    @Override
    public List<IChemicalTank> getChemicalTanks() {
        return chemicalTanks;
    }

    @NotNull
    @Override
    public List<IEnergyContainer> getEnergyContainers() {
        return energyContainers;
    }

    @NotNull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(Direction side) {
        return heatCapacitors;
    }

    public static class RejectContents {

        public final Object2LongMap<ItemResource> rejectedItems = new Object2LongOpenHashMap<>();
        public final Object2LongMap<FluidResource> rejectedFluids = new Object2LongOpenHashMap<>();
        public final Object2LongMap<ChemicalResource> rejectedChemicals = new Object2LongOpenHashMap<>();
    }

    public abstract static class CacheSubstance<ELEMENT extends ValueIOSerializable> {

        public static final CacheSubstance<IInventorySlot> ITEMS = new CacheSubstance<>(ContainerType.ITEM) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.inventorySlots.add(BasicInventorySlot.at(cache, 0, 0));
            }

            @Override
            protected List<IInventorySlot> containerList(IMultiblockContents inventory) {
                return inventory.getInventorySlots();
            }

            @Override
            public void sync(IInventorySlot cache, IInventorySlot data) {
                cache.setContents(data.getResource(), data.amountAsLong());
            }
        };

        public static final CacheSubstance<IFluidTank> FLUID = new CacheSubstance<>(ContainerType.FLUID) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.fluidTanks.add(BasicFluidTank.create(Long.MAX_VALUE, cache));
            }

            @Override
            protected List<IFluidTank> containerList(IMultiblockContents fluidHandler) {
                return fluidHandler.getFluidTanks();
            }

            @Override
            public void sync(IFluidTank cache, IFluidTank data) {
                cache.setContents(data.getResource(), data.amountAsLong());
            }
        };

        public static final CacheSubstance<IChemicalTank> CHEMICAL = new CacheSubstance<>(ContainerType.CHEMICAL) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.chemicalTanks.add(BasicChemicalTank.createAllValid(Long.MAX_VALUE, cache));
            }

            @Override
            protected List<IChemicalTank> containerList(IMultiblockContents tracker) {
                return tracker.getChemicalTanks();
            }

            @Override
            public void sync(IChemicalTank cache, IChemicalTank data) {
                cache.setContents(data.getResource(), data.amountAsLong());
            }
        };

        public static final CacheSubstance<IEnergyContainer> ENERGY = new CacheSubstance<>(ContainerType.ENERGY) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.energyContainers.add(BasicEnergyContainer.create(Long.MAX_VALUE, cache));
            }

            @Override
            protected List<IEnergyContainer> containerList(IMultiblockContents handler) {
                return handler.getEnergyContainers();
            }

            @Override
            public void sync(IEnergyContainer cache, IEnergyContainer data) {
                cache.setEnergy(data.getEnergy());
            }
        };

        public static final CacheSubstance<IHeatCapacitor> HEAT = new CacheSubstance<>(ContainerType.HEAT) {
            @Override
            protected void defaultPrefab(MultiblockCache<?> cache) {
                cache.heatCapacitors.add(BasicHeatCapacitor.create(HeatAPI.DEFAULT_HEAT_CAPACITY, null, cache));
            }

            @Override
            protected List<IHeatCapacitor> containerList(IMultiblockContents handler) {
                return handler.getHeatCapacitors();
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
        public static final CacheSubstance<ValueIOSerializable>[] VALUES = new CacheSubstance[]{
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

        protected abstract List<ELEMENT> containerList(IMultiblockContents handler);

        private void prefab(MultiblockCache<?> cache, int count) {
            for (int i = 0; i < count; i++) {
                defaultPrefab(cache);
            }
        }

        public abstract void sync(ELEMENT cache, ELEMENT data);

        public void copy(ELEMENT from, ELEMENT to) {
            containerType.copy(from, to);
        }

        public void preHandleMerge(MultiblockCache<?> cache, MultiblockCache<?> merge) {
            int diff = containerList(merge).size() - containerList(cache).size();
            if (diff > 0) {
                prefab(cache, diff);
            }
        }

        protected String getStoredTagKey() {
            return containerType.getTag() + "_stored";
        }

        public void readFrom(ValueInput input, MultiblockCache<?> cache) {
            int stored = input.getIntOr(getStoredTagKey(), 0);
            if (stored > 0) {
                prefab(cache, stored);
                containerType.readFrom(input, containerList(cache));
            }
        }

        public void saveTo(ValueOutput output, MultiblockCache<?> holder) {
            List<ELEMENT> containers = containerList(holder);
            if (!containers.isEmpty()) {
                //Note: We can skip putting stored at zero if containers is empty (in addition to skipping actually writing the containers)
                // because getInt will default to 0 for keys that aren't present
                output.putInt(getStoredTagKey(), containers.size());
                containerType.saveTo(output, containerList(holder));
            }
        }
    }
}