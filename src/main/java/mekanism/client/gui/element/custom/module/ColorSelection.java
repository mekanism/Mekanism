package mekanism.client.gui.element.custom.module;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.ModuleColorData;
import mekanism.client.gui.GuiModuleTweaker;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.GuiColorWindow;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.content.gear.shared.ModuleColorModulationUnit;
import mekanism.common.lib.Color;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.text.TextUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

class ColorSelection extends MiniElement {

    private static final int OFFSET_Y = 1;
    private final int OFFSET_X;

    private final ModuleConfigItem<Integer> data;
    @Nullable
    private final GuiModuleTweaker.ArmorPreview armorPreview;
    private final boolean handlesAlpha;

    ColorSelection(GuiModuleScreen parent, ModuleConfigItem<Integer> data, int xPos, int yPos, int dataIndex, boolean handlesAlpha, @Nullable GuiModuleTweaker.ArmorPreview armorPreview) {
        super(parent, xPos, yPos, dataIndex);
        this.data = data;
        this.handlesAlpha = handlesAlpha;
        this.armorPreview = armorPreview;
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
    protected void renderBackground(PoseStack matrix, int mouseX, int mouseY) {
        int xTarget = getX() + OFFSET_X;
        int yTarget = getY() + OFFSET_Y;
        GuiUtils.drawOutline(matrix, xTarget, yTarget, 18, 18, GuiTextField.SCREEN_COLOR.getAsInt());
        //Render the transparency grid inside it
        RenderSystem.setShaderTexture(0, GuiColorWindow.TRANSPARENCY_GRID);
        parent.blit(matrix, xTarget + 1, yTarget + 1, 0, 0, 16, 16);
        //Draw color
        GuiUtils.fill(matrix, xTarget + 1, yTarget + 1, 16, 16, data.get());
    }

    @Override
    protected void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        int textColor = parent.screenTextColor();
        parent.drawTextWithScale(matrix, data.getDescription(), getRelativeX() + 3, getRelativeY(), textColor, 0.8F);
        String hex;
        if (handlesAlpha) {
            hex = TextUtils.hex(false, 4, data.get());
        } else {
            hex = TextUtils.hex(false, 3, getColor().rgb());
        }
        parent.drawTextExact(matrix, MekanismLang.GENERIC_HEX.translate(hex), getRelativeX() + 3, getRelativeY() + 11, textColor);
    }

    @Override
    protected void click(double mouseX, double mouseY) {
        if (mouseOver(mouseX, mouseY, OFFSET_X, OFFSET_Y, 18, 18)) {
            Consumer<Color> updatePreviewColor = null;
            Runnable previewReset = null;
            IModule<?> currentModule = parent.getCurrentModule();
            if (armorPreview != null && data.matches(MekanismModules.COLOR_MODULATION_UNIT, ModuleColorModulationUnit.COLOR_CONFIG_KEY) && currentModule != null) {
                ItemStack stack = currentModule.getContainer().copy();
                if (stack.getItem() instanceof ArmorItem armorItem) {
                    Module<ModuleColorModulationUnit> colorModulation = ModuleHelper.INSTANCE.load(stack, MekanismModules.COLOR_MODULATION_UNIT);
                    if (colorModulation != null) {
                        Optional<ModuleConfigItem<Integer>> matchedData = colorModulation.getConfigItems().stream()
                              .filter(e -> e.getName().equals(ModuleColorModulationUnit.COLOR_CONFIG_KEY) && e.getData() instanceof ModuleColorData)
                              .map(e -> (ModuleConfigItem<Integer>) e)
                              .findFirst();
                        if (matchedData.isPresent()) {
                            //Ensure the preview has been initialized
                            armorPreview.get();
                            EquipmentSlot slot = armorItem.getSlot();
                            //Replace the current preview with our copy
                            armorPreview.updatePreview(slot, stack);
                            updatePreviewColor = c -> matchedData.get().set(c.argb());
                            previewReset = () -> armorPreview.resetToDefault(slot);
                        }
                    }
                }
            }
            GuiColorWindow window = new GuiColorWindow(parent.gui(), parent.getGuiWidth() / 2 - 160 / 2, parent.getGuiHeight() / 2 - 120 / 2, handlesAlpha,
                  color -> setData(data, color.argb()), armorPreview, updatePreviewColor, previewReset);
            window.setColor(getColor());
            parent.gui().addWindow(window);
        }
    }
}