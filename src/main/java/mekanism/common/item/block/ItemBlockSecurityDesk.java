package mekanism.common.item.block;

import javax.annotation.Nonnull;
import mekanism.client.render.item.block.RenderSecurityDeskItem;
import mekanism.common.block.basic.BlockSecurityDesk;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;

public class ItemBlockSecurityDesk extends ItemBlockTooltip<BlockSecurityDesk> {

    public ItemBlockSecurityDesk(BlockSecurityDesk block) {
        super(block, new Item.Properties().setTEISR(() -> RenderSecurityDeskItem::new));
    }

    @Override
    public boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        if (!MekanismUtils.isValidReplaceableBlock(context.getWorld(), context.getPos().up())) {
            //If there is not enough room, fail
            return false;
        }
        return super.placeBlock(context, state);
    }
}