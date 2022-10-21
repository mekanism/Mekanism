package mekanism.client.gui.element.custom.module;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.common.content.gear.ModuleConfigItem;

abstract class MiniElement {

    protected final GuiModuleScreen parent;
    protected final int xPos, yPos, dataIndex;

    protected MiniElement(GuiModuleScreen parent, int xPos, int yPos, int dataIndex) {
        this.parent = parent;
        this.xPos = xPos;
        this.yPos = yPos;
        this.dataIndex = dataIndex;
    }

    protected abstract void renderBackground(PoseStack matrix, int mouseX, int mouseY);

    protected abstract void renderForeground(PoseStack matrix, int mouseX, int mouseY);

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
        return parent.x + xPos;
    }

    protected int getY() {
        return parent.y + yPos;
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