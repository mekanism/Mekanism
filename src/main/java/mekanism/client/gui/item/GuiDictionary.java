package mekanism.client.gui.item;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.inventory.container.item.DictionaryContainer;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.StackUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

public class GuiDictionary extends GuiMekanism<DictionaryContainer> {

    private ItemStack itemType = ItemStack.EMPTY;

    private GuiTextScrollList scrollList;

    public GuiDictionary(DictionaryContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiSlot(SlotType.NORMAL, this, 5, 5).setRenderHover(true));
        func_230480_a_(scrollList = new GuiTextScrollList(this, 7, 29, 162, 42));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawTitleText(MekanismItems.DICTIONARY.getTextComponent(), 5);
        drawString(MekanismLang.INVENTORY.translate(), 8, getYSize() - 96 + 2, titleTextColor());
        renderItem(itemType, 6, 6);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean func_231177_au__() {
        return false;
    }

    @Override
    public boolean func_231044_a_(double mouseX, double mouseY, int button) {
        double xAxis = mouseX - getGuiLeft();
        double yAxis = mouseY - getGuiTop();
        if (button == 0) {
            if (func_231173_s_()) {
                Slot hovering = null;
                for (int i = 0; i < container.inventorySlots.size(); i++) {
                    Slot slot = container.inventorySlots.get(i);
                    if (isMouseOverSlot(slot, mouseX, mouseY)) {
                        hovering = slot;
                        break;
                    }
                }

                if (hovering != null) {
                    ItemStack stack = hovering.getStack();
                    if (!stack.isEmpty()) {
                        itemType = StackUtils.size(stack, 1);
                        scrollList.setText(TagCache.getItemTags(itemType));
                        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        return true;
                    }
                }
            }

            if (xAxis >= 6 && xAxis <= 22 && yAxis >= 6 && yAxis <= 22) {
                ItemStack stack = getMinecraft().player.inventory.getItemStack();
                if (!stack.isEmpty() && !func_231173_s_()) {
                    itemType = StackUtils.size(stack, 1);
                    scrollList.setText(TagCache.getItemTags(itemType));
                } else if (stack.isEmpty() && func_231173_s_()) {
                    itemType = ItemStack.EMPTY;
                    scrollList.setText(null);
                }
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            }
        }
        return super.func_231044_a_(mouseX, mouseY, button);
    }
}