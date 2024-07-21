package mekanism.common.item.block;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.laser.TileEntityLaserAmplifier.RedstoneOutput;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class ItemBlockLaserAmplifier extends ItemBlockTooltip<BlockTileModel<TileEntityLaserAmplifier, BlockTypeTile<TileEntityLaserAmplifier>>> {

    public ItemBlockLaserAmplifier(BlockTileModel<TileEntityLaserAmplifier, BlockTypeTile<TileEntityLaserAmplifier>> block, Item.Properties properties) {
        super(block, true, properties
              .component(MekanismDataComponents.REDSTONE_OUTPUT, RedstoneOutput.OFF)
              .component(MekanismDataComponents.DELAY, 0)
              .component(MekanismDataComponents.MIN_THRESHOLD, 0L)
        );
    }

    @Override
    protected Predicate<@NotNull AutomationType> getEnergyCapInsertPredicate() {
        //Don't allow charging laser amplifiers inside of energy storage devices
        return BasicEnergyContainer.manualOnly;
    }

    @Override
    protected boolean exposesEnergyCap() {
        //Don't allow charging laser amplifiers inside of energy storage devices
        return false;
    }
}