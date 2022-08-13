package mekanism.client.gui.element.scroll;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.ModuleData.ExclusiveFlag;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
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
    private ModuleData<?> selected;

    public GuiModuleScrollList(IGuiWrapper gui, int x, int y, int width, int height, Supplier<ItemStack> itemSupplier, Consumer<Module<?>> callback) {
        super(gui, x, y, width, height, TEXTURE_HEIGHT / 3, GuiElementHolder.HOLDER, GuiElementHolder.HOLDER_SIZE);
        this.itemSupplier = itemSupplier;
        this.callback = callback;
        updateItemAndList(itemSupplier.get());
    }

    public void updateItemAndList(ItemStack stack) {
        currentItem = stack;
        currentList.clear();
        currentList.addAll(MekanismAPI.getModuleHelper().loadAllTypes(currentItem));
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
        if (selected == null) {
            callback.accept(null);
        } else {
            callback.accept(ModuleHelper.INSTANCE.load(currentItem, selected));
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
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        recheckItem();
        forEachModule((module, multipliedElement) -> {
            IModule<?> instance = MekanismAPI.getModuleHelper().load(currentItem, module);
            if (instance != null) {
                int color = module.isExclusive(ExclusiveFlag.ANY) ? (instance.isEnabled() ? 0x635BD4 : 0x2E2A69) : (instance.isEnabled() ? titleTextColor() : 0x5E1D1D);
                drawScaledTextScaledBound(matrix, TextComponentUtil.build(module), relativeX + 13, relativeY + 3 + multipliedElement, color, 86, 0.7F);
            }
        });
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        if (mouseX >= x + 1 && mouseX < x + barXShift - 1) {
            forEachModule((module, multipliedElement) -> {
                IModule<?> instance = MekanismAPI.getModuleHelper().load(currentItem, module);
                if (instance != null && mouseY >= y + 1 + multipliedElement && mouseY < y + 1 + multipliedElement + elementHeight) {
                    Component t = MekanismLang.GENERIC_FRACTION.translateColored(EnumColor.GRAY, instance.getInstalledCount(), module.getMaxStackSize());
                    displayTooltips(matrix, mouseX, mouseY, MekanismLang.MODULE_INSTALLED.translate(t));
                }
            });
        }
    }

    @Override
    public void renderElements(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        //Draw elements
        RenderSystem.setShaderTexture(0, MODULE_SELECTION);
        forEachModule((module, multipliedElement) -> {
            int shiftedY = y + 1 + multipliedElement;
            int j = 1;
            if (module == getSelection()) {
                j = 2;
            } else if (mouseX >= x + 1 && mouseX < barX - 1 && mouseY >= shiftedY && mouseY < shiftedY + elementHeight) {
                j = 0;
            }
            blit(matrix, x + 1, shiftedY, 0, elementHeight * j, TEXTURE_WIDTH, elementHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            MekanismRenderer.resetColor();
        });
        //Note: This needs to be in its own loop as rendering the items is likely to cause the texture manager to be bound to a different texture
        // and thus would make the selection area background get all screwed up
        forEachModule((module, multipliedElement) -> gui().renderItem(matrix, module.getItemProvider().getItemStack(), x + 3, y + 3 + multipliedElement, 0.5F));
    }

    private void forEachModule(ObjIntConsumer<ModuleData<?>> consumer) {
        for (int i = 0; i < getFocusedElements(); i++) {
            int index = getCurrentSelection() + i;
            if (index > currentList.size() - 1) {
                break;
            }
            consumer.accept(currentList.get(index), elementHeight * i);
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