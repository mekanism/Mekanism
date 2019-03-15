package mekanism.generators.client.render.item;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelWindGenerator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderWindGeneratorItem {

    private static ModelWindGenerator windGenerator = new ModelWindGenerator();

    public static void renderStack() {
        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.4F, 0.0F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "WindGenerator.png"));
        windGenerator.render(0.016F, 0);
    }
}