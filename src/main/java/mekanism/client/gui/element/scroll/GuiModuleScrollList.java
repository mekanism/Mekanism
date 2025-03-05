package mekanism.client.gui.element.scroll;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.ModuleData.ExclusiveFlag;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleContainer;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GuiModuleScrollList extends GuiInstallableScrollList<ModuleData<?>> {

    private static final ResourceLocation MODULE_SELECTION = MekanismUtils.getResource(ResourceType.GUI, "module_selection.png");

    private final Consumer<Module<?>> callback;
    private final List<ModuleData<?>> currentList = new ArrayList<>();
    private final Supplier<ItemStack> itemSupplier;
    private ItemStack currentItem;
    @Nullable
    private ModuleContainer currentContainer;

    @Nullable
    private Component lastInfo = null;
    @Nullable
    private Tooltip lastTooltip;

    public GuiModuleScrollList(IGuiWrapper gui, int x, int y, int height, Supplier<ItemStack> itemSupplier, Consumer<Module<?>> callback) {
        super(gui, x, y, height, GuiElementHolder.HOLDER, GuiElementHolder.HOLDER_SIZE, MODULE_SELECTION, 112, 36);
        this.itemSupplier = itemSupplier;
        this.callback = callback;
        updateItemAndList(itemSupplier.get());
    }

    public void updateItemAndList(ItemStack stack) {
        currentItem = stack;
        currentContainer = ModuleHelper.get().getModuleContainer(stack);
        currentList.clear();
        if (currentContainer != null) {
            currentList.addAll(currentContainer.moduleTypes());
        }
    }

    private void recheckItem() {
        ItemStack stack = itemSupplier.get();
        if (!ItemStack.matches(currentItem, stack)) {
            updateItemAndList(stack);
            ModuleData<?> prevSelect = getSelection();
            if (prevSelect != null) {
                if (currentList.contains(prevSelect)) {
                    //The item still supports the existing selection, mark that the selected data changed
                    onSelectedChange();
                } else {
                    //Otherwise, it doesn't have the same type still, we need to clear the current selection
                    clearSelection();
                }
            }
        }
    }

    @Override
    protected void setSelected(@Nullable ModuleData<?> newData) {
        if (selectedType != newData) {
            selectedType = newData;
            onSelectedChange();
        }
    }

    private void onSelectedChange() {
        callback.accept(getModule(selectedType));
    }

    @Nullable
    private Module<?> getModule(@Nullable ModuleData<?> data) {
        if (data == null || currentContainer == null) {
            return null;
        }
        return currentContainer.getRaw(data);
    }

    @Override
    protected List<ModuleData<?>> getCurrentInstalled() {
        return currentList;
    }

    @Override
    protected void drawName(GuiGraphics guiGraphics, ModuleData<?> module, int y) {
        IModule<?> instance = getModule(module);
        if (instance != null) {
            boolean enabled = instance.isEnabled();
            int color = module.isExclusive(ExclusiveFlag.ANY) ? (enabled ? 0x635BD4 : 0x2E2A69) : (enabled ? titleTextColor() : 0x5E1D1D);
            drawNameText(guiGraphics, y, TextComponentUtil.build(module), color, 0.7F);
        }
    }

    @Override
    protected ItemStack getRenderStack(ModuleData<?> moduleData) {
        return new ItemStack(moduleData.getItemHolder());
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        recheckItem();
        super.renderForeground(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        if (currentContainer != null && mouseX >= getX() + 1 && mouseX < getX() + barXShift - 1) {
            int currentSelection = getCurrentSelection();
            for (int i = 0, focused = getFocusedElements(); i < focused; i++) {
                int index = currentSelection + i;
                if (index > currentList.size() - 1) {
                    break;
                }
                ModuleData<?> module = currentList.get(index);
                int installed = currentContainer.installedCount(module);
                int multipliedElement = elementHeight * i;
                if (installed > 0 && mouseY >= getY() + 1 + multipliedElement && mouseY < getY() + 1 + multipliedElement + elementHeight) {
                    Component info = MekanismLang.MODULE_INSTALLED.translate(MekanismLang.GENERIC_FRACTION.translateColored(EnumColor.GRAY, installed, module.getMaxStackSize()));
                    if (!info.equals(lastInfo)) {
                        lastInfo = info;
                        lastTooltip = TooltipUtils.create(info);
                    }
                    cachedTooltipRect = new ScreenRectangle(getX() + 1, getY() + 1 + multipliedElement, barXShift - 2, elementHeight);
                    setTooltip(lastTooltip);
                    return;
                }
            }
        }
        cachedTooltipRect = null;
        lastInfo = null;
        setTooltip(lastTooltip = null);
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        GuiModuleScrollList old = (GuiModuleScrollList) element;
        if (ItemStack.matches(currentItem, old.currentItem)) {
            //If the item is the same just change what module data we have as our selected
            // and don't notify the callback with a fresh read of the module data as it
            // should have the data it expects already
            selectedType = old.selectedType;
        } else if (old.selectedType != null) {
            if (currentList.contains(old.selectedType)) {
                //If the item doesn't match (in general it will) and it still has the corresponding module,
                // we need to update the selected value and fire the corresponding callbacks
                setSelected(old.selectedType);
            } else {
                //If the data is no longer present we need to fire the callbacks to ensure we propagate the clear to the current selection
                onSelectedChange();
            }
        }
    }
}