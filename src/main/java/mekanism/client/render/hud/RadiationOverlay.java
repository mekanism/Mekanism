package mekanism.client.render.hud;

import mekanism.api.radiation.IRadiationManager;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class RadiationOverlay implements LayeredDraw.Layer {

    public static final RadiationOverlay INSTANCE = new RadiationOverlay();

    private double prevRadiation = 0;
    private long lastTick;

    private RadiationOverlay() {
    }

    public void resetRadiation() {
        prevRadiation = 0;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, float partialTicks) {
        Player player = Minecraft.getInstance().player;
        if (player != null && IRadiationManager.INSTANCE.isRadiationEnabled() && MekanismUtils.isPlayingMode(player)) {
            double radiation = player.getData(MekanismAttachmentTypes.RADIATION);
            double severity = RadiationScale.getScaledDoseSeverity(radiation) * 0.8;
            //Only update the previous radiation level at most once a tick
            if (lastTick != player.level().getGameTime()) {
                lastTick = player.level().getGameTime();
                if (prevRadiation < severity) {
                    prevRadiation = Math.min(severity, prevRadiation + 0.01);
                }
                if (prevRadiation > severity) {
                    prevRadiation = Math.max(severity, prevRadiation - 0.01);
                }
            }
            if (severity > RadiationManager.BASELINE) {
                int effect = (int) (prevRadiation * 255);
                int color = (0x701E1E << 8) + effect;
                MekanismRenderer.renderColorOverlay(graphics, 0, 0, color);
            }
        }
    }
}