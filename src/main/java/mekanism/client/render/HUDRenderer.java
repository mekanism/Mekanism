package mekanism.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.IHUDElement;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.HUDElement.HUDColor;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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

    public void renderHUD(PoseStack matrix, float partialTick, int screenWidth, int screenHeight, int maxTextHeight, boolean reverseHud) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        update(minecraft.level, player);
        if (MekanismConfig.client.hudOpacity.get() < 0.05F) {
            return;
        }
        Font font = minecraft.font;
        int color = HUDColor.REGULAR.getColorARGB();
        matrix.pushPose();
        float yawJitter = -absSqrt(player.yHeadRot - prevRotationYaw);
        float pitchJitter = -absSqrt(player.getXRot() - prevRotationPitch);
        matrix.translate(yawJitter, pitchJitter, 0);
        if (MekanismConfig.client.hudCompassEnabled.get()) {
            renderCompass(player, font, matrix, partialTick, screenWidth, screenHeight, maxTextHeight, reverseHud, color);
        }

        renderMekaSuitEnergyIcons(player, font, matrix, color);
        renderMekaSuitModuleIcons(player, font, matrix, screenWidth, screenHeight, reverseHud, color);

        matrix.popPose();
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
        float ret = (float) Math.sqrt(Math.abs(val));
        return val < 0 ? -ret : ret;
    }

    private void renderMekaSuitEnergyIcons(Player player, Font font, PoseStack matrix, int color) {
        matrix.pushPose();
        matrix.translate(10, 10, 0);
        int posX = 0;
        Predicate<Item> showArmorPercent = item -> item instanceof ItemMekaSuitArmor;
        for (int i = 0; i < EnumUtils.ARMOR_SLOTS.length; i++) {
            posX += renderEnergyIcon(player, font, matrix, posX, color, ARMOR_ICONS[i], EnumUtils.ARMOR_SLOTS[i], showArmorPercent);
        }
        Predicate<Item> showToolPercent = item -> item instanceof ItemMekaTool;
        for (EquipmentSlot hand : EnumUtils.HAND_SLOTS) {
            posX += renderEnergyIcon(player, font, matrix, posX, color, TOOL_ICON, hand, showToolPercent);
        }
        matrix.popPose();
    }

    private int renderEnergyIcon(Player player, Font font, PoseStack matrix, int posX, int color, ResourceLocation icon, EquipmentSlot slot, Predicate<Item> showPercent) {
        ItemStack stack = player.getItemBySlot(slot);
        if (showPercent.test(stack.getItem())) {
            renderHUDElement(font, matrix, posX, 0, MekanismAPI.getModuleHelper().hudElementPercent(icon, StorageUtils.getEnergyRatio(stack)), color, false);
            return 48;
        }
        return 0;
    }

    private void renderMekaSuitModuleIcons(Player player, Font font, PoseStack matrix, int screenWidth, int screenHeight, boolean reverseHud, int color) {
        int startX = screenWidth - 10;
        int curY = screenHeight - 10;
        matrix.pushPose();
        //Render any elements that might be on modules in the meka suit while worn or on the meka tool while held
        for (EquipmentSlot type : EQUIPMENT_ORDER) {
            ItemStack stack = player.getItemBySlot(type);
            if (stack.getItem() instanceof IModuleContainerItem item) {
                for (IHUDElement element : item.getHUDElements(player, stack)) {
                    curY -= 18;
                    if (reverseHud) {
                        //Align the mekasuit module icons to the left of the screen
                        renderHUDElement(font, matrix, 10, curY, element, color, false);
                    } else {
                        //Align the mekasuit module icons to the right of the screen
                        int elementWidth = 24 + font.width(element.getText());
                        renderHUDElement(font, matrix, startX - elementWidth, curY, element, color, true);
                    }
                }
            }
        }
        matrix.popPose();
    }

    private void renderHUDElement(Font font, PoseStack matrix, int x, int y, IHUDElement element, int color, boolean iconRight) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        MekanismRenderer.color(color);
        RenderSystem.setShaderTexture(0, element.getIcon());
        GuiComponent.blit(matrix, iconRight ? x + font.width(element.getText()) + 2 : x, y, 0, 0, 16, 16, 16, 16);
        MekanismRenderer.resetColor();
        font.draw(matrix, element.getText(), iconRight ? x : x + 18, y + 5, element.getColor());
    }

    private void renderCompass(Player player, Font font, PoseStack matrix, float partialTick, int screenWidth, int screenHeight, int maxTextHeight, boolean reverseHud,
          int color) {
        //Reversed hud causes the compass to render on the right side of the screen
        int posX = reverseHud ? screenWidth - 125 : 25;
        //Pin the compass above the bottom of the screen and also above the text hud that may render below it
        int posY = Math.min(screenHeight - 20, maxTextHeight) - 80;
        matrix.pushPose();
        matrix.translate(posX + 50, posY + 50, 0);
        matrix.pushPose();

        matrix.pushPose();
        matrix.scale(0.7F, 0.7F, 0.7F);
        Component coords = MekanismLang.GENERIC_BLOCK_POS.translate(player.getBlockX(), player.getBlockY(), player.getBlockZ());
        font.draw(matrix, coords, -font.width(coords) / 2F, -4, color);
        matrix.popPose();

        float angle = 180 - player.getViewYRot(partialTick);
        matrix.mulPose(Vector3f.XP.rotationDegrees(-60));
        matrix.mulPose(Vector3f.ZP.rotationDegrees(angle));
        RenderSystem.setShaderTexture(0, COMPASS);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        MekanismRenderer.color(color);
        GuiComponent.blit(matrix, -50, -50, 100, 100, 0, 0, 256, 256, 256, 256);
        rotateStr(font, matrix, MekanismLang.NORTH_SHORT, angle, 0, color);
        rotateStr(font, matrix, MekanismLang.EAST_SHORT, angle, 90, color);
        rotateStr(font, matrix, MekanismLang.SOUTH_SHORT, angle, 180, color);
        rotateStr(font, matrix, MekanismLang.WEST_SHORT, angle, 270, color);
        MekanismRenderer.resetColor();
        matrix.popPose();
        matrix.popPose();
    }

    private void rotateStr(Font font, PoseStack matrix, ILangEntry langEntry, float rotation, float shift, int color) {
        matrix.pushPose();
        matrix.mulPose(Vector3f.ZP.rotationDegrees(shift));
        matrix.translate(0, -50, 0);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(-rotation - shift));
        font.draw(matrix, langEntry.translate(), -2.5F, -4, color);
        matrix.popPose();
    }
}