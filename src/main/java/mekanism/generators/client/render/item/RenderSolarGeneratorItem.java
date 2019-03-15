package mekanism.generators.client.render.item;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelSolarGenerator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderSolarGeneratorItem {

    private static ModelSolarGenerator solarGenerator = new ModelSolarGenerator();

    public static void renderStack() {
        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(90F, 0.0F, -1.0F, 0.0F);
        GL11.glTranslated(0.0F, -1.0F, 0.0F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SolarGenerator.png"));
        solarGenerator.render(0.0625F);
    }
}