package mekanism.client.gui.filter;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.sound.SoundHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiMaterialFilter<FILTER extends IMaterialFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>, CONTAINER extends
      FilterContainer<FILTER, TILE>> extends GuiTypeFilter<FILTER, TILE, CONTAINER> {

    protected GuiMaterialFilter(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void tick() {
        super.tick();
        if (ticker > 0) {
            ticker--;
        } else {
            status = MekanismLang.STATUS_OK.translateColored(EnumColor.DARK_GREEN);
        }
    }

    @Override
    protected void addButtons() {
        addButton(new GuiInnerScreen(this, 33, 18, 111, 43, () -> {
            List<ITextComponent> list = new ArrayList<>();
            list.add(MekanismLang.STATUS.translate(status));
            list.add(MekanismLang.MATERIAL_FILTER_DETAILS.translate());
            if (!filter.getMaterialItem().isEmpty()) {
                list.add(filter.getMaterialItem().getDisplayName());
            }
            return list;
        }).clearFormat());
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString((isNew ? MekanismLang.FILTER_NEW : MekanismLang.FILTER_EDIT).translate(MekanismLang.MATERIAL_FILTER), 43, 6, titleTextColor());
        drawForegroundLayer(mouseX, mouseY);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    protected void materialMouseClicked() {
        ItemStack stack = minecraft.player.inventory.getItemStack();
        if (!stack.isEmpty() && !hasShiftDown()) {
            if (stack.getItem() instanceof BlockItem) {
                //TODO: Either look at unbreakable blocks or make a tag for a blacklist
                if (Block.getBlockFromItem(stack.getItem()) != Blocks.BEDROCK) {
                    filter.setMaterialItem(stack.copy());
                    filter.getMaterialItem().setCount(1);
                }
            }
        } else if (stack.isEmpty() && hasShiftDown()) {
            filter.setMaterialItem(ItemStack.EMPTY);
        }
        updateRenderStacks();
        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
    }
}