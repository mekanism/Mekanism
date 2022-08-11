package mekanism.client.gui.element.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData.ExclusiveFlag;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigData;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.IHasTextComponent;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.scroll.GuiScrollList;
import mekanism.client.gui.element.scroll.GuiScrollableElement;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.DisableableModuleConfigItem;
import mekanism.common.network.to_server.PacketUpdateModuleSettings;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiModuleScreen extends GuiScrollableElement {

    private static final ResourceLocation RADIO = MekanismUtils.getResource(ResourceType.GUI, "radio_button.png");
    private static final ResourceLocation SLIDER = MekanismUtils.getResource(ResourceType.GUI, "slider.png");
    private static final int ELEMENT_SPACER = 4;

    private final IntSupplier slotIdSupplier;

    private IModule<?> currentModule;
    private List<MiniElement> miniElements = new ArrayList<>();
    private int maxElements;

    public GuiModuleScreen(IGuiWrapper gui, int x, int y, IntSupplier slotIdSupplier) {
        this(gui, x, y, 102, 134, slotIdSupplier);
    }

    private GuiModuleScreen(IGuiWrapper gui, int x, int y, int width, int height, IntSupplier slotIdSupplier) {
        super(GuiScrollList.SCROLL_LIST, gui, x, y, width, height, width - 6, 2, 4, 4, height - 4);
        this.slotIdSupplier = slotIdSupplier;
    }

    private Runnable getCallback(ModuleConfigData<?> configData, int dataIndex) {
        return () -> {
            if (currentModule != null) {//Shouldn't be null but validate just in case
                Mekanism.packetHandler().sendToServer(PacketUpdateModuleSettings.create(slotIdSupplier.getAsInt(), currentModule.getData(), dataIndex, configData));
            }
        };
    }

    @SuppressWarnings("unchecked")
    public void setModule(Module<?> module) {
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
                        //TODO: Eventually we may want to make it slightly "faster" in that it allows updating the toggle elements rather than just
                        // not adding them back when switching to another module and then back again
                        continue;
                    }
                    element = new BooleanToggle((ModuleConfigItem<Boolean>) configItem, 2, startY, i);
                } else if (configItem.getData() instanceof ModuleEnumData) {
                    EnumToggle<?> toggle = createEnumToggle(configItem, 2, startY, i);
                    element = toggle;
                    // allow the dragger to continue sliding, even when we reset the config element
                    if (currentModule != null && currentModule.getData() == module.getData() && miniElements.get(i) instanceof EnumToggle<?> enumToggle) {
                        toggle.dragging = enumToggle.dragging;
                    }
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
        return new EnumToggle<>((ModuleConfigItem<TYPE>) data, xPos, yPos, dataIndex);
    }

    private int getStartY(@Nullable IModule<?> module) {
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

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        renderBackgroundTexture(matrix, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
        drawScrollBar(matrix, GuiScrollList.TEXTURE_WIDTH, GuiScrollList.TEXTURE_HEIGHT);

        //Draw contents
        //Note: Scissor width at edge of monitor to make it, so we effectively only are scissoring height
        enableScissor(0, this.y + 1, minecraft.getWindow().getGuiScaledWidth(), this.y + this.height - 1);
        matrix.pushPose();
        int shift = getCurrentSelection();
        matrix.translate(0, -shift, 0);
        mouseY += shift;

        int startY = getStartY(currentModule);
        for (MiniElement element : miniElements) {
            if (startY >= shift + height) {
                //If we are past the max draw spot, stop attempting to draw
                break;
            } else if (startY + element.getNeededHeight() > shift) {
                //Only draw it if it would be in our view
                element.renderBackground(matrix, mouseX, mouseY);
            }
            startY += element.getNeededHeight() + ELEMENT_SPACER;
        }

        matrix.popPose();
        disableScissor();
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
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        //Note: Scissor width at edge of monitor to make it, so we effectively only are scissoring height
        enableScissor(0, this.y + 1, minecraft.getWindow().getGuiScaledWidth(), this.y + this.height - 1);
        matrix.pushPose();
        int shift = getCurrentSelection();
        matrix.translate(0, -shift, 0);
        mouseY += shift;
        int startY = 5;
        if (currentModule != null) {
            if (currentModule.getData().isExclusive(ExclusiveFlag.ANY)) {
                if (startY + 13 > shift) {
                    drawTextWithScale(matrix, MekanismLang.MODULE_EXCLUSIVE.translate(), relativeX + 5, relativeY + startY, 0x635BD4, 0.8F);
                }
                startY += 13;
            }
            if (currentModule.getData().getMaxStackSize() > 1) {
                if (startY + 13 > shift) {
                    drawTextWithScale(matrix, MekanismLang.MODULE_INSTALLED.translate(currentModule.getInstalledCount()), relativeX + 5, relativeY + startY,
                          screenTextColor(), 0.8F);
                }
                startY += 13;
            }
        }
        //Draw elements
        for (MiniElement element : miniElements) {
            if (startY >= shift + height) {
                //If we are past the max draw spot, stop attempting to draw
                break;
            } else if (startY + element.getNeededHeight() > shift) {
                //Only draw it if it would be in our view
                element.renderForeground(matrix, mouseX, mouseY);
            }
            startY += element.getNeededHeight() + ELEMENT_SPACER;
        }
        matrix.popPose();
        disableScissor();
    }

    abstract class MiniElement {

        protected final int xPos, yPos, dataIndex;

        public MiniElement(int xPos, int yPos, int dataIndex) {
            this.xPos = xPos;
            this.yPos = yPos;
            this.dataIndex = dataIndex;
        }

        protected abstract void renderBackground(PoseStack matrix, int mouseX, int mouseY);

        protected abstract void renderForeground(PoseStack matrix, int mouseX, int mouseY);

        protected abstract void click(double mouseX, double mouseY);

        protected void release(double mouseX, double mouseY) {
        }

        protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        }

        protected abstract int getNeededHeight();

        protected int getRelativeX() {
            return relativeX + xPos;
        }

        protected int getRelativeY() {
            return relativeY + yPos;
        }

        protected int getX() {
            return x + xPos;
        }

        protected int getY() {
            return y + yPos;
        }

        protected boolean mouseOver(double mouseX, double mouseY, int relativeX, int relativeY, int width, int height) {
            int x = getX();
            int y = getY();
            return mouseX >= x + relativeX && mouseX < x + relativeX + width && mouseY >= y + relativeY && mouseY < y + relativeY + height;
        }

        protected <TYPE> void setData(ModuleConfigItem<TYPE> data, TYPE value) {
            data.set(value, getCallback(data.getData(), dataIndex));
        }
    }

    class BooleanToggle extends MiniElement {

        private static final int RADIO_SIZE = 8;

        private final ModuleConfigItem<Boolean> data;

        BooleanToggle(ModuleConfigItem<Boolean> data, int xPos, int yPos, int dataIndex) {
            super(xPos, yPos, dataIndex);
            this.data = data;
        }

        @Override
        protected int getNeededHeight() {
            return 20;
        }

        @Override
        protected void renderBackground(PoseStack matrix, int mouseX, int mouseY) {
            RenderSystem.setShaderTexture(0, RADIO);
            drawRadio(matrix, mouseX, mouseY, data.get(), 4, 11, 0);
            drawRadio(matrix, mouseX, mouseY, !data.get(), 50, 11, RADIO_SIZE);
        }

        private void drawRadio(PoseStack matrix, int mouseX, int mouseY, boolean selected, int relativeX, int relativeY, int selectedU) {
            if (selected) {
                blit(matrix, getX() + relativeX, getY() + relativeY, selectedU, RADIO_SIZE, RADIO_SIZE, RADIO_SIZE, 16, 16);
            } else {
                boolean hovered = mouseOver(mouseX, mouseY, relativeX, relativeY, RADIO_SIZE, RADIO_SIZE);
                blit(matrix, getX() + relativeX, getY() + relativeY, hovered ? RADIO_SIZE : 0, 0, RADIO_SIZE, RADIO_SIZE, 16, 16);
            }
        }

        @Override
        protected void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
            int textColor = screenTextColor();
            drawTextWithScale(matrix, data.getDescription(), getRelativeX() + 3, getRelativeY(), textColor, 0.8F);
            drawTextWithScale(matrix, MekanismLang.TRUE.translate(), getRelativeX() + 16, getRelativeY() + 11, textColor, 0.8F);
            drawTextWithScale(matrix, MekanismLang.FALSE.translate(), getRelativeX() + 62, getRelativeY() + 11, textColor, 0.8F);
        }

        @Override
        protected void click(double mouseX, double mouseY) {
            if (data.get()) {
                if (mouseOver(mouseX, mouseY, 50, 11, RADIO_SIZE, RADIO_SIZE)) {
                    setDataFromClick(false);
                }
            } else if (mouseOver(mouseX, mouseY, 4, 11, RADIO_SIZE, RADIO_SIZE)) {
                setDataFromClick(true);
            }
        }

        private void setDataFromClick(boolean value) {
            setData(data, value);
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(MekanismSounds.BEEP.get(), 1.0F));
        }
    }

    class EnumToggle<TYPE extends Enum<TYPE> & IHasTextComponent> extends MiniElement {

        private static final float TEXT_SCALE = 0.7F;
        private static final int BAR_START = 10;

        private final int BAR_LENGTH = GuiModuleScreen.this.barXShift - 24;
        private final ModuleConfigItem<TYPE> data;
        private final int optionDistance;
        private boolean dragging = false;

        EnumToggle(ModuleConfigItem<TYPE> data, int xPos, int yPos, int dataIndex) {
            super(xPos, yPos, dataIndex);
            this.data = data;
            this.optionDistance = (BAR_LENGTH / (getData().getEnums().size() - 1));
        }

        @Override
        protected int getNeededHeight() {
            return 28;
        }

        private ModuleEnumData<TYPE> getData() {
            return (ModuleEnumData<TYPE>) data.getData();
        }

        @Override
        protected void renderBackground(PoseStack matrix, int mouseX, int mouseY) {
            RenderSystem.setShaderTexture(0, SLIDER);
            int center = optionDistance * data.get().ordinal();
            blit(matrix, getX() + BAR_START + center - 2, getY() + 11, 0, 0, 5, 6, 8, 8);
            blit(matrix, getX() + BAR_START, getY() + 17, 0, 6, BAR_LENGTH, 2, 8, 8);
        }

        @Override
        protected void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
            int textColor = screenTextColor();
            drawTextWithScale(matrix, data.getDescription(), getRelativeX() + 3, getRelativeY(), textColor, 0.8F);
            List<TYPE> options = getData().getEnums();
            for (int i = 0, count = options.size(); i < count; i++) {
                int center = optionDistance * i;
                drawScaledCenteredText(matrix, options.get(i).getTextComponent(), getRelativeX() + BAR_START + center, getRelativeY() + 20, textColor, TEXT_SCALE);
            }
        }

        @Override
        protected void click(double mouseX, double mouseY) {
            if (!dragging) {
                int center = optionDistance * data.get().ordinal();
                if (mouseOver(mouseX, mouseY, BAR_START + center - 2, 11, 5, 6)) {
                    dragging = true;
                } else if (mouseOver(mouseX, mouseY, BAR_START, 10, BAR_LENGTH, 12)) {
                    setData(getData().getEnums(), mouseX);
                }
            }
        }

        @Override
        protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
            if (dragging) {
                setData(getData().getEnums(), mouseX);
            }
        }

        private void setData(List<TYPE> options, double mouseX) {
            int size = options.size() - 1;
            int cur = (int) Math.round(((mouseX - getX() - BAR_START) / BAR_LENGTH) * size);
            cur = Mth.clamp(cur, 0, size);
            if (cur != data.get().ordinal()) {
                setData(data, options.get(cur));
            }
        }

        @Override
        protected void release(double mouseX, double mouseY) {
            dragging = false;
        }
    }
}