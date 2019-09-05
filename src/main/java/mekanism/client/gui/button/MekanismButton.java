package mekanism.client.gui.button;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//TODO: Convert some of the buttons to being GuiElement instead. Mainly ones that don't even bother rendering text
public class MekanismButton extends Button {

    private final IHoverable onHover;
    private final IPressable onRightClick;

    public MekanismButton(int x, int y, int width, int height, String text, IPressable onPress, IHoverable onHover) {
        this(x, y, width, height, text, onPress, onPress, onHover);
        //TODO: Decide if default implementation for right clicking should be do nothing, or act as left click
    }

    public MekanismButton(int x, int y, int width, int height, String text, IPressable onPress, IPressable onRightClick, IHoverable onHover) {
        super(x, y, width, height, text, onPress);
        this.onHover = onHover;
        this.onRightClick = onRightClick;
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        if (onHover != null) {
            onHover.onHover(this, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.active && this.visible && isHovered()) {
            if (button == 1) {
                //Right clicked
                playDownSound(Minecraft.getInstance().getSoundHandler());
                onRightClick();
                return true;
            }
        }
        return false;
    }

    protected boolean resetColorBeforeRender() {
        return true;
    }

    //TODO: Add right click support to GuiElement
    protected void onRightClick() {
        if (onRightClick != null) {
            onRightClick.onPress(this);
        }
    }

    //TODO: Convert this stuff into a javadoc
    //Based off how it is drawn in Widget, except that instead of drawing left half and right half, we draw all four corners individually
    // The benefit of drawing all four corners instead of just left and right halves, is that we ensure we include the bottom black bar of the texture
    // Math has also been added to fix rendering odd size buttons.
    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        if (resetColorBeforeRender()) {
            //TODO: Support alpha like super? Is there a point
            MekanismRenderer.resetColor();
        }
        MekanismRenderer.bindTexture(WIDGETS_LOCATION);
        int i = getYImage(isHovered());
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        int halfWidthLeft = width / 2;
        int halfWidthRight = width % 2 == 0 ? halfWidthLeft : halfWidthLeft + 1;
        int halfHeightTop = height / 2;
        int halfHeightBottom = height % 2 == 0 ? halfHeightTop : halfHeightTop + 1;
        int position = 46 + i * 20;
        //Left Top Corner
        blit(x, y, 0, position, halfWidthLeft, halfHeightTop);
        //Left Bottom Corner
        blit(x, y + halfHeightTop, 0, position + 20 - halfHeightBottom, halfWidthLeft, halfHeightBottom);
        //Right Top Corner
        blit(x + halfWidthLeft, y, 200 - halfWidthRight, position, halfWidthRight, halfHeightTop);
        //Right Bottom Corner
        blit(x + halfWidthLeft, y + halfHeightTop, 200 - halfWidthRight, position + 20 - halfHeightBottom, halfWidthRight, halfHeightBottom);

        //TODO: Add support for buttons that are larger than 200x20 in either direction (most likely would be in the height direction

        Minecraft minecraft = Minecraft.getInstance();
        this.renderBg(minecraft, mouseX, mouseY);

        if (!getMessage().isEmpty()) {
            //TODO: Improve the math for this so that it calculates the y value better
            drawCenteredString(minecraft.fontRenderer, getMessage(),
                  x + halfWidthLeft, y + (height - 8) / 2,
                  getFGColor() | MathHelper.ceil(alpha * 255.0F) << 24);
        }
        GlStateManager.disableBlend();
    }

    @OnlyIn(Dist.CLIENT)
    @FunctionalInterface
    public interface IHoverable {
        void onHover(Button button, int mouseX, int mouseY);
    }
}