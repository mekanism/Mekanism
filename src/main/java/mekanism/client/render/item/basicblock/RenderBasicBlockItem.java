package mekanism.client.render.item.basicblock;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.SubTypeItemRenderer;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBasicBlockItem extends SubTypeItemRenderer<BasicBlockType> {

    public static Map<BasicBlockType, ItemLayerWrapper> modelMap = new HashMap<>();

    @Override
    protected boolean earlyExit() {
        return true;
    }

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        BasicBlockType basicType = BasicBlockType.get(stack);

        if (basicType != null) {
            if (basicType == BasicBlockType.BIN) {
                RenderBinItem.renderStack(stack, transformType);
            } else if (basicType == BasicBlockType.SECURITY_DESK) {
                RenderSecurityDeskItem.renderStack(stack, transformType);
            }
        }
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {

    }

    @Nullable
    @Override
    protected ItemLayerWrapper getModel(BasicBlockType basicBlockType) {
        return modelMap.get(basicBlockType);
    }

    @Nullable
    @Override
    protected BasicBlockType getType(@Nonnull ItemStack stack) {
        return BasicBlockType.get(stack);
    }
}