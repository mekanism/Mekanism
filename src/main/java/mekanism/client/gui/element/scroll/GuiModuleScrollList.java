package mekanism.client.gui.element.scroll;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.Modules;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiModuleScrollList extends GuiScrollList {

    private static ResourceLocation MODULE_SELECTION = MekanismUtils.getResource(ResourceType.GUI, "module_selection.png");
    private static int TEXTURE_WIDTH = 100;
    private static int TEXTURE_HEIGHT = 36;

    @Nullable
    private int selectIndex;

    private Consumer<Module> callback;
    private List<Module> currentList = new ArrayList<>();
    private Supplier<ItemStack> itemSupplier;
    private ItemStack currentItem;
    private boolean resetOnChanged = false;

    public GuiModuleScrollList(IGuiWrapper gui, int x, int y, int width, int height, Supplier<ItemStack> itemSupplier, Consumer<Module> callback) {
        super(gui, x, y, width, height, TEXTURE_HEIGHT / 3, new GuiElementHolder(gui, x, y, width, height));
        this.itemSupplier = itemSupplier;
        this.callback = callback;
        updateList(itemSupplier.get(), true);
    }

    public void setResetOnChanged() {
        resetOnChanged = true;
    }

    public void updateList(ItemStack currentItem, boolean resetSelect) {
        this.currentItem = currentItem;
        currentList.clear();
        currentList.addAll(Modules.loadAll(currentItem));
        if (!currentList.isEmpty() && !resetSelect && selectIndex >= 0 && selectIndex < currentList.size()) {
            setSelected(selectIndex);
        } else {
            clearSelection();
        }
    }

    @Override
    protected int getMaxElements() {
        return currentList.size();
    }

    @Override
    public boolean hasSelection() {
        return selectIndex != -1;
    }

    @Override
    protected void setSelected(int index) {
        if (index >= 0 && index < currentList.size()) {
            selectIndex = index;
            callback.accept(currentList.get(index));
        }
    }

    @Nullable
    public Module getSelection() {
        return selectIndex == -1 ? null : currentList.get(selectIndex);
    }

    @Override
    public void clearSelection() {
        selectIndex = -1;
        callback.accept(null);
    }

    @Override
    public void renderForeground(int mouseX, int mouseY, int xAxis, int yAxis) {
        super.renderForeground(mouseX, mouseY, xAxis, yAxis);
        ItemStack stack = itemSupplier.get();
        if (!ItemStack.areItemStacksEqual(currentItem, stack)) {
            updateList(stack, resetOnChanged);
        }
        for (int i = 0; i < getFocusedElements(); i++) {
            int index = getCurrentSelection() + i;
            if (index > currentList.size() - 1) {
                break;
            }
            Module module = currentList.get(index);
            int multipliedElement = elementHeight * i;
            //Always render the name and module
            renderScaledText(TextComponentUtil.build(module.getData()), relativeX + 13, relativeY + 3 + multipliedElement, 0x404040, 86);
            renderModule(module, relativeX + 3, relativeY + 3 + multipliedElement, 0.5F);
            //Only render the tooltip describing the module when hovering over it though
            if (mouseX >= x + 1 && mouseX < barX - 1 && mouseY >= y + 1 + multipliedElement && mouseY < y + 1 + multipliedElement + elementHeight) {
                guiObj.displayTooltip(module.getData().getDescription(), xAxis, yAxis, guiObj.getWidth());
            }
        }
    }

    @Override
    public void renderElements(int mouseX, int mouseY, float partialTicks) {
        //Draw elements
        minecraft.textureManager.bindTexture(MODULE_SELECTION);
        for (int i = 0; i < getFocusedElements(); i++) {
            int index = getCurrentSelection() + i;
            if (index > currentList.size() - 1) {
                break;
            }
            Module module = currentList.get(index);
            int shiftedY = y + 1 + elementHeight * i;
            int j = 1;
            if (module == getSelection()) {
                j = 2;
            } else if (mouseX >= x + 1 && mouseX < barX - 1 && mouseY >= shiftedY && mouseY < shiftedY + elementHeight) {
                j = 0;
            }
            blit(x + 1, shiftedY, 0, elementHeight * j, TEXTURE_WIDTH, elementHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            MekanismRenderer.resetColor();
        }
    }

    private void renderModule(Module type, int x, int y, float size) {
        guiObj.renderItem(type.getData().getStack(), (int) (x / size), (int) (y / size), size);
    }
}
