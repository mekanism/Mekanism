package mekanism.client.gui.element.gauge;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.warning.WarningTracker.WarningType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiGauge<T> extends GuiTexturedElement {

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

    //TODO - WARNING SYSTEM: Hook up usage of warnings
    public GuiGauge<T> warning(@Nonnull WarningType type, @Nonnull BooleanSupplier warningSupplier) {
        this.warningSupplier = gui().trackWarning(type, warningSupplier);
        return this;
    }

    public abstract int getScaledLevel();

    public abstract TextureAtlasSprite getIcon();

    public abstract ITextComponent getLabel();

    public abstract List<ITextComponent> getTooltipText();

    public GaugeOverlay getGaugeOverlay() {
        return gaugeType.getGaugeOverlay();
    }

    protected GaugeInfo getGaugeColor() {
        return gaugeType.getGaugeInfo();
    }

    protected void applyRenderColor() {
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        GaugeInfo color = getGaugeColor();
        renderExtendedTexture(matrix, color.getResourceLocation(), color.getSideWidth(), color.getSideHeight());
        if (!dummy) {
            renderContents(matrix);
        }
    }

    public void renderContents(MatrixStack matrix) {
        boolean warning = warningSupplier != null && warningSupplier.getAsBoolean();
        if (warning) {
            //Draw background (we do it regardless of if we are full or not as if the thing being drawn has transparency
            // we may as well show the background)
            minecraft.textureManager.bind(GuiSlot.WARNING_BACKGROUND_TEXTURE);
            blit(matrix, x + 1, y + 1, 0, 0, width - 2, height - 2, 256, 256);
        }
        int scale = getScaledLevel();
        TextureAtlasSprite icon = getIcon();
        if (scale > 0 && icon != null) {
            applyRenderColor();
            drawTiledSprite(matrix, x + 1, y + 1, height - 2, width - 2, scale, icon, TilingDirection.UP_RIGHT);
            MekanismRenderer.resetColor();
            if (warning && scale == height - 2) {
                //TODO - WARNING SYSTEM: Also decide if this should be using some check for when it is just close to max so that it is easily visible
                //If we have a warning and the gauge is entirely filled draw a warning vertically next to it
                minecraft.textureManager.bind(WARNING_TEXTURE);
                int halfWidth = (width - 2) / 2;
                //Note: We also start the drawing after half the width so that we are sure it will properly line up with the background
                blit(matrix, x + 1 + halfWidth, y + 1, halfWidth, 0, halfWidth, height - 2, 256, 256);
            }
        }
        //Draw the bar overlay
        drawBarOverlay(matrix);
    }

    public void drawBarOverlay(MatrixStack matrix) {
        minecraft.textureManager.bind(getResource());
        GaugeOverlay gaugeOverlay = getGaugeOverlay();
        blit(matrix, x + 1, y + 1, getWidth() - 2, getHeight() - 2, 0, 0, gaugeOverlay.getWidth(), gaugeOverlay.getHeight(), gaugeOverlay.getWidth(), gaugeOverlay.getHeight());
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        ItemStack stack = minecraft.player.inventory.getCarried();
        EnumColor color = getGaugeColor().getColor();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator && color != null) {
            if (gui() instanceof GuiMekanismTile) {
                TileEntityMekanism tile = ((GuiMekanismTile<?, ?>) gui()).getTileEntity();
                if (tile instanceof ISideConfiguration && getTransmission() != null) {
                    DataType dataType = null;
                    ConfigInfo config = ((ISideConfiguration) tile).getConfig().getConfig(getTransmission());
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
                        displayTooltip(matrix, MekanismLang.GENERIC_PARENTHESIS.translateColored(color, color.getName()), mouseX, mouseY);
                    } else {
                        displayTooltip(matrix, MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(color, dataType, color.getName()), mouseX, mouseY);
                    }
                }
            }
        } else {
            List<ITextComponent> list = new ArrayList<>();
            if (getLabel() != null) {
                list.add(getLabel());
            }
            list.addAll(getTooltipText());
            displayTooltips(matrix, list, mouseX, mouseY);
        }
    }

    @Nullable
    public abstract TransmissionType getTransmission();

    public void setDummyType(T type) {
        dummyType = type;
    }
}