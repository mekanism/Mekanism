package mekanism.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.IFancyFontRenderer;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public abstract class GuiElement extends AbstractWidget implements IFancyFontRenderer {

    private static final int BUTTON_TEX_X = 200, BUTTON_TEX_Y = 60, BUTTON_INDIVIDUAL_TEX_Y = BUTTON_TEX_Y / 3;
    public static final ResourceLocation WARNING_BACKGROUND_TEXTURE = MekanismUtils.getResource(ResourceType.GUI, "warning_background.png");
    public static final ResourceLocation WARNING_TEXTURE = MekanismUtils.getResource(ResourceType.GUI, "warning.png");

    public static final Minecraft minecraft = Minecraft.getInstance();

    protected ButtonBackground buttonBackground = ButtonBackground.NONE;

    protected final List<GuiElement> children = new ArrayList<>();
    /**
     * Children that don't get drawn or checked for beyond transferring data. This is mainly a helper to make it easier to update positioning information of background
     * helpers.
     */
    private final List<GuiElement> positionOnlyChildren = new ArrayList<>();

    private IGuiWrapper guiObj;
    @Nullable
    protected Holder<SoundEvent> clickSound;
    protected int relativeX;
    protected int relativeY;
    public boolean isOverlay;

    public GuiElement(IGuiWrapper gui, int x, int y, int width, int height) {
        this(gui, x, y, width, height, Component.empty());
    }

    public GuiElement(IGuiWrapper gui, int x, int y, int width, int height, Component text) {
        super(gui.getGuiLeft() + x, gui.getGuiTop() + y, width, height, text);
        this.relativeX = x;
        this.relativeY = y;
        this.guiObj = gui;
    }

    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput output) {
        //TODO: See GuiMekanism#addRenderableWidget for more details, and also figure out how to make this properly support nested narratables
        // as some of our GuiElements have sub GuiElements and those are the ones we actually would want to narrate
    }

    public int getRelativeX() {
        return relativeX;
    }

    public int getRelativeY() {
        return relativeY;
    }

    /**
     * Transfers this {@link GuiElement} to a new parent {@link IGuiWrapper}, and moves elements as needed.
     */
    public void transferToNewGui(IGuiWrapper gui) {
        int prevLeft = getGuiLeft();
        int prevTop = getGuiTop();
        //Use a separate method to update the guiObj for the element and all children
        // so that resize only gets called once
        transferToNewGuiInternal(gui);
        resize(prevLeft, prevTop, getGuiLeft(), getGuiTop());
    }

    private void transferToNewGuiInternal(IGuiWrapper gui) {
        guiObj = gui;
        children.forEach(child -> child.transferToNewGuiInternal(gui));
        //Transfer position only children as well
        positionOnlyChildren.forEach(child -> child.transferToNewGuiInternal(gui));
    }

    protected <ELEMENT extends GuiElement> ELEMENT addChild(ELEMENT element) {
        children.add(element);
        if (isOverlay) {
            element.isOverlay = true;
        }
        return element;
    }

    protected <ELEMENT extends GuiElement> ELEMENT addPositionOnlyChild(ELEMENT element) {
        positionOnlyChildren.add(element);
        return element;
    }

    public final IGuiWrapper gui() {
        return guiObj;
    }

    public final int getGuiLeft() {
        return guiObj.getGuiLeft();
    }

    public final int getGuiTop() {
        return guiObj.getGuiTop();
    }

    public final int getGuiWidth() {
        return guiObj.getXSize();
    }

    public final int getGuiHeight() {
        return guiObj.getYSize();
    }

    public List<GuiElement> children() {
        return children;
    }

    public void tick() {
        children.forEach(GuiElement::tick);
    }

    /**
     * @apiNote prevLeft and prevTop may be equal to left and top when things are being reinitialized such as when returning from viewing recipes in JEI.
     */
    public void resize(int prevLeft, int prevTop, int left, int top) {
        setX(getX() - prevLeft + left);
        setY(getY() - prevTop + top);
        children.forEach(child -> child.resize(prevLeft, prevTop, left, top));
        positionOnlyChildren.forEach(child -> child.resize(prevLeft, prevTop, left, top));
    }

    public boolean childrenContainsElement(Predicate<GuiElement> checker) {
        return children.stream().anyMatch(e -> e.containsElement(checker));
    }

    public boolean containsElement(Predicate<GuiElement> checker) {
        return checker.test(this) || childrenContainsElement(checker);
    }

    public void move(int changeX, int changeY) {
        setX(getX() + changeX);
        setY(getY() + changeY);
        //Note: When moving we need to adjust our relative position but when resizing, we don't as we are relative to the
        // positions changing when resizing, instead of moving where we are in relation to
        relativeX += changeX;
        relativeY += changeY;
        children.forEach(child -> child.move(changeX, changeY));
        positionOnlyChildren.forEach(child -> child.move(changeX, changeY));
    }

    public void onWindowClose() {
        children.forEach(GuiElement::onWindowClose);
    }

    protected ResourceLocation getButtonLocation(String name) {
        return MekanismUtils.getResource(ResourceType.GUI_BUTTON, name + ".png");
    }

    protected IHoverable getOnHover(ILangEntry translationHelper) {
        return getOnHover((Supplier<Component>) translationHelper::translate);
    }

    protected IHoverable getOnHover(Supplier<Component> componentSupplier) {
        return (onHover, guiGraphics, mouseX, mouseY) -> displayTooltips(guiGraphics, mouseX, mouseY, componentSupplier.get());
    }

    public boolean hasPersistentData() {
        return children.stream().anyMatch(GuiElement::hasPersistentData);
    }

    public void syncFrom(GuiElement element) {
        int numChildren = children.size();
        if (numChildren > 0) {
            for (int i = 0; i < element.children.size(); i++) {
                GuiElement prevChild = element.children.get(i);
                if (prevChild.hasPersistentData() && i < numChildren) {
                    GuiElement child = children.get(i);
                    // we're forced to assume that the children list is the same before and after the resize.
                    // for verification, we run a lightweight class equality check
                    if (child.getClass() == prevChild.getClass()) {
                        child.syncFrom(prevChild);
                    }
                }
            }
        }
    }

    public final void onRenderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, int zOffset, int totalOffset) {
        if (visible) {
            PoseStack pose = guiGraphics.pose();
            pose.translate(0, 0, zOffset);
            // update the max total offset to prevent clashing of future overlays
            GuiMekanism.maxZOffset = Math.max(totalOffset, GuiMekanism.maxZOffset);
            // render background overlay and children above everything else
            renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
            // render children just above background overlay
            children.forEach(child -> child.renderShifted(guiGraphics, mouseX, mouseY, 0));
            children.forEach(child -> child.onDrawBackground(guiGraphics, mouseX, mouseY, 0));
            renderForeground(guiGraphics, mouseX, mouseY);
            // translate forward to render child foreground
            children.forEach(child -> {
                //Only apply the z shift to each child instead of having future children be translated by more as well
                // Note: Does not apply to compounding with grandchildren as we want those to compound
                pose.pushPose();
                child.onRenderForeground(guiGraphics, mouseX, mouseY, 50, totalOffset + 50);
                pose.popPose();
            });
        }
    }

    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawButtonText(guiGraphics, mouseX, mouseY);
    }

    public void renderBackgroundOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    //TODO - 1.20: Evaluate new tooltip system and maybe make use of it??
    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        children.stream().filter(child -> child.isMouseOver(mouseX, mouseY))
              .forEach(child -> child.renderToolTip(guiGraphics, mouseX, mouseY));
    }

    public void displayTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, Component... components) {
        guiObj.displayTooltips(guiGraphics, mouseX, mouseY, components);
    }

    public void displayTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> components) {
        guiObj.displayTooltips(guiGraphics, mouseX, mouseY, components);
    }

    //TODO - 1.20: Do more testing to make sure all implementations of this behave appropriately
    // Also do we want things like the merged bars/gauges to have setFocused also mark the "children" as focused?
    @Nullable
    public GuiElement mouseClickedNested(double mouseX, double mouseY, int button) {
        for (int i = children.size() - 1; i >= 0; i--) {
            GuiElement child = children.get(i);
            GuiElement childResult = child.mouseClickedNested(mouseX, mouseY, button);
            if (childResult != null) {
                return childResult;
            }
        }
        //Vanilla Copy of super.mouseClicked modified to make the click keep track of the button used and return a GuiElement instead of a boolean
        if (this.active && this.visible && isValidClickButton(button) && clicked(mouseX, mouseY)) {
            playDownSound(minecraft.getSoundManager());
            onClick(mouseX, mouseY, button);
            return this;
        }
        return null;
    }

    @Override
    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        return mouseClickedNested(mouseX, mouseY, button) != null;
    }

    @Override
    public final void onClick(double mouseX, double mouseY) {
        //Redirect any calls of the vanilla on click methods mods may do for some reason to act as if clicked with the left mouse button
        onClick(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_LEFT);
    }

    public void onClick(double mouseX, double mouseY, int button) {
        //Pass on to basic super click call, which just happens to be a no-op
        super.onClick(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return GuiUtils.checkChildren(children, child -> child.keyPressed(keyCode, scanCode, modifiers)) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        return GuiUtils.checkChildren(children, child -> child.charTyped(c, keyCode)) || super.charTyped(c, keyCode);
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        children.forEach(element -> element.onDrag(mouseX, mouseY, deltaX, deltaY));
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        children.forEach(element -> element.onRelease(mouseX, mouseY));
        super.onRelease(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        return GuiUtils.checkChildren(children, child -> child.mouseScrolled(mouseX, mouseY, xDelta, yDelta)) || super.mouseScrolled(mouseX, mouseY, xDelta, yDelta);
    }

    @Override
    public Font getFont() {
        return guiObj.getFont();
    }

    @Override
    public final int getXSize() {
        return getWidth();
    }

    public void setButtonBackground(ButtonBackground buttonBackground) {
        this.buttonBackground = buttonBackground;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        //The code for clicked and isMouseOver is the same. Overriding it here lets us override isMouseOver in subclasses
        // and have it propagate to clicking
        return isMouseOver(mouseX, mouseY);
    }

    /**
     * Override this to render the button with a different x position than this GuiElement
     */
    protected int getButtonX() {
        //TODO: Re-evaluate uses of relativeX and see what would be more logical to have using this/getButtonY/Width/Height and potentially just override this in more locations
        return relativeX;
    }

    /**
     * Override this to render the button with a different y position than this GuiElement
     */
    protected int getButtonY() {
        return relativeY;
    }

    /**
     * Override this to render the button with a different width than this GuiElement
     */
    protected int getButtonWidth() {
        return width;
    }

    /**
     * Override this to render the button with a different height than this GuiElement
     */
    protected int getButtonHeight() {
        return height;
    }

    /**
     * Override this if you do not want {@link #drawButton(GuiGraphics, int, int)} to reset the color before drawing.
     */
    protected boolean resetColorBeforeRender() {
        return true;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY) || GuiUtils.checkChildren(children, child -> child.isMouseOver(mouseX, mouseY));
    }

    /**
     * Does the same as {@link #isMouseOver(double, double)}, but validates there is no window in the way
     */
    public final boolean isMouseOverCheckWindows(double mouseX, double mouseY) {
        //TODO: Ideally we would have the various places that call this instead check isHovered if we can properly override setting that
        boolean isHovering = isMouseOver(mouseX, mouseY);
        return checkWindows(mouseX, mouseY, isHovering);
    }

    /**
     * Helper to correct potentially inaccurate hovering or in bounds checks.
     */
    protected final boolean checkWindows(double mouseX, double mouseY) {
        return checkWindows(mouseX, mouseY, true);
    }

    /**
     * Helper to correct potentially inaccurate hovering or in bounds checks.
     */
    protected final boolean checkWindows(double mouseX, double mouseY, boolean isHovering) {
        if (isHovering) {
            //If the mouse is over this element, check if there is a window that would intercept the mouse
            GuiWindow window = guiObj.getWindowHovering(mouseX, mouseY);
            if (window != null && !window.childrenContainsElement(e -> e == this)) {
                //If there is and this element is not part of that window,
                // then mark that our mouse is not over the element
                isHovering = false;
            }
        }
        return isHovering;
    }

    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (buttonBackground != ButtonBackground.NONE) {
            drawButton(guiGraphics, mouseX, mouseY);
        }
    }

    public final void onDrawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    public final void renderShifted(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //Note: We copy super's visible check here so that if it is not visible we can skip the pose stack transforms
        if (visible) {
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            // fix render offset to be as we expect things to be for how we implement our render methods (based on relatives)
            pose.translate(getGuiLeft(), getGuiTop(), 0);
            renderShifted(guiGraphics, mouseX, mouseY, partialTicks);
            pose.popPose();
        }
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    }

    @Override
    public int getFGColor() {
        if (packedFGColor != UNSET_FG_COLOR){
            return packedFGColor;
        }
        return this.active ? activeButtonTextColor() : inactiveButtonTextColor();
    }

    protected int getButtonTextColor(int mouseX, int mouseY) {
        return getFGColor();
    }

    protected void drawButtonText(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component text = getMessage();
        //Only attempt to draw the message if we have a message to draw
        if (!text.getString().isEmpty()) {
            int color = getButtonTextColor(mouseX, mouseY) | Mth.ceil(alpha * 255.0F) << 24;
            drawCenteredTextScaledBound(guiGraphics, text, width - 4, height / 2F - 4, color);
        }
    }

    protected int getButtonTextureY(boolean hoveredOrFocused) {
        if (!this.active) {
            return 0;
        } else if (hoveredOrFocused) {
            return 2;
        }
        return 1;
    }

    /**
     * Based on the code in AbstractButton#renderWidget
     */
    protected void drawButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (resetColorBeforeRender()) {
            MekanismRenderer.resetColor(guiGraphics);
        }
        ResourceLocation texture = buttonBackground.getTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int i = getButtonTextureY(isMouseOverCheckWindows(mouseX, mouseY));
        //Note: SliceWidth and sliceHeight are copied from AbstractButton
        GuiUtils.blitNineSlicedSized(guiGraphics, texture, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight(), 20, 4, BUTTON_TEX_X,
              BUTTON_INDIVIDUAL_TEX_Y, 0, i * 20, BUTTON_TEX_X, BUTTON_TEX_Y);
    }

    protected void renderExtendedTexture(GuiGraphics guiGraphics, ResourceLocation resource, int sideWidth, int sideHeight) {
        GuiUtils.renderExtendedTexture(guiGraphics, resource, sideWidth, sideHeight, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
    }

    protected void renderBackgroundTexture(GuiGraphics guiGraphics, ResourceLocation resource, int sideWidth, int sideHeight) {
        GuiUtils.renderBackgroundTexture(guiGraphics, resource, sideWidth, sideHeight, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight(), 256, 256);
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundHandler) {
        if (clickSound != null) {
            playClickSound(soundHandler, clickSound);
        }
    }

    protected static void playClickSound(Holder<SoundEvent> sound) {
        playClickSound(minecraft.getSoundManager(), sound);
    }

    private static void playClickSound(@NotNull SoundManager soundHandler, @NotNull Holder<SoundEvent> sound) {
        soundHandler.play(SimpleSoundInstance.forUI(sound, 1.0F));
    }

    protected void drawTiledSprite(GuiGraphics guiGraphics, int xPosition, int yPosition, int yOffset, int desiredWidth, int desiredHeight, TextureAtlasSprite sprite,
          TilingDirection tilingDirection) {
        GuiUtils.drawTiledSprite(guiGraphics, xPosition, yPosition, yOffset, desiredWidth, desiredHeight, sprite, 16, 16, 0, tilingDirection);
    }

    @Override
    public void drawCenteredTextScaledBound(GuiGraphics guiGraphics, Component text, float maxLength, float x, float y, int color) {
        IFancyFontRenderer.super.drawCenteredTextScaledBound(guiGraphics, text, maxLength, relativeX + x, relativeY + y, color);
    }

    public enum ButtonBackground {
        DEFAULT(MekanismUtils.getResource(ResourceType.GUI, "button.png")),
        DIGITAL(MekanismUtils.getResource(ResourceType.GUI, "button_digital.png")),
        NONE(null);

        private final ResourceLocation texture;

        ButtonBackground(ResourceLocation texture) {
            this.texture = texture;
        }

        public ResourceLocation getTexture() {
            return texture;
        }
    }

    @FunctionalInterface
    public interface IHoverable {

        void onHover(GuiElement element, GuiGraphics guiGraphics, int mouseX, int mouseY);
    }

    @FunctionalInterface
    public interface IClickable {

        boolean onClick(GuiElement element, int mouseX, int mouseY);
    }
}