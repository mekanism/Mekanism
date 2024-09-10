package mekanism.client.gui.element.scroll;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiModuleScrollList extends GuiScrollList {

    private static final ResourceLocation MODULE_SELECTION = MekanismUtils.getResource(ResourceType.GUI, "module_selection.png");
    private static final int TEXTURE_WIDTH = 100;
    private static final int TEXTURE_HEIGHT = 36;

    private final Consumer<Module<?>> callback;
    private final List<ModuleData<?>> currentList = new ArrayList<>();
    private final Supplier<ItemStack> itemSupplier;
    private ItemStack currentItem;
    @Nullable
    private ModuleContainer currentContainer;
    @Nullable
    private ModuleData<?> selected;

    @Nullable
    private Component lastInfo = null;
    @Nullable
    private Tooltip lastTooltip;
    @Nullable
    private ScreenRectangle cachedTooltipRect;

    public GuiModuleScrollList(IGuiWrapper gui, int x, int y, int width, int height, Supplier<ItemStack> itemSupplier, Consumer<Module<?>> callback) {
        super(gui, x, y, width, height, TEXTURE_HEIGHT / 3, GuiElementHolder.HOLDER, GuiElementHolder.HOLDER_SIZE);
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
    protected int getMaxElements() {
        return currentList.size();
    }

    @Override
    public boolean hasSelection() {
        return selected != null;
    }

    @Override
    protected void setSelected(int index) {
        if (index >= 0 && index < currentList.size()) {
            setSelected(currentList.get(index));
        }
    }

    private void setSelected(@Nullable ModuleData<?> newData) {
        if (selected != newData) {
            selected = newData;
            onSelectedChange();
        }
    }

    private void onSelectedChange() {
        if (selected == null || currentContainer == null) {
            callback.accept(null);
        } else {
            callback.accept(currentContainer.get(selected));
        }
    }

    @Nullable
    public ModuleData<?> getSelection() {
        return selected;
    }

    @Override
    public void clearSelection() {
        setSelected(null);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        recheckItem();
        forEachModule((module, multipliedElement) -> {
            if (currentContainer != null) {
                IModule<?> instance = currentContainer.get(module);
                if (instance != null) {
                    boolean enabled = instance.isEnabled();
                    int color = module.isExclusive(ExclusiveFlag.ANY) ? (enabled ? 0x635BD4 : 0x2E2A69) : (enabled ? titleTextColor() : 0x5E1D1D);
                    drawScaledScrollingString(guiGraphics, TextComponentUtil.build(module), 12, 3 + multipliedElement, TextAlignment.LEFT, color, barXShift - 14, 2, false, 0.7F);
                }
            }
        });
    }

    @NotNull
    @Override
    protected ScreenRectangle getTooltipRectangle(int mouseX, int mouseY) {
        return cachedTooltipRect == null ? super.getTooltipRectangle(mouseX, mouseY) : cachedTooltipRect;
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        if (currentContainer != null && mouseX >= getX() + 1 && mouseX < getX() + barXShift - 1) {
            for (int i = 0; i < getFocusedElements(); i++) {
                int index = getCurrentSelection() + i;
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
    public void renderElements(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //Draw elements
        forEachModule((module, multipliedElement) -> {
            int shiftedY = getY() + 1 + multipliedElement;
            int j = 1;
            if (module == getSelection()) {
                j = 2;
            } else if (mouseX >= getX() + 1 && mouseX < getX() + barXShift - 1 && mouseY >= shiftedY && mouseY < shiftedY + elementHeight) {
                j = 0;
            }
            guiGraphics.blit(MODULE_SELECTION, relativeX + 1, relativeY + 1 + multipliedElement, 0, elementHeight * j, TEXTURE_WIDTH, elementHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        });
        //Note: This needs to be in its own loop as rendering the items is likely to cause the texture manager to be bound to a different texture
        // and thus would make the selection area background get all screwed up
        forEachModule((module, multipliedElement) -> gui().renderItem(guiGraphics, module.getItemProvider().getItemStack(), relativeX + 3, relativeY + 3 + multipliedElement, 0.5F));
    }

    private void forEachModule(ObjIntConsumer<ModuleData<?>> consumer) {
        int currentSelection = getCurrentSelection();
        int max = Math.min(getFocusedElements(), currentList.size());
        for (int i = 0; i < max; i++) {
            consumer.accept(currentList.get(currentSelection + i), elementHeight * i);
        }
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        GuiModuleScrollList old = (GuiModuleScrollList) element;
        if (ItemStack.matches(currentItem, old.currentItem)) {
            //If the item is the same just change what module data we have as our selected
            // and don't notify the callback with a fresh read of the module data as it
            // should have the data it expects already
            selected = old.selected;
        } else if (old.selected != null) {
            if (currentList.contains(old.selected)) {
                //If the item doesn't match (in general it will) and it still has the corresponding module,
                // we need to update the selected value and fire the corresponding callbacks
                setSelected(old.selected);
            } else {
                //If the data is no longer present we need to fire the callbacks to ensure we propagate the clear to the current selection
                onSelectedChange();
            }
        }
    }
}