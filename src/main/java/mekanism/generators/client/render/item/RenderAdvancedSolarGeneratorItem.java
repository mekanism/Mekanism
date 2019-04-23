package mekanism.generators.client.render.item;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelAdvancedSolarGenerator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderAdvancedSolarGeneratorItem {

    private static ModelAdvancedSolarGenerator advancedSolarGenerator = new ModelAdvancedSolarGenerator();

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType) {
        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(90F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.2F, 0.0F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "AdvancedSolarGenerator.png"));
        advancedSolarGenerator.render(0.022F);
    }
}