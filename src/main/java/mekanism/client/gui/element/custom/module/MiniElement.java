package mekanism.client.gui.element.custom.module;

import mekanism.api.gear.config.ModuleConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

abstract class MiniElement<TYPE> {

    protected final GuiModuleScreen parent;
    protected final Component description;
    protected final int xPos, yPos;
    protected ModuleConfig<TYPE> data;

    protected MiniElement(GuiModuleScreen parent, ModuleConfig<TYPE> data, Component description, int xPos, int yPos) {
        this.parent = parent;
        this.data = data;
        this.description = description;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    protected abstract void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY);

    protected abstract void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY);

    protected abstract void click(double mouseX, double mouseY);

    protected void release(double mouseX, double mouseY) {
    }

    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
    }

    protected abstract int getNeededHeight();

    protected int getRelativeX() {
        return parent.getRelativeX() + xPos;
    }

    protected int getRelativeY() {
        return parent.getRelativeY() + yPos;
    }

    protected int getX() {
        return parent.getX() + xPos;
    }

    protected int getY() {
        return parent.getY() + yPos;
    }

    protected boolean mouseOver(double mouseX, double mouseY, int relativeX, int relativeY, int width, int height) {
        int x = getX();
        int y = getY();
        return mouseX >= x + relativeX && mouseX < x + relativeX + width && mouseY >= y + relativeY && mouseY < y + relativeY + height;
    }

    protected void setData(TYPE value) {
        //TODO - 1.21: Fix it so that after it syncs to the server we then actually update what our data value is
        // We sort of do this now, but options that have side effects don't necessarily get properly updated
        this.data = data.with(value);
        parent.saveCallback.accept(data);
    }
}