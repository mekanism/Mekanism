package mekanism.common.tile.qio;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileEntityQIOFilterHandler extends TileEntityQIOComponent implements ITileFilterHolder<QIOFilter<?>> {

    //TODO - 26.2: Do we want to up this to Item.ABSOLUTE_MAX_STACK_SIZE? Even if not, should we switch this to Item.DEFAULT_MAX_STACK_SIZE?
    private static final int BASE_TRANSIT_COUNT = 64;

    @SuppressWarnings({"unchecked", "rawtypes"})
    private final SortableFilterManager<QIOFilter<?>> filterManager = new SortableFilterManager<QIOFilter<?>>((Class) QIOFilter.class, this::markForSave, this::getLevel);
    private int maxTransitCount = BASE_TRANSIT_COUNT;
    private int maxTransitTypes = 1;

    public TileEntityQIOFilterHandler(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public SortableFilterManager<QIOFilter<?>> getFilterManager() {
        return filterManager;
    }

    @Override
    public void recalculateUpgrades(HolderGetter<Upgrade> upgrades, Holder<Upgrade> upgrade, int totalInstalled) {
        super.recalculateUpgrades(upgrades, upgrade, totalInstalled);
        if (upgrade.is(UpgradeIds.SPEED)) {
            // 64 to 320 items
            maxTransitCount = getUpgradeEffect(totalInstalled, BASE_TRANSIT_COUNT);
            // 1 to 5 types
            maxTransitTypes = getUpgradeEffect(totalInstalled, 1);
        }
    }

    private float getUpgradeEffectRaw(int totalInstalled, int base) {
        return base + (base * totalInstalled) / 2F;
    }

    private int getUpgradeEffect(int totalInstalled, int base) {
        return Math.round(getUpgradeEffectRaw(totalInstalled, base));
    }

    @Override
    public List<Component> getUpgradeWindowInfo(Holder<Upgrade> upgrade) {
        if (upgrade.is(UpgradeIds.SPEED)) {
            if (supportsUpgrade(upgrade) && upgrade.value().supportsMultiple()) {
                double effect = getUpgradeEffectRaw(getUpgrades(upgrade), 1);
                return Collections.singletonList(MekanismLang.UPGRADES_EFFECT.translate(Math.round(effect * 100) / 100F));
            }
            return Collections.emptyList();
        }
        return super.getUpgradeWindowInfo(upgrade);
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
