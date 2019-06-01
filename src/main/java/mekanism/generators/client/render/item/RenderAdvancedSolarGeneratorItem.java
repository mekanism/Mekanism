package mekanism.generators.client.render.item;

import javax.annotation.Nonnull;
import mekanism.client.render.GLSMHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelAdvancedSolarGenerator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderAdvancedSolarGeneratorItem {

    private static ModelAdvancedSolarGenerator advancedSolarGenerator = new ModelAdvancedSolarGenerator();

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType) {
        GLSMHelper.INSTANCE.rotateZ(180, 1).rotateY(90, 1).translateY(0.2F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "AdvancedSolarGenerator.png"));
        advancedSolarGenerator.render(0.022F);
    }
}