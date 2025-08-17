package mekanism.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.text.ILangEntry;
import mekanism.client.render.hud.MekanismHUD.DelayedString;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.HUDElement.HUDColor;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

//TODO - 1.20: Decide if we want font rendering in this to support GuiUtils#drawBackdrop and if so how to best go about it
public class HUDRenderer {

    private static final EquipmentSlot[] EQUIPMENT_ORDER = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND,
                                                            EquipmentSlot.OFFHAND};
    private static final ResourceLocation[] ARMOR_ICONS = {MekanismUtils.getResource(ResourceType.GUI_HUD, "hud_mekasuit_helmet.png"),
                                                           MekanismUtils.getResource(ResourceType.GUI_HUD, "hud_mekasuit_chest.png"),
                                                           MekanismUtils.getResource(ResourceType.GUI_HUD, "hud_mekasuit_leggings.png"),
                                                           MekanismUtils.getResource(ResourceType.GUI_HUD, "hud_mekasuit_boots.png")};
    private static final ResourceLocation TOOL_ICON = MekanismUtils.getResource(ResourceType.GUI_HUD, "hud_mekatool.png");

    private static final ResourceLocation COMPASS = MekanismUtils.getResource(ResourceType.GUI, "compass.png");

    private int lastSubtitleGuiTick = -1;
    private int lastSubtitleWidth = 0;
    private long lastTick = -1;
    private float prevRotationYaw;
    private float prevRotationPitch;

    public void renderHUD(Minecraft minecraft, GuiGraphics guiGraphics, Font font, List<DelayedString> delayedDraws, DeltaTracker delta, int screenWidth, int screenHeight,
          int maxTextHeight, boolean reverseHud) {
        Player player = minecraft.player;
        update(minecraft.level, player);
        if (MekanismConfig.client.hudOpacity.get() < 0.05F) {
            return;
        }
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        float yawJitter = -absSqrt(player.yHeadRot - prevRotationYaw);
        float pitchJitter = -absSqrt(player.getXRot() - prevRotationPitch);
        pose.translate(yawJitter, pitchJitter, 0);
        int audibleSubtitlesWidth = MekanismConfig.client.hudAvoidSoundSubtitleOverlay.get() ? getAudibleSubtitlesWidth(minecraft, font) : 0;
        if (MekanismConfig.client.hudCompassEnabled.get()) {
            renderCompass(player, font, guiGraphics, delayedDraws, delta, screenWidth, screenHeight, maxTextHeight, reverseHud, audibleSubtitlesWidth);
        }

        renderMekaSuitEnergyIcons(player, font, guiGraphics, delayedDraws);
        renderMekaSuitModuleIcons(player, font, guiGraphics, delayedDraws, screenWidth, screenHeight, reverseHud, audibleSubtitlesWidth);

        pose.popPose();
    }

    private void update(Level level, Player player) {
        // if we're just now rendering the HUD after a pause, reset the pitch/yaw trackers
        if (lastTick == -1 || level.getGameTime() - lastTick > 1 || !level.tickRateManager().runsNormally()) {
            prevRotationYaw = player.getYRot();
            prevRotationPitch = player.getXRot();
        }
        lastTick = level.getGameTime();
        float yawDiff = player.yHeadRot - prevRotationYaw;
        float pitchDiff = player.getXRot() - prevRotationPitch;
        float jitter = MekanismConfig.client.hudJitter.get();
        prevRotationYaw += yawDiff / jitter;
        prevRotationPitch += pitchDiff / jitter;
    }

    private static float absSqrt(float val) {
        float ret = Mth.sqrt(Math.abs(val));
        return val < 0 ? -ret : ret;
    }

    private void renderMekaSuitEnergyIcons(Player player, Font font, GuiGraphics guiGraphics, List<DelayedString> delayedDraws) {
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(10, 10, 0);
        Matrix4f matrix = new Matrix4f(pose.last().pose());
        int posX = 0;
        Predicate<Item> showArmorPercent = item -> item instanceof ItemMekaSuitArmor;
        for (int i = 0; i < EnumUtils.ARMOR_SLOTS.length; i++) {
            posX += renderEnergyIcon(player, font, guiGraphics, matrix, delayedDraws, posX, ARMOR_ICONS[i], EnumUtils.ARMOR_SLOTS[i], showArmorPercent);
        }
        Predicate<Item> showToolPercent = item -> item instanceof ItemMekaTool;
        for (EquipmentSlot hand : EnumUtils.HAND_SLOTS) {
            posX += renderEnergyIcon(player, font, guiGraphics, matrix, delayedDraws, posX, TOOL_ICON, hand, showToolPercent);
        }
        pose.popPose();
    }

    private int renderEnergyIcon(Player player, Font font, GuiGraphics guiGraphics, Matrix4f matrix, List<DelayedString> delayedDraws, int posX, ResourceLocation icon,
          EquipmentSlot slot, Predicate<Item> showPercent) {
        ItemStack stack = player.getItemBySlot(slot);
        if (showPercent.test(stack.getItem())) {
            renderHUDElement(font, guiGraphics, matrix, delayedDraws, posX, 0, IModuleHelper.INSTANCE.hudElementPercent(icon, StorageUtils.getEnergyRatio(stack)),
                  false);
            return 48;
        }
        return 0;
    }

    private void renderMekaSuitModuleIcons(Player player, Font font, GuiGraphics guiGraphics, List<DelayedString> delayedDraws, int screenWidth, int screenHeight,
          boolean reverseHud, int subtitlesWidth) {
        int startX = screenWidth - 10;
        int curY = screenHeight - 10;
        Matrix4f matrix = new Matrix4f(guiGraphics.pose().last().pose());
        //Render any elements that might be on modules in the meka suit while worn or on the meka tool while held
        for (EquipmentSlot type : EQUIPMENT_ORDER) {
            ItemStack stack = player.getItemBySlot(type);
            IModuleContainer moduleContainer = IModuleHelper.INSTANCE.getModuleContainer(stack);
            if (moduleContainer != null) {
                for (IHUDElement element : moduleContainer.getHUDElements(player, stack)) {
                    curY -= 18;
                    if (reverseHud) {
                        //Align the mekasuit module icons to the left of the screen
                        renderHUDElement(font, guiGraphics, matrix, delayedDraws, 10, curY, element, false);
                    } else {
                        //Align the mekasuit module icons to the right of the screen
                        int elementWidth = subtitlesWidth + 24 + font.width(element.getText());
                        renderHUDElement(font, guiGraphics, matrix, delayedDraws, startX - elementWidth, curY, element, true);
                    }
                }
            }
        }
    }

    /**
     * Based on how {@link SubtitleOverlay#render(GuiGraphics)} calculates the width
     */
    private int getAudibleSubtitlesWidth(Minecraft minecraft, Font font) {
        if (!minecraft.options.showSubtitles().get() || minecraft.gui.subtitleOverlay.audibleSubtitles.isEmpty()) {
            //Subtitles are disabled or none are currently showing, don't bother calculating a width
            return 0;
        }
        if (lastSubtitleGuiTick != minecraft.gui.getGuiTicks()) {
            lastSubtitleGuiTick = minecraft.gui.getGuiTicks();
            int maxWidth = 0;
            for (SubtitleOverlay.Subtitle subtitle : minecraft.gui.subtitleOverlay.audibleSubtitles) {
                //Note: We know all subtitles here are still active, so we can skip checking
                maxWidth = Math.max(maxWidth, font.width(subtitle.getText()));
            }
            //Note: This mirrors vanilla having them as separate pieces for calculating the width instead of just doing font.width("< > ")
            // Presumably this is because Font#width returns Math#ceil, so the below has a chance of being larger than doing it as a single calculation
            maxWidth += font.width("<") + font.width(" ") + font.width(">") + font.width(" ");

            lastSubtitleWidth = maxWidth;
        }
        return lastSubtitleWidth;
    }

    private void renderHUDElement(Font font, GuiGraphics guiGraphics, Matrix4f matrix, List<DelayedString> delayedDraws, int x, int y, IHUDElement element,
          boolean iconRight) {
        int color = element.getColor();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        MekanismRenderer.color(guiGraphics, color);
        guiGraphics.blit(element.getIcon(), iconRight ? x + font.width(element.getText()) + 2 : x, y, 0, 0, 16, 16, 16, 16);
        MekanismRenderer.resetColor(guiGraphics);
        delayedDraws.add(new DelayedString(matrix, element.getText(), iconRight ? x : x + 18, y + 5, color, false));
    }

    private void renderCompass(Player player, Font font, GuiGraphics guiGraphics, List<DelayedString> delayedDraws, DeltaTracker delta, int screenWidth,
          int screenHeight, int maxTextHeight, boolean reverseHud, int audibleSubtitlesWidth) {
        int color = HUDColor.REGULAR.getColorARGB();
        //Reversed hud causes the compass to render on the right side of the screen
        int posX = reverseHud ? screenWidth - 125 - audibleSubtitlesWidth : 25;
        //Pin the compass above the bottom of the screen and also above the text hud that may render below it
        int posY = Math.min(screenHeight - 20, maxTextHeight) - 80;
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(posX + 50, posY + 50, 0);
        pose.pushPose();

        pose.pushPose();
        pose.scale(0.7F, 0.7F, 0.7F);
        Component coords = MekanismLang.GENERIC_BLOCK_POS.translate(player.getBlockX(), player.getBlockY(), player.getBlockZ());
        delayedDraws.add(new DelayedString(pose, coords, -font.width(coords) / 2F, -4, color, false));
        pose.popPose();

        float angle = 180 - player.getViewYRot(delta.getGameTimeDeltaPartialTick(false));
        pose.mulPose(Axis.XP.rotationDegrees(-60));
        pose.mulPose(Axis.ZP.rotationDegrees(angle));
        rotateStr(guiGraphics, delayedDraws, MekanismLang.NORTH_SHORT, angle, 0, color);
        rotateStr(guiGraphics, delayedDraws, MekanismLang.EAST_SHORT, angle, 90, color);
        rotateStr(guiGraphics, delayedDraws, MekanismLang.SOUTH_SHORT, angle, 180, color);
        rotateStr(guiGraphics, delayedDraws, MekanismLang.WEST_SHORT, angle, 270, color);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        MekanismRenderer.color(guiGraphics, color);
        guiGraphics.blit(COMPASS, -50, -50, 100, 100, 0, 0, 256, 256, 256, 256);
        MekanismRenderer.resetColor(guiGraphics);
        pose.popPose();
        pose.popPose();
    }

    private void rotateStr(GuiGraphics guiGraphics, List<DelayedString> delayedDraws, ILangEntry langEntry, float rotation, float shift, int color) {
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.mulPose(Axis.ZP.rotationDegrees(shift));
        pose.translate(0, -50, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(-rotation - shift));
        delayedDraws.add(new DelayedString(pose, langEntry.translate(), -2.5F, -4, color, false));
        pose.popPose();
    }
}