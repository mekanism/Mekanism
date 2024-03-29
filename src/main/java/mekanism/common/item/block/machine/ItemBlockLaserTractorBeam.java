package mekanism.common.item.block.machine;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.item.block.ItemBlockTooltip;
import org.jetbrains.annotations.NotNull;

public class ItemBlockLaserTractorBeam extends ItemBlockTooltip<BlockTile<?, ?>> {

    public ItemBlockLaserTractorBeam(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    protected Predicate<@NotNull AutomationType> getEnergyCapInsertPredicate() {
        //Don't allow charging laser tractor beams inside of energy storage devices
        return BasicEnergyContainer.manualOnly;
    }
}