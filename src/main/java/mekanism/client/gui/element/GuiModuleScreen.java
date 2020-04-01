package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.BooleanData;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import mekanism.common.content.gear.ModuleConfigItem.IntEnum;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiModuleScreen extends GuiElement {

    private static final ResourceLocation RADIO = MekanismUtils.getResource(ResourceType.GUI, "radio_button.png");
    private static final ResourceLocation SLIDER = MekanismUtils.getResource(ResourceType.GUI, "slider.png");

    private GuiElementHolder background;
    private Runnable callback;

    private Module currentModule;
    private List<MiniElement> miniElements = new ArrayList<>();

    public GuiModuleScreen(IGuiWrapper gui, int x, int y, Runnable callback) {
        super(gui, x, y, 102, 98, "");
        this.callback = callback;
        background = new GuiElementHolder(gui, x, y, 102, 98);
    }

    public void setModule(Module module) {
        currentModule = module;
        miniElements.clear();

        if (module != null) {
            int startY = y + module.getData().getMaxStackSize() > 1 ? 11 : 2;
            for (ModuleConfigItem<?> configItem : currentModule.getConfigItems()) {
                if (configItem.getData() instanceof BooleanData) {
                    miniElements.add(new BooleanToggle((ModuleConfigItem<Boolean>) configItem, 2, startY));
                    startY += 22;
                } else if (configItem.getData() instanceof EnumData) {
                    miniElements.add(new EnumToggle((ModuleConfigItem<Enum<? extends IntEnum>>) configItem, 2, startY));
                    startY += 31;
                }
            }
        }
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
            drawString(MekanismLang.MODULE_INSTALLED.translate(currentModule.getInstalledCount()), x + 2, y + 2, 0x303030);
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

            boolean hover = mouseX >= xPos + 4 && mouseX < xPos + 8 && mouseY >= yPos + 13 && mouseY < yPos + 17;
            if (data.get()) {
                blit(xPos + 4, yPos + 13, 0, 4, 4, 4, 8, 8);
            } else {
                blit(xPos + 4, yPos + 13, hover ? 4 : 0, 0, 4, 4, 8, 8);
            }
            hover = mouseX >= xPos + 30 && mouseX < xPos + 34 && mouseY >= yPos + 13 && mouseY < yPos + 17;
            if (!data.get()) {
                blit(xPos + 30, yPos + 13, 4, 4, 4, 4, 8, 8);
            } else {
                blit(xPos + 30, yPos + 13, hover ? 4 : 0, 0, 4, 4, 8, 8);
            }
        }
        @Override
        public void renderForeground(int mouseX, int mouseY) {
            drawString(data.getDescription().translate(), xPos + 2, yPos, 0x303030);
            drawString(MekanismLang.TRUE.translate(), xPos + 10, yPos + 11, 0x303030);
            drawString(MekanismLang.FALSE.translate(), xPos + 36, yPos + 11, 0x303030);
        }
        @Override
        public void click(double mouseX, double mouseY) {
            if (!data.get() && mouseX >= xPos + 4 && mouseX < xPos + 8 && mouseY >= yPos + 13 && mouseY < yPos + 17) {
                data.set(true, callback);
            }

            if (data.get() && mouseX >= xPos + 30 && mouseX < xPos + 34 && mouseY >= yPos + 13 && mouseY < yPos + 17) {
                data.set(false, callback);
            }
        }
    }

    class EnumToggle extends MiniElement {
        final int BAR_LENGTH = getWidth() - 24;
        ModuleConfigItem<Enum<? extends IntEnum>> data;
        boolean dragging = false;

        EnumToggle(ModuleConfigItem<Enum<? extends IntEnum>> data, int xPos, int yPos) {
            super(xPos, yPos);
            this.data = data;
        }
        @Override
        public void renderBackground(int mouseX, int mouseY) {
            minecraft.textureManager.bindTexture(SLIDER);
            int count = ((EnumData) data.getData()).getEnumClass().getEnumConstants().length;
            int center = (BAR_LENGTH / count) * data.get().ordinal();
            blit(xPos + 10 + center - 2, yPos + 11, 0, 0, 5, 6, 8, 8);
            blit(xPos + 10, yPos + 17, 0, 6, BAR_LENGTH, 2, 8, 8);
        }
        @Override
        public void renderForeground(int mouseX, int mouseY) {
            drawString(data.getDescription().translate(), xPos + 2, yPos, 0x303030);
            Enum<? extends IntEnum>[] arr = ((EnumData) data.getData()).getEnumClass().getEnumConstants();
            for (int i = 0; i < arr.length; i++) {
                int center = (BAR_LENGTH / arr.length) * i;
                drawCenteredText(Integer.toString(((IntEnum) arr[i]).getValue()), xPos + 10 + center, yPos + 20, 0x303030);
            }

            if (dragging) {
                int cur = (int)(((double)(mouseX - xPos - 10) / (double)BAR_LENGTH) * arr.length);
                cur = Math.max(arr.length-1, Math.min(0, cur));
                if (cur != data.get().ordinal()) {
                    data.set(arr[cur], callback);
                }
            }
        }
        @Override
        public void click(double mouseX, double mouseY) {
            if (!dragging) {
                int count = ((EnumData) data.getData()).getEnumClass().getEnumConstants().length;
                int center = (BAR_LENGTH / count) * data.get().ordinal();
                if (mouseX >= xPos + 10 + center - 2 && mouseX < xPos + 10 + center + 3 && mouseY >= yPos + 11 && mouseY < yPos + 17) {
                    dragging = true;
                }
            }
        }
        @Override
        public void release(double mouseX, double mouseY) {
            dragging = false;
        }
    }
}
