package mekanism.common.tile.qio;

import java.util.Collection;
import mekanism.api.Upgrade;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityQIOFilterHandler extends TileEntityQIOComponent implements ITileFilterHolder<QIOFilter<?>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private final SortableFilterManager<QIOFilter<?>> filterManager = new SortableFilterManager<QIOFilter<?>>((Class) QIOFilter.class, this::markForSave);
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
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            int speedUpgrades = upgradeComponent.getUpgrades(Upgrade.SPEED);
            // 64 to 320 items
            maxTransitCount = 64 + 32 * speedUpgrades;
            // 1 to 5 types
            maxTransitTypes = Math.round(1F + speedUpgrades / 2F);
        }
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
