package mekanism.client.gui.element.custom.module;

import mekanism.common.content.gear.ModuleConfigItem;
import net.minecraft.client.gui.GuiGraphics;

abstract class MiniElement {

    protected final GuiModuleScreen parent;
    protected final int xPos, yPos, dataIndex;

    protected MiniElement(GuiModuleScreen parent, int xPos, int yPos, int dataIndex) {
        this.parent = parent;
        this.xPos = xPos;
        this.yPos = yPos;
        this.dataIndex = dataIndex;
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

    protected <TYPE> void setData(ModuleConfigItem<TYPE> data, TYPE value) {
        data.set(value, () -> parent.saveCallback.accept(data, dataIndex));
    }
}