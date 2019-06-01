package mekanism.generators.client.render.item;

import javax.annotation.Nonnull;
import mekanism.client.render.GLSMHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelGasGenerator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGasGeneratorItem {

    private static ModelGasGenerator gasGenerator = new ModelGasGenerator();

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType) {
        GLSMHelper.INSTANCE.rotateYZ(180, 1, 1).rotateX(90, -1).translateY(-1.0F).rotateY(180, 1);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "GasGenerator.png"));
        gasGenerator.render(0.0625F);
    }
}