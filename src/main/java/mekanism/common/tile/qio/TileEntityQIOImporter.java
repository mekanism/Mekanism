package mekanism.common.tile.qio;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.SerializationConstants;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityQIOImporter extends TileEntityQIOFilterHandler {

    private static final int MAX_DELAY = MekanismUtils.TICKS_PER_HALF_SECOND;

    //TODO - 26.1: Do we want to make filters able to check resources directly instead of having to convert it to a stack?
    private final Predicate<ItemResource> FILTER_ENABLED = resource -> getFilterManager().anyEnabledMatch(resource.toStack(), QIOFilter::test);

    @Nullable
    private BlockCapabilityCache<ResourceHandler<ItemResource>, @Nullable Direction> backInventory;
    private int delay = 0;
    private boolean importWithoutFilter = true;

    public TileEntityQIOImporter(BlockPos pos, BlockState state) {
        super(MekanismBlocks.QIO_IMPORTER, pos, state);
    }

    @Override
    protected boolean onUpdateServer(@Nullable QIOFrequency frequency) {
        boolean needsUpdate = super.onUpdateServer(frequency);
        if (frequency != null && canFunction()) {
            if (delay > 0) {
                delay--;
            } else {
                tryImport(frequency);
                delay = MAX_DELAY;
            }
        }
        return needsUpdate;
    }

    @Override
    protected void invalidateDirectionCaches(Direction newDirection) {
        super.invalidateDirectionCaches(newDirection);
        backInventory = null;
    }

    private void tryImport(QIOFrequency freq) {
        if (backInventory == null) {
            Direction direction = getDirection();
            backInventory = Capabilities.ITEM.createCache((ServerLevel) level, worldPosition.relative(direction.getOpposite()), direction);
        }
        ResourceHandler<ItemResource> inventory = backInventory.getCapability();
        if (inventory == null) {//Not an IItemHandler
            return;
        }

        Predicate<ItemResource> canFilter;
        if (getFilterManager().hasEnabledFilters()) {
            canFilter = FILTER_ENABLED;
        } else if (importWithoutFilter) {
            // return true if we don't have any enabled filters installed, and we allow for filterless importing
            canFilter = ConstantPredicates.alwaysTrue();
        } else {
            //If we don't have any enabled filters installed, and we don't allow filterless importing
            return;
        }
        int slots = inventory.size();
        if (slots == 0) {
            //If the inventory has no slots just exit early
            return;
        }
        Set<ItemResource> typesAdded = new HashSet<>();
        int maxTypes = getMaxTransitTypes();
        int maxCount = getMaxTransitCount();
        int countAdded = 0;

        for (int i = slots - 1; i >= 0; i--) {
            ItemResource type = inventory.getResource(i);
            // if the slot is empty, or we don't have room for another item type, skip
            if (type.isEmpty() || (!typesAdded.contains(type) && typesAdded.size() == maxTypes)) {
                continue;
            }
            // if we can't filter this item type, skip
            if (!canFilter.test(type)) {
                continue;
            }
            int extractable;
            int amountInserted;
            try (Transaction transaction = Transaction.openRoot()) {
                //TODO - 26.1: Ignore caring about the index for extracting, and instead skip over things already in typesAdded
                extractable = inventory.extract(i, type, maxCount - countAdded, transaction);
                if (extractable == 0) {//Nothing can be extracted, skip it
                    continue;
                }
                int inserted = freq.addItem(type, extractable);
                if (extractable == inserted) {
                    //Everything from our initial extraction could be inserted, just commit the transaction as the changes made are the ones we want
                    transaction.commit();
                    // and add it as a type that was successful
                    typesAdded.add(type);
                    countAdded += extractable;
                    continue;
                }
                amountInserted = extractable - inserted;
            }
            if (amountInserted > 0) {
                //We were unable to add everything our initial extraction attempt got to the frequency
                // This means we let it revert the inventory to the previous state, so need to extract how much we have added to the frequency
                // from the inventory
                try (Transaction transaction = Transaction.openRoot()) {
                    int extracted = inventory.extract(i, type, amountInserted, transaction);
                    if (amountInserted != extracted) {//TODO - 26.1: Maybe rework this error message if we even have any "simulation" once we move qio insertion to transactions
                        Mekanism.logger.error("QIO insertion error: item resource handler at {} in {} returned {} of {} during simulated extraction, but returned {} during execution. This is wrong!",
                              worldPosition.relative(getOppositeDirection()), level.dimension().identifier(), extractable, type, extracted);
                    }
                    transaction.commit();
                    typesAdded.add(type);
                    //TODO - 26.1: extracted should always be <= amountInserted, so should we be using extracted instead of amountInserted? In case things don't line up?
                    countAdded += amountInserted;
                }
            }
        }
    }

    @ComputerMethod
    public boolean getImportWithoutFilter() {
        return importWithoutFilter;
    }

    public void toggleImportWithoutFilter() {
        importWithoutFilter = !importWithoutFilter;
        markForSave();
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::getImportWithoutFilter, value -> importWithoutFilter = value));
    }

    @Override
    public void writeSustainedData(@NotNull ValueOutput output) {
        super.writeSustainedData(output);
        output.putBoolean(SerializationConstants.AUTO, importWithoutFilter);
    }

    @Override
    public void readSustainedData(@NotNull ValueInput input) {
        super.readSustainedData(input);
        importWithoutFilter = input.getBooleanOr(SerializationConstants.AUTO, importWithoutFilter);
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.AUTO, importWithoutFilter);
    }

    @Override
    protected void applyImplicitComponents(@NotNull DataComponentGetter input) {
        super.applyImplicitComponents(input);
        importWithoutFilter = input.getOrDefault(MekanismDataComponents.AUTO, importWithoutFilter);
    }

    //Methods relating to IComputerTile
    @ComputerMethod(requiresPublicSecurity = true)
    void setImportsWithoutFilter(boolean value) throws ComputerException {
        validateSecurityIsPublic();
        if (importWithoutFilter != value) {
            toggleImportWithoutFilter();
        }
    }
    //End methods IComputerTile
}
