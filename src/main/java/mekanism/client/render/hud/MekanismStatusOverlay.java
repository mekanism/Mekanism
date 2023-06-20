package mekanism.client.render.hud;

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
                    int x = guiGraphics.guiWidth();
                    int y = guiGraphics.guiHeight();
                    int color = Color.rgbad(1, 1, 1, modeSwitchTimer / 100F).argb();
                    Font font = gui.getFont();
                    guiGraphics.drawString(font, scrollTextComponent, (x - font.width(scrollTextComponent)) / 2, y - 60, color, false);
                }
            }
            modeSwitchTimer--;
        }
    }
}