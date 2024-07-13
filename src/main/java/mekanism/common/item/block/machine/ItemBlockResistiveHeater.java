package mekanism.common.item.block.machine;

import mekanism.common.attachments.containers.energy.ComponentBackedResistiveEnergyContainer;
import mekanism.common.attachments.containers.energy.EnergyContainersBuilder;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class ItemBlockResistiveHeater extends ItemBlockTooltip<BlockTile<?, ?>> {

    public ItemBlockResistiveHeater(BlockTile<?, ?> block, Item.Properties properties) {
        super(block, true, properties
              .component(MekanismDataComponents.ENERGY_USAGE, TileEntityResistiveHeater.BASE_USAGE)
        );
    }

    @Nullable
    @Override
    protected EnergyContainersBuilder addDefaultEnergyContainers(EnergyContainersBuilder builder) {
        return builder.addContainer(ComponentBackedResistiveEnergyContainer::create);
    }
}