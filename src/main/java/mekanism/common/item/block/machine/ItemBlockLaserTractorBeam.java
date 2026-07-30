package mekanism.common.item.block.machine;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;

public class ItemBlockLaserTractorBeam extends ItemBlockTooltip<BlockTile<?, ?>> {

    public ItemBlockLaserTractorBeam(BlockTile<?, ?> block, Item.Properties properties) {
        super(block, properties.component(MekanismDataComponents.DETAILS, Unit.INSTANCE));
    }

    @Override
    protected Predicate<AutomationType> getEnergyCapInsertPredicate() {
        //Don't allow charging laser tractor beams inside of energy storage devices
        return BasicEnergyContainer.manualOnly;
    }

    @Override
    protected boolean exposesEnergyCap() {
        //Don't allow charging laser amplifiers inside of energy storage devices
        return false;
    }
}