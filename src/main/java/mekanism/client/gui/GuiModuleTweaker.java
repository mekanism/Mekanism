package mekanism.client.gui;

import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiModuleTweaker extends GuiMekanism<ModuleTweakerContainer> {

    private GuiModuleScrollList scrollList;

    private int selected = -1;
    private Module currentModule;

    public GuiModuleTweaker(ModuleTweakerContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        xSize = 247;
    }

    @Override
    public void init() {
        super.init();

        int size = container.inventorySlots.size();
        for (int i = 0; i < size; i++) {
            Slot slot = container.inventorySlots.get(i);
            final int index = i;
            // initialize selected item
            if (selected == -1 && isValidItem(index)) {
                select(index);
            }
            addButton(new GuiSlot(SlotType.NORMAL, this, slot.xPos - 1, slot.yPos - 1)
                .click((e, x, y) -> select(index))
                .overlayColor(isValidItem(index) ? () -> null : () -> 0xCC333333)
                .with(() -> index == selected ? SlotOverlay.SELECT : null));
        }

        addButton(scrollList = new GuiModuleScrollList(this, 30, 20, 108, 98, selected == -1 ? null : getStack(selected), this::onModuleSelected));
        addButton(new GuiElementHolder(this, 138, 20, 102, 98));
    }

    private void onModuleSelected(Module module) {
        currentModule = module;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        drawString(MekanismLang.MODULE_TWEAKER.translate(), 64, 5, 0x404040);
        if (currentModule != null) {
            int startY = 22;
            if (currentModule.getData().getMaxStackSize() > 1) {
                drawString(MekanismLang.MODULE_INSTALLED.translate(), 140, startY, 0x303030);
                startY += 9;
            }

            for (ModuleConfigItem<?> configItem : currentModule.getConfigItems()) {

            }
        }
    }

    private void select(int index) {
        selected = index;
        scrollList.updateList(getStack(index));
    }

    private boolean isValidItem(int index) {
        ItemStack stack = getStack(index);
        return stack != null && stack.getItem() instanceof IModuleContainerItem;
    }

    private ItemStack getStack(int index) {
        return container.inventorySlots.get(index).getStack();
    }
}
