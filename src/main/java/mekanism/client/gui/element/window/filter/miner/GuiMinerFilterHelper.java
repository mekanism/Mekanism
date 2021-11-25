package mekanism.client.gui.element.window.filter.miner;

import java.util.function.UnaryOperator;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.window.filter.GuiFilter;
import mekanism.client.gui.element.window.filter.GuiFilterHelper;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostBlockItemConsumer;
import mekanism.common.MekanismLang;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;

public interface GuiMinerFilterHelper extends GuiFilterHelper<TileEntityDigitalMiner> {

    default void addMinerDefaults(IGuiWrapper gui, MinerFilter<?> filter, int slotOffset, UnaryOperator<GuiElement> childAdder) {
        childAdder.apply(new GuiSlot(SlotType.NORMAL, gui, getRelativeX() + 148, getRelativeY() + slotOffset).setRenderHover(true)
              .stored(() -> new ItemStack(filter.replaceTarget)).setGhostHandler((IGhostBlockItemConsumer) ingredient -> {
                  filter.replaceTarget = ((ItemStack) ingredient).getItem();
                  Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
              }));
        childAdder.apply(new MekanismImageButton(gui, getRelativeX() + 148, getRelativeY() + 45, 14, 16,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "exclamation.png"), () -> filter.requiresReplacement = !filter.requiresReplacement,
              (onHover, matrix, xAxis, yAxis) -> gui.displayTooltip(matrix, MekanismLang.MINER_REQUIRE_REPLACE.translate(YesNo.of(filter.requiresReplacement)), xAxis, yAxis)));
    }

    @Override
    default GuiMinerFilerSelect getFilterSelect(IGuiWrapper gui, TileEntityDigitalMiner tile) {
        return new GuiMinerFilerSelect(gui, tile);
    }

    default boolean tryClickReplaceStack(IGuiWrapper gui, double mouseX, double mouseY, int button, int slotOffset, MinerFilter<?> filter) {
        return GuiFilter.mouseClickSlot(gui, button, mouseX, mouseY, getRelativeX() + 149, getRelativeY() + slotOffset + 1, GuiFilter.NOT_EMPTY_BLOCK, stack -> {
            filter.replaceTarget = stack.getItem();
            Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        });
    }
}