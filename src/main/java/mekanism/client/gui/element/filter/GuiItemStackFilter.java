package mekanism.client.gui.element.filter;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.StackUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiItemStackFilter<FILTER extends IItemStackFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
      extends GuiFilter<FILTER, TILE> {

    protected GuiItemStackFilter(IGuiWrapper gui, int x, int y, int width, int height, TILE tile, FILTER origFilter) {
        super(gui, x, y, width, height, MekanismLang.ITEM_FILTER.translate(), tile, origFilter);
    }

    @Override
    protected List<ITextComponent> getScreenText() {
        List<ITextComponent> list = super.getScreenText();
        if (filter.hasFilter()) {
            list.add(filter.getItemStack().getDisplayName());
        }
        return list;
    }

    @Override
    protected ILangEntry getNoFilterSaveError() {
        return MekanismLang.ITEM_FILTER_NO_ITEM;
    }

    @Nonnull
    @Override
    protected List<ItemStack> getRenderStacks() {
        ItemStack stack = filter.getItemStack();
        return stack.isEmpty() ? Collections.emptyList() : Collections.singletonList(stack);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double xAxis = mouseX - guiObj.getLeft();
            double yAxis = mouseY - guiObj.getTop();
            if (xAxis >= relativeX + 8 && xAxis < relativeX + 22 && yAxis >= relativeY + getSlotOffset() + 1 && yAxis < relativeY + getSlotOffset() + 17) {
                if (Screen.hasShiftDown()) {
                    filter.setItemStack(ItemStack.EMPTY);
                } else {
                    ItemStack stack = minecraft.player.inventory.getItemStack();
                    if (stack.isEmpty()) {
                        return super.mouseClicked(mouseX, mouseY, button);
                    }
                    filter.setItemStack(StackUtils.size(stack, 1));
                }
                slotDisplay.updateStackList();
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}