package mekanism.client.gui.element.scroll;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.Modules;
import mekanism.common.content.gear.Modules.ModuleData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiModuleScrollList extends GuiScrollList {

    private static final ResourceLocation MODULE_SELECTION = MekanismUtils.getResource(ResourceType.GUI, "module_selection.png");
    private static final int TEXTURE_WIDTH = 100;
    private static final int TEXTURE_HEIGHT = 36;

    private int selectIndex = -1;

    private final Consumer<Module> callback;
    private final List<ModuleData<?>> currentList = new ArrayList<>();
    private final Supplier<ItemStack> itemSupplier;
    private ItemStack currentItem;

    public GuiModuleScrollList(IGuiWrapper gui, int x, int y, int width, int height, Supplier<ItemStack> itemSupplier, Consumer<Module> callback) {
        super(gui, x, y, width, height, TEXTURE_HEIGHT / 3, new GuiElementHolder(gui, x, y, width, height));
        this.itemSupplier = itemSupplier;
        this.callback = callback;
        updateList(itemSupplier.get(), true);
    }

    public void updateList(ItemStack currentItem, boolean forceReset) {
        ModuleData<?> prevSelect = getSelection();
        this.currentItem = currentItem;
        currentList.clear();
        currentList.addAll(Modules.loadAll(currentItem).stream().map(Module::getData).collect(Collectors.toList()));
        boolean selected = false;
        if (!forceReset && prevSelect != null) {
            for (int i = 0; i < currentList.size(); i++) {
                if (currentList.get(i) == prevSelect) {
                    setSelected(i);
                    selected = true;
                    break;
                }
            }
        }
        if (!selected) {
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
            callback.accept(Modules.load(currentItem, currentList.get(index)));
        }
    }

    @Nullable
    public ModuleData<?> getSelection() {
        return selectIndex == -1 ? null : currentList.get(selectIndex);
    }

    @Override
    public void clearSelection() {
        selectIndex = -1;
        callback.accept(null);
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        ItemStack stack = itemSupplier.get();
        if (!ItemStack.areItemStacksEqual(currentItem, stack)) {
            updateList(stack, false);
        }
        for (int i = 0; i < getFocusedElements(); i++) {
            int index = getCurrentSelection() + i;
            if (index > currentList.size() - 1) {
                break;
            }
            ModuleData<?> module = currentList.get(index);
            int multipliedElement = elementHeight * i;
            //Always render the name and module
            Module instance = Modules.load(currentItem, module);
            int color = module.isExclusive() ? (instance.isEnabled() ? 0x635BD4 : 0x2E2A69) : (instance.isEnabled() ? titleTextColor() : 0x5E1D1D);
            drawScaledTextScaledBound(matrix, TextComponentUtil.build(module), relativeX + 13, relativeY + 3 + multipliedElement, color, 86, 0.7F);
            renderModule(matrix, module, relativeX + 3, relativeY + 3 + multipliedElement, 0.5F);
        }
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        for (int i = 0; i < getFocusedElements(); i++) {
            int index = getCurrentSelection() + i;
            if (index > currentList.size() - 1) {
                break;
            }
            ModuleData<?> module = currentList.get(index);
            Module instance = Modules.load(currentItem, module);
            int multipliedElement = elementHeight * i;
            if (instance != null && mouseX >= relativeX + 1 && mouseX < relativeX + barXShift - 1 && mouseY >= relativeY + 1 + multipliedElement &&
                mouseY < relativeY + 1 + multipliedElement + elementHeight) {
                ITextComponent t = MekanismLang.GENERIC_FRACTION.translateColored(EnumColor.GRAY, instance.getInstalledCount(), module.getMaxStackSize());
                gui().displayTooltip(matrix, MekanismLang.MODULE_INSTALLED.translate(t), mouseX, mouseY, getGuiWidth());
            }
        }
    }

    @Override
    public void renderElements(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        //Draw elements
        minecraft.textureManager.bindTexture(MODULE_SELECTION);
        for (int i = 0; i < getFocusedElements(); i++) {
            int index = getCurrentSelection() + i;
            if (index > currentList.size() - 1) {
                break;
            }
            ModuleData<?> module = currentList.get(index);
            int shiftedY = y + 1 + elementHeight * i;
            int j = 1;
            if (module == getSelection()) {
                j = 2;
            } else if (mouseX >= x + 1 && mouseX < barX - 1 && mouseY >= shiftedY && mouseY < shiftedY + elementHeight) {
                j = 0;
            }
            blit(matrix, x + 1, shiftedY, 0, elementHeight * j, TEXTURE_WIDTH, elementHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            MekanismRenderer.resetColor();
        }
    }

    private void renderModule(MatrixStack matrix, ModuleData<?> type, int x, int y, float size) {
        gui().renderItem(matrix, type.getStack(), (int) (x / size), (int) (y / size), size);
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        GuiModuleScrollList old = (GuiModuleScrollList) element;
        setSelected(old.selectIndex);
    }
}