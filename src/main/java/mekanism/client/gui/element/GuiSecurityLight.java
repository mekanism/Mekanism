package mekanism.client.gui.element;

import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiSecurityLight extends GuiTexturedElement {

    private final Supplier<SecurityLight> lightSupplier;

    public GuiSecurityLight(IGuiWrapper gui, int x, int y, Supplier<SecurityLight> lightSupplier) {
        super(GuiInnerScreen.SCREEN, gui, x, y, 8, 8);
        this.lightSupplier = lightSupplier;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, lightSupplier.get().texture(), relativeX + 1, relativeY + 1, width - 2, height - 2);
    }

    public enum SecurityLight {
        ENABLED("enabled"),
        DISABLED("disabled"),
        NOT_APPLICABLE("not_applicable");

        private final Identifier texture;

        SecurityLight(String texture) {
            this.texture = Mekanism.rl("light/security/" + texture);
        }

        public Identifier texture() {
            return texture;
        }
    }
}