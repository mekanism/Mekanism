package mekanism.client.render;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.common.item.interfaces.IRadialModeItem.IRadialSelectorEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public class RadialSelectorRenderer {

    private static final float DRAWS = 300;

    private static final float INNER = 40, OUTER = 100;
    private static final float SELECT_RADIUS = 10;

    private final Minecraft minecraft = Minecraft.getInstance();
    private final Class<? extends IRadialSelectorEnum> enumClass;
    private final IRadialSelectorEnum[] types;
    private final Supplier<IRadialSelectorEnum> curSupplier;
    private final Consumer<IRadialSelectorEnum> changeHandler;

    private IRadialSelectorEnum selection = null;

    public RadialSelectorRenderer(Class<? extends IRadialSelectorEnum> enumClass, Supplier<IRadialSelectorEnum> curSupplier, Consumer<IRadialSelectorEnum> changeHandler) {
        this.enumClass = enumClass;
        this.curSupplier = curSupplier;
        this.changeHandler = changeHandler;
        types = enumClass.getEnumConstants();
    }

    public void render(float partialTick) {
        // center of screen
        float centerX = minecraft.getMainWindow().getScaledWidth() / 2F;
        float centerY = minecraft.getMainWindow().getScaledHeight() / 2F;
        // scaled mouse position
        double mouseX = (minecraft.mouseHelper.getMouseX() * minecraft.getMainWindow().getScaledWidth() / minecraft.getMainWindow().getWidth());
        double mouseY = (minecraft.mouseHelper.getMouseY() * minecraft.getMainWindow().getScaledHeight() / minecraft.getMainWindow().getHeight());

        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.translatef(centerX, centerY, 0);

        // draw base
        RenderSystem.color4f(0.3F, 0.3F, 0.3F, 0.5F);
        drawTorus(0, 360);

        IRadialSelectorEnum cur = curSupplier.get();
        if (cur != null) {
            // draw current selected
            if (cur.getColor() == null) {
                RenderSystem.color4f(0.4F, 0.4F, 0.4F, 0.7F);
            } else {
                MekanismRenderer.color(cur.getColor(), 0.3F);
            }
            drawTorus(-90F + 360F * (-0.5F + cur.ordinal()) / types.length, 360F / types.length);

            double xDiff = mouseX - centerX;
            double yDiff = mouseY - centerY;
            if (Math.sqrt(xDiff * xDiff + yDiff * yDiff) >= SELECT_RADIUS) {
                // draw mouse selection highlight
                float angle = (float) Math.toDegrees(Math.atan2(yDiff, xDiff));
                RenderSystem.color4f(0.8F, 0.8F, 0.8F, 0.3F);
                drawTorus(360F * (-0.5F / types.length) + angle, 360F / types.length);

                float selectionAngle = angle + 90F + (360F * (0.5F / types.length));
                while (selectionAngle < 0)
                    selectionAngle += 360F;
                selection = types[(int) (selectionAngle * (types.length / 360F))];
                // draw hovered selection
                RenderSystem.color4f(0.6F, 0.6F, 0.6F, 0.7F);
                drawTorus(-90F + 360F * (-0.5F + selection.ordinal()) / types.length, 360F / types.length);
            } else {
                selection = null;
            }
        }

        MekanismRenderer.resetColor();

        for (int i = 0; i < types.length; i++) {
            double angle = Math.toRadians(270 + 360 * ((float) i / types.length));
            float x = (float) Math.cos(angle) * (INNER + OUTER) / 2F;
            float y = (float) Math.sin(angle) * (INNER + OUTER) / 2F;
            // draw icon
            minecraft.textureManager.bindTexture(types[i].getIcon());
            AbstractGui.blit(Math.round(x - 12), Math.round(y - 20), 24, 24, 0, 0, 18, 18, 18, 18);
            // draw label
            RenderSystem.pushMatrix();
            int width = minecraft.fontRenderer.getStringWidth(types[i].getShortText().getString());
            RenderSystem.translatef(x, y, 0);
            RenderSystem.scalef(0.6F, 0.6F, 0.6F);
            minecraft.fontRenderer.drawString(types[i].getShortText().getFormattedText(), -width / 2F, 8, 0xCCFFFFFF);
            RenderSystem.popMatrix();
        }

        MekanismRenderer.resetColor();
        RenderSystem.popMatrix();
    }

    private void drawTorus(float startAngle, float sizeAngle) {
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        float draws = DRAWS * (sizeAngle / 360F);
        for(int i = 0; i <= draws; i++) {
            double angle = Math.toRadians(startAngle + (i / DRAWS) * 360);
            GL11.glVertex2d(INNER * Math.cos(angle), INNER * Math.sin(angle));
            GL11.glVertex2d(OUTER * Math.cos(angle), OUTER * Math.sin(angle));
        }
        GL11.glEnd();
    }

    public void updateSelection() {
        if (selection != null) {
            changeHandler.accept(selection);
            selection = null;
        }
    }

    public Class<? extends IRadialSelectorEnum> getEnumClass() {
        return enumClass;
    }
}
