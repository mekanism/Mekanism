package mekanism.client.gui.element.tab;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.element.GuiWindow;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.util.ResourceLocation;

public abstract class GuiWindowCreatorTab<ELEMENT extends GuiWindowCreatorTab<ELEMENT>> extends GuiInsetElement<TileEntityMekanism> {

    @Nonnull
    private final Supplier<ELEMENT> elementSupplier;

    public GuiWindowCreatorTab(ResourceLocation overlay, IGuiWrapper gui, TileEntityMekanism tile, int x, int y, int height, int innerSize, boolean left,
          @Nonnull Supplier<ELEMENT> elementSupplier) {
        super(overlay, gui, tile, x, y, height, innerSize, left);
        this.elementSupplier = elementSupplier;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        GuiWindow window = createWindow();
        window.setListenerTab(elementSupplier);
        active = false;
        guiObj.addWindow(window);
    }

    public abstract GuiWindow createWindow();
}
