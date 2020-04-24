package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFW;
import mekanism.client.gui.element.GuiModuleScreen;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.network.PacketUpdateInventorySlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiModuleTweaker extends GuiMekanism<ModuleTweakerContainer> {

    private GuiModuleScrollList scrollList;
    private GuiModuleScreen moduleScreen;

    private int selected = -1;

    public GuiModuleTweaker(ModuleTweakerContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        xSize = 248;
        ySize += 20;
    }

    @Override
    public void init() {
        super.init();

        addButton(moduleScreen = new GuiModuleScreen(this, 138, 20, stack -> {
            int slotId = container.inventorySlots.get(selected).getSlotIndex();
            Mekanism.packetHandler.sendToServer(new PacketUpdateInventorySlot(stack, slotId));
            playerInventory.player.inventory.setInventorySlotContents(slotId, stack);
        }));
        addButton(scrollList = new GuiModuleScrollList(this, 30, 20, 108, 134, () -> getStack(selected), this::onModuleSelected));
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
                .overlayColor(isValidItem(index) ? null : () -> 0xCC333333)
                .with(() -> index == selected ? SlotOverlay.SELECT : null));
        }
    }

    private void onModuleSelected(Module module) {
        moduleScreen.setModule(module);
    }

    @Override
    public boolean keyPressed(int key, int i, int j) {
        if (super.keyPressed(key, i, j)) {
            return true;
        }

        if (selected != -1) {
            int curIndex = -1;
            List<Integer> selectable = new ArrayList<>();
            for (int index = 0; index < container.inventorySlots.size(); index++) {
                if (isValidItem(index)) {
                    selectable.add(index);
                    if (index == selected) {
                        curIndex = selectable.size()-1;
                    }
                }
            }

            if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_LEFT) {
                curIndex = curIndex == 0 ? curIndex + selectable.size() - 1 : curIndex - 1;
                select(selectable.get(curIndex % selectable.size()));
                return true;
            } else if (key == GLFW.GLFW_KEY_DOWN || key == GLFW.GLFW_KEY_RIGHT) {
                select(selectable.get((curIndex + 1) % selectable.size()));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // make sure we get the release event
        moduleScreen.onRelease(mouseX, mouseY);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        renderTitleText(MekanismLang.MODULE_TWEAKER.translate(), 6);
    }

    private void select(int index) {
        if (isValidItem(index)) {
            selected = index;
            scrollList.updateList(getStack(index), true);
        }
    }

    private boolean isValidItem(int index) {
        return getStack(index).getItem() instanceof IModuleContainerItem;
    }

    private ItemStack getStack(int index) {
        if (index == -1) {
            return ItemStack.EMPTY;
        }
        return container.inventorySlots.get(index).getStack();
    }
}
