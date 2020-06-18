package mekanism.client.gui.element.filter;

import java.util.Collections;
import java.util.List;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiMaterialFilter<FILTER extends IMaterialFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
      extends GuiFilter<FILTER, TILE> {

    protected GuiMaterialFilter(IGuiWrapper gui, int x, int y, int width, int height, TILE tile, FILTER origFilter) {
        super(gui, x, y, width, height, MekanismLang.MATERIAL_FILTER.translate(), tile, origFilter);
        if (filter.hasFilter()) {
            slotDisplay.updateStackList();
        }
    }

    @Override
    protected List<ITextComponent> getScreenText() {
        List<ITextComponent> list = super.getScreenText();
        list.add(MekanismLang.MATERIAL_FILTER_DETAILS.translate());
        if (filter.hasFilter()) {
            list.add(filter.getMaterialItem().getDisplayName());
        }
        return list;
    }

    @Override
    protected ILangEntry getNoFilterSaveError() {
        return MekanismLang.ITEM_FILTER_NO_ITEM;
    }

    @Override
    protected List<ItemStack> getRenderStacks() {
        ItemStack stack = filter.getMaterialItem();
        return stack.isEmpty() ? Collections.emptyList() : Collections.singletonList(stack);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double xAxis = mouseX - guiObj.getLeft();
            double yAxis = mouseY - guiObj.getTop();
            //TODO: Check if mouse is over the slot?
            if (xAxis >= relativeX + 8 && xAxis < relativeX + 24 && yAxis >= relativeY + 19 && yAxis < relativeY + 35) {
                ItemStack stack = minecraft.player.inventory.getItemStack();
                if (!stack.isEmpty() && !Screen.hasShiftDown()) {
                    if (stack.getItem() instanceof BlockItem) {
                        //TODO - V10: Either look at unbreakable blocks or make a tag for a blacklist
                        if (Block.getBlockFromItem(stack.getItem()) != Blocks.BEDROCK) {
                            filter.setMaterialItem(StackUtils.size(stack, 1));
                        }
                    }
                } else if (stack.isEmpty() && Screen.hasShiftDown()) {
                    filter.setMaterialItem(ItemStack.EMPTY);
                }
                slotDisplay.updateStackList();
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}