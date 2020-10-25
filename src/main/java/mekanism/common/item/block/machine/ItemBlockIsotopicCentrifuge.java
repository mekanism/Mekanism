package mekanism.common.item.block.machine;

import javax.annotation.Nonnull;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.tile.machine.TileEntityIsotopicCentrifuge;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;

public class ItemBlockIsotopicCentrifuge extends ItemBlockMachine {

    public ItemBlockIsotopicCentrifuge(BlockTile<TileEntityIsotopicCentrifuge, Machine<TileEntityIsotopicCentrifuge>> block) {
        super(block);
    }

    @Override
    public boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        if (!WorldUtils.isValidReplaceableBlock(context.getWorld(), context.getPos().up())) {
            //If there isn't room then fail
            return false;
        }
        return super.placeBlock(context, state);
    }
}
