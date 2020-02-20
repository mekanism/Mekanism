package mekanism.client.gui.element.gauge;

import java.util.Set;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiGauge<T> extends GuiTexturedElement {

    protected EnumColor color;
    protected final int texX;
    protected final int texY;
    protected final int number;
    protected boolean dummy;

    protected T dummyType;

    public GuiGauge(Type type, IGuiWrapper gui, int x, int y) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, type.textureLocation), gui, x, y, type.width, type.height);
        texX = type.texX;
        texY = type.texY;
        color = type.color;
        number = type.number;
    }

    public abstract int getScaledLevel();

    public abstract TextureAtlasSprite getIcon();

    public abstract ITextComponent getTooltipText();

    protected void applyRenderColor() {
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        guiObj.drawTexturedRect(x, y, texX, texY, width, height);
        if (!dummy) {
            int scale = getScaledLevel();
            TextureAtlasSprite icon = getIcon();
            if (scale > 0 && icon != null) {
                int start = 0;
                applyRenderColor();
                minecraft.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                while (scale > 0) {
                    int renderRemaining;
                    if (scale > 16) {
                        renderRemaining = 16;
                        scale -= 16;
                    } else {
                        renderRemaining = scale;
                        scale = 0;
                    }
                    for (int i = 0; i < number; i++) {
                        guiObj.drawTexturedRectFromIcon(x + 16 * i + 1, y + height - renderRemaining - start - 1, icon, 16, renderRemaining);
                    }
                    start += 16;
                }
                MekanismRenderer.resetColor();
                minecraft.textureManager.bindTexture(getResource());
            }
            guiObj.drawTexturedRect(x, y, width, 0, width, height);
        }
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        ItemStack stack = minecraft.player.inventory.getItemStack();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator && color != null) {
            if (guiObj instanceof GuiMekanismTile) {
                TileEntityMekanism tile = ((GuiMekanismTile<?, ?>) guiObj).getTileEntity();
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
                        guiObj.displayTooltip(MekanismLang.GENERIC_PARENTHESIS.translateColored(color, color.getName()), mouseX, mouseY);
                    } else {
                        guiObj.displayTooltip(TextComponentUtil.build(color, dataType, MekanismLang.GENERIC_PARENTHESIS.translate(color.getName())), mouseX, mouseY);
                    }
                }
            }
        } else {
            guiObj.displayTooltip(getTooltipText(), mouseX, mouseY);
        }
    }

    public abstract TransmissionType getTransmission();

    public void setDummyType(T type) {
        dummyType = type;
    }

    public enum Type {
        STANDARD(null, 18, 60, 0, 0, 1, "standard_gauge.png"),
        STANDARD_YELLOW(EnumColor.YELLOW, 18, 60, 0, 60, 1, "standard_gauge.png"),
        STANDARD_RED(EnumColor.DARK_RED, 18, 60, 0, 120, 1, "standard_gauge.png"),
        STANDARD_ORANGE(EnumColor.ORANGE, 18, 60, 0, 180, 1, "standard_gauge.png"),
        STANDARD_BLUE(EnumColor.DARK_BLUE, 18, 60, 0, 240, 1, "standard_gauge.png"),
        WIDE(null, 66, 50, 0, 0, 4, "wide_gauge.png"),
        WIDE_YELLOW(EnumColor.YELLOW, 66, 50, 0, 50, 4, "wide_gauge.png"),
        WIDE_RED(EnumColor.DARK_RED, 66, 50, 0, 100, 4, "wide_gauge.png"),
        WIDE_ORANGE(EnumColor.ORANGE, 66, 50, 0, 150, 4, "wide_gauge.png"),
        WIDE_BLUE(EnumColor.DARK_BLUE, 66, 50, 0, 200, 4, "wide_gauge.png"),
        SMALL(null, 18, 30, 0, 0, 1, "small_gauge.png"),
        SMALL_YELLOW(EnumColor.YELLOW, 18, 30, 0, 30, 1, "small_gauge.png"),
        SMALL_RED(EnumColor.DARK_RED, 18, 30, 0, 60, 1, "small_gauge.png"),
        SMALL_ORANGE(EnumColor.ORANGE, 18, 30, 0, 90, 1, "small_gauge.png"),
        SMALL_BLUE(EnumColor.DARK_BLUE, 18, 30, 0, 120, 1, "small_gauge.png");

        public final EnumColor color;
        public final int width;
        public final int height;
        public final int texX;
        public final int texY;
        public final int number;
        public final String textureLocation;

        Type(EnumColor c, int w, int h, int tx, int ty, int n, String t) {
            color = c;
            width = w;
            height = h;
            texX = tx;
            texY = ty;
            number = n;
            textureLocation = t;
        }
    }
}