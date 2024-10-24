package mekanism.client.gui.element.custom.module;

import java.util.function.Consumer;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.config.ModuleColorConfig;
import mekanism.client.gui.GuiModuleTweaker;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.GuiColorWindow;
import mekanism.client.render.IFancyFontRenderer.TextAlignment;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.shared.ModuleColorModulationUnit;
import mekanism.common.lib.Color;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

class ColorSelection extends MiniElement<Integer> {

    private static final int OFFSET_Y = 1;
    private final int OFFSET_X;
    private final boolean supportsAlpha;

    @Nullable
    private final GuiModuleTweaker.ArmorPreview armorPreview;

    ColorSelection(GuiModuleScreen parent, ModuleColorConfig data, Component description, int xPos, int yPos, @Nullable GuiModuleTweaker.ArmorPreview armorPreview) {
        super(parent, data, description, xPos, yPos);
        this.armorPreview = armorPreview;
        this.supportsAlpha = data.supportsAlpha();
        OFFSET_X = this.parent.getScreenWidth() - 26;
    }

    private Color getColor() {
        //Note: We can use argb regardless of if it handles alpha as the color data
        return Color.argb(data.get());
    }

    @Override
    protected int getNeededHeight() {
        return 20;
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int xTarget = getRelativeX() + OFFSET_X;
        int yTarget = getRelativeY() + OFFSET_Y;
        GuiUtils.drawOutline(guiGraphics, xTarget, yTarget, 18, 18, GuiTextField.SCREEN_COLOR.getAsInt());
        //Render the transparency grid inside it
        guiGraphics.blit(GuiColorWindow.TRANSPARENCY_GRID, xTarget + 1, yTarget + 1, 0, 0, 16, 16);
        //Draw color
        GuiUtils.fill(guiGraphics, xTarget + 1, yTarget + 1, 16, 16, data.get());
    }

    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int textColor = parent.screenTextColor();
        parent.drawScaledScrollingString(guiGraphics, description, xPos, yPos, TextAlignment.LEFT, textColor, OFFSET_X, 3, false, 0.8F);
        String hex;
        if (supportsAlpha) {
            hex = TextUtils.hex(false, 4, data.get());
        } else {
            hex = TextUtils.hex(false, 3, getColor().rgb());
        }
        //TODO: Do we want to draw the hex in the RGB color it is set to (intentionally ignore alpha)
        parent.drawScrollingString(guiGraphics, MekanismLang.GENERIC_HEX.translate(hex), xPos, yPos + 11, TextAlignment.LEFT, textColor, OFFSET_X, 3, false);
    }

    @Override
    protected void click(double mouseX, double mouseY) {
        if (mouseOver(mouseX, mouseY, OFFSET_X, OFFSET_Y, 18, 18)) {
            Consumer<Color> updatePreviewColor = null;
            Runnable previewReset = null;
            IModule<?> currentModule = parent.getCurrentModule();
            if (armorPreview != null && data.name().equals(ModuleColorModulationUnit.COLOR) && currentModule != null) {
                ItemStack stack = parent.getContainerStack().copy();
                if (stack.getItem() instanceof ArmorItem armorItem) {
                    //Ensure the preview has been initialized
                    armorPreview.get();
                    EquipmentSlot slot = armorItem.getEquipmentSlot();
                    //Replace the current preview with our copy
                    armorPreview.updatePreview(slot, stack);
                    updatePreviewColor = c -> {
                        IModuleContainer moduleContainer = IModuleHelper.INSTANCE.getModuleContainer(stack);
                        if (moduleContainer != null) {//Note: Should always be present
                            //Note: We can use the source data to ensure we have the correct config option, as with does not mutate it
                            moduleContainer.replaceModuleConfig(Minecraft.getInstance().level.registryAccess(), stack, MekanismModules.COLOR_MODULATION_UNIT,
                                  data.with(c.argb()));
                        }
                    };
                    previewReset = () -> armorPreview.resetToDefault(slot);
                }
            }
            parent.gui().addWindow(new GuiColorWindow(parent.gui(), (parent.getGuiWidth() - 160) / 2, (parent.getGuiHeight() - 120) / 2, supportsAlpha,
                              getColor(), color -> setData(color.argb()), armorPreview, updatePreviewColor, previewReset));
        }
    }
}