package mekanism.client.gui.element;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//TODO: Better name?
@OnlyIn(Dist.CLIENT)
public abstract class GuiInsetElement<TILE extends TileEntity> extends GuiTileEntityElement<TILE> {

    public static final ResourceLocation INSET_HOLDER_LEFT = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "inset_holder_left.png");
    public static final ResourceLocation INSET_HOLDER_RIGHT = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "inset_holder_right.png");
    protected final int border;
    protected final int innerWidth;
    protected final int innerHeight;
    protected final boolean left;

    //TODO: Improve the overlays by having some spots have alpha for transparency rather than only keeping the "key" parts. Do the same for the MekanismImageButtons
    public GuiInsetElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def, TILE tile, int x, int y, int size, int innerSize) {
        this(resource, gui, def, tile, x, y, size, size, innerSize);
    }

    public GuiInsetElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def, TILE tile, int x, int y, int width, int height, int innerSize) {
        super(resource, gui, def, tile, x, y, width, height);
        this.innerWidth = innerSize;
        this.innerHeight = innerSize;
        //TODO: decide what to do if this doesn't divide nicely
        this.border = (width - innerSize) / 2;
        this.left = x < 0;
    }

    protected ResourceLocation getHolderTexture() {
        return left ? INSET_HOLDER_LEFT : INSET_HOLDER_RIGHT;
    }

    protected ResourceLocation getOverlay() {
        return RESOURCE;
    }

    @Override
    public boolean isMouseOver(double xAxis, double yAxis) {
        //TODO: override isHovered
        return this.active && this.visible && xAxis >= x + border && xAxis < x + width - border && yAxis >= y + border && yAxis < y + height - border;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }

    protected void colorTab() {
        //Don't do any coloring by default
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getHolderTexture());
        colorTab();
        guiObj.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        MekanismRenderer.resetColor();

        //Draw the button background
        int buttonX = x + border + (left ? 1 : -1);
        int buttonY = y + border;
        drawButton(mouseX, mouseY, buttonX, buttonY);

        minecraft.textureManager.bindTexture(getOverlay());
        guiObj.drawModalRectWithCustomSizedTexture(buttonX, buttonY, 0, 0, innerWidth, innerHeight, innerWidth, innerHeight);
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    //TODO: Move this to a helper method somewhere instead of duplicating it both here and in MekanismButton.
    private void drawButton(int mouseX, int mouseY, int buttonX, int buttonY) {
        MekanismRenderer.bindTexture(WIDGETS_LOCATION);
        int i = getYImage(isMouseOver(mouseX, mouseY));
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        int halfWidthLeft = innerWidth / 2;
        int halfWidthRight = innerWidth % 2 == 0 ? halfWidthLeft : halfWidthLeft + 1;
        int halfHeightTop = innerHeight / 2;
        int halfHeightBottom = innerHeight % 2 == 0 ? halfHeightTop : halfHeightTop + 1;
        int position = 46 + i * 20;
        //Left Top Corner
        blit(buttonX, buttonY, 0, position, halfWidthLeft, halfHeightTop);
        //Left Bottom Corner
        blit(buttonX, buttonY + halfHeightTop, 0, position + 20 - halfHeightBottom, halfWidthLeft, halfHeightBottom);
        //Right Top Corner
        blit(buttonX + halfWidthLeft, buttonY, 200 - halfWidthRight, position, halfWidthRight, halfHeightTop);
        //Right Bottom Corner
        blit(buttonX + halfWidthLeft, buttonY + halfHeightTop, 200 - halfWidthRight, position + 20 - halfHeightBottom, halfWidthRight, halfHeightBottom);
    }
}