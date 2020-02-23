package mekanism.client.gui;

import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;

public class GuiUtils {

    public static void renderExtendedTexture(ResourceLocation resource, int sideWidth, int sideHeight, int left, int top, int width, int height) {
        //TODO: Do we want to add in some validation here about dimensions
        int textureWidth = 2 * sideWidth + 1;
        int textureHeight = 2 * sideHeight + 1;
        int centerWidth = width - 2 * sideWidth;
        int centerHeight = height - 2 * sideHeight;
        int leftEdgeEnd = left + sideHeight;
        int rightEdgeStart = leftEdgeEnd + centerWidth;
        int topEdgeEnd = top + sideWidth;
        int bottomEdgeStart = topEdgeEnd + centerHeight;
        MekanismRenderer.bindTexture(resource);
        //Left Side
        //Top Left Corner
        AbstractGui.blit(left, top, 0, 0, sideWidth, sideHeight, textureWidth, textureHeight);
        //Left Middle
        if (centerHeight > 0) {
            AbstractGui.blit(left, topEdgeEnd, sideWidth, centerHeight, 0, sideHeight, sideWidth, 1, textureWidth, textureHeight);
        }
        //Bottom Left Corner
        AbstractGui.blit(left, bottomEdgeStart, 0, sideHeight + 1, sideWidth, sideHeight, textureWidth, textureHeight);

        //Middle
        if (centerWidth > 0) {
            //Top Middle
            AbstractGui.blit(leftEdgeEnd, top, centerWidth, sideHeight, sideWidth, 0, 1, sideHeight, textureWidth, textureHeight);
            if (centerHeight > 0) {
                //Center
                AbstractGui.blit(leftEdgeEnd, topEdgeEnd, centerWidth, centerHeight, sideWidth, sideHeight, 1, 1, textureWidth, textureHeight);
            }
            //Bottom Middle
            AbstractGui.blit(leftEdgeEnd, bottomEdgeStart, centerWidth, sideHeight, sideWidth, sideHeight + 1, 1, sideHeight, textureWidth, textureHeight);
        }

        //Right side
        //Top Right Corner
        AbstractGui.blit(rightEdgeStart, top, sideWidth + 1, 0, sideWidth, sideHeight, textureWidth, textureHeight);
        //Right Middle
        if (centerHeight > 0) {
            AbstractGui.blit(rightEdgeStart, topEdgeEnd, sideWidth, centerHeight, sideWidth + 1, sideHeight, sideWidth, 1, textureWidth, textureHeight);
        }
        //Bottom Right Corner
        AbstractGui.blit(rightEdgeStart, bottomEdgeStart, sideWidth + 1, sideHeight + 1, sideWidth, sideHeight, textureWidth, textureHeight);
    }
}