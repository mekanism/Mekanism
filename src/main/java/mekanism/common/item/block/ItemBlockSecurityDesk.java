package mekanism.common.item.block;

import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import mekanism.client.render.item.block.RenderSecurityDeskItem;
import mekanism.common.block.basic.BlockSecurityDesk;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockSecurityDesk extends ItemBlockTooltip<BlockSecurityDesk> {

    public ItemBlockSecurityDesk(BlockSecurityDesk block) {
        super(block, ItemDeferredRegister.getMekBaseProperties().setISTER(() -> getISTER()));
    }

    @OnlyIn(Dist.CLIENT)
    private static Callable<ItemStackTileEntityRenderer> getISTER() {
        //NOTE: This extra method is needed to avoid classloading issues on servers
        return RenderSecurityDeskItem::new;
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