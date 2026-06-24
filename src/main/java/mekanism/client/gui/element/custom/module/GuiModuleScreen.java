package mekanism.client.gui.element.custom.module;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.ModuleData.ExclusiveFlag;
import mekanism.api.gear.config.ModuleBooleanConfig;
import mekanism.api.gear.config.ModuleColorConfig;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.api.gear.config.ModuleEnumConfig;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.GuiModuleTweaker.ArmorPreview;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.scroll.GuiScrollList;
import mekanism.client.gui.element.scroll.GuiScrollableElement;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2fStack;

//TODO: Eventually try to add support for defining ways to render custom config types
public class GuiModuleScreen extends GuiScrollableElement {

    private static final int ELEMENT_SPACER = 4;

    final Consumer<ModuleConfig<?>> saveCallback;
    private final Supplier<ItemResource> itemSupplier;
    private final ArmorPreview armorPreview;

    @Nullable
    private Module<?> currentModule;
    private Map<Identifier, MiniElement<?>> miniElements = new LinkedHashMap<>();
    private int maxElements;

    public GuiModuleScreen(IGuiWrapper gui, int x, int y, Supplier<ItemResource> itemSupplier, Consumer<ModuleConfig<?>> saveCallback, ArmorPreview armorPreview) {
        this(gui, x, y, 108, 134, itemSupplier, saveCallback, armorPreview);
    }

    private GuiModuleScreen(IGuiWrapper gui, int x, int y, int width, int height, Supplier<ItemResource> itemSupplier, Consumer<ModuleConfig<?>> saveCallback, ArmorPreview armorPreview) {
        super(gui, x, y, width, height, width - 6, 2, 4, 4, height - 4);
        this.itemSupplier = itemSupplier;
        this.saveCallback = saveCallback;
        this.armorPreview = armorPreview;
    }

    public void setModule(@Nullable Module<?> module) {
        Map<Identifier, MiniElement<?>> newElements = new LinkedHashMap<>();

        if (module != null) {
            ModuleData<?> untypedData = module.getUntypedData();
            int startY = getStartY(untypedData);
            for (ModuleConfig<?> configItem : module.getConfigs()) {
                if (configItem.isConfigDisabled()) {
                    //Skip options that are force disabled by the config
                    continue;
                }
                Component description = TextComponentUtil.translate(Util.makeDescriptionId("module", configItem.name()));
                Identifier name = configItem.name();
                MiniElement<?> element = switch (configItem) {
                    // Don't show the enabled option if this is enabled by default
                    case ModuleBooleanConfig config when !name.equals(ModuleConfig.ENABLED_KEY) || !untypedData.isNoDisable() ->
                          new BooleanToggle(this, config, description, 2, startY);
                    case ModuleEnumConfig<?> config -> {
                        EnumToggle<?> toggle = new EnumToggle<>(this, config, description, 2, startY);
                        // allow the dragger to continue sliding, even when we reset the config element
                        if (currentModule != null && currentModule.getUntypedData() == untypedData && miniElements.get(name) instanceof EnumToggle<?> enumToggle) {
                            toggle.dragging = enumToggle.dragging;
                        }
                        yield toggle;
                    }
                    case ModuleColorConfig config -> new ColorSelection(this, config, description, 2, startY, armorPreview);
                    default -> null;
                };
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

    private static int getStartY(@Nullable IModule<?> module) {
        if (module == null) {
            return ELEMENT_SPACER + 1;
        }
        return getStartY(module.getUntypedData());
    }

    private static int getStartY(ModuleData<?> untypedData) {
        int startY = ELEMENT_SPACER + 1;
        if (untypedData.isExclusive(ExclusiveFlag.ANY)) {
            startY += 13;
        }
        if (untypedData.getMaxStackSize() > 1) {
            startY += 13;
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

    public ItemResource getContainerType() {
        return itemSupplier.get();
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
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        super.onClick(event, isDoubleClick);
        //Shift the mouse y by the proper amount so that we click the correct spots
        double mouseY = event.y() + getCurrentSelection();
        for (MiniElement<?> element : miniElements.values()) {
            element.click(event.x(), mouseY);
        }
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        super.onRelease(event);
        //Shift the mouse y by the proper amount so that we click the correct spots
        double mouseY = event.y() + getCurrentSelection();
        for (MiniElement<?> element : miniElements.values()) {
            element.release(event.x(), mouseY);
        }
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double deltaX, double deltaY) {
        super.onDrag(event, deltaX, deltaY);
        double mouseX = event.x();
        //Shift the mouse y by the proper amount so that we click the correct spots
        double mouseY = event.y() + getCurrentSelection();
        for (MiniElement<?> element : miniElements.values()) {
            element.onDrag(mouseX, mouseY, deltaX, deltaY);
        }
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mx, int my, float partialTicks) {
        super.drawBackground(guiGraphics, mx, my, partialTicks);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, GuiInnerScreen.SCREEN, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
        drawScrollBar(guiGraphics);
        //Draw contents
        scissorScreen(guiGraphics, mx, my, (_, module, _) -> getStartY(module), MiniElement::renderBackground);
    }

    @Override
    public void renderForeground(GuiGraphicsExtractor guiGraphics, int mx, int my) {
        super.renderForeground(guiGraphics, mx, my);
        scissorScreen(guiGraphics, mx, my, (g, module, shift) -> {
            int startY = ELEMENT_SPACER + 1;
            if (module != null) {
                if (module.getUntypedData().isExclusive(ExclusiveFlag.ANY)) {
                    if (startY + 13 > shift) {
                        drawScaledScrollingString(g, MekanismLang.MODULE_EXCLUSIVE.translate(), 2, startY, TextAlignment.LEFT, 0xFF635BD4,
                              getScreenWidth() - GuiScrollList.TEXTURE_WIDTH, 2, false, 0.8F);
                    }
                    startY += 13;
                }
                if (module.getUntypedData().getMaxStackSize() > 1) {
                    if (startY + 13 > shift) {
                        drawScaledScrollingString(g, MekanismLang.MODULE_INSTALLED.translate(module.getInstalledCount()), 2, startY, TextAlignment.LEFT, screenTextColor(),
                              getScreenWidth() - GuiScrollList.TEXTURE_WIDTH, 2, false, 0.8F);
                    }
                    startY += 13;
                }
            }
            return startY;
        }, MiniElement::renderForeground);
    }

    private void scissorScreen(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, ScissorRender renderer, ScissorMiniElementRender miniElementRender) {
        //Note: Scissor width at edge of monitor to make it, so we effectively only are scissoring height
        guiGraphics.enableScissor(relativeX, relativeY + 1, guiGraphics.guiWidth() - relativeX, relativeY + height - 1);
        Matrix3x2fStack matrix = guiGraphics.pose();
        matrix.pushMatrix();
        int shift = getCurrentSelection();
        matrix.translate(0, -shift);
        //Shift the mouse y by the proper amount
        mouseY += shift;

        //Draw any needed text and calculate where our elements will start rendering
        int startY = renderer.render(guiGraphics, currentModule, shift);
        //Draw elements
        for (MiniElement<?> element : miniElements.values()) {
            if (startY >= shift + height) {
                //If we are past the max draw spot, stop attempting to draw
                break;
            } else if (startY + element.getNeededHeight() > shift) {
                //Only draw it if it would be in our view
                miniElementRender.render(element, guiGraphics, mouseX, mouseY);
            }
            startY += element.getNeededHeight() + ELEMENT_SPACER;
        }

        matrix.popMatrix();
        guiGraphics.disableScissor();
    }

    @FunctionalInterface
    private interface ScissorRender {

        int render(GuiGraphicsExtractor guiGraphics, @Nullable IModule<?> module, int shift);
    }

    @FunctionalInterface
    private interface ScissorMiniElementRender {

        void render(MiniElement<?> element, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);
    }
}