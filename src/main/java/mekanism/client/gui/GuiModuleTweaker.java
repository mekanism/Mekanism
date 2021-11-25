package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.custom.GuiModuleScreen;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.window.GuiMekaSuitHelmetOptions;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismItems;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiModuleTweaker extends GuiMekanism<ModuleTweakerContainer> {

    private GuiModuleScrollList scrollList;
    private GuiModuleScreen moduleScreen;
    private TranslationButton optionsButton;

    private int selected = -1;

    public GuiModuleTweaker(ModuleTweakerContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        imageWidth = 248;
        imageHeight += 20;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        moduleScreen = addButton(new GuiModuleScreen(this, 138, 20, () -> menu.slots.get(selected).getSlotIndex()));
        scrollList = addButton(new GuiModuleScrollList(this, 30, 20, 108, 116, () -> getStack(selected), this::onModuleSelected));
        addButton(new GuiElementHolder(this, 30, 136, 108, 18));
        optionsButton = addButton(new TranslationButton(this, 31, 137, 106, 16, MekanismLang.BUTTON_OPTIONS, this::openOptions));
        optionsButton.active = false;
        int size = menu.slots.size();
        for (int i = 0; i < size; i++) {
            Slot slot = menu.slots.get(i);
            final int index = i;
            // initialize selected item
            if (selected == -1 && isValidItem(index)) {
                select(index);
            }
            addButton(new GuiSlot(SlotType.NORMAL, this, slot.x - 1, slot.y - 1)
                  .click((e, x, y) -> select(index))
                  .overlayColor(isValidItem(index) ? null : () -> 0xCC333333)
                  .with(() -> index == selected ? SlotOverlay.SELECT : null));
        }
    }

    private void onModuleSelected(Module<?> module) {
        moduleScreen.setModule(module);
    }

    private void openOptions() {
        addWindow(new GuiMekaSuitHelmetOptions(this, getWidth() / 2 - 140 / 2, getHeight() / 2 - 90 / 2));
    }

    @Override
    public boolean keyPressed(int key, int i, int j) {
        if (super.keyPressed(key, i, j)) {
            return true;
        }
        if (selected != -1 && (isPreviousButton(key) || isNextButton(key))) {
            int curIndex = -1;
            IntList selectable = new IntArrayList();
            for (int index = 0, slots = menu.slots.size(); index < slots; index++) {
                if (isValidItem(index)) {
                    selectable.add(index);
                    if (index == selected) {
                        curIndex = selectable.size() - 1;
                    }
                }
            }
            int targetIndex;
            if (isPreviousButton(key)) {
                targetIndex = curIndex == 0 ? selectable.size() - 1 : curIndex - 1;
            } else {//isNextButton
                targetIndex = curIndex + 1;
            }
            select(selectable.getInt(targetIndex % selectable.size()));
            return true;
        }
        return false;
    }

    private boolean isPreviousButton(int key) {
        return key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_LEFT;
    }

    private boolean isNextButton(int key) {
        return key == GLFW.GLFW_KEY_DOWN || key == GLFW.GLFW_KEY_RIGHT;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // make sure we get the release event
        moduleScreen.onRelease(mouseX, mouseY);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, MekanismLang.MODULE_TWEAKER.translate(), titleLabelY);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    private void select(int index) {
        if (isValidItem(index)) {
            selected = index;
            ItemStack stack = getStack(index);
            scrollList.updateList(stack, true);
            optionsButton.active = stack.getItem() == MekanismItems.MEKASUIT_HELMET.get();
        }
    }

    private boolean isValidItem(int index) {
        return ModuleTweakerContainer.isTweakableItem(getStack(index));
    }

    private ItemStack getStack(int index) {
        if (index == -1) {
            return ItemStack.EMPTY;
        }
        return menu.slots.get(index).getItem();
    }
}
