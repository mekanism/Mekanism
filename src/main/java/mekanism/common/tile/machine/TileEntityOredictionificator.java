package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedValue.IConfigValueInvalidationListener;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.oredictionificator.OredictionificatorFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.list.SyncableFilterList;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.collection.HashList;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.ItemHandlerHelper;

//TODO - V11: Make this support other tag types, such as fluids
public class TileEntityOredictionificator extends TileEntityConfigurableMachine implements ISustainedData, ITileFilterHolder<OredictionificatorItemFilter> {

    private HashList<OredictionificatorItemFilter> filters = new HashList<>();
    public boolean didProcess;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem")
    private InputInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem")
    private OutputInventorySlot outputSlot;
    private final IConfigValueInvalidationListener validFiltersListener = () -> {
        for (OredictionificatorItemFilter filter : filters) {
            //Check each filter for validity
            filter.checkValidity();
        }
    };

    public TileEntityOredictionificator() {
        super(MekanismBlocks.OREDICTIONIFICATOR);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM);
        configComponent.setupIOConfig(TransmissionType.ITEM, inputSlot, outputSlot, RelativeSide.RIGHT);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        //Only allow inserting items with tags that match filters, but mark all items that have any filterable tags as valid
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> !getResult(item).isEmpty(), this::hasFilterableTags, this, 26, 115));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 134, 115));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (CommonWorldTickHandler.flushTagAndRecipeCaches) {
            for (OredictionificatorFilter<?, ?, ?> filter : filters) {
                filter.flushCachedTag();
            }
        }
        didProcess = false;
        if (MekanismUtils.canFunction(this) && !inputSlot.isEmpty()) {
            ItemStack result = getResult(inputSlot.getStack());
            if (!result.isEmpty()) {
                ItemStack outputStack = outputSlot.getStack();
                if (outputStack.isEmpty()) {
                    inputSlot.shrinkStack(1, Action.EXECUTE);
                    outputSlot.setStack(result);
                    didProcess = true;
                } else if (ItemHandlerHelper.canItemStacksStack(outputStack, result) && outputStack.getCount() < outputSlot.getLimit(outputStack)) {
                    inputSlot.shrinkStack(1, Action.EXECUTE);
                    outputSlot.growStack(1, Action.EXECUTE);
                    didProcess = true;
                }
                markDirty(false);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        MekanismConfig.general.validOredictionificatorFilters.addInvalidationListener(validFiltersListener);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        removeInvalidationListener();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        removeInvalidationListener();
    }

    public void removeInvalidationListener() {
        MekanismConfig.general.validOredictionificatorFilters.removeInvalidationListener(validFiltersListener);
    }

    private List<ResourceLocation> getFilterableTags(ItemStack stack) {
        //TODO: Cache this and hasFilterableTags?
        Set<ResourceLocation> tags = stack.getItem().getTags();
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, List<String>> possibleFilters = MekanismConfig.general.validOredictionificatorFilters.get();
        List<ResourceLocation> filterableTags = new ArrayList<>();
        for (ResourceLocation resource : tags) {
            if (possibleFilters.getOrDefault(resource.getNamespace(), Collections.emptyList()).stream().anyMatch(pre -> resource.getPath().startsWith(pre))) {
                //For each tag that matches a tag that is filterable, add it to the resulting list
                filterableTags.add(resource);
            }
        }
        return filterableTags;
    }

    private boolean hasFilterableTags(ItemStack stack) {
        Set<ResourceLocation> tags = stack.getItem().getTags();
        if (!tags.isEmpty()) {
            Map<String, List<String>> possibleFilters = MekanismConfig.general.validOredictionificatorFilters.get();
            for (ResourceLocation resource : tags) {
                if (possibleFilters.getOrDefault(resource.getNamespace(), Collections.emptyList()).stream().anyMatch(pre -> resource.getPath().startsWith(pre))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isValidTarget(ResourceLocation tag) {
        if (ItemTags.getAllTags().getAvailableTags().contains(tag)) {
            for (String filter : MekanismConfig.general.validOredictionificatorFilters.get().getOrDefault(tag.getNamespace(), Collections.emptyList())) {
                if (tag.getPath().startsWith(filter)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ItemStack getResult(ItemStack stack) {
        if (!filters.isEmpty()) {
            for (ResourceLocation filterableTag : getFilterableTags(stack)) {
                for (OredictionificatorItemFilter filter : filters) {
                    if (filter.filterMatches(filterableTag)) {
                        ItemStack result = filter.getResult();
                        if (!result.isEmpty()) {
                            //If the result is empty, continue to try and find matches for other filters that are valid for the item
                            return result;
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void addGeneralPersistentData(CompoundNBT data) {
        super.addGeneralPersistentData(data);
        if (!filters.isEmpty()) {
            data.put(NBTConstants.FILTERS, writeFilters());
        }
    }

    @Override
    protected void loadGeneralPersistentData(CompoundNBT data) {
        super.loadGeneralPersistentData(data);
        if (data.contains(NBTConstants.FILTERS, NBT.TAG_LIST)) {
            setFilters(data.getList(NBTConstants.FILTERS, NBT.TAG_COMPOUND));
        }
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (!filters.isEmpty()) {
            ItemDataUtils.setList(itemStack, NBTConstants.FILTERS, writeFilters());
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        if (ItemDataUtils.hasData(itemStack, NBTConstants.FILTERS, NBT.TAG_LIST)) {
            setFilters(ItemDataUtils.getList(itemStack, NBTConstants.FILTERS));
        }
    }

    private ListNBT writeFilters() {
        ListNBT filterList = new ListNBT();
        for (OredictionificatorFilter<?, ?, ?> filter : filters) {
            filterList.add(filter.write(new CompoundNBT()));
        }
        return filterList;
    }

    private void setFilters(ListNBT filterList) {
        for (int i = 0; i < filterList.size(); i++) {
            IFilter<?> filter = BaseFilter.readFromNBT(filterList.getCompound(i));
            if (filter instanceof OredictionificatorItemFilter) {
                filters.add((OredictionificatorItemFilter) filter);
            }
        }
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put(NBTConstants.FILTERS, NBTConstants.FILTERS);
        return remap;
    }

    @Override
    public boolean canPulse() {
        return true;
    }

    @Override
    @ComputerMethod
    public HashList<OredictionificatorItemFilter> getFilters() {
        return filters;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(() -> didProcess, value -> didProcess = value));
        container.track(SyncableFilterList.create(this::getFilters, value -> {
            if (value instanceof HashList) {
                filters = (HashList<OredictionificatorItemFilter>) value;
            } else {
                filters = new HashList<>(value);
            }
        }));
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private boolean addFilter(OredictionificatorItemFilter filter) throws ComputerException {
        validateSecurityIsPublic();
        return filters.add(filter);
    }

    @ComputerMethod
    private boolean removeFilter(OredictionificatorItemFilter filter) throws ComputerException {
        validateSecurityIsPublic();
        return filters.remove(filter);
    }
    //End methods IComputerTile
}