package mekanism.client.gui.element.custom.module;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
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
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiModuleScreen extends GuiScrollableElement {

    private static final int ELEMENT_SPACER = 4;

    final Consumer<ModuleConfigItem<?>> saveCallback;
    private final ArmorPreview armorPreview;

    @Nullable
    private Module<?> currentModule;
    private Map<String, MiniElement> miniElements = new LinkedHashMap<>();
    private int maxElements;

    public GuiModuleScreen(IGuiWrapper gui, int x, int y, Consumer<ModuleConfigItem<?>> saveCallback, ArmorPreview armorPreview) {
        this(gui, x, y, 102, 134, saveCallback, armorPreview);
    }

    private GuiModuleScreen(IGuiWrapper gui, int x, int y, int width, int height, Consumer<ModuleConfigItem<?>> saveCallback, ArmorPreview armorPreview) {
        super(GuiScrollList.SCROLL_LIST, gui, x, y, width, height, width - 6, 2, 4, 4, height - 4);
        this.saveCallback = saveCallback;
        this.armorPreview = armorPreview;
    }

    @SuppressWarnings("unchecked")
    public void setModule(@Nullable Module<?> module) {
        Map<String, MiniElement> newElements = new LinkedHashMap<>();

        if (module != null) {
            int startY = getStartY(module);
            for (ModuleConfigItem<?> configItem : module.getConfigItems()) {
                String name = configItem.getName();
                MiniElement element = null;
                // Don't show the enabled option if this is enabled by default
                if (configItem.getData() instanceof ModuleBooleanData && (!name.equals(Module.ENABLED_KEY) || !module.getData().isNoDisable())) {
                    if (configItem instanceof DisableableModuleConfigItem item && !item.isConfigEnabled()) {
                        //Skip options that are force disabled by the config
                        continue;
                    }
                    element = new BooleanToggle(this, (ModuleConfigItem<Boolean>) configItem, 2, startY);
                } else if (configItem.getData() instanceof ModuleEnumData) {
                    EnumToggle<?> toggle = createEnumToggle(configItem, 2, startY);
                    element = toggle;
                    // allow the dragger to continue sliding, even when we reset the config element
                    if (currentModule != null && currentModule.getData() == module.getData() && miniElements.get(name) instanceof EnumToggle<?> enumToggle) {
                        toggle.dragging = enumToggle.dragging;
                    }
                } else if (configItem.getData() instanceof ModuleColorData data) {
                    element = new ColorSelection(this, (ModuleConfigItem<Integer>) configItem, 2, startY, data.handlesAlpha(), armorPreview);
                }
                if (element != null) {
                    newElements.put(name, element);
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
    private <TYPE extends Enum<TYPE> & IHasTextComponent> EnumToggle<TYPE> createEnumToggle(ModuleConfigItem<?> data, int xPos, int yPos) {
        return new EnumToggle<>(this, (ModuleConfigItem<TYPE>) data, xPos, yPos);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        return isMouseOver(mouseX, mouseY) && adjustScroll(yDelta) || super.mouseScrolled(mouseX, mouseY, xDelta, yDelta);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        super.onClick(mouseX, mouseY, button);
        //Shift the mouse y by the proper amount so that we click the correct spots
        mouseY += getCurrentSelection();
        for (MiniElement element : miniElements.values()) {
            element.click(mouseX, mouseY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        //Shift the mouse y by the proper amount so that we click the correct spots
        mouseY += getCurrentSelection();
        for (MiniElement element : miniElements.values()) {
            element.release(mouseX, mouseY);
        }
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
        //Shift the mouse y by the proper amount so that we click the correct spots
        mouseY += getCurrentSelection();
        for (MiniElement element : miniElements.values()) {
            element.onDrag(mouseX, mouseY, deltaX, deltaY);
        }
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mx, int my, float partialTicks) {
        super.drawBackground(guiGraphics, mx, my, partialTicks);
        renderBackgroundTexture(guiGraphics, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
        drawScrollBar(guiGraphics, GuiScrollList.TEXTURE_WIDTH, GuiScrollList.TEXTURE_HEIGHT);
        //Draw contents
        scissorScreen(guiGraphics, mx, my, (g, mouseX, mouseY, module, shift) -> getStartY(module), MiniElement::renderBackground);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mx, int my) {
        super.renderForeground(guiGraphics, mx, my);
        scissorScreen(guiGraphics, mx, my, (g, mouseX, mouseY, module, shift) -> {
            int startY = 5;
            if (module != null) {
                if (module.getData().isExclusive(ExclusiveFlag.ANY)) {
                    if (startY + 13 > shift) {
                        drawTextWithScale(g, MekanismLang.MODULE_EXCLUSIVE.translate(), relativeX + 5, relativeY + startY, 0x635BD4, 0.8F);
                    }
                    startY += 13;
                }
                if (module.getData().getMaxStackSize() > 1) {
                    if (startY + 13 > shift) {
                        drawTextWithScale(g, MekanismLang.MODULE_INSTALLED.translate(module.getInstalledCount()), relativeX + 5, relativeY + startY,
                              screenTextColor(), 0.8F);
                    }
                    startY += 13;
                }
            }
            return startY;
        }, MiniElement::renderForeground);
    }

    private void scissorScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, ScissorRender renderer, ScissorMiniElementRender miniElementRender) {
        //Note: Scissor width at edge of monitor to make it, so we effectively only are scissoring height
        guiGraphics.enableScissor(0, getY() + 1, guiGraphics.guiWidth(), getBottom() - 1);
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        int shift = getCurrentSelection();
        pose.translate(0, -shift, 0);
        //Shift the mouse y by the proper amount
        mouseY += shift;

        //Draw any needed text and calculate where our elements will start rendering
        int startY = renderer.render(guiGraphics, mouseX, mouseY, currentModule, shift);
        //Draw elements
        for (MiniElement element : miniElements.values()) {
            if (startY >= shift + height) {
                //If we are past the max draw spot, stop attempting to draw
                break;
            } else if (startY + element.getNeededHeight() > shift) {
                //Only draw it if it would be in our view
                miniElementRender.render(element, guiGraphics, mouseX, mouseY);
            }
            startY += element.getNeededHeight() + ELEMENT_SPACER;
        }

        pose.popPose();
        guiGraphics.disableScissor();
    }

    @FunctionalInterface
    private interface ScissorRender {

        int render(GuiGraphics guiGraphics, int mouseX, int mouseY, @Nullable IModule<?> module, int shift);
    }

    @FunctionalInterface
    private interface ScissorMiniElementRender {

        void render(MiniElement element, GuiGraphics guiGraphics, int mouseX, int mouseY);
    }
}