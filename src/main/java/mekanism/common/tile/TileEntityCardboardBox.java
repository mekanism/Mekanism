package mekanism.common.tile;

import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityUpdateable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCardboardBox extends TileEntityUpdateable {

    public TileEntityCardboardBox(BlockPos pos, BlockState state) {
        super(MekanismTileEntityTypes.CARDBOARD_BOX, pos, state);
        syncAttachmentType(MekanismAttachmentTypes.BLOCK_DATA);
    }

    @Override
    public void readFromStack(ItemStack stack) {
        super.readFromStack(stack);
        if (stack.hasData(MekanismAttachmentTypes.BLOCK_DATA)) {
            setData(MekanismAttachmentTypes.BLOCK_DATA, stack.getData(MekanismAttachmentTypes.BLOCK_DATA));
        }
    }

    @Override
    public void writeToStack(ItemStack stack) {
        super.writeToStack(stack);
        if (hasData(MekanismAttachmentTypes.BLOCK_DATA)) {
            stack.setData(MekanismAttachmentTypes.BLOCK_DATA, getData(MekanismAttachmentTypes.BLOCK_DATA));
        }
    }
}