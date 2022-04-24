package mekanism.client.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;

public record NOOPDrawable(int width, int height) implements IDrawable {

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void draw(PoseStack matrix, int xOffset, int yOffset) {
        //NO-OP
    }
}