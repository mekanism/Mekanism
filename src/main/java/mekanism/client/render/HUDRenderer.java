package mekanism.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.common.MekanismLang;
import mekanism.common.lib.Color;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class HUDRenderer {

    private static final ResourceLocation COMPASS = MekanismUtils.getResource(ResourceType.GUI, "compass.png");
    private static final Color color = Color.rgbad(0.25, 0.96, 0.94, 0.3);

    private long lastTick = -1;
    private float prevRotationPitch; // must track manually, for some reason it's already synced in the entity

    private Minecraft minecraft = Minecraft.getInstance();

    public void renderHUD(float partialTick) {
        checkTime();

        RenderSystem.pushMatrix();
        float yawJitter = -(minecraft.player.rotationYawHead - minecraft.player.prevRotationYawHead) * 0.25F;
        float pitchJitter = -(minecraft.player.rotationPitch - prevRotationPitch) * 0.25F;
        RenderSystem.translated(yawJitter, pitchJitter, 0);
        int posX = 25;
        int posY = minecraft.getMainWindow().getScaledHeight() - 100;
        RenderSystem.translatef(posX + 50, posY + 50, 0);
        renderCompass(partialTick);
        RenderSystem.popMatrix();

        update();
    }

    private void checkTime() {
        if (lastTick == -1 || minecraft.world.getGameTime() - lastTick > 1) {
            update();
        }
    }

    private void update() {
        if (lastTick < minecraft.world.getGameTime()) {
            lastTick = minecraft.world.getGameTime();
            prevRotationPitch = minecraft.player.rotationPitch;
        }
    }

    private void renderCompass(float partialTick) {
        RenderSystem.pushMatrix();
        float angle = 180 - MathHelper.lerp(partialTick, minecraft.player.prevRotationYawHead, minecraft.player.rotationYawHead);
        RenderSystem.pushMatrix();
        RenderSystem.scaled(0.7, 0.7, 0.7);
        String coords = MekanismLang.GENERIC_BLOCK_POS.translate((int) minecraft.player.getPosX(), (int) minecraft.player.getPosY(), (int) minecraft.player.getPosZ()).getString();
        minecraft.fontRenderer.drawString(coords, -minecraft.fontRenderer.getStringWidth(coords) / 2F, -4, color.argb());
        RenderSystem.popMatrix();
        RenderSystem.rotatef(-60, 1, 0, 0);
        RenderSystem.rotatef(angle, 0, 0, 1);
        minecraft.getTextureManager().bindTexture(COMPASS);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        MekanismRenderer.color(color);
        AbstractGui.blit(-50, -50, 100, 100, 0, 0, 256, 256, 256, 256);
        rotateStr("N", angle, 0, color);
        rotateStr("E", angle, 90, color);
        rotateStr("S", angle, 180, color);
        rotateStr("W", angle, 270, color);
        MekanismRenderer.resetColor();
        RenderSystem.popMatrix();
    }

    private void rotateStr(String s, float rotation, float shift, Color color) {
        RenderSystem.pushMatrix();
        RenderSystem.rotatef(shift, 0, 0, 1);
        RenderSystem.translatef(0, -50, 0);
        RenderSystem.rotatef(-rotation - shift, 0, 0, 1);
        minecraft.fontRenderer.drawString(s, -2.5F, -4, color.argb());
        RenderSystem.popMatrix();
    }
}
