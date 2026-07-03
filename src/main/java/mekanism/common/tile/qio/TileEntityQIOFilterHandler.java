package mekanism.common.tile.qio;

import java.util.Collection;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileEntityQIOFilterHandler extends TileEntityQIOComponent implements ITileFilterHolder<QIOFilter<?>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private final SortableFilterManager<QIOFilter<?>> filterManager = new SortableFilterManager<QIOFilter<?>>((Class) QIOFilter.class, this::markForSave, this::getLevel);
    //TODO - 26.2: Do we want to up this to Item.ABSOLUTE_MAX_STACK_SIZE? Even if not, should we switch this to Item.DEFAULT_MAX_STACK_SIZE?
    private int maxTransitCount = 64;
    private int maxTransitTypes = 1;

    public TileEntityQIOFilterHandler(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public SortableFilterManager<QIOFilter<?>> getFilterManager() {
        return filterManager;
    }

    @Override
    public void recalculateUpgrades(HolderLookup.Provider registries, Holder<Upgrade> upgrade, int totalInstalled) {
        super.recalculateUpgrades(registries, upgrade, totalInstalled);
        if (upgrade.is(UpgradeIds.SPEED)) {
            // 64 to 320 items
            maxTransitCount = 64 + 32 * totalInstalled;
            // 1 to 5 types
            maxTransitTypes = Math.round(1F + totalInstalled / 2F);
        }
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
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        filterManager.addContainerTrackers(container);
    }

    protected int getMaxTransitCount() {
        return maxTransitCount;
    }

    protected int getMaxTransitTypes() {
        return maxTransitTypes;
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    Collection<QIOFilter<?>> getFilters() {
        return filterManager.getFilters();
    }

    @ComputerMethod(requiresPublicSecurity = true)
    boolean addFilter(QIOFilter<?> filter) throws ComputerException {
        validateSecurityIsPublic();
        return filterManager.addFilter(filter);
    }

    @ComputerMethod(requiresPublicSecurity = true)
    boolean removeFilter(QIOFilter<?> filter) throws ComputerException {
        validateSecurityIsPublic();
        return filterManager.removeFilter(filter);
    }
    //End methods IComputerTile
}
