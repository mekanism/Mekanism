package mekanism.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Optional;
import java.util.function.Predicate;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiUtils;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.HUDElement.HUDColor;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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

    private long lastTick = -1;
    private float prevRotationYaw;
    private float prevRotationPitch;

    public void renderHUD(Minecraft minecraft, GuiGraphics guiGraphics, Font font, float partialTick, int screenWidth, int screenHeight, int maxTextHeight,
          boolean reverseHud) {
        Player player = minecraft.player;
        update(minecraft.level, player);
        if (MekanismConfig.client.hudOpacity.get() < 0.05F) {
            return;
        }
        int color = HUDColor.REGULAR.getColorARGB();
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        float yawJitter = -absSqrt(player.yHeadRot - prevRotationYaw);
        float pitchJitter = -absSqrt(player.getXRot() - prevRotationPitch);
        pose.translate(yawJitter, pitchJitter, 0);
        if (MekanismConfig.client.hudCompassEnabled.get()) {
            renderCompass(player, font, guiGraphics, partialTick, screenWidth, screenHeight, maxTextHeight, reverseHud, color);
        }

        renderMekaSuitEnergyIcons(player, font, guiGraphics, color);
        renderMekaSuitModuleIcons(player, font, guiGraphics, screenWidth, screenHeight, reverseHud, color);

        pose.popPose();
    }

    private void update(Level level, Player player) {
        // if we're just now rendering the HUD after a pause, reset the pitch/yaw trackers
        if (lastTick == -1 || level.getGameTime() - lastTick > 1) {
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

    private void renderMekaSuitEnergyIcons(Player player, Font font, GuiGraphics guiGraphics, int color) {
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(10, 10, 0);
        int posX = 0;
        Predicate<Item> showArmorPercent = item -> item instanceof ItemMekaSuitArmor;
        for (int i = 0; i < EnumUtils.ARMOR_SLOTS.length; i++) {
            posX += renderEnergyIcon(player, font, guiGraphics, posX, color, ARMOR_ICONS[i], EnumUtils.ARMOR_SLOTS[i], showArmorPercent);
        }
        Predicate<Item> showToolPercent = item -> item instanceof ItemMekaTool;
        for (EquipmentSlot hand : EnumUtils.HAND_SLOTS) {
            posX += renderEnergyIcon(player, font, guiGraphics, posX, color, TOOL_ICON, hand, showToolPercent);
        }
        pose.popPose();
    }

    private int renderEnergyIcon(Player player, Font font, GuiGraphics guiGraphics, int posX, int color, ResourceLocation icon, EquipmentSlot slot,
          Predicate<Item> showPercent) {
        ItemStack stack = player.getItemBySlot(slot);
        if (showPercent.test(stack.getItem())) {
            renderHUDElement(font, guiGraphics, posX, 0, IModuleHelper.INSTANCE.hudElementPercent(icon, StorageUtils.getEnergyRatio(stack)), color, false);
            return 48;
        }
        return 0;
    }

    private void renderMekaSuitModuleIcons(Player player, Font font, GuiGraphics guiGraphics, int screenWidth, int screenHeight, boolean reverseHud, int color) {
        int startX = screenWidth - 10;
        int curY = screenHeight - 10;
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        //Render any elements that might be on modules in the meka suit while worn or on the meka tool while held
        for (EquipmentSlot type : EQUIPMENT_ORDER) {
            IModuleContainer moduleContainer = IModuleHelper.INSTANCE.getModuleContainerNullable(player, type);
            if (moduleContainer != null) {
                for (IHUDElement element : moduleContainer.getHUDElements(player)) {
                    curY -= 18;
                    if (reverseHud) {
                        //Align the mekasuit module icons to the left of the screen
                        renderHUDElement(font, guiGraphics, 10, curY, element, color, false);
                    } else {
                        //Align the mekasuit module icons to the right of the screen
                        int elementWidth = 24 + font.width(element.getText());
                        renderHUDElement(font, guiGraphics, startX - elementWidth, curY, element, color, true);
                    }
                }
            }
        }
        pose.popPose();
    }

    private void renderHUDElement(Font font, GuiGraphics guiGraphics, int x, int y, IHUDElement element, int color, boolean iconRight) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        MekanismRenderer.color(guiGraphics, color);
        guiGraphics.blit(element.getIcon(), iconRight ? x + font.width(element.getText()) + 2 : x, y, 0, 0, 16, 16, 16, 16);
        MekanismRenderer.resetColor(guiGraphics);
        guiGraphics.drawString(font, element.getText(), iconRight ? x : x + 18, y + 5, element.getColor(), false);
    }

    private void renderCompass(Player player, Font font, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight, int maxTextHeight, boolean reverseHud,
          int color) {
        //Reversed hud causes the compass to render on the right side of the screen
        int posX = reverseHud ? screenWidth - 125 : 25;
        //Pin the compass above the bottom of the screen and also above the text hud that may render below it
        int posY = Math.min(screenHeight - 20, maxTextHeight) - 80;
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(posX + 50, posY + 50, 0);
        pose.pushPose();

        pose.pushPose();
        pose.scale(0.7F, 0.7F, 0.7F);
        Component coords = MekanismLang.GENERIC_BLOCK_POS.translate(player.getBlockX(), player.getBlockY(), player.getBlockZ());
        GuiUtils.drawString(guiGraphics, font, coords, -font.width(coords) / 2F, -4, color, false);
        pose.popPose();

        float angle = 180 - player.getViewYRot(partialTick);
        pose.mulPose(Axis.XP.rotationDegrees(-60));
        pose.mulPose(Axis.ZP.rotationDegrees(angle));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        MekanismRenderer.color(guiGraphics, color);
        guiGraphics.blit(COMPASS, -50, -50, 100, 100, 0, 0, 256, 256, 256, 256);
        rotateStr(font, guiGraphics, MekanismLang.NORTH_SHORT, angle, 0, color);
        rotateStr(font, guiGraphics, MekanismLang.EAST_SHORT, angle, 90, color);
        rotateStr(font, guiGraphics, MekanismLang.SOUTH_SHORT, angle, 180, color);
        rotateStr(font, guiGraphics, MekanismLang.WEST_SHORT, angle, 270, color);
        MekanismRenderer.resetColor(guiGraphics);
        pose.popPose();
        pose.popPose();
    }

    private void rotateStr(Font font, GuiGraphics guiGraphics, ILangEntry langEntry, float rotation, float shift, int color) {
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.mulPose(Axis.ZP.rotationDegrees(shift));
        pose.translate(0, -50, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(-rotation - shift));
        GuiUtils.drawString(guiGraphics, font, langEntry.translate(), -2.5F, -4, color, false);
        pose.popPose();
    }
}