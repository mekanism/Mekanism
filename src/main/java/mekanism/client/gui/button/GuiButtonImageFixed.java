package mekanism.client.gui.button;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonImageFixed extends GuiButtonImage {

    public GuiButtonImageFixed(int id, int x, int y, int width, int height, int offsetX, int offsetY, int hoverOffset, ResourceLocation resource) {
        super(id, x, y, width, height, offsetX, offsetY, hoverOffset, resource);
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //Ensure that the color gets reset before attempting to draw our button
            MekanismRenderer.resetColor();
            super.drawButton(mc, mouseX, mouseY, partialTicks);
        }
    }
}