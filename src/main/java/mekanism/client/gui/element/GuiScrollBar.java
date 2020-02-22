package mekanism.client.gui.element;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiScrollBar extends GuiTexturedElement {

    private static final ResourceLocation BAR = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "scroll_bar.png");
    private static final int TEXTURE_WIDTH = 24;
    private static final int TEXTURE_HEIGHT = 15;

    private final GuiElementHolder holder;
    private final BooleanSupplier canScrollSupplier;
    private final DoubleSupplier scrollGetter;
    private final DoubleConsumer scrollSetter;
    private int dragOffset;

    public GuiScrollBar(IGuiWrapper gui, int x, int y, int height, BooleanSupplier canScrollSupplier, DoubleSupplier scrollGetter, DoubleConsumer scrollSetter) {
        super(BAR, gui, x, y, 14, height);
        holder = new GuiElementHolder(gui, x, y, 14, height);
        this.canScrollSupplier = canScrollSupplier;
        this.scrollGetter = scrollGetter;
        this.scrollSetter = scrollSetter;
        //Note: scroll wheel is handled by the class that adds the scroll bar
        // This is because it is a large scroll bar and makes more sense to have it be able to be used with the scroll wheel
        // from anywhere on the gui
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        //Draw Black and border
        holder.renderButton(mouseX, mouseY, partialTicks);
        minecraft.textureManager.bindTexture(getResource());
        blit(x + 1, y + 1 + getScroll(), canScrollSupplier.getAsBoolean() ? 0 : 12, 0, 12, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int scroll = getScroll();
        int yStart = y + 1 + scroll;
        if (mouseX >= x + 1 && mouseX <= x + 13 && mouseY >= yStart && mouseY <= yStart + TEXTURE_HEIGHT) {
            if (canScrollSupplier.getAsBoolean()) {
                double yAxis = mouseY - guiObj.getTop();
                dragOffset = (int) (yAxis - (scroll + 18));
            } else {
                scrollSetter.accept(0);
            }
        }
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double mouseXOld, double mouseYOld) {
        if (canScrollSupplier.getAsBoolean()) {
            double yAxis = mouseY - guiObj.getTop();
            scrollSetter.accept(Math.min(Math.max((yAxis - 18 - dragOffset) / (float) getMax(), 0), 1));
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        dragOffset = 0;
    }

    private int getMax() {
        return height - 2 - TEXTURE_HEIGHT;
    }

    private int getScroll() {
        //Calculate thumb position along scrollbar
        int max = getMax();
        return Math.max(Math.min((int) (scrollGetter.getAsDouble() * max), max), 0);
    }
}