package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.lib.Color;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;

public class HUDRenderer {

    private static final ResourceLocation HEAD_ICON = Mekanism.rl("gui/hud/hud_mekasuit_helmet.png"),
                                          CHEST_ICON = Mekanism.rl("gui/hud/hud_mekasuit_chest.png"),
                                          LEGS_ICON = Mekanism.rl("gui/hud/hud_mekasuit_leggings.png"),
                                          BOOTS_ICON = Mekanism.rl("gui/hud/hud_mekasuit_boots.png");

    private static final ResourceLocation COMPASS = MekanismUtils.getResource(ResourceType.GUI, "compass.png");
    private static final Color color = Color.rgbad(0.25, 0.96, 0.94, 0.3);

    private long lastTick = -1;
    private float prevRotationPitch; // must track manually, for some reason it's already synced in the entity

    private final Minecraft minecraft = Minecraft.getInstance();

    public void renderHUD(MatrixStack matrix, float partialTick) {
        checkTime();

        matrix.push();
        float yawJitter = -(minecraft.player.rotationYawHead - minecraft.player.prevRotationYawHead) * 0.25F;
        float pitchJitter = -(minecraft.player.rotationPitch - prevRotationPitch) * 0.25F;
        matrix.translate(yawJitter, pitchJitter, 0);
        if (MekanismConfig.client.mekaSuitHelmetCompassEnabled.get()) {
            renderCompass(matrix, partialTick);
        }

        renderMekaSuitEnergyIcons(matrix, partialTick);
        renderMekaSuitModuleIcons(matrix, partialTick);

        matrix.pop();

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

    private void renderMekaSuitEnergyIcons(MatrixStack matrix, float partialTick) {
        matrix.push();
        matrix.translate(10, 10, 0);
        int posX = 0;
        if (getStack(EquipmentSlotType.HEAD).getItem() instanceof ItemMekaSuitArmor) {
            renderIcon(matrix, HEAD_ICON, posX, 0, StorageUtils.getEnergyPercent(getStack(EquipmentSlotType.HEAD)));
            posX += 48;
        }
        if (getStack(EquipmentSlotType.CHEST).getItem() instanceof ItemMekaSuitArmor) {
            renderIcon(matrix, CHEST_ICON, posX, 0, StorageUtils.getEnergyPercent(getStack(EquipmentSlotType.CHEST)));
            posX += 48;
        }
        if (getStack(EquipmentSlotType.LEGS).getItem() instanceof ItemMekaSuitArmor) {
            renderIcon(matrix, LEGS_ICON, posX, 0, StorageUtils.getEnergyPercent(getStack(EquipmentSlotType.LEGS)));
            posX += 48;
        }
        if (getStack(EquipmentSlotType.FEET).getItem() instanceof ItemMekaSuitArmor) {
            renderIcon(matrix, BOOTS_ICON, posX, 0, StorageUtils.getEnergyPercent(getStack(EquipmentSlotType.FEET)));
        }
        matrix.pop();
    }

    private void renderMekaSuitModuleIcons(MatrixStack matrix, float partialTick) {
        matrix.push();
        matrix.pop();
    }

    private void renderIcon(MatrixStack matrix, ResourceLocation icon, int x, int y, ITextComponent text) {
        RenderSystem.color4f(1, 1, 1, 0.6F);
        minecraft.getTextureManager().bindTexture(icon);
        AbstractGui.func_238463_a_(matrix, x, y, 0, 0, 16, 16, 16, 16);
        minecraft.fontRenderer.func_238422_b_(matrix, text, x + 18, y + 5, color.argb());
        MekanismRenderer.resetColor();
    }

    private void renderCompass(MatrixStack matrix, float partialTick) {
        matrix.push();
        int posX = 25;
        int posY = minecraft.getMainWindow().getScaledHeight() - 100;
        matrix.translate(posX + 50, posY + 50, 0);
        matrix.push();
        float angle = 180 - MathHelper.lerp(partialTick, minecraft.player.prevRotationYawHead, minecraft.player.rotationYawHead);
        matrix.push();
        matrix.scale(0.7F, 0.7F, 0.7F);
        ITextComponent coords = MekanismLang.GENERIC_BLOCK_POS.translate((int) minecraft.player.getPosX(), (int) minecraft.player.getPosY(), (int) minecraft.player.getPosZ());
        minecraft.fontRenderer.func_238422_b_(matrix, coords, -minecraft.fontRenderer.func_238414_a_(coords) / 2F, -4, color.argb());
        matrix.pop();
        matrix.rotate(Vector3f.XP.rotationDegrees(-60));
        matrix.rotate(Vector3f.ZP.rotationDegrees(angle));
        minecraft.getTextureManager().bindTexture(COMPASS);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        MekanismRenderer.color(color);
        AbstractGui.func_238466_a_(matrix, -50, -50, 100, 100, 0, 0, 256, 256, 256, 256);
        rotateStr(matrix, MekanismLang.NORTH_SHORT, angle, 0, color);
        rotateStr(matrix, MekanismLang.EAST_SHORT, angle, 90, color);
        rotateStr(matrix, MekanismLang.SOUTH_SHORT, angle, 180, color);
        rotateStr(matrix, MekanismLang.WEST_SHORT, angle, 270, color);
        MekanismRenderer.resetColor();
        matrix.pop();
        matrix.pop();
    }

    private void rotateStr(MatrixStack matrix, ILangEntry langEntry, float rotation, float shift, Color color) {
        matrix.push();
        matrix.rotate(Vector3f.ZP.rotationDegrees(shift));
        matrix.translate(0, -50, 0);
        matrix.rotate(Vector3f.ZP.rotationDegrees(-rotation - shift));
        minecraft.fontRenderer.func_238422_b_(matrix, langEntry.translate(), -2.5F, -4, color.argb());
        matrix.pop();
    }

    private ItemStack getStack(EquipmentSlotType type) {
        return minecraft.player.getItemStackFromSlot(type);
    }
}
