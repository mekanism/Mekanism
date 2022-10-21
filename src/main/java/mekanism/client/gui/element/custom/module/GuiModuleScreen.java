package mekanism.client.gui.element.custom.module;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ObjIntConsumer;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData.ExclusiveFlag;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleColorData;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.IHasTextComponent;
import mekanism.client.gui.GuiModuleTweaker.ArmorPreview;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.scroll.GuiScrollList;
import mekanism.client.gui.element.scroll.GuiScrollableElement;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.DisableableModuleConfigItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiModuleScreen extends GuiScrollableElement {

    private static final int ELEMENT_SPACER = 4;

    final ObjIntConsumer<ModuleConfigItem<?>> saveCallback;
    private final ArmorPreview armorPreview;

    @Nullable
    private Module<?> currentModule;
    private List<MiniElement> miniElements = new ArrayList<>();
    private int maxElements;

    public GuiModuleScreen(IGuiWrapper gui, int x, int y, ObjIntConsumer<ModuleConfigItem<?>> saveCallback, ArmorPreview armorPreview) {
        this(gui, x, y, 102, 134, saveCallback, armorPreview);
    }

    private GuiModuleScreen(IGuiWrapper gui, int x, int y, int width, int height, ObjIntConsumer<ModuleConfigItem<?>> saveCallback, ArmorPreview armorPreview) {
        super(GuiScrollList.SCROLL_LIST, gui, x, y, width, height, width - 6, 2, 4, 4, height - 4);
        this.saveCallback = saveCallback;
        this.armorPreview = armorPreview;
    }

    @SuppressWarnings("unchecked")
    public void setModule(@Nullable Module<?> module) {
        List<MiniElement> newElements = new ArrayList<>();

        if (module != null) {
            int startY = getStartY(module);
            List<ModuleConfigItem<?>> configItems = module.getConfigItems();
            for (int i = 0, configItemsCount = configItems.size(); i < configItemsCount; i++) {
                ModuleConfigItem<?> configItem = configItems.get(i);
                MiniElement element = null;
                // Don't show the enabled option if this is enabled by default
                if (configItem.getData() instanceof ModuleBooleanData && (!configItem.getName().equals(Module.ENABLED_KEY) || !module.getData().isNoDisable())) {
                    if (configItem instanceof DisableableModuleConfigItem item && !item.isConfigEnabled()) {
                        //Skip options that are force disabled by the config
                        continue;
                    }
                    element = new BooleanToggle(this, (ModuleConfigItem<Boolean>) configItem, 2, startY, i);
                } else if (configItem.getData() instanceof ModuleEnumData) {
                    EnumToggle<?> toggle = createEnumToggle(configItem, 2, startY, i);
                    element = toggle;
                    // allow the dragger to continue sliding, even when we reset the config element
                    if (currentModule != null && currentModule.getData() == module.getData() && miniElements.get(i) instanceof EnumToggle<?> enumToggle) {
                        toggle.dragging = enumToggle.dragging;
                    }
                } else if (configItem.getData() instanceof ModuleColorData data) {
                    element = new ColorSelection(this, (ModuleConfigItem<Integer>) configItem, 2, startY, i, data.handlesAlpha(), armorPreview);
                }
                if (element != null) {
                    newElements.add(element);
                    startY += element.getNeededHeight() + ELEMENT_SPACER;
                }
            }
            maxElements = newElements.isEmpty() ? startY : startY - ELEMENT_SPACER;
        } else {
            maxElements = 0;
        }

        currentModule = module;
        miniElements = newElements;
    }

    @SuppressWarnings("unchecked")
    private <TYPE extends Enum<TYPE> & IHasTextComponent> EnumToggle<TYPE> createEnumToggle(ModuleConfigItem<?> data, int xPos, int yPos, int dataIndex) {
        return new EnumToggle<>(this, (ModuleConfigItem<TYPE>) data, xPos, yPos, dataIndex);
    }

    private static int getStartY(@Nullable IModule<?> module) {
        int startY = 5;
        if (module != null) {
            if (module.getData().isExclusive(ExclusiveFlag.ANY)) {
                startY += 13;
            }
            if (module.getData().getMaxStackSize() > 1) {
                startY += 13;
            }
        }
        return startY;
    }

    @Override
    protected int getMaxElements() {
        return maxElements;
    }

    @Override
    protected int getFocusedElements() {
        return height - 2;
    }

    @Override
    protected int getScrollElementScaler() {
        return 10;
    }

    int getScreenWidth() {
        //Actual width of screen not including scroll bar
        return barXShift;
    }

    @Nullable
    public IModule<?> getCurrentModule() {
        return currentModule;
    }

    @Override
    public void syncFrom(GuiElement element) {
        GuiModuleScreen old = (GuiModuleScreen) element;
        setModule(old.currentModule);
        super.syncFrom(element);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return isMouseOver(mouseX, mouseY) && adjustScroll(delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        //Shift the mouse y by the proper amount so that we click the correct spots
        mouseY += getCurrentSelection();
        for (MiniElement element : miniElements) {
            element.click(mouseX, mouseY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        //Shift the mouse y by the proper amount so that we click the correct spots
        mouseY += getCurrentSelection();
        for (MiniElement element : miniElements) {
            element.release(mouseX, mouseY);
        }
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
        //Shift the mouse y by the proper amount so that we click the correct spots
        mouseY += getCurrentSelection();
        for (MiniElement element : miniElements) {
            element.onDrag(mouseX, mouseY, deltaX, deltaY);
        }
    }

    @Override
    public void drawBackground(@NotNull PoseStack poseStack, int mx, int my, float partialTicks) {
        super.drawBackground(poseStack, mx, my, partialTicks);
        renderBackgroundTexture(poseStack, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
        drawScrollBar(poseStack, GuiScrollList.TEXTURE_WIDTH, GuiScrollList.TEXTURE_HEIGHT);
        //Draw contents
        scissorScreen(poseStack, mx, my, (matrix, mouseX, mouseY, module, shift) -> getStartY(module), MiniElement::renderBackground);
    }

    @Override
    public void renderForeground(PoseStack poseStack, int mx, int my) {
        super.renderForeground(poseStack, mx, my);
        scissorScreen(poseStack, mx, my, (matrix, mouseX, mouseY, module, shift) -> {
            int startY = 5;
            if (module != null) {
                if (module.getData().isExclusive(ExclusiveFlag.ANY)) {
                    if (startY + 13 > shift) {
                        drawTextWithScale(matrix, MekanismLang.MODULE_EXCLUSIVE.translate(), relativeX + 5, relativeY + startY, 0x635BD4, 0.8F);
                    }
                    startY += 13;
                }
                if (module.getData().getMaxStackSize() > 1) {
                    if (startY + 13 > shift) {
                        drawTextWithScale(matrix, MekanismLang.MODULE_INSTALLED.translate(module.getInstalledCount()), relativeX + 5, relativeY + startY,
                              screenTextColor(), 0.8F);
                    }
                    startY += 13;
                }
            }
            return startY;
        }, MiniElement::renderForeground);
    }

    private void scissorScreen(PoseStack matrix, int mouseX, int mouseY, ScissorRender renderer, ScissorMiniElementRender miniElementRender) {
        //Note: Scissor width at edge of monitor to make it, so we effectively only are scissoring height
        enableScissor(0, this.y + 1, minecraft.getWindow().getGuiScaledWidth(), this.y + this.height - 1);
        matrix.pushPose();
        int shift = getCurrentSelection();
        matrix.translate(0, -shift, 0);
        //Shift the mouse y by the proper amount
        mouseY += shift;

        //Draw any needed text and calculate where our elements will start rendering
        int startY = renderer.render(matrix, mouseX, mouseY, currentModule, shift);
        //Draw elements
        for (MiniElement element : miniElements) {
            if (startY >= shift + height) {
                //If we are past the max draw spot, stop attempting to draw
                break;
            } else if (startY + element.getNeededHeight() > shift) {
                //Only draw it if it would be in our view
                miniElementRender.render(element, matrix, mouseX, mouseY);
            }
            startY += element.getNeededHeight() + ELEMENT_SPACER;
        }

        matrix.popPose();
        disableScissor();
    }

    @FunctionalInterface
    private interface ScissorRender {

        int render(PoseStack matrix, int mouseX, int mouseY, @Nullable IModule<?> module, int shift);
    }

    @FunctionalInterface
    private interface ScissorMiniElementRender {

        void render(MiniElement element, PoseStack matrix, int mouseX, int mouseY);
    }
}