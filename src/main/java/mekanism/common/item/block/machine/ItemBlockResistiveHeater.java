package mekanism.common.item.block.machine;

import mekanism.common.attachments.containers.energy.ComponentBackedResistiveEnergyContainer;
import mekanism.common.attachments.containers.energy.EnergyContainersBuilder;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.ItemBlockTooltip;
import org.jetbrains.annotations.Nullable;

public class ItemBlockResistiveHeater extends ItemBlockTooltip<BlockTile<?, ?>> {

    public ItemBlockResistiveHeater(BlockTile<?, ?> block) {
        super(block);
    }

    @Nullable
    @Override
    protected EnergyContainersBuilder addDefaultEnergyContainers(EnergyContainersBuilder builder) {
        return builder.addContainer(ComponentBackedResistiveEnergyContainer::create);
    }
}