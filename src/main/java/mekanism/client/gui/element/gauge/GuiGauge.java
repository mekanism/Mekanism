package mekanism.client.gui.element.gauge;

import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiGauge<T> extends GuiElement {

    protected EnumColor color;
    protected final int xLocation;
    protected final int yLocation;
    protected final int texX;
    protected final int texY;
    protected final int width;
    protected final int height;
    protected final int number;
    protected boolean dummy;

    protected T dummyType;

    public GuiGauge(Type type, IGuiWrapper gui, ResourceLocation def, int x, int y) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, type.textureLocation), gui, def);
        xLocation = x;
        yLocation = y;

        width = type.width;
        height = type.height;

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
    public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(guiWidth + xLocation, guiHeight + yLocation, texX, texY, width, height);
        if (!dummy) {
            renderScale(guiWidth, guiHeight);
        }
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    public void renderScale(int guiWidth, int guiHeight) {
        if (getScaledLevel() == 0 || getIcon() == null) {
            guiObj.drawTexturedRect(guiWidth + xLocation, guiHeight + yLocation, width, 0, width, height);
            return;
        }

        int scale = getScaledLevel();
        int start = 0;

        applyRenderColor();
        while (scale > 0) {
            int renderRemaining;
            if (scale > 16) {
                renderRemaining = 16;
                scale -= 16;
            } else {
                renderRemaining = scale;
                scale = 0;
            }

            minecraft.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            for (int i = 0; i < number; i++) {
                guiObj.drawTexturedRectFromIcon(guiWidth + xLocation + 16 * i + 1, guiHeight + yLocation + height - renderRemaining - start - 1, getIcon(), 16, renderRemaining);
            }
            start += 16;
            if (scale == 0) {
                break;
            }
        }
        MekanismRenderer.resetColor();
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(guiWidth + xLocation, guiHeight + yLocation, width, 0, width, height);
    }

    @Override
    public void renderForeground(int xAxis, int yAxis) {
        if (xAxis >= xLocation + 1 && xAxis <= xLocation + width - 1 && yAxis >= yLocation + 1 && yAxis <= yLocation + height - 1) {
            ItemStack stack = minecraft.player.inventory.getItemStack();
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator && color != null) {
                if (guiObj instanceof GuiMekanismTile) {
                    TileEntity tile = ((GuiMekanismTile) guiObj).getTileEntity();
                    if (tile instanceof ISideConfiguration && getTransmission() != null) {
                        SideData data = null;
                        for (SideData iterData : ((ISideConfiguration) tile).getConfig().getOutputs(getTransmission())) {
                            if (iterData.color == color) {
                                data = iterData;
                                break;
                            }
                        }
                        if (data == null) {
                            guiObj.displayTooltip(TextComponentUtil.build(color, "(", color.getColoredName(), ")"), xAxis, yAxis);
                        } else {
                            guiObj.displayTooltip(TextComponentUtil.build(color, data, " (", color.getColoredName(), ")"), xAxis, yAxis);
                        }
                    }
                }
            } else {
                guiObj.displayTooltip(getTooltipText(), xAxis, yAxis);
            }
        }
    }

    @Override
    public boolean preMouseClicked(double mouseX, double mouseY, int button) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
    }

    public abstract TransmissionType getTransmission();

    public void setDummyType(T type) {
        dummyType = type;
    }

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth + xLocation, guiHeight + yLocation, width, height);
    }

    public enum Type {
        STANDARD(null, 18, 60, 0, 0, 1, "GuiGaugeStandard.png"),
        STANDARD_YELLOW(EnumColor.YELLOW, 18, 60, 0, 60, 1, "GuiGaugeStandard.png"),
        STANDARD_RED(EnumColor.DARK_RED, 18, 60, 0, 120, 1, "GuiGaugeStandard.png"),
        STANDARD_ORANGE(EnumColor.ORANGE, 18, 60, 0, 180, 1, "GuiGaugeStandard.png"),
        STANDARD_BLUE(EnumColor.DARK_BLUE, 18, 60, 0, 240, 1, "GuiGaugeStandard.png"),
        WIDE(null, 66, 50, 0, 0, 4, "GuiGaugeWide.png"),
        WIDE_YELLOW(EnumColor.YELLOW, 66, 50, 0, 50, 4, "GuiGaugeWide.png"),
        WIDE_RED(EnumColor.DARK_RED, 66, 50, 0, 100, 4, "GuiGaugeWide.png"),
        WIDE_ORANGE(EnumColor.ORANGE, 66, 50, 0, 150, 4, "GuiGaugeWide.png"),
        WIDE_BLUE(EnumColor.DARK_BLUE, 66, 50, 0, 200, 4, "GuiGaugeWide.png"),
        SMALL(null, 18, 30, 0, 0, 1, "GuiGaugeSmall.png"),
        SMALL_YELLOW(EnumColor.YELLOW, 18, 30, 0, 30, 1, "GuiGaugeSmall.png"),
        SMALL_RED(EnumColor.DARK_RED, 18, 30, 0, 60, 1, "GuiGaugeSmall.png"),
        SMALL_ORANGE(EnumColor.ORANGE, 18, 30, 0, 90, 1, "GuiGaugeSmall.png"),
        SMALL_BLUE(EnumColor.DARK_BLUE, 18, 30, 0, 120, 1, "GuiGaugeSmall.png");

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