package mekanism.common.item.block;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import org.jetbrains.annotations.NotNull;

public class ItemBlockLaserAmplifier extends ItemBlockTooltip<BlockTileModel<TileEntityLaserAmplifier, BlockTypeTile<TileEntityLaserAmplifier>>> {

    public ItemBlockLaserAmplifier(BlockTileModel<TileEntityLaserAmplifier, BlockTypeTile<TileEntityLaserAmplifier>> block) {
        super(block);
    }

    @Override
    protected Predicate<@NotNull AutomationType> getEnergyCapInsertPredicate() {
        //Don't allow charging laser amplifiers inside of energy storage devices
        return BasicEnergyContainer.manualOnly;
    }
}