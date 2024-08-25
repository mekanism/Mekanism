package mekanism.common.tile.machine;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
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
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

//TODO - V11: Make this support other tag types, such as fluids
public class TileEntityOredictionificator extends TileEntityConfigurableMachine implements ITileFilterHolder<OredictionificatorItemFilter> {

    private final FilterManager<OredictionificatorItemFilter> filterManager = new FilterManager<>(OredictionificatorItemFilter.class, this::markForSave);
    public boolean didProcess;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem", docPlaceholder = "input slot")
    InputInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "output slot")
    OutputInventorySlot outputSlot;
    private final IConfigValueInvalidationListener validFiltersListener = new ODConfigValueInvalidationListener();

    public TileEntityOredictionificator(BlockPos pos, BlockState state) {
        super(MekanismBlocks.OREDICTIONIFICATOR, pos, state);
        configComponent.setupIOConfig(TransmissionType.ITEM, inputSlot, outputSlot, RelativeSide.RIGHT);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        //Only allow inserting items with tags that match filters, but mark all items that have any filterable tags as valid
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> hasResult(filterManager.getEnabledFilters(), item), this::hasFilterableTags, listener, 56, 115));
        builder.addSlot(outputSlot = OutputInventorySlot.at(listener, 164, 115));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        if (CommonWorldTickHandler.flushTagAndRecipeCaches) {
            for (OredictionificatorFilter<?, ?, ?> filter : filterManager.getFilters()) {
                filter.flushCachedTag();
            }
        }
        didProcess = false;
        if (canFunction() && !inputSlot.isEmpty()) {
            ItemStack result = getResult(filterManager.getEnabledFilters(), inputSlot.getStack());
            if (!result.isEmpty()) {
                ItemStack outputStack = outputSlot.getStack();
                if (outputStack.isEmpty()) {
                    inputSlot.shrinkStack(1, Action.EXECUTE);
                    outputSlot.setStack(result);
                    didProcess = true;
                } else if (ItemStack.isSameItemSameComponents(outputStack, result) && outputStack.getCount() < outputSlot.getLimit(outputStack)) {
                    inputSlot.shrinkStack(1, Action.EXECUTE);
                    outputSlot.growStack(1, Action.EXECUTE);
                    didProcess = true;
                }
            }
        }
        return sendUpdatePacket;
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

    private static List<ResourceLocation> getFilterableTags(ItemStack stack) {
        //TODO: Cache this and hasFilterableTags?
        //For each tag that matches a tag that is filterable, add it to the resulting list
        return stack.getTags()
              .map(TagKey::location)
              .filter(TileEntityOredictionificator::isPossibleFilter)
              .toList();
    }

    private boolean hasFilterableTags(ItemStack stack) {
        return stack.getTags().anyMatch(tag -> isPossibleFilter(tag.location()));
    }

    private static boolean isPossibleFilter(ResourceLocation resource) {
        //Note: We get the possible filters inside the stream so that we don't have to capture it
        // as while we look it for every item, it is cached on the config value, so it becomes a simple getter
        Map<String, List<String>> possibleFilters = MekanismConfig.general.validOredictionificatorFilters.get();
        for (String pre : possibleFilters.getOrDefault(resource.getNamespace(), Collections.emptyList())) {
            if (resource.getPath().startsWith(pre)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidTarget(ResourceLocation tag) {
        if (BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tag)).isPresent()) {
            for (String filter : MekanismConfig.general.validOredictionificatorFilters.get().getOrDefault(tag.getNamespace(), Collections.emptyList())) {
                if (tag.getPath().startsWith(filter)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasResult(List<OredictionificatorItemFilter> enabledFilters, ItemStack stack) {
        return !getResult(enabledFilters, stack).isEmpty();
    }

    private static ItemStack getResult(List<OredictionificatorItemFilter> enabledFilters, ItemStack stack) {
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
    public void writeSustainedData(HolderLookup.Provider provider, CompoundTag dataMap) {
        super.writeSustainedData(provider, dataMap);
        filterManager.writeToNBT(provider, dataMap);
    }

    @Override
    public void readSustainedData(HolderLookup.Provider provider, @NotNull CompoundTag dataMap) {
        super.readSustainedData(provider, dataMap);
        filterManager.readFromNBT(provider, dataMap);
    }

    @Override
    public boolean supportsMode(RedstoneControl mode) {
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
    Collection<OredictionificatorItemFilter> getFilters() {
        return filterManager.getFilters();
    }

    @ComputerMethod(requiresPublicSecurity = true)
    boolean addFilter(OredictionificatorItemFilter filter) throws ComputerException {
        validateSecurityIsPublic();
        return filterManager.addFilter(filter);
    }

    @ComputerMethod(requiresPublicSecurity = true)
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
