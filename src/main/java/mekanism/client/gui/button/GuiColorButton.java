package mekanism.client.gui.button;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiColorButton extends GuiButton {

    private final Supplier<EnumColor> colorSupplier;

    public GuiColorButton(int id, int x, int y, int width, int height, Supplier<EnumColor> colorSupplier) {
        super(id, x, y, width, height, "");
        this.colorSupplier = colorSupplier;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //Ensure the color gets reset. The default GuiButtonImage doesn't so other GuiButton's can have the color leak out of them
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            EnumColor color = colorSupplier.get();
            if (color != null) {
                drawRect(this.x, this.y, this.x + this.width, this.y + this.height, MekanismRenderer.getColorARGB(color, 1));
                MekanismRenderer.resetColor();
            }
        }
    }
}