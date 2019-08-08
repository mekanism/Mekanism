package mekanism.client.gui.button;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiColorButton extends Button {

    private final Supplier<EnumColor> colorSupplier;

    public GuiColorButton(int x, int y, int width, int height, Supplier<EnumColor> colorSupplier, IPressable pressable) {
        super(x, y, width, height, "", pressable);
        this.colorSupplier = colorSupplier;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //Ensure the color gets reset. The default GuiButtonImage doesn't so other GuiButton's can have the color leak out of them
            EnumColor color = colorSupplier.get();
            if (color != null) {
                drawRect(this.x, this.y, this.x + this.width, this.y + this.height, MekanismRenderer.getColorARGB(color, 1));
                MekanismRenderer.resetColor();
            }
        }
    }
}