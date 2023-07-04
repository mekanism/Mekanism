package mekanism.client.gui.element;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.lib.Color;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;

public class GuiElementHolder extends GuiScalableElement {

    public static final ResourceLocation HOLDER = MekanismUtils.getResource(ResourceType.GUI, "element_holder.png");
    public static final int HOLDER_SIZE = 32;
    private static int BACKGROUND_COLOR = 0xFF787878;

    public GuiElementHolder(IGuiWrapper gui, int x, int y, int width, int height) {
        super(HOLDER, gui, x, y, width, height, HOLDER_SIZE, HOLDER_SIZE);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackgroundTexture(guiGraphics, getResource(), sideWidth, sideHeight);
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    }

    public static int getBackgroundColor() {
        return BACKGROUND_COLOR;
    }

    public static void updateBackgroundColor() {
        //TODO: Try to do this in a more generic way. We don't directly use our ColorAtlas because we want to automatically
        // get it from the texture
        try (InputStream stream = Minecraft.getInstance().getResourceManager().open(HOLDER);
             NativeImage image = NativeImage.read(stream)) {
            int argb = Color.argbToFromABGR(image.getPixelRGBA(HOLDER_SIZE + 1, HOLDER_SIZE + 1));
            if (FastColor.ARGB32.alpha(argb) == 0) {
                //Don't allow fully transparent colors, fallback to default color.
                // Mark as null for now so that it can default to the proper color
                argb = 0xFF787878;
                Mekanism.logger.warn("Unable to retrieve background color for element holder.");
            }
            BACKGROUND_COLOR = argb;
        } catch (Exception e) {
            Mekanism.logger.error("Failed to retrieve background color for element holder", e);
        }
    }
}