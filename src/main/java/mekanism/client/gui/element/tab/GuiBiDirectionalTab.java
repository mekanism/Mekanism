package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public abstract class GuiBiDirectionalTab extends GuiTexturedElement {

    protected GuiBiDirectionalTab(ResourceLocation resource, IGuiWrapper gui, int x, int y, int width, int height) {
        super(resource, gui, x, y, width, height);
    }

    protected abstract void onRightClick(double mouseX, double mouseY);

    @Override
    public boolean func_231044_a_(double mouseX, double mouseY, int button) {
        if (super.func_231044_a_(mouseX, mouseY, button)) {
            return true;
        }
        //TODO: We may want to eventually move this logic into GuiElement as it is shared by GuiButton
        if (this.field_230693_o_ && this.field_230694_p_ && func_230449_g_()) {
            if (button == 1) {
                //Right clicked
                func_230988_a_(Minecraft.getInstance().getSoundHandler());
                onRightClick(mouseX, mouseY);
                return true;
            }
        }
        return false;
    }
}