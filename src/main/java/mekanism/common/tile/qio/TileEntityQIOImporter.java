package mekanism.common.tile.qio;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.SerializationConstants;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityQIOImporter extends TileEntityQIOFilterHandler {

    private static final int MAX_DELAY = MekanismUtils.TICKS_PER_HALF_SECOND;

    private final Predicate<ItemStack> FILTER_ENABLED = stack -> getFilterManager().anyEnabledMatch(stack, (filter, s) -> filter.getFinder().test(s));

    @Nullable
    private BlockCapabilityCache<IItemHandler, @Nullable Direction> backInventory;
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
        IItemHandler inventory = backInventory.getCapability();
        if (inventory == null) {//Not an IItemHandler
            return;
        }

        Predicate<ItemStack> canFilter;
        if (getFilterManager().hasEnabledFilters()) {
            canFilter = FILTER_ENABLED;
        } else if (importWithoutFilter) {
            // return true if we don't have any enabled filters installed, and we allow for filterless importing
            canFilter = ConstantPredicates.alwaysTrue();
        } else {
            //If we don't have any enabled filters installed, and we don't allow filterless importing
            return;
        }
        int slots = inventory.getSlots();
        if (slots == 0) {
            //If the inventory has no slots just exit early
            return;
        }
        Set<HashedItem> typesAdded = new HashSet<>();
        int maxTypes = getMaxTransitTypes(), maxCount = getMaxTransitCount(), countAdded = 0;

        for (int i = slots - 1; i >= 0; i--) {
            ItemStack stack = inventory.extractItem(i, maxCount - countAdded, true);
            if (stack.isEmpty()) {
                continue;
            }
            HashedItem type = HashedItem.create(stack);
            // if we don't have room for another item type, skip
            if (!typesAdded.contains(type) && typesAdded.size() == maxTypes) {
                continue;
            }
            // if we can't filter this item type, skip
            if (!canFilter.test(stack)) {
                continue;
            }
            ItemStack used = TransporterManager.getToUse(stack, freq.addItem(stack));
            ItemStack ret = inventory.extractItem(i, used.getCount(), false);
            if (!InventoryUtils.areItemsStackable(used, ret) || used.getCount() != ret.getCount()) {
                Mekanism.logger.error("QIO insertion error: item handler at {} in {} returned {} during simulated extraction, but returned {} during execution. This is wrong!",
                      worldPosition.relative(getOppositeDirection()), level.dimension().location(), stack, ret);
            }
            typesAdded.add(type);
            countAdded += used.getCount();
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
    public void writeSustainedData(HolderLookup.Provider provider, CompoundTag dataMap) {
        super.writeSustainedData(provider, dataMap);
        dataMap.putBoolean(SerializationConstants.AUTO, importWithoutFilter);
    }

    @Override
    public void readSustainedData(HolderLookup.Provider provider, @NotNull CompoundTag dataMap) {
        super.readSustainedData(provider, dataMap);
        NBTUtils.setBooleanIfPresent(dataMap, SerializationConstants.AUTO, value -> importWithoutFilter = value);
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.AUTO, importWithoutFilter);
    }

    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
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
