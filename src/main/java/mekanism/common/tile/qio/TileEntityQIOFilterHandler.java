package mekanism.common.tile.qio;

import java.util.List;
import java.util.Map;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityQIOFilterHandler extends TileEntityQIOComponent implements ITileFilterHolder<QIOFilter<?>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private final SortableFilterManager<QIOFilter<?>> filterManager = new SortableFilterManager<QIOFilter<?>>((Class) QIOFilter.class, this::markForSave);

    public TileEntityQIOFilterHandler(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public SortableFilterManager<QIOFilter<?>> getFilterManager() {
        return filterManager;
    }

    @Override
    public void writeSustainedData(CompoundTag dataMap) {
        super.writeSustainedData(dataMap);
        filterManager.writeToNBT(dataMap);
    }

    @Override
    public void readSustainedData(CompoundTag dataMap) {
        super.readSustainedData(dataMap);
        filterManager.readFromNBT(dataMap);
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = super.getTileDataRemap();
        remap.put(NBTConstants.FILTERS, NBTConstants.FILTERS);
        return remap;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        filterManager.addContainerTrackers(container);
    }

    protected int getMaxTransitCount() {
        // 64 to 320 items
        return 64 + 32 * upgradeComponent.getUpgrades(Upgrade.SPEED);
    }

    protected int getMaxTransitTypes() {
        // 1 to 5 types
        return Math.round(1F + upgradeComponent.getUpgrades(Upgrade.SPEED) / 2F);
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private List<QIOFilter<?>> getFilters() {
        return filterManager.getFilters();
    }

    @ComputerMethod
    private boolean addFilter(QIOFilter<?> filter) throws ComputerException {
        validateSecurityIsPublic();
        return filterManager.addFilter(filter);
    }

    @ComputerMethod
    private boolean removeFilter(QIOFilter<?> filter) throws ComputerException {
        validateSecurityIsPublic();
        return filterManager.removeFilter(filter);
    }
    //End methods IComputerTile
}