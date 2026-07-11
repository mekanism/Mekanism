package mekanism.client.recipe_viewer.emi;

import dev.emi.emi.api.EmiExclusionArea;
import dev.emi.emi.api.widget.Bounds;
import java.util.function.Consumer;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.recipe_viewer.GuiElementHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

public class EmiExclusionHandler implements EmiExclusionArea<Screen> {

    @Override
    public void addExclusionArea(Screen screen, Consumer<Bounds> consumer) {
        if (screen instanceof GuiMekanism<?> gui) {
            for (Rect2i extraArea : GuiElementHandler.getGuiExtraAreas(gui)) {
                consumer.accept(new Bounds(extraArea.getX(), extraArea.getY(), extraArea.getWidth(), extraArea.getHeight()));
            }
        }
    }
}