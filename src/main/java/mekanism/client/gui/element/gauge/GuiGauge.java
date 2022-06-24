package mekanism.client.gui.element.gauge;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiGauge<T> extends GuiTexturedElement implements ISupportsWarning<GuiGauge<T>> {

    private final GaugeType gaugeType;
    protected boolean dummy;
    protected T dummyType;
    @Nullable
    private BooleanSupplier warningSupplier;

    public GuiGauge(GaugeType gaugeType, IGuiWrapper gui, int x, int y) {
        this(gaugeType, gui, x, y, gaugeType.getGaugeOverlay().getWidth() + 2, gaugeType.getGaugeOverlay().getHeight() + 2);
    }

    public GuiGauge(GaugeType gaugeType, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        super(gaugeType.getGaugeOverlay().getBarOverlay(), gui, x, y, sizeX, sizeY);
        this.gaugeType = gaugeType;
    }

    @Override
    public GuiGauge<T> warning(@NotNull WarningType type, @NotNull BooleanSupplier warningSupplier) {
        this.warningSupplier = ISupportsWarning.compound(this.warningSupplier, gui().trackWarning(type, warningSupplier));
        return this;
    }

    public abstract int getScaledLevel();

    public abstract TextureAtlasSprite getIcon();

    public abstract Component getLabel();

    public abstract List<Component> getTooltipText();

    public GaugeOverlay getGaugeOverlay() {
        return gaugeType.getGaugeOverlay();
    }

    protected GaugeInfo getGaugeColor() {
        return gaugeType.getGaugeInfo();
    }

    protected void applyRenderColor() {
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        GaugeInfo color = getGaugeColor();
        renderExtendedTexture(matrix, color.getResourceLocation(), color.getSideWidth(), color.getSideHeight());
        if (!dummy) {
            renderContents(matrix);
        }
    }

    public void renderContents(PoseStack matrix) {
        boolean warning = warningSupplier != null && warningSupplier.getAsBoolean();
        if (warning) {
            //Draw background (we do it regardless of if we are full or not as if the thing being drawn has transparency
            // we may as well show the background)
            RenderSystem.setShaderTexture(0, GuiSlot.WARNING_BACKGROUND_TEXTURE);
            blit(matrix, x + 1, y + 1, 0, 0, width - 2, height - 2, 256, 256);
        }
        int scale = getScaledLevel();
        TextureAtlasSprite icon = getIcon();
        if (scale > 0 && icon != null) {
            applyRenderColor();
            drawTiledSprite(matrix, x + 1, y + 1, height - 2, width - 2, scale, icon, TilingDirection.UP_RIGHT);
            MekanismRenderer.resetColor();
            if (warning && (scale / (double) (height - 2)) > 0.98) {
                //If we have a warning and the gauge is entirely filled (or almost completely filled, > 95%), draw a warning vertically next to it
                RenderSystem.setShaderTexture(0, WARNING_TEXTURE);
                int halfWidth = (width - 2) / 2;
                //Note: We also start the drawing after half the width so that we are sure it will properly line up with the background
                blit(matrix, x + 1 + halfWidth, y + 1, halfWidth, 0, halfWidth, height - 2, 256, 256);
            }
        }
        //Draw the bar overlay
        drawBarOverlay(matrix);
    }

    public void drawBarOverlay(PoseStack matrix) {
        RenderSystem.setShaderTexture(0, getResource());
        GaugeOverlay gaugeOverlay = getGaugeOverlay();
        blit(matrix, x + 1, y + 1, getWidth() - 2, getHeight() - 2, 0, 0, gaugeOverlay.getWidth(), gaugeOverlay.getHeight(), gaugeOverlay.getWidth(), gaugeOverlay.getHeight());
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        ItemStack stack = minecraft.player.containerMenu.getCarried();
        EnumColor color = getGaugeColor().getColor();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator && color != null) {
            if (gui() instanceof GuiMekanismTile<?, ?> gui && gui.getTileEntity() instanceof ISideConfiguration sideConfig && getTransmission() != null) {
                DataType dataType = null;
                ConfigInfo config = sideConfig.getConfig().getConfig(getTransmission());
                if (config != null) {
                    Set<DataType> supportedDataTypes = config.getSupportedDataTypes();
                    for (DataType type : supportedDataTypes) {
                        if (type.getColor() == color) {
                            dataType = type;
                            break;
                        }
                    }
                }
                if (dataType == null) {
                    displayTooltips(matrix, mouseX, mouseY, MekanismLang.GENERIC_PARENTHESIS.translateColored(color, color.getName()));
                } else {
                    displayTooltips(matrix, mouseX, mouseY, MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(color, dataType, color.getName()));
                }
            }
        } else {
            List<Component> list = new ArrayList<>();
            if (getLabel() != null) {
                list.add(getLabel());
            }
            list.addAll(getTooltipText());
            displayTooltips(matrix, mouseX, mouseY, list);
        }
    }

    @Nullable
    public abstract TransmissionType getTransmission();

    public void setDummyType(T type) {
        dummyType = type;
    }
}