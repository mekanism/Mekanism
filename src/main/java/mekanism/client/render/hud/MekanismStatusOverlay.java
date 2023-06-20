package mekanism.client.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.gui.GuiUtils;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class MekanismStatusOverlay implements IGuiOverlay {

    public static final MekanismStatusOverlay INSTANCE = new MekanismStatusOverlay();

    private int modeSwitchTimer = 0;
    private long lastTick;

    private MekanismStatusOverlay() {
    }

    public void setTimer() {
        modeSwitchTimer = 100;
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int screenWidth, int screenHeight) {
        Minecraft minecraft = gui.getMinecraft();
        if (!minecraft.options.hideGui && modeSwitchTimer > 1 && minecraft.player != null) {
            ItemStack stack = minecraft.player.getMainHandItem();
            if (IModeItem.isModeItem(stack, EquipmentSlot.MAINHAND)) {
                Component scrollTextComponent = ((IModeItem) stack.getItem()).getScrollTextComponent(stack);
                if (scrollTextComponent != null) {
                    Color color = Color.rgbad(1, 1, 1, modeSwitchTimer / 100F);
                    Font font = gui.getFont();
                    int componentWidth = font.width(scrollTextComponent);
                    PoseStack pose = guiGraphics.pose();
                    pose.pushPose();
                    pose.translate((screenWidth - componentWidth) / 2F, screenHeight - 60, 0);
                    GuiUtils.drawBackdrop(guiGraphics, minecraft, 0, 0, componentWidth, color.a());
                    guiGraphics.drawString(font, scrollTextComponent, 0, 0, color.argb());
                    pose.popPose();
                }
            }
            //Only decrement the switch timer once a tick
            if (lastTick != minecraft.player.level().getGameTime()) {
                lastTick = minecraft.player.level().getGameTime();
                modeSwitchTimer--;
            }
        }
    }
}