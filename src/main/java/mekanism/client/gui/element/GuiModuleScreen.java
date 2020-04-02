package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.BooleanData;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import mekanism.common.content.gear.ModuleConfigItem.IntEnum;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiModuleScreen extends GuiTexturedElement {

    private static final ResourceLocation RADIO = MekanismUtils.getResource(ResourceType.GUI, "radio_button.png");
    private static final ResourceLocation SLIDER = MekanismUtils.getResource(ResourceType.GUI, "slider.png");

    private static final int TEXT_COLOR = 0x202020;

    private GuiElementHolder background;
    private Consumer<ItemStack> callback;

    private Module currentModule;
    private List<MiniElement> miniElements = new ArrayList<>();

    public GuiModuleScreen(IGuiWrapper gui, int x, int y, Consumer<ItemStack> callback) {
        super(null, gui, x, y, 102, 134);
        this.callback = callback;
        background = new GuiElementHolder(gui, x, y, 102, 134);
    }

    @SuppressWarnings("unchecked")
    public void setModule(Module module) {
        List<MiniElement> newElements = new ArrayList<>();

        if (module != null) {
            int startY = module.getData().getMaxStackSize() > 1 ? 16 : 3;
            for (int i = 0; i < module.getConfigItems().size(); i++) {
                ModuleConfigItem<?> configItem = module.getConfigItems().get(i);
                if (configItem.getData() instanceof BooleanData) {
                    newElements.add(new BooleanToggle((ModuleConfigItem<Boolean>) configItem, 2, startY));
                    startY += 24;
                } else if (configItem.getData() instanceof EnumData) {
                    EnumToggle toggle = new EnumToggle((ModuleConfigItem<Enum<? extends IntEnum>>) configItem, 2, startY);
                    newElements.add(toggle);
                    startY += 34;
                    // allow the dragger to continue sliding, even when we reset the config element
                    if (currentModule != null && module != null && currentModule.getData() == module.getData() && miniElements.get(i) instanceof EnumToggle) {
                        toggle.dragging = ((EnumToggle) miniElements.get(i)).dragging;
                    }
                }
            }
        }

        currentModule = module;
        miniElements = newElements;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        background.renderButton(mouseX, mouseY, partialTicks);
        for (MiniElement element : miniElements) {
            element.renderBackground(mouseX, mouseY);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        for (MiniElement element : miniElements) {
            element.click(mouseX, mouseY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        for (MiniElement element : miniElements) {
            element.release(mouseX, mouseY);
        }
    }

    @Override
    public void renderForeground(int mouseX, int mouseY, int xAxis, int yAxis) {
        super.renderForeground(mouseX, mouseY, xAxis, yAxis);

        if (currentModule != null && currentModule.getData().getMaxStackSize() > 1) {
            drawString(MekanismLang.MODULE_INSTALLED.translate(currentModule.getInstalledCount()), relativeX + 4, relativeY + 4, TEXT_COLOR);
        }

        for (MiniElement element : miniElements) {
            element.renderForeground(mouseX, mouseY);
        }
    }

    abstract class MiniElement {
        int xPos, yPos;
        public MiniElement(int xPos, int yPos) {
            this.xPos = xPos;
            this.yPos = yPos;
        }
        abstract void renderBackground(int mouseX, int mouseY);
        abstract void renderForeground(int mouseX, int mouseY);
        abstract void click(double mouseX, double mouseY);
        void release(double mouseX, double mouseY) {}
        int getRelativeX() { return relativeX + xPos; }
        int getRelativeY() { return relativeY + yPos; }
        int getX() { return x + xPos; }
        int getY() { return y + yPos; }
    }

    class BooleanToggle extends MiniElement {
        ModuleConfigItem<Boolean> data;
        BooleanToggle(ModuleConfigItem<Boolean> data, int xPos, int yPos) {
            super(xPos, yPos);
            this.data = data;
        }
        @Override
        public void renderBackground(int mouseX, int mouseY) {
            minecraft.textureManager.bindTexture(RADIO);

            boolean hover = mouseX >= getX() + 4 && mouseX < getX() + 12 && mouseY >= getY() + 11 && mouseY < getY() + 19;
            if (data.get()) {
                blit(getX() + 4, getY() + 11, 0, 8, 8, 8, 16, 16);
            } else {
                blit(getX() + 4, getY() + 11, hover ? 8 : 0, 0, 8, 8, 16, 16);
            }
            hover = mouseX >= getX() + 50 && mouseX < getX() + 58 && mouseY >= getY() + 11 && mouseY < getY() + 19;
            if (!data.get()) {
                blit(getX() + 50, getY() + 11, 8, 8, 8, 8, 16, 16);
            } else {
                blit(getX() + 50, getY() + 11, hover ? 8 : 0, 0, 8, 8, 16, 16);
            }
        }
        @Override
        public void renderForeground(int mouseX, int mouseY) {
            drawString(data.getDescription().translate(), getRelativeX() + 2, getRelativeY(), TEXT_COLOR);
            drawString(MekanismLang.TRUE.translate(), getRelativeX() + 16, getRelativeY() + 11, TEXT_COLOR);
            drawString(MekanismLang.FALSE.translate(), getRelativeX() + 62, getRelativeY() + 11, TEXT_COLOR);
        }
        @Override
        public void click(double mouseX, double mouseY) {
            if (!data.get() && mouseX >= getX() + 4 && mouseX < getX() + 12 && mouseY >= getY() + 11 && mouseY < getY() + 19) {
                data.set(true, callback);
            }

            if (data.get() && mouseX >= getX() + 50 && mouseX < getX() + 58 && mouseY >= getY() + 11 && mouseY < getY() + 19) {
                data.set(false, callback);
            }
        }
    }

    class EnumToggle extends MiniElement {
        final int BAR_LENGTH = getWidth() - 24;
        final int BAR_START = 10;
        ModuleConfigItem<Enum<? extends IntEnum>> data;
        boolean dragging = false;

        EnumToggle(ModuleConfigItem<Enum<? extends IntEnum>> data, int xPos, int yPos) {
            super(xPos, yPos);
            this.data = data;
        }
        @Override
        public void renderBackground(int mouseX, int mouseY) {
            minecraft.textureManager.bindTexture(SLIDER);
            int count = ((EnumData<?>) data.getData()).getSelectableCount();
            int center = (BAR_LENGTH / (count-1)) * data.get().ordinal();
            blit(getX() + BAR_START + center - 2, getY() + 11, 0, 0, 5, 6, 8, 8);
            blit(getX() + BAR_START, getY() + 17, 0, 6, BAR_LENGTH, 2, 8, 8);
        }
        @Override
        public void renderForeground(int mouseX, int mouseY) {
            drawString(data.getDescription().translate(), getRelativeX() + 2, getRelativeY(), TEXT_COLOR);
            Enum<? extends IntEnum>[] arr = ((EnumData<?>) data.getData()).getEnums();
            int count = ((EnumData<?>) data.getData()).getSelectableCount();
            for (int i = 0; i < count; i++) {
                int center = (BAR_LENGTH / (count-1)) * i;
                drawCenteredText(Integer.toString(((IntEnum) arr[i]).getValue()), getRelativeX() + BAR_START + center, getRelativeY() + 20, TEXT_COLOR);
            }

            if (dragging) {
                int cur = (int)Math.round(((double)(mouseX - getX() - BAR_START) / (double)BAR_LENGTH) * (count-1));
                cur = Math.min(count-1, Math.max(0, cur));
                if (cur != data.get().ordinal()) {
                    data.set(arr[cur], callback);
                }
            }
        }
        @Override
        public void click(double mouseX, double mouseY) {
            int count = ((EnumData<?>) data.getData()).getSelectableCount();
            if (!dragging) {
                int center = (BAR_LENGTH / (count-1)) * data.get().ordinal();
                if (mouseX >= getX() + BAR_START + center - 2 && mouseX < getX() + BAR_START + center + 3 && mouseY >= getY() + 11 && mouseY < getY() + 17) {
                    dragging = true;
                }
            }
            if (!dragging) {
                Enum<? extends IntEnum>[] arr = ((EnumData<?>) data.getData()).getEnums();
                if (mouseX >= getX() + BAR_START && mouseX < getX() + BAR_START + BAR_LENGTH && mouseY >= getY() + 10 && mouseY < getY() + 22) {
                    int cur = (int)Math.round(((mouseX - getX() - BAR_START) / BAR_LENGTH) * (count-1));
                    cur = Math.min(count-1, Math.max(0, cur));
                    if (cur != data.get().ordinal()) {
                        data.set(arr[cur], callback);
                    }
                }
            }
        }
        @Override
        public void release(double mouseX, double mouseY) {
            dragging = false;
        }
    }
}
