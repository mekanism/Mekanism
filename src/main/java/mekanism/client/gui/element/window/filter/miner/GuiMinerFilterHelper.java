package mekanism.client.gui.element.window.filter.miner;

import java.util.function.UnaryOperator;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.TooltipToggleButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.window.filter.GuiFilter;
import mekanism.client.gui.element.window.filter.GuiFilterHelper;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerGhostTarget.IGhostBlockItemConsumer;
import mekanism.common.MekanismLang;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

public interface GuiMinerFilterHelper extends GuiFilterHelper<TileEntityDigitalMiner> {

    int MINER_FILTER_WIDTH = 173;

    @Override
    MinerFilter<?> getFilter();

    default void addMinerDefaults(IGuiWrapper gui, int slotOffset, UnaryOperator<GuiElement> childAdder) {
        childAdder.apply(new GuiSlot(SlotType.NORMAL, gui, getRelativeX() + 148, getRelativeY() + slotOffset).setRenderHover(true)
              .stored(() -> new ItemStack(getFilter().replaceTarget)).click(GuiFilter.getHandleClickSlot(GuiFilter.NOT_EMPTY_BLOCK, stack -> getFilter().replaceTarget = stack.getItem()))
              .setGhostHandler((IGhostBlockItemConsumer) ingredient -> {
                  getFilter().replaceTarget = ((ItemStack) ingredient).getItem();
                  Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
              }));
        childAdder.apply(new TooltipToggleButton(gui, getRelativeX() + 148, getRelativeY() + 45, 14, 16,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "exclamation.png"), () -> getFilter().requiresReplacement, (element, mouseX, mouseY) -> {
            MinerFilter<?> filter = getFilter();
            filter.requiresReplacement = !filter.requiresReplacement;
            return true;
        }, MekanismLang.MINER_REQUIRE_REPLACE.translate(YesNo.YES), MekanismLang.MINER_REQUIRE_REPLACE.translate(YesNo.NO)));
    }

    @Override
    default GuiMinerFilerSelect getFilterSelect(IGuiWrapper gui, TileEntityDigitalMiner tile) {
        return new GuiMinerFilerSelect(gui, tile);
    }
}