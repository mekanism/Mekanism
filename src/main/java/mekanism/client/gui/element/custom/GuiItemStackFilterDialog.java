package mekanism.client.gui.element.custom;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

public class GuiItemStackFilterDialog extends GuiFilterDialog<QIOItemStackFilter> {

    private <TILE extends TileEntityMekanism & ITileFilterHolder<QIOFilter<?>>>
    GuiItemStackFilterDialog(IGuiWrapper gui, int x, int y, TILE tile, QIOItemStackFilter origFilter) {
        super(gui, x, y, 152, 90, MekanismLang.ITEM_FILTER.translate(), origFilter);
        addChild(new GuiSlot(SlotType.NORMAL, gui, relativeX + 7, relativeY + 18).setRenderHover(true));
        addChild(new GuiInnerScreen(gui, relativeX + 29, relativeY + 18, width - 29 - 7, 43, () -> {
            List<ITextComponent> list = new ArrayList<>();
            list.add(MekanismLang.STATUS.translate(status));
            if (!filter.getItemStack().isEmpty()) {
                list.add(filter.getItemStack().getDisplayName());
            }
            return list;
        }).clearFormat());
        addChild(new TranslationButton(gui, gui.getLeft() + relativeX + width / 2 - 61, gui.getTop() + relativeY + 63, 60, 20, isNew ? MekanismLang.BUTTON_CANCEL : MekanismLang.BUTTON_DELETE, () -> {
            if (origFilter != null) {
                Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), true, origFilter, null));
            }
            close();
        }));
        addChild(new TranslationButton(gui, gui.getLeft() + relativeX + width / 2 + 1, gui.getTop() + relativeY + 63, 60, 20, MekanismLang.BUTTON_SAVE, () -> {
            if (!filter.getItemStack().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(tile.getPos(), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), false, origFilter, filter));
                }
                close();
            } else {
                status = MekanismLang.ITEM_FILTER_NO_ITEM.translateColored(EnumColor.DARK_RED);
                ticker = 20;
            }
        }));
    }

    public static <TILE extends TileEntityMekanism & ITileFilterHolder<QIOFilter<?>>> GuiItemStackFilterDialog create(IGuiWrapper gui, TILE tile) {
        return new GuiItemStackFilterDialog(gui, gui.getWidth() / 2 - 152 / 2, 15, tile, null);
    }

    public static <TILE extends TileEntityMekanism & ITileFilterHolder<QIOFilter<?>>> GuiItemStackFilterDialog edit(IGuiWrapper gui, TILE tile, QIOItemStackFilter filter) {
        return new GuiItemStackFilterDialog(gui, gui.getWidth() / 2 - 152 / 2, 15, tile, filter);
    }

    @Override
    public QIOItemStackFilter createNewFilter() {
        return new QIOItemStackFilter();
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        guiObj.getItemRenderer().zLevel += 200;
        guiObj.renderItem(filter.getItemStack(), relativeX + 8, relativeY + 19);
        guiObj.getItemRenderer().zLevel -= 200;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double xAxis = mouseX - guiObj.getLeft();
            double yAxis = mouseY - guiObj.getTop();
            if (xAxis >= relativeX + 8 && xAxis < relativeX + 24 && yAxis >= relativeY + 19 && yAxis < relativeY + 35) {
                ItemStack stack = minecraft.player.inventory.getItemStack();
                if (!stack.isEmpty() && !Screen.hasShiftDown()) {
                    filter.setItemStack(stack.copy());
                    filter.getItemStack().setCount(1);
                } else if (stack.isEmpty() && Screen.hasShiftDown()) {
                    filter.setItemStack(ItemStack.EMPTY);
                }
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
