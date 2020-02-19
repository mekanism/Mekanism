package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiInnerScreen extends GuiTexturedElement {

    private static final ResourceLocation SCREEN = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "inner_screen.png");
    private static final int textureDimensions = 5;
    private static final int sideWidth = 2;
    private static final int sideHeight = 2;

    private final int centerWidth;
    private final int centerHeight;
    private final int leftEdgeEnd;
    private final int rightEdgeStart;
    private final int topEdgeEnd;
    private final int bottomEdgeStart;

    public GuiInnerScreen(IGuiWrapper gui, int x, int y, int width, int height) {
        super(SCREEN, gui, x, y, width, height);
        if (width < 4 || height < 4) {
            Mekanism.logger.warn("Inner screen was too small, must be at least 4 by 4.");
            //TODO: Should we somehow exit early
        }
        centerWidth = this.width - 2 * sideWidth;
        centerHeight = this.height - 2 * sideHeight;
        leftEdgeEnd = this.x + sideHeight;
        rightEdgeStart = leftEdgeEnd + centerWidth;
        topEdgeEnd = this.y + sideWidth;
        bottomEdgeStart = topEdgeEnd + centerHeight;
        active = false;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        //TODO: Can some of this code that also exists in GuiMekanism be moved to some util class or something
        minecraft.textureManager.bindTexture(getResource());
        //Left Side
        //Top Left Corner (2x2)
        blit(x, y, 0, 0, sideWidth, sideHeight, textureDimensions, textureDimensions);
        //Left Middle (2x1)
        if (centerHeight > 0) {
            blit(x, topEdgeEnd, sideWidth, centerHeight, 0, 2, sideWidth, 1, textureDimensions, textureDimensions);
        }
        //Bottom Left Corner (2x2)
        blit(x, bottomEdgeStart, 0, 3, sideWidth, sideHeight, textureDimensions, textureDimensions);

        //Middle
        if (centerWidth > 0) {
            //Top Middle (1x2)
            blit(leftEdgeEnd, y, centerWidth, sideHeight, 2, 0, 1, sideHeight, textureDimensions, textureDimensions);
            if (centerHeight > 0) {
                //Center
                blit(leftEdgeEnd, topEdgeEnd, centerWidth, centerHeight, 2, 2, 1, 1, textureDimensions, textureDimensions);
            }
            //Bottom Middle (1x2)
            blit(leftEdgeEnd, bottomEdgeStart, centerWidth, sideHeight, 2, 3, 1, sideHeight, textureDimensions, textureDimensions);
        }

        //Right side
        //Top Right Corner (2x2)
        blit(rightEdgeStart, y, 3, 0, sideWidth, sideHeight, textureDimensions, textureDimensions);
        //Right Middle (2x1)
        if (centerHeight > 0) {
            blit(rightEdgeStart, topEdgeEnd, sideWidth, centerHeight, 3, 2, sideWidth, 1, textureDimensions, textureDimensions);
        }
        //Bottom Right Corner (2x2)
        blit(rightEdgeStart, bottomEdgeStart, 3, 3, sideWidth, sideHeight, textureDimensions, textureDimensions);
    }
}