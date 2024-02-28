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
        stack.getExistingData(MekanismAttachmentTypes.BLOCK_DATA).ifPresent(storedData -> setData(MekanismAttachmentTypes.BLOCK_DATA, storedData));
    }

    @Override
    public void writeToStack(ItemStack stack) {
        super.writeToStack(stack);
        getExistingData(MekanismAttachmentTypes.BLOCK_DATA).ifPresent(storedData -> stack.setData(MekanismAttachmentTypes.BLOCK_DATA, storedData));
    }
}