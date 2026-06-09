package mekanism.common.item.block.machine;

import mekanism.api.energy.IEnergyContainer;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.component.containers.creator.IContainerCreator;
import mekanism.common.component.containers.energy.EnergyContainersBuilder;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import net.minecraft.world.item.Item;

public class ItemBlockResistiveHeater extends ItemBlockTooltip<BlockTile<?, ?>> {

    public ItemBlockResistiveHeater(BlockTile<?, ?> block, Item.Properties properties) {
        super(block, true, properties
              .component(MekanismDataComponents.ENERGY_USAGE, TileEntityResistiveHeater.BASE_USAGE)
        );
    }

    @Override
    protected IContainerCreator<IEnergyContainer, Long> getDefaultEnergyContainer() {
        return EnergyContainersBuilder.RESISTIVE_HEATER;
    }
}