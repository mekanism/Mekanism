package mekanism.client.gui.element.custom.module;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Eventually try to add support for defining ways to render custom config types
public class GuiModuleScreen extends GuiScrollableElement {

    private static final int ELEMENT_SPACER = 4;

    final Consumer<ModuleConfig<?>> saveCallback;
    private final Supplier<ItemStack> itemSupplier;
    private final ArmorPreview armorPreview;

    @Nullable
    private Module<?> currentModule;
    private Map<ResourceLocation, MiniElement<?>> miniElements = new LinkedHashMap<>();
    private int maxElements;

    public GuiModuleScreen(IGuiWrapper gui, int x, int y, Supplier<ItemStack> itemSupplier, Consumer<ModuleConfig<?>> saveCallback, ArmorPreview armorPreview) {
        this(gui, x, y, 108, 134, itemSupplier, saveCallback, armorPreview);
    }

    private GuiModuleScreen(IGuiWrapper gui, int x, int y, int width, int height, Supplier<ItemStack> itemSupplier, Consumer<ModuleConfig<?>> saveCallback, ArmorPreview armorPreview) {
        super(GuiScrollList.SCROLL_LIST, gui, x, y, width, height, width - 6, 2, 4, 4, height - 4);
        this.itemSupplier = itemSupplier;
        this.saveCallback = saveCallback;
        this.armorPreview = armorPreview;
    }

    public void setModule(@Nullable Module<?> module) {
        Map<ResourceLocation, MiniElement<?>> newElements = new LinkedHashMap<>();

        if (module != null) {
            ModuleData<?> untypedData = module.getUntypedData();
            int startY = getStartY(untypedData);
            for (ModuleConfig<?> configItem : module.getConfigs()) {
                if (configItem.isConfigDisabled()) {
                    //Skip options that are force disabled by the config
                    continue;
                }
                Component description = TextComponentUtil.translate(Util.makeDescriptionId("module", configItem.name()));
                ResourceLocation name = configItem.name();
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

    public ItemStack getContainerStack() {
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
    public void onClick(double mouseX, double mouseY, int button) {
        super.onClick(mouseX, mouseY, button);
        //Shift the mouse y by the proper amount so that we click the correct spots
        mouseY += getCurrentSelection();
        for (MiniElement<?> element : miniElements.values()) {
            element.click(mouseX, mouseY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        //Shift the mouse y by the proper amount so that we click the correct spots
        mouseY += getCurrentSelection();
        for (MiniElement<?> element : miniElements.values()) {
            element.release(mouseX, mouseY);
        }
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
        //Shift the mouse y by the proper amount so that we click the correct spots
        mouseY += getCurrentSelection();
        for (MiniElement<?> element : miniElements.values()) {
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
            int startY = ELEMENT_SPACER + 1;
            if (module != null) {
                if (module.getUntypedData().isExclusive(ExclusiveFlag.ANY)) {
                    if (startY + 13 > shift) {
                        drawScaledScrollingString(g, MekanismLang.MODULE_EXCLUSIVE.translate(), 2, startY, TextAlignment.LEFT, 0x635BD4,
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

        pose.popPose();
        guiGraphics.disableScissor();
    }

    @FunctionalInterface
    private interface ScissorRender {

        int render(GuiGraphics guiGraphics, int mouseX, int mouseY, @Nullable IModule<?> module, int shift);
    }

    @FunctionalInterface
    private interface ScissorMiniElementRender {

        void render(MiniElement<?> element, GuiGraphics guiGraphics, int mouseX, int mouseY);
    }
}