package mekanism.client.render.hud;

import mekanism.client.gui.GuiUtils;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.Color;
import net.minecraft.SharedConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.joml.Matrix3x2fStack;

public class MekanismStatusOverlay implements GuiLayer {

    public static final MekanismStatusOverlay INSTANCE = new MekanismStatusOverlay();
    private static final int BASE_TIMER = 5 * SharedConstants.TICKS_PER_SECOND;

    private int modeSwitchTimer = 0;
    private int lastTick;

    private MekanismStatusOverlay() {
    }

    public void setTimer() {
        modeSwitchTimer = BASE_TIMER;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        if (modeSwitchTimer > 1 && minecraft.player != null && !minecraft.player.isSpectator() && !minecraft.gui.hud.isHidden()) {
            ItemStack stack = minecraft.player.getMainHandItem();
            if (IModeItem.isModeItem(stack, EquipmentSlot.MAINHAND)) {
                Component scrollTextComponent = ((IModeItem) stack.getItem()).getScrollTextComponent(stack);
                if (scrollTextComponent != null) {
                    Color color = Color.rgbad(1, 1, 1, modeSwitchTimer / (float) BASE_TIMER);
                    Font font = minecraft.gui.hud.getFont();
                    int componentWidth = font.width(scrollTextComponent);
                    int targetShift = Math.max(59, Math.max(minecraft.gui.hud.leftHeight, minecraft.gui.hud.rightHeight));
                    if (minecraft.gameMode != null && !minecraft.gameMode.canHurtPlayer()) {
                        //Same shift as done in Gui#renderSelectedItemName
                        targetShift -= 14;
                    } else if (minecraft.gui.hud.overlayMessageTime > 0) {
                        //If we are in survival though that means our thing will end up intersecting the subtitle text if there is any,
                        // so we need to check if there is, and if so shift our target further
                        targetShift += 14;
                    }
                    //Shift the rendering to be above the previous line
                    targetShift += 13;
                    Matrix3x2fStack pose = graphics.pose();
                    pose.pushMatrix();
                    pose.translate((graphics.guiWidth() - componentWidth) / 2F, graphics.guiHeight() - targetShift);
                    GuiUtils.drawBackdrop(graphics, minecraft, 0, 0, componentWidth, color.a());
                    graphics.text(font, scrollTextComponent, 0, 0, color.argb());
                    pose.popMatrix();
                }
            }
            //Only decrement the switch timer once a tick
            if (lastTick != minecraft.gui.hud.getGuiTicks()) {
                lastTick = minecraft.gui.hud.getGuiTicks();
                modeSwitchTimer--;
            }
        }
    }
}