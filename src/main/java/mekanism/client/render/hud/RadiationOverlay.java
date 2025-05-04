package mekanism.client.render.hud;

import mekanism.api.radiation.IRadiationManager;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationScale;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class RadiationOverlay implements LayeredDraw.Layer {

    public static final RadiationOverlay INSTANCE = new RadiationOverlay();

    private double prevRadiation = 0;
    private int lastTick;

    private RadiationOverlay() {
    }

    public void resetRadiation() {
        prevRadiation = 0;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, @NotNull DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player != null && RadiationManager.isGlobalRadiationEnabled() && MekanismUtils.isPlayingMode(player)) {
            double radiation = player.getData(MekanismAttachmentTypes.RADIATION);
            double severity = RadiationScale.getScaledDoseSeverity(radiation) * 0.8;
            //Only update the previous radiation level at most once a tick
            if (lastTick != minecraft.gui.getGuiTicks()) {
                lastTick = minecraft.gui.getGuiTicks();
                if (prevRadiation < severity) {
                    prevRadiation = Math.min(severity, prevRadiation + 0.01);
                }
                if (prevRadiation > severity) {
                    prevRadiation = Math.max(severity, prevRadiation - 0.01);
                }
            }
            if (severity > IRadiationManager.INSTANCE.baselineRadiation()) {
                int effect = (int) (prevRadiation * 255);
                int color = (0x701E1E << 8) + effect;
                MekanismRenderer.renderColorOverlay(graphics, 0, 0, color);
            }
        }
    }
}