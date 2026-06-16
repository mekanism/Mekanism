package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.component.containers.type.IListContainerType;
import mekanism.common.component.containers.type.ISingleContainerType;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public class MultiblockCache<T extends MultiblockData> implements IMultiblockContents {

    private final List<IInventorySlot> inventorySlots = new ArrayList<>();
    private final List<IFluidTank> fluidTanks = new ArrayList<>();
    private final List<IChemicalTank> chemicalTanks = new ArrayList<>();
    private final List<IHeatCapacitor> heatCapacitors = new ArrayList<>();
    @Nullable
    private IEnergyContainer energyContainer;

    public void apply(T data) {
        for (CacheSubstance<ValueIOSerializable> type : CACHE_SUBSTANCES) {
            type.apply(data, this);
        }
    }

    public void sync(T data) {
        for (CacheSubstance<ValueIOSerializable> type : CACHE_SUBSTANCES) {
            type.sync(data, this);
        }
    }

    public void load(ValueInput input) {
        for (CacheSubstance<ValueIOSerializable> type : CACHE_SUBSTANCES) {
            type.readFrom(input, this);
        }
    }

    public void save(ValueOutput output) {
        for (CacheSubstance<ValueIOSerializable> type : CACHE_SUBSTANCES) {
            type.saveTo(output, this);
        }
    }

    public void merge(MultiblockCache<T> mergeCache, RejectContents rejectContents) {
        // prefab enough containers for each substance type to support the merge cache
        for (CacheSubstance<ValueIOSerializable> type : CACHE_SUBSTANCES) {
            type.preHandleMerge(this, mergeCache);
        }

        try (Transaction transaction = Transaction.openRoot()) {
            // Items
            ContainerType.ITEM.merge(getInventorySlots(), mergeCache.getInventorySlots(), rejectContents.rejectedItems, transaction);
            // Fluid
            ContainerType.FLUID.merge(getFluidTanks(), mergeCache.getFluidTanks(), rejectContents.rejectedFluids, transaction);
            // Chemical
            ContainerType.CHEMICAL.merge(getChemicalTanks(), mergeCache.getChemicalTanks(), rejectContents.rejectedChemicals, transaction);
            // Energy
            StorageUtils.mergeEnergyContainers(getEnergyContainer(), mergeCache.getEnergyContainer(), transaction);
            // Heat
            StorageUtils.mergeHeatCapacitors(getHeatCapacitors(), mergeCache.getHeatCapacitors());
            transaction.commit();
        }
    }

    @Override
    public List<IInventorySlot> getInventorySlots() {
        return inventorySlots;
    }

    @Override
    public List<IFluidTank> getFluidTanks() {
        return fluidTanks;
    }

    @Override
    public List<IChemicalTank> getChemicalTanks() {
        return chemicalTanks;
    }

    @Nullable
    @Override
    public IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side) {
        return heatCapacitors;
    }

    public static class RejectContents {

        public final Object2LongMap<ItemResource> rejectedItems = new Object2LongOpenHashMap<>();
        public final Object2LongMap<FluidResource> rejectedFluids = new Object2LongOpenHashMap<>();
        public final Object2LongMap<ChemicalResource> rejectedChemicals = new Object2LongOpenHashMap<>();
    }

    public static final CacheSubstance<IInventorySlot> ITEMS = new CacheListSubstance<>(ContainerType.ITEM) {
        @Override
        protected void defaultPrefab(MultiblockCache<?> cache) {
            cache.inventorySlots.add(BasicInventorySlot.at(null, 0, 0));
        }

        @Override
        protected List<IInventorySlot> containerList(IMultiblockContents inventory) {
            return inventory.getInventorySlots();
        }
    };

    public static final CacheSubstance<IFluidTank> FLUID = new CacheListSubstance<>(ContainerType.FLUID) {
        @Override
        protected void defaultPrefab(MultiblockCache<?> cache) {
            cache.fluidTanks.add(BasicFluidTank.create(Long.MAX_VALUE, null));
        }

        @Override
        protected List<IFluidTank> containerList(IMultiblockContents fluidHandler) {
            return fluidHandler.getFluidTanks();
        }
    };

    public static final CacheSubstance<IChemicalTank> CHEMICAL = new CacheListSubstance<>(ContainerType.CHEMICAL) {
        @Override
        protected void defaultPrefab(MultiblockCache<?> cache) {
            cache.chemicalTanks.add(BasicChemicalTank.createAllValid(Long.MAX_VALUE, null));
        }

        @Override
        protected List<IChemicalTank> containerList(IMultiblockContents tracker) {
            return tracker.getChemicalTanks();
        }
    };

    public static final CacheSubstance<IEnergyContainer> ENERGY = new CacheSingleSubstance<>(ContainerType.ENERGY) {
        @Override
        protected void defaultPrefab(MultiblockCache<?> cache) {
            cache.energyContainer = BasicEnergyContainer.create(Long.MAX_VALUE, null);
        }

        @Nullable
        @Override
        protected IEnergyContainer container(IMultiblockContents handler) {
            return handler.getEnergyContainer();
        }
    };

    public static final CacheSubstance<IHeatCapacitor> HEAT = new CacheListSubstance<>(ContainerType.HEAT) {
        @Override
        protected void defaultPrefab(MultiblockCache<?> cache) {
            cache.heatCapacitors.add(BasicHeatCapacitor.create(HeatAPI.DEFAULT_HEAT_CAPACITY, null, null));
        }

        @Override
        protected List<IHeatCapacitor> containerList(IMultiblockContents handler) {
            return handler.getHeatCapacitors();
        }
    };

    @SuppressWarnings("unchecked")
    private static final CacheSubstance<ValueIOSerializable>[] CACHE_SUBSTANCES = new CacheSubstance[]{
          CHEMICAL,
          ITEMS,
          FLUID,
          ENERGY,
          HEAT
    };

    public abstract static class CacheSubstance<ELEMENT extends ValueIOSerializable> {

        protected final IContainerType<ELEMENT, ?> containerType;

        public CacheSubstance(IContainerType<ELEMENT, ?> containerType) {
            this.containerType = containerType;
        }

        protected abstract void defaultPrefab(MultiblockCache<?> cache);

        protected void prefab(MultiblockCache<?> cache, int count) {
            for (int i = 0; i < count; i++) {
                defaultPrefab(cache);
            }
        }

        protected String getStoredTagKey() {
            return containerType.getTag() + "_stored";
        }

        public void copy(ELEMENT from, ELEMENT to) {
            containerType.copy(from, to, null);
        }

        public abstract <DATA extends MultiblockData> void apply(DATA data, MultiblockCache<DATA> cache);

        public abstract <DATA extends MultiblockData> void sync(DATA data, MultiblockCache<DATA> cache);

        public abstract void preHandleMerge(MultiblockCache<?> cache, MultiblockCache<?> merge);

        public abstract void readFrom(ValueInput input, MultiblockCache<?> cache);

        public abstract void saveTo(ValueOutput output, MultiblockCache<?> holder);
    }

    public abstract static class CacheListSubstance<ELEMENT extends ValueIOSerializable> extends CacheSubstance<ELEMENT> {

        public CacheListSubstance(IListContainerType<?, ELEMENT, ?> containerType) {
            super(containerType);
        }

        @SuppressWarnings("unchecked")
        private IListContainerType<?, ELEMENT, ?> containerType() {
            return (IListContainerType<?, ELEMENT, ?>) containerType;
        }

        protected abstract List<ELEMENT> containerList(IMultiblockContents handler);

        @Override
        public <DATA extends MultiblockData> void apply(DATA data, MultiblockCache<DATA> cache) {
            List<ELEMENT> containers = containerList(data);
            List<ELEMENT> cacheContainers = containerList(cache);
            for (int i = 0; i < cacheContainers.size(); i++) {
                if (i < containers.size()) {
                    copy(cacheContainers.get(i), containers.get(i));
                }
            }
        }

        @Override
        public <DATA extends MultiblockData> void sync(DATA data, MultiblockCache<DATA> cache) {
            List<ELEMENT> containersToCopy = containerList(data);
            List<ELEMENT> cacheContainers = containerList(cache);
            if (cacheContainers.isEmpty()) {
                prefab(cache, containersToCopy.size());
            }
            for (int i = 0; i < containersToCopy.size(); i++) {
                copy(containersToCopy.get(i), cacheContainers.get(i));
            }
        }

        @Override
        public void preHandleMerge(MultiblockCache<?> cache, MultiblockCache<?> merge) {
            int diff = containerList(merge).size() - containerList(cache).size();
            if (diff > 0) {
                prefab(cache, diff);
            }
        }

        @Override
        public void readFrom(ValueInput input, MultiblockCache<?> cache) {
            int stored = input.getIntOr(getStoredTagKey(), 0);
            if (stored > 0) {
                prefab(cache, stored);
                containerType().readFrom(input, containerList(cache));
            }
        }

        @Override
        public void saveTo(ValueOutput output, MultiblockCache<?> holder) {
            List<ELEMENT> containers = containerList(holder);
            if (!containers.isEmpty()) {
                //Note: We can skip putting stored at zero if containers is empty (in addition to skipping actually writing the containers)
                // because getInt will default to 0 for keys that aren't present
                output.putInt(getStoredTagKey(), containers.size());
                containerType().saveTo(output, containerList(holder));
            }
        }
    }

    public abstract static class CacheSingleSubstance<ELEMENT extends ValueIOSerializable> extends CacheSubstance<ELEMENT> {

        public CacheSingleSubstance(ISingleContainerType<ELEMENT, ?> containerType) {
            super(containerType);
        }

        private ISingleContainerType<ELEMENT, ?> containerType() {
            return (ISingleContainerType<ELEMENT, ?>) containerType;
        }

        @Nullable
        protected abstract ELEMENT container(IMultiblockContents handler);

        protected ELEMENT containerOrInit(MultiblockCache<?> cache) {
            ELEMENT container = container(cache);
            if (container == null) {
                prefab(cache, 1);
                return Objects.requireNonNull(container(cache), "Container should be present after initialization");
            }
            return container;
        }

        @Override
        public <DATA extends MultiblockData> void apply(DATA data, MultiblockCache<DATA> cache) {
            ELEMENT container = container(data);
            if (container != null) {
                ELEMENT cacheContainer = container(cache);
                if (cacheContainer != null) {
                    copy(cacheContainer, container);
                }
            }
        }

        @Override
        public <DATA extends MultiblockData> void sync(DATA data, MultiblockCache<DATA> cache) {
            ELEMENT container = container(data);
            if (container != null) {
                copy(container, containerOrInit(cache));
            }
        }

        @Override
        public void preHandleMerge(MultiblockCache<?> cache, MultiblockCache<?> merge) {
            boolean cacheHasContainer = container(cache) == null;
            boolean mergeHasContainer = container(merge) == null;
            if (cacheHasContainer != mergeHasContainer) {
                //TODO - 26.2: Re-evaluate this
                prefab(cacheHasContainer ? merge : cache, 1);
            }
        }

        @Override
        public void readFrom(ValueInput input, MultiblockCache<?> cache) {
            containerType().readFrom(input, containerOrInit(cache));
        }

        @Override
        public void saveTo(ValueOutput output, MultiblockCache<?> holder) {
            containerType().saveTo(output, container(holder));
        }
    }
}