package mekanism.common.tile.machine;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.UnknownNullability;

//TODO - V11: Make this support other tag types, such as fluids
public class TileEntityOredictionificator extends TileEntityConfigurableMachine implements ITileFilterHolder<OredictionificatorItemFilter> {

    private final FilterManager<OredictionificatorItemFilter> filterManager = new FilterManager<>(OredictionificatorItemFilter.class, this::markForSave, this::getLevel);
    public boolean didProcess;

    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem", docPlaceholder = "input slot")
    InputInventorySlot inputSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "output slot")
    OutputInventorySlot outputSlot;
    private final IConfigValueInvalidationListener validFiltersListener = new ODConfigValueInvalidationListener();

    public TileEntityOredictionificator(BlockPos pos, BlockState state) {
        super(MekanismBlocks.OREDICTIONIFICATOR, pos, state);
        configComponent.setupIOConfig(TransmissionType.ITEM, inputSlot, outputSlot);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSideWithItemConfig(this);
        //Only allow inserting items with tags that match filters, but mark all items that have any filterable tags as valid
        builder.addContainer(inputSlot = InputInventorySlot.at((itemType, _) -> hasResult(filterManager.getEnabledFilters(), itemType), this::hasFilterableTags, listener, 56, 115));
        builder.addContainer(outputSlot = OutputInventorySlot.at(listener, 164, 115));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        if (CommonWorldTickHandler.flushTagAndRecipeCaches) {
            for (OredictionificatorFilter<?, ?, ?> filter : filterManager.getFilters()) {
                filter.flushCachedTag();
            }
        }
        didProcess = false;
        if (canFunction() && !inputSlot.isEmpty()) {
            ItemResource inputType = inputSlot.resource();
            ItemResource result = getResult(filterManager.getEnabledFilters(), inputType);
            if (!result.isEmpty()) {
                int outputNeeded = outputSlot.getNeededAsInt(result);
                if (outputNeeded > 0) {
                    try (Transaction transaction = Transaction.openRoot()) {
                        int available = inputSlot.extract(inputType, outputNeeded, transaction, AutomationType.INTERNAL);
                        if (available > 0 && outputSlot.insert(result, available, transaction, AutomationType.INTERNAL) == available) {
                            transaction.commit();
                            didProcess = true;
                        }
                    }
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

    private static List<Identifier> getFilterableTags(ItemResource itemType) {
        //TODO: Cache this and hasFilterableTags?
        //For each tag that matches a tag that is filterable, add it to the resulting list
        return itemType.typeHolder().tags()
              .map(TagKey::location)
              .filter(TileEntityOredictionificator::isPossibleFilter)
              .toList();
    }

    private boolean hasFilterableTags(ItemResource stack) {
        return stack.tags().anyMatch(tag -> isPossibleFilter(tag.location()));
    }

    private static boolean isPossibleFilter(Identifier resource) {
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

    public static boolean isValidTarget(Identifier tag) {
        if (BuiltInRegistries.ITEM.get(TagKey.create(Registries.ITEM, tag)).isPresent()) {
            for (String filter : MekanismConfig.general.validOredictionificatorFilters.get().getOrDefault(tag.getNamespace(), Collections.emptyList())) {
                if (tag.getPath().startsWith(filter)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasResult(List<OredictionificatorItemFilter> enabledFilters, ItemResource itemType) {
        return !getResult(enabledFilters, itemType).isEmpty();
    }

    private static ItemResource getResult(List<OredictionificatorItemFilter> enabledFilters, ItemResource itemType) {
        if (!enabledFilters.isEmpty()) {
            for (Identifier filterableTag : getFilterableTags(itemType)) {
                for (OredictionificatorItemFilter filter : enabledFilters) {
                    if (filter.filterMatches(filterableTag)) {
                        ItemResource result = filter.getResult();
                        if (!result.isEmpty()) {
                            //If the result is empty, continue to try and find matches for other filters that are valid for the item
                            return result;
                        }
                    }
                }
            }
        }
        return ItemResource.EMPTY;
    }

    @Override
    public void writeSustainedData(ValueOutput output) {
        super.writeSustainedData(output);
        filterManager.serialize(output);
    }

    @Override
    public void readSustainedData(ValueInput input) {
        super.readSustainedData(input);
        filterManager.deserialize(input);
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
