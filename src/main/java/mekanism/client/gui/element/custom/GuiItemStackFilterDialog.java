package mekanism.client.gui.element.custom;

import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;

public class GuiItemStackFilterDialog extends GuiFilterDialog<QIOItemStackFilter> {

    public <TILE extends TileEntityMekanism & ITileFilterHolder<QIOItemStackFilter>>
    GuiItemStackFilterDialog(IGuiWrapper gui, int x, int y, int width, int height, TILE tile) {
        super(gui, x, y, width, height, MekanismLang.ITEM_FILTER.translate());

        addChild(new GuiSlot(SlotType.NORMAL, gui, 11, 18).setRenderHover(true));
        addChild(new GuiInnerScreen(gui, 33, 18, 111, 43));
        addChild(new TranslationButton(gui, gui.getLeft() + 27, gui.getTop() + 62, 60, 20, MekanismLang.BUTTON_SAVE, () -> {
            if (!filter.getItemStack().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(tile.getPos(), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), false, origFilter, filter));
                }
                gui.removeElement(this);
            } else {
                status = MekanismLang.ITEM_FILTER_NO_ITEM.translateColored(EnumColor.DARK_RED);
                ticker = 20;
            }
        }));
        addChild(new TranslationButton(gui, gui.getLeft() + 89, gui.getTop() + 62, 60, 20, isNew ? MekanismLang.BUTTON_CANCEL : MekanismLang.BUTTON_DELETE, () -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), true, origFilter, null));
            gui.removeElement(this);
        }));
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        if (!filter.getItemStack().isEmpty()) {
            drawScaledText(filter.getItemStack().getDisplayName(), 35, 41, screenTextColor(), 107);
        }
        guiObj.renderItem(filter.getItemStack(), 12, 19);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            double xAxis = mouseX - guiObj.getLeft();
            double yAxis = mouseY - guiObj.getTop();
            if (xAxis >= 12 && xAxis < 28 && yAxis >= 19 && yAxis < 35) {
                ItemStack stack = minecraft.player.inventory.getItemStack();
                if (!stack.isEmpty() && !Screen.hasShiftDown()) {
                    if (stack.getItem() instanceof BlockItem) {
                        filter.setItemStack(stack.copy());
                        filter.getItemStack().setCount(1);
                    }
                } else if (stack.isEmpty() && Screen.hasShiftDown()) {
                    filter.setItemStack(ItemStack.EMPTY);
                }
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            }
        }
        return true;
    }
}
