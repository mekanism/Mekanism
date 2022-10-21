package mekanism.client.gui.element.custom.module;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

class EnumToggle<TYPE extends Enum<TYPE> & IHasTextComponent> extends MiniElement {

    private static final ResourceLocation SLIDER = MekanismUtils.getResource(ResourceType.GUI, "slider.png");
    private static final float TEXT_SCALE = 0.7F;
    private static final int BAR_START = 10;

    private final int BAR_LENGTH;
    private final ModuleConfigItem<TYPE> data;
    private final int optionDistance;
    boolean dragging = false;

    EnumToggle(GuiModuleScreen parent, ModuleConfigItem<TYPE> data, int xPos, int yPos, int dataIndex) {
        super(parent, xPos, yPos, dataIndex);
        this.data = data;
        BAR_LENGTH = this.parent.getScreenWidth() - 24;
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
        GuiComponent.blit(matrix, getX() + BAR_START + center - 2, getY() + 11, 0, 0, 5, 6, 8, 8);
        GuiComponent.blit(matrix, getX() + BAR_START, getY() + 17, 0, 6, BAR_LENGTH, 2, 8, 8);
    }

    @Override
    protected void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        int textColor = parent.screenTextColor();
        parent.drawTextWithScale(matrix, data.getDescription(), getRelativeX() + 3, getRelativeY(), textColor, 0.8F);
        List<TYPE> options = getData().getEnums();
        for (int i = 0, count = options.size(); i < count; i++) {
            int center = optionDistance * i;
            Component text = options.get(i).getTextComponent();
            //Similar to logic for drawScaledCenteredText except shifts values slightly if they go past the max length
            int textWidth = parent.getStringWidth(text);
            float widthScaling = (textWidth / 2F) * TEXT_SCALE;
            float left = BAR_START + center - widthScaling;
            if (left < 0) {
                left = 0;
            } else {
                int max = parent.getScreenWidth() - 1;
                int end = xPos + (int) Math.ceil(left + textWidth * TEXT_SCALE);
                if (end > max) {
                    left -= end - max;
                }
            }
            parent.drawTextWithScale(matrix, text, getRelativeX() + left, getRelativeY() + 20, textColor, TEXT_SCALE);
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