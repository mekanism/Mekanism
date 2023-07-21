package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedValue.IConfigValueInvalidationListener;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.oredictionificator.OredictionificatorFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.TagUtils;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;

//TODO - V11: Make this support other tag types, such as fluids
public class TileEntityOredictionificator extends TileEntityConfigurableMachine implements ISustainedData, ITileFilterHolder<OredictionificatorItemFilter> {

    private final FilterManager<OredictionificatorItemFilter> filterManager = new FilterManager<>(OredictionificatorItemFilter.class, this::markForSave);
    public boolean didProcess;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem")
    private InputInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem")
    private OutputInventorySlot outputSlot;
    private final IConfigValueInvalidationListener validFiltersListener = new ODConfigValueInvalidationListener();

    public TileEntityOredictionificator(BlockPos pos, BlockState state) {
        super(MekanismBlocks.OREDICTIONIFICATOR, pos, state);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM);
        configComponent.setupIOConfig(TransmissionType.ITEM, inputSlot, outputSlot, RelativeSide.RIGHT);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        //Only allow inserting items with tags that match filters, but mark all items that have any filterable tags as valid
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> !getResult(item).isEmpty(), this::hasFilterableTags, listener, 26, 115));
        builder.addSlot(outputSlot = OutputInventorySlot.at(listener, 134, 115));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (CommonWorldTickHandler.flushTagAndRecipeCaches) {
            for (OredictionificatorFilter<?, ?, ?> filter : filterManager.getFilters()) {
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
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        MekanismConfig.general.validOredictionificatorFilters.addInvalidationListener(validFiltersListener);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        MekanismConfig.general.validOredictionificatorFilters.removeInvalidationListener(validFiltersListener);
    }

    private List<ResourceLocation> getFilterableTags(ItemStack stack) {
        //TODO: Cache this and hasFilterableTags?
        Set<ResourceLocation> tags = TagUtils.tagNames(stack.getTags());
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
        Set<ResourceLocation> tags = TagUtils.tagNames(stack.getTags());
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
        ITagManager<Item> manager = TagUtils.manager(ForgeRegistries.ITEMS);
        if (manager.isKnownTagName(manager.createTagKey(tag))) {
            for (String filter : MekanismConfig.general.validOredictionificatorFilters.get().getOrDefault(tag.getNamespace(), Collections.emptyList())) {
                if (tag.getPath().startsWith(filter)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ItemStack getResult(ItemStack stack) {
        List<OredictionificatorItemFilter> enabledFilters = filterManager.getEnabledFilters();
        if (!enabledFilters.isEmpty()) {
            for (ResourceLocation filterableTag : getFilterableTags(stack)) {
                for (OredictionificatorItemFilter filter : enabledFilters) {
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
    public void writeSustainedData(CompoundTag dataMap) {
        filterManager.writeToNBT(dataMap);
    }

    @Override
    public void readSustainedData(CompoundTag dataMap) {
        filterManager.readFromNBT(dataMap);
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
    public FilterManager<OredictionificatorItemFilter> getFilterManager() {
        return filterManager;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(() -> didProcess, value -> didProcess = value));
        filterManager.addContainerTrackers(container);
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    List<OredictionificatorItemFilter> getFilters() {
        return filterManager.getFilters();
    }

    @ComputerMethod
    boolean addFilter(OredictionificatorItemFilter filter) throws ComputerException {
        validateSecurityIsPublic();
        return filterManager.addFilter(filter);
    }

    @ComputerMethod
    boolean removeFilter(OredictionificatorItemFilter filter) throws ComputerException {
        validateSecurityIsPublic();
        return filterManager.removeFilter(filter);
    }
    //End methods IComputerTile

    public class ODConfigValueInvalidationListener implements IConfigValueInvalidationListener {

        @Override
        public void run() {
            for (OredictionificatorItemFilter filter : filterManager.getFilters()) {
                //Check each filter for validity
                filter.checkValidity();
            }
        }

        public boolean isIn(Level level) {
            return getLevel() == level;
        }
    }
}