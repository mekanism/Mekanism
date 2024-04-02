package mekanism.common.tile;

import java.util.Optional;
import mekanism.common.attachments.BlockData;
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
        Optional<BlockData> existingData = stack.getExistingData(MekanismAttachmentTypes.BLOCK_DATA);
        //noinspection OptionalIsPresent - Capturing lambda
        if (existingData.isPresent()) {
            setData(MekanismAttachmentTypes.BLOCK_DATA, existingData.get());
        }
    }

    @Override
    public void writeToStack(ItemStack stack) {
        super.writeToStack(stack);
        Optional<BlockData> existingData = getExistingData(MekanismAttachmentTypes.BLOCK_DATA);
        //noinspection OptionalIsPresent - Capturing lambda
        if (existingData.isPresent()) {
            stack.setData(MekanismAttachmentTypes.BLOCK_DATA, existingData.get());
        }
    }
}