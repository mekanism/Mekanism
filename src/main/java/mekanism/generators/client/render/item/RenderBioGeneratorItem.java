package mekanism.generators.client.render.item;

import javax.annotation.Nonnull;
import mekanism.client.render.GLSMHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelBioGenerator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBioGeneratorItem {

    private static ModelBioGenerator bioGenerator = new ModelBioGenerator();

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType) {
        GLSMHelper.INSTANCE.rotateZ(180, 1).translateY(-1.0F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "BioGenerator.png"));
        bioGenerator.render(0.0625F);
    }
}