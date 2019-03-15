package mekanism.client.render.item.machine;

import mekanism.client.model.ModelDigitalMiner;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderDigitalMinerItem {

    private static ModelDigitalMiner digitalMiner = new ModelDigitalMiner();

    public static void renderStack() {
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(0.35F, 0.1F, 0.0F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "DigitalMiner.png"));
        digitalMiner.render(0.022F, false, Minecraft.getMinecraft().renderEngine, true);
    }
}