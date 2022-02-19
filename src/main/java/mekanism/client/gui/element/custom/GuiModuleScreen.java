package mekanism.client.gui.element.custom;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigData;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.IHasTextComponent;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.DisableableModuleConfigItem;
import mekanism.common.network.to_server.PacketUpdateModuleSettings;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiModuleScreen extends GuiElement {

    private static final ResourceLocation RADIO = MekanismUtils.getResource(ResourceType.GUI, "radio_button.png");
    private static final ResourceLocation SLIDER = MekanismUtils.getResource(ResourceType.GUI, "slider.png");

    private final int TEXT_COLOR = screenTextColor();

    private final IntSupplier slotIdSupplier;

    private IModule<?> currentModule;
    private List<MiniElement> miniElements = new ArrayList<>();

    public GuiModuleScreen(IGuiWrapper gui, int x, int y, IntSupplier slotIdSupplier) {
        super(gui, x, y, 102, 134);
        this.slotIdSupplier = slotIdSupplier;
    }

    private Runnable getCallback(ModuleConfigData<?> configData, int dataIndex) {
        return () -> {
            if (currentModule != null) {//Shouldn't be null but validate just in case
                Mekanism.packetHandler.sendToServer(PacketUpdateModuleSettings.create(slotIdSupplier.getAsInt(), currentModule.getData(), dataIndex, configData));
            }
        };
    }

    @SuppressWarnings("unchecked")
    public void setModule(Module<?> module) {
        List<MiniElement> newElements = new ArrayList<>();

        if (module != null) {
            int startY = 3;
            if (module.getData().isExclusive()) {
                startY += 13;
            }
            if (module.getData().getMaxStackSize() > 1) {
                startY += 13;
            }
            List<ModuleConfigItem<?>> configItems = module.getConfigItems();
            for (int i = 0, configItemsCount = configItems.size(); i < configItemsCount; i++) {
                ModuleConfigItem<?> configItem = configItems.get(i);
                // Don't show the enabled option if this is enabled by default
                if (configItem.getData() instanceof ModuleBooleanData && (!configItem.getName().equals(Module.ENABLED_KEY) || !module.getData().isNoDisable())) {
                    if (configItem instanceof DisableableModuleConfigItem && !((DisableableModuleConfigItem) configItem).isConfigEnabled()) {
                        //Skip options that are force disabled by the config
                        //TODO: Eventually we may want to make it slightly "faster" in that it allows updating the toggle elements rather than just
                        // not adding them back when switching to another module and then back again
                        continue;
                    }
                    newElements.add(new BooleanToggle((ModuleConfigItem<Boolean>) configItem, 2, startY, i));
                    startY += 24;
                } else if (configItem.getData() instanceof ModuleEnumData) {
                    EnumToggle toggle = new EnumToggle((ModuleConfigItem<Enum<? extends IHasTextComponent>>) configItem, 2, startY, i);
                    newElements.add(toggle);
                    startY += 34;
                    // allow the dragger to continue sliding, even when we reset the config element
                    if (currentModule != null && currentModule.getData() == module.getData() && miniElements.get(i) instanceof EnumToggle) {
                        toggle.dragging = ((EnumToggle) miniElements.get(i)).dragging;
                    }
                }
            }
        }

        currentModule = module;
        miniElements = newElements;
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        renderBackgroundTexture(matrix, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
        for (MiniElement element : miniElements) {
            element.renderBackground(matrix, mouseX, mouseY);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        for (MiniElement element : miniElements) {
            element.click(mouseX, mouseY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        for (MiniElement element : miniElements) {
            element.release(mouseX, mouseY);
        }
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);

        if (currentModule != null) {
            int startY = relativeY + 5;
            if (currentModule.getData().isExclusive()) {
                ITextComponent comp = MekanismLang.MODULE_EXCLUSIVE.translate();
                drawTextWithScale(matrix, comp, relativeX + 5, startY, 0x635BD4, 0.8F);
                startY += 13;
            }
            if (currentModule.getData().getMaxStackSize() > 1) {
                drawTextWithScale(matrix, MekanismLang.MODULE_INSTALLED.translate(currentModule.getInstalledCount()), relativeX + 5, startY, TEXT_COLOR, 0.8F);
                startY += 13;
            }
        }

        for (MiniElement element : miniElements) {
            element.renderForeground(matrix, mouseX, mouseY);
        }
    }

    abstract class MiniElement {

        final int xPos, yPos, dataIndex;

        public MiniElement(int xPos, int yPos, int dataIndex) {
            this.xPos = xPos;
            this.yPos = yPos;
            this.dataIndex = dataIndex;
        }

        abstract void renderBackground(MatrixStack matrix, int mouseX, int mouseY);

        abstract void renderForeground(MatrixStack matrix, int mouseX, int mouseY);

        abstract void click(double mouseX, double mouseY);

        void release(double mouseX, double mouseY) {
        }

        int getRelativeX() {
            return relativeX + xPos;
        }

        int getRelativeY() {
            return relativeY + yPos;
        }

        int getX() {
            return x + xPos;
        }

        int getY() {
            return y + yPos;
        }
    }

    class BooleanToggle extends MiniElement {

        final ModuleConfigItem<Boolean> data;

        BooleanToggle(ModuleConfigItem<Boolean> data, int xPos, int yPos, int dataIndex) {
            super(xPos, yPos, dataIndex);
            this.data = data;
        }

        @Override
        public void renderBackground(MatrixStack matrix, int mouseX, int mouseY) {
            minecraft.textureManager.bind(RADIO);

            boolean hover = mouseX >= getX() + 4 && mouseX < getX() + 12 && mouseY >= getY() + 11 && mouseY < getY() + 19;
            if (data.get()) {
                blit(matrix, getX() + 4, getY() + 11, 0, 8, 8, 8, 16, 16);
            } else {
                blit(matrix, getX() + 4, getY() + 11, hover ? 8 : 0, 0, 8, 8, 16, 16);
            }
            hover = mouseX >= getX() + 50 && mouseX < getX() + 58 && mouseY >= getY() + 11 && mouseY < getY() + 19;
            if (!data.get()) {
                blit(matrix, getX() + 50, getY() + 11, 8, 8, 8, 8, 16, 16);
            } else {
                blit(matrix, getX() + 50, getY() + 11, hover ? 8 : 0, 0, 8, 8, 16, 16);
            }
        }

        @Override
        public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
            drawTextWithScale(matrix, data.getDescription(), getRelativeX() + 3, getRelativeY(), TEXT_COLOR, 0.8F);
            drawTextWithScale(matrix, MekanismLang.TRUE.translate(), getRelativeX() + 16, getRelativeY() + 11, TEXT_COLOR, 0.8F);
            drawTextWithScale(matrix, MekanismLang.FALSE.translate(), getRelativeX() + 62, getRelativeY() + 11, TEXT_COLOR, 0.8F);
        }

        @Override
        public void click(double mouseX, double mouseY) {
            if (!data.get() && mouseX >= getX() + 4 && mouseX < getX() + 12 && mouseY >= getY() + 11 && mouseY < getY() + 19) {
                data.set(true, getCallback(data.getData(), dataIndex));
                minecraft.getSoundManager().play(SimpleSound.forUI(MekanismSounds.BEEP.get(), 1.0F));
            }

            if (data.get() && mouseX >= getX() + 50 && mouseX < getX() + 58 && mouseY >= getY() + 11 && mouseY < getY() + 19) {
                data.set(false, getCallback(data.getData(), dataIndex));
                minecraft.getSoundManager().play(SimpleSound.forUI(MekanismSounds.BEEP.get(), 1.0F));
            }
        }
    }

    class EnumToggle extends MiniElement {

        final int BAR_LENGTH = getWidth() - 24;
        final int BAR_START = 10;
        final float TEXT_SCALE = 0.7F;
        final ModuleConfigItem<Enum<? extends IHasTextComponent>> data;
        boolean dragging = false;

        EnumToggle(ModuleConfigItem<Enum<? extends IHasTextComponent>> data, int xPos, int yPos, int dataIndex) {
            super(xPos, yPos, dataIndex);
            this.data = data;
        }

        @Override
        public void renderBackground(MatrixStack matrix, int mouseX, int mouseY) {
            minecraft.textureManager.bind(SLIDER);
            int count = ((ModuleEnumData<?>) data.getData()).getEnums().size();
            int center = (BAR_LENGTH / (count - 1)) * data.get().ordinal();
            blit(matrix, getX() + BAR_START + center - 2, getY() + 11, 0, 0, 5, 6, 8, 8);
            blit(matrix, getX() + BAR_START, getY() + 17, 0, 6, BAR_LENGTH, 2, 8, 8);
        }

        @Override
        public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
            ModuleEnumData<?> enumData = (ModuleEnumData<?>) data.getData();
            drawTextWithScale(matrix, data.getDescription(), getRelativeX() + 3, getRelativeY(), TEXT_COLOR, 0.8F);
            List<? extends Enum<? extends IHasTextComponent>> options = enumData.getEnums();
            int count = options.size();
            for (int i = 0; i < count; i++) {
                int diffFromCenter = ((BAR_LENGTH / (count - 1)) * i) - (BAR_LENGTH / 2);
                float diffScale = 1 - (1 - TEXT_SCALE) / 2F;
                int textCenter = getRelativeX() + BAR_START + (BAR_LENGTH / 2) + (int) (diffFromCenter * diffScale);
                drawScaledCenteredText(matrix, ((IHasTextComponent) options.get(i)).getTextComponent(), textCenter, getRelativeY() + 20, TEXT_COLOR, TEXT_SCALE);
            }

            if (dragging) {
                int cur = (int) Math.round(((double) (mouseX - getX() - BAR_START) / (double) BAR_LENGTH) * (count - 1));
                cur = Math.min(count - 1, Math.max(0, cur));
                if (cur != data.get().ordinal()) {
                    data.set(options.get(cur), getCallback(data.getData(), dataIndex));
                }
            }
        }

        @Override
        public void click(double mouseX, double mouseY) {
            List<? extends Enum<? extends IHasTextComponent>> options = ((ModuleEnumData<?>) data.getData()).getEnums();
            if (!dragging) {
                int center = (BAR_LENGTH / (options.size() - 1)) * data.get().ordinal();
                if (mouseX >= getX() + BAR_START + center - 2 && mouseX < getX() + BAR_START + center + 3 && mouseY >= getY() + 11 && mouseY < getY() + 17) {
                    dragging = true;
                }
            }
            if (!dragging) {
                if (mouseX >= getX() + BAR_START && mouseX < getX() + BAR_START + BAR_LENGTH && mouseY >= getY() + 10 && mouseY < getY() + 22) {
                    int count = options.size();
                    int cur = (int) Math.round(((mouseX - getX() - BAR_START) / BAR_LENGTH) * (count - 1));
                    cur = Math.min(count - 1, Math.max(0, cur));
                    if (cur != data.get().ordinal()) {
                        data.set(options.get(cur), getCallback(data.getData(), dataIndex));
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