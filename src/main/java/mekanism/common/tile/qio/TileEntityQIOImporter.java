package mekanism.common.tile.qio;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.SerializationConstants;
import mekanism.api.functions.ConstantPredicates;
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

    private final Predicate<ItemResource> FILTER_ENABLED = resource -> getFilterManager().anyEnabledMatch(resource, QIOFilter::test);

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
        int slots = inventory.size();
        if (slots == 0) {
            //If the inventory has no slots just exit early
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
        Set<ItemResource> seenTypes = new HashSet<>();
        int maxTypes = getMaxTransitTypes();
        int maxCount = getMaxTransitCount();
        int countAdded = 0;
        int typesAdded = 0;

        for (int slot = slots - 1; slot >= 0; slot--) {
            ItemResource type = inventory.getResource(slot);
            if (type.isEmpty() || !seenTypes.add(type) || typesAdded == maxTypes || !canFilter.test(type)) {
                // if the slot is empty, we have already seen the item type, we don't have room for another item type,
                // or we can't filter this item type, skip
                continue;
            }
            int extractable;
            try (Transaction simulation = Transaction.openRoot()) {
                extractable = inventory.extract(type, maxCount - countAdded, simulation);
                if (extractable == 0) {//Nothing can be extracted, skip it
                    continue;
                }
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int inserted = freq.addItem(type, extractable, transaction);
                if (inserted > 0 && inventory.extract(type, inserted, transaction) == inserted) {
                    countAdded += inserted;
                    typesAdded++;
                    transaction.commit();
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
