package mekanism.client.gui.element.filter.miner;

import java.util.function.Consumer;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StackUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;

public interface GuiMinerFilterHelper {

    default void addMinerDefaults(IGuiWrapper gui, MinerFilter<?> filter, int slotOffset, Consumer<GuiElement> childAdder) {
        childAdder.accept(new GuiSlot(SlotType.NORMAL, gui, getRelativeX() + 148, getRelativeY() + slotOffset).setRenderHover(true));
        childAdder.accept(new MekanismImageButton(gui, gui.getLeft() + getRelativeX() + 148, gui.getTop() + getRelativeY() + 45, 14, 16,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "exclamation.png"), () -> filter.requireStack = !filter.requireStack,
              (onHover, xAxis, yAxis) -> gui.displayTooltip(MekanismLang.MINER_REQUIRE_REPLACE.translate(YesNo.of(filter.requireStack)), xAxis, yAxis)));
    }

    default void renderMinerForeground(IGuiWrapper gui, MinerFilter<?> filter) {
        gui.getItemRenderer().zLevel += 200;
        gui.renderItem(filter.replaceStack, getRelativeX() + 149, getRelativeY() + 19);
        gui.getItemRenderer().zLevel -= 200;
    }

    int getRelativeX();

    int getRelativeY();

    //TODO: Clean this up
    default boolean tryClickReplaceStack(IGuiWrapper gui, double mouseX, double mouseY, int button, MinerFilter<?> filter) {
        if (button == 0) {
            double xAxis = mouseX - gui.getLeft();
            double yAxis = mouseY - gui.getTop();
            //Over replace output
            if (xAxis >= 149 && xAxis <= 165 && yAxis >= 19 && yAxis <= 35) {
                boolean doNull = false;
                ItemStack stack = Minecraft.getInstance().player.inventory.getItemStack();
                ItemStack toUse = ItemStack.EMPTY;
                if (!stack.isEmpty() && !Screen.hasShiftDown()) {
                    if (stack.getItem() instanceof BlockItem) {
                        //TODO: Either look at unbreakable blocks or make a tag for a blacklist
                        if (Block.getBlockFromItem(stack.getItem()) != Blocks.BEDROCK) {
                            toUse = StackUtils.size(stack, 1);
                        }
                    }
                } else if (stack.isEmpty() && Screen.hasShiftDown()) {
                    doNull = true;
                }
                if (!toUse.isEmpty() || doNull) {
                    filter.replaceStack = toUse;
                }
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                return true;
            }
        }
        return false;
    }
}