package mekanism.client.gui.element.window.filter.miner;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Consumer;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.window.filter.GuiFilter;
import mekanism.client.gui.element.window.filter.GuiFilterHelper;
import mekanism.client.gui.element.window.filter.GuiFilterSelect;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostBlockItemConsumer;
import mekanism.common.MekanismLang;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StackUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;

public interface GuiMinerFilterHelper extends GuiFilterHelper<TileEntityDigitalMiner> {

    default void addMinerDefaults(IGuiWrapper gui, MinerFilter<?> filter, int slotOffset, Consumer<GuiElement> childAdder) {
        childAdder.accept(new GuiSlot(SlotType.NORMAL, gui, getRelativeX() + 148, getRelativeY() + slotOffset).setRenderHover(true)
              .setGhostHandler((IGhostBlockItemConsumer) ingredient -> {
                  filter.replaceStack = StackUtils.size((ItemStack) ingredient, 1);
                  Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
              }));
        childAdder.accept(new MekanismImageButton(gui, gui.getLeft() + getRelativeX() + 148, gui.getTop() + getRelativeY() + 45, 14, 16,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "exclamation.png"), () -> filter.requireStack = !filter.requireStack,
              (onHover, matrix, xAxis, yAxis) -> gui.displayTooltip(matrix, MekanismLang.MINER_REQUIRE_REPLACE.translate(YesNo.of(filter.requireStack)), xAxis, yAxis)));
    }

    @Override
    default GuiFilterSelect getFilterSelect(IGuiWrapper gui, TileEntityDigitalMiner tile) {
        return new GuiMinerFilerSelect(gui, tile);
    }

    default void renderReplaceStack(MatrixStack matrix, IGuiWrapper gui, MinerFilter<?> filter) {
        if (!filter.replaceStack.isEmpty()) {
            gui.getItemRenderer().zLevel += 200;
            gui.renderItem(matrix, filter.replaceStack, getRelativeX() + 149, getRelativeY() + 19);
            gui.getItemRenderer().zLevel -= 200;
        }
    }

    default boolean tryClickReplaceStack(IGuiWrapper gui, double mouseX, double mouseY, int button, int slotOffset, MinerFilter<?> filter) {
        return GuiFilter.mouseClickSlot(gui, button, mouseX, mouseY, getRelativeX() + 149, getRelativeY() + slotOffset + 1, GuiFilter.NOT_EMPTY_BLOCK, stack -> {
            filter.replaceStack = stack;
            Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        });
    }
}