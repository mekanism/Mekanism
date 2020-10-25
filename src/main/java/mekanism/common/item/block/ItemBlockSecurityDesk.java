package mekanism.common.item.block;

import javax.annotation.Nonnull;
import mekanism.common.block.basic.BlockSecurityDesk;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;

public class ItemBlockSecurityDesk extends ItemBlockTooltip<BlockSecurityDesk> {

    public ItemBlockSecurityDesk(BlockSecurityDesk block) {
        super(block, ItemDeferredRegister.getMekBaseProperties());
    }

    @Override
    public boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        if (!WorldUtils.isValidReplaceableBlock(context.getWorld(), context.getPos().up())) {
            //If there is not enough room, fail
            return false;
        }
        return super.placeBlock(context, state);
    }
}