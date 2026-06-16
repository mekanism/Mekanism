package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.render.IFancyFontRenderer;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.FocusNavigationEvent.ArrowNavigation;
import net.minecraft.client.gui.navigation.FocusNavigationEvent.InitialFocus;
import net.minecraft.client.gui.navigation.FocusNavigationEvent.TabNavigation;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

//Note: We don't just extend AbstractContainerWidget as we want to be able to reference default implementations of AbstractWidget
public abstract class GuiElement extends AbstractWidget implements IFancyFontRenderer, ContainerEventHandler {

    public static final Identifier WARNING_BACKGROUND_TEXTURE = MekanismUtils.getResource(ResourceType.GUI, "warning_background.png");
    public static final Identifier WARNING_TEXTURE = MekanismUtils.getResource(ResourceType.GUI, "warning.png");
    protected static Supplier<SoundEvent> BUTTON_CLICK_SOUND = SoundEvents.UI_BUTTON_CLICK::value;

    public static final Minecraft minecraft = Minecraft.getInstance();

    protected ButtonBackground buttonBackground = ButtonBackground.NONE;

    private final List<GuiElement> children = new ArrayList<>();
    /// Children that don't get drawn or checked for beyond transferring data. This is mainly a helper to make it easier to update positioning information of background
    /// helpers.
    private final List<GuiElement> positionOnlyChildren = new ArrayList<>();

    private IGuiWrapper guiObj;
    @Nullable
    protected Supplier<SoundEvent> clickSound;
    //Default to the value from SimpleSoundInstance.forUI
    protected float clickVolume = 0.25F;
    protected int relativeX;
    protected int relativeY;
    public boolean isOverlay;

    @Nullable
    private GuiElement focused;
    private boolean isDragging;

    public GuiElement(IGuiWrapper gui, int x, int y, int width, int height) {
        this(gui, x, y, width, height, CommonComponents.EMPTY);
    }

    public GuiElement(IGuiWrapper gui, int x, int y, int width, int height, Component text) {
        super(gui.getGuiLeft() + x, gui.getGuiTop() + y, width, height, text);
        this.relativeX = x;
        this.relativeY = y;
        this.guiObj = gui;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        //TODO: See GuiMekanism#addRenderableWidget for more details, and also figure out how to make this properly support nested narratables
        // as some of our GuiElements have sub GuiElements and those are the ones we actually would want to narrate
    }

    public GuiElement setTooltip(ILangEntry langEntry) {
        setTooltip(TooltipUtils.create(langEntry));
        return this;
    }

    /// Perform pre [#extractRenderState] tasks like update visibility.
    ///
    /// Only works under [GuiMekanism]
    ///
    /// This exists because [AbstractWidget#extractRenderState(GuiGraphicsExtractor, int, int, float)] is final
    public void updateBeforeExtract() {
    }

    protected void clearTooltip() {
        setTooltip((Tooltip) null);
    }

    public int getRelativeX() {
        return relativeX;
    }

    public int getRelativeY() {
        return relativeY;
    }

    public int getRelativeRight() {
        return getRelativeX() + getWidth();
    }

    public int getRelativeBottom() {
        return getRelativeY() + getHeight();
    }

    /// Transfers this [GuiElement] to a new parent [IGuiWrapper], and moves elements as needed.
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
        for (GuiElement guiElement : children) {
            guiElement.transferToNewGuiInternal(gui);
        }
        //Transfer position only children as well
        for (GuiElement child : positionOnlyChildren) {
            child.transferToNewGuiInternal(gui);
        }
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

    @Override
    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(getGuiLeft() + getButtonX(), getGuiTop() + getButtonY(), getButtonWidth(), getButtonHeight());
    }

    protected ScreenRectangle getTooltipRectangle(int mouseX, int mouseY) {
        return getRectangle();
    }

    @Override
    public List<GuiElement> children() {
        return children;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        super.visitWidgets(consumer);
        children.forEach(consumer);
    }

    public void tick() {
        children.forEach(GuiElement::tick);
    }

    /// @apiNote prevLeft and prevTop may be equal to left and top when things are being reinitialized such as when returning from viewing recipes in JEI.
    public void resize(int prevLeft, int prevTop, int left, int top) {
        setPosition(getX() - prevLeft + left, getY() - prevTop + top);
        for (GuiElement guiElement : children) {
            guiElement.resize(prevLeft, prevTop, left, top);
        }
        for (GuiElement child : positionOnlyChildren) {
            child.resize(prevLeft, prevTop, left, top);
        }
    }

    public final boolean childrenContainsElement(Predicate<GuiElement> checker) {
        for (GuiElement child : children) {
            if (child.containsElement(checker)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsElement(Predicate<GuiElement> checker) {
        return checker.test(this) || childrenContainsElement(checker);
    }

    public void move(int changeX, int changeY) {
        setPosition(getX() + changeX, getY() + changeY);
        //Note: When moving we need to adjust our relative position but when resizing, we don't as we are relative to the
        // positions changing when resizing, instead of moving where we are in relation to
        relativeX += changeX;
        relativeY += changeY;
        for (GuiElement child : children) {
            child.move(changeX, changeY);
        }
        for (GuiElement child : positionOnlyChildren) {
            child.move(changeX, changeY);
        }
    }

    public void onWindowClose() {
        children.forEach(GuiElement::onWindowClose);
    }

    protected static Identifier getButtonLocation(String name) {
        return MekanismUtils.getResource(ResourceType.GUI_BUTTON, name + ".png");
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

    public final void onRenderForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (visible) {
            // render background overlay and children above everything else
            renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
            // render children just above background overlay
            for (GuiElement guiElement : children) {
                guiElement.renderShifted(guiGraphics, mouseX, mouseY, 0);
            }
            for (GuiElement guiElement : children) {
                guiElement.onDrawBackground(guiGraphics, mouseX, mouseY, 0);
            }
            renderForeground(guiGraphics, mouseX, mouseY);
            // translate forward to render child foreground
            for (GuiElement child : children) {
                // Note: Does not apply to compounding with grandchildren as we want those to compound
                child.onRenderForeground(guiGraphics, mouseX, mouseY);
            }
        }
    }

    public void renderForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        drawButtonText(guiGraphics, mouseX, mouseY);
    }

    public void renderBackgroundOverlay(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
    }

    public void openPinnedWindows() {
        children.forEach(GuiElement::openPinnedWindows);
    }

    //TODO: Evaluate if we can somehow move the remaining uses to the new tooltip system
    public void renderToolTip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        updateTooltip(mouseX, mouseY);
        //If there is a tooltip, update it for the next render pass
        // We also call it regardless of whether the backing tooltip is null so that we properly mark wasDisplayed as false
        //Note: We only call this method if we are hovering the proper spot
        //TODO - 26.2: Is this the correct mouse x and mouse y to be passing? Do we still need to be calling updateTooltip above?
        tooltip.refreshTooltipForNextRenderPass(guiGraphics, mouseX, mouseY, true, isFocused(), getTooltipRectangle(mouseX, mouseY));
        //We do this before child renders so that if one has a tooltip then they can override the target tooltip
        for (GuiElement child : children) {
            if (child.isMouseOver(mouseX, mouseY)) {
                child.renderToolTip(guiGraphics, mouseX, mouseY);
            }
        }
    }

    public void updateTooltip(int mouseX, int mouseY) {
        //TODO: Update child tooltips? Currently sort of is handled by renderTooltip
    }

    @Override
    public final boolean isDragging() {
        return this.isDragging;
    }

    @Override
    public final void setDragging(boolean dragging) {
        this.isDragging = dragging;
    }

    @Override
    public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        if (checkWindows(mouseX, mouseY)) {
            //If we are are not covered by a window, try to locate which child we are over
            return Optional.ofNullable(GuiUtils.findChild(children, mouseX, mouseY, GuiElement::isMouseOver));
        }
        return Optional.empty();
    }

    @Nullable
    @Override
    public GuiElement getFocused() {
        return this.focused;
    }

    @Override
    public boolean isFocused() {
        return super.isFocused() || getFocused() != null;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }
        if (listener instanceof GuiElement child) {
            child.setFocused(true);
            this.focused = child;
        } else {
            this.focused = null;
        }
    }

    private void clearFocus() {
        //Copy of Screen#clearFocus
        // We use super's getFocusPath so that we don't change the focus of this element
        ComponentPath path = ContainerEventHandler.super.getCurrentFocusPath();
        if (path != null) {
            path.applyFocus(false);
        }
    }

    @Nullable
    @Override
    public ComponentPath getCurrentFocusPath() {
        GuiElement currentFocus = getFocused();
        if (currentFocus != null) {
            return ComponentPath.path(this, currentFocus.getCurrentFocusPath());
        } else if (isFocused()) {
            return ComponentPath.leaf(this);
        }
        return null;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        if (!this.active || !this.visible) {
            //If we aren't active or aren't visible, don't check if we can be the next focus path
            return null;
        }
        if (!isFocused()) {
            return switch (event) {
                case ArrowNavigation _ when supportsArrowNavigation() -> ComponentPath.leaf(this);
                case TabNavigation _ when supportsTabNavigation() -> ComponentPath.leaf(this);
                case InitialFocus _ -> ComponentPath.leaf(this);
                default -> ContainerEventHandler.super.nextFocusPath(event);
            };
        }
        return ContainerEventHandler.super.nextFocusPath(event);
    }

    protected boolean supportsTabNavigation() {
        //TODO: Support navigation (both tab and arrow) for more elements
        return false;
    }

    protected boolean supportsArrowNavigation() {
        return false;
    }

    //TODO - 1.20: Do we want things like the merged bars/gauges to have setFocused also mark the "children" as focused?
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        GuiElement clickedChild = GuiUtils.findChild(children, event, isDoubleClick, GuiElement::mouseClicked);
        //Note: This setFocused call is outside the clickedChild find, so that if we couldn't find one
        // then we un-focus whatever child is currently focused
        if (clickedChild != null) {
            setFocused(clickedChild);
            return true;
        } else if (!children.isEmpty()) {
            //If we can't find a child, but we do have some, allow clearing whatever focus we currently have
            clearFocus();
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return GuiUtils.checkChildren(children, event, GuiElement::keyPressed) || super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return GuiUtils.checkChildrenChar(children, event, GuiElement::charTyped) || super.charTyped(event);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double deltaX, double deltaY) {
        //TODO - 1.20.4: For this and onRelease etc do we want to somewhat do something like ContainerEventHandler does
        // where it only does the focused element?
        for (GuiElement element : children) {
            element.onDrag(event, deltaX, deltaY);
        }
        super.onDrag(event, deltaX, deltaY);
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        setDragging(false);
        for (GuiElement element : children) {
            element.onRelease(event);
        }
        super.onRelease(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        for (int i = children.size() - 1; i >= 0; i--) {
            GuiElement child = children.get(i);
            if (child.mouseScrolled(mouseX, mouseY, xDelta, yDelta)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, xDelta, yDelta);
    }

    @Override
    public Font font() {
        return guiObj.font();
    }

    @Override
    public final int getXSize() {
        return getWidth();
    }

    public void setButtonBackground(ButtonBackground buttonBackground) {
        this.buttonBackground = buttonBackground;
    }

    //TODO - 26.2: I am guessing mojang removed the duplicate behavior and made it so that it just checks the one method. Validate that there isn't anything else we are meant to override instead
    /*@Override
    protected boolean clicked(double mouseX, double mouseY) {
        //The code for clicked and isMouseOver is the same. Overriding it here lets us override isMouseOver in subclasses
        // and have it propagate to clicking
        return isMouseOver(mouseX, mouseY);
    }*/

    /// Override this to render the button with a different x position than this GuiElement
    protected int getButtonX() {
        //TODO: Re-evaluate uses of relativeX and see what would be more logical to have using this/getButtonY/Width/Height and potentially just override this in more locations
        return relativeX;
    }

    /// Override this to render the button with a different y position than this GuiElement
    protected int getButtonY() {
        return relativeY;
    }

    /// Override this to render the button with a different width than this GuiElement
    protected int getButtonWidth() {
        return width;
    }

    /// Override this to render the button with a different height than this GuiElement
    protected int getButtonHeight() {
        return height;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY) || GuiUtils.checkChildren(children, mouseX, mouseY, GuiElement::isMouseOver);
    }

    /// Does the same as [#isMouseOver(double, double)], but validates there is no window in the way
    public final boolean isMouseOverCheckWindows(double mouseX, double mouseY) {
        //TODO: Ideally we would have the various places that call this instead check isHovered if we can properly override setting that
        boolean isHovering = isMouseOver(mouseX, mouseY);
        return checkWindows(mouseX, mouseY, isHovering);
    }

    /// Helper to correct potentially inaccurate hovering or in bounds checks.
    protected final boolean checkWindows(double mouseX, double mouseY) {
        return checkWindows(mouseX, mouseY, true);
    }

    /// Helper to correct potentially inaccurate hovering or in bounds checks.
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

    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (buttonBackground != ButtonBackground.NONE) {
            drawButton(guiGraphics, mouseX, mouseY);
        }
    }

    public final void onDrawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    public final void renderShifted(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //Copy of super.render, except doesn't update the tooltip for the next render pass, as we handle that via renderTooltip
        if (this.visible) {
            //TODO - 1.21: Do we need to add support for guiGraphics.containsPointInScissor(mouseX, mouseY) to more places where we do adhoc mouse over checks?
            this.isHovered = guiGraphics.containsPointInScissor(mouseX, mouseY) && mouseX >= this.getX() && mouseY >= this.getY() &&
                             mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
            renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //Note: We copy super's visible check here so that if it is not visible we can skip the pose stack transforms
        if (visible) {
            Matrix3x2fStack matrix = guiGraphics.pose();
            matrix.pushMatrix();
            // fix render offset to be as we expect things to be for how we implement our render methods (based on relatives)
            matrix.translate(getGuiLeft(), getGuiTop());
            renderShifted(guiGraphics, mouseX, mouseY, partialTicks);
            matrix.popMatrix();
        }
    }

    public void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
    }

    @Override
    public int getFGColor() {
        if (packedFGColor != UNSET_FG_COLOR) {
            return packedFGColor;
        }
        return this.active ? activeButtonTextColor() : inactiveButtonTextColor();
    }

    protected int getButtonTextColor(int mouseX, int mouseY) {
        return getFGColor();
    }

    protected boolean displayButtonTextShadow() {
        return true;
    }

    protected void drawButtonText(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        Component text = getMessage();
        //Only attempt to draw the message if we have a message to draw
        if (!text.getString().isEmpty()) {
            int color = ARGB.color(alpha, getButtonTextColor(mouseX, mouseY));
            //Note: We add one to the button height as it is considered bounds, and we want to include the bottom pixel of the button in our calculations of where the text should land
            //Note: We call super as currently getButtonX and getButtonY already factor in the relative positioning
            IFancyFontRenderer.super.drawScrollingString(guiGraphics, text, getButtonX(), getButtonY(), TextAlignment.CENTER, color, getButtonWidth(),
                  getButtonHeight() + 1, 2, displayButtonTextShadow(), getTimeOpened());
        }
    }

    @Nullable
    protected Identifier getButtonVariant(boolean hoveredOrFocused) {
        if (!this.active) {
            return buttonBackground.inactive();
        } else if (hoveredOrFocused) {
            return buttonBackground.focus();
        }
        return buttonBackground.base();
    }

    protected int getButtonBlitColor() {
        return CommonColors.WHITE;//no color by default
    }

    protected void drawButton(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        Identifier texture = getButtonVariant(isMouseOverCheckWindows(mouseX, mouseY));
        if (texture != null) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight(), getButtonBlitColor());
        }
    }

    @Override
    public void playDownSound(SoundManager soundHandler) {
        if (clickSound != null) {
            playClickSound(soundHandler, clickSound, clickVolume);
        }
    }

    protected static void playClickSound(Supplier<SoundEvent> sound) {
        //Default to the value from SimpleSoundInstance.forUI
        playClickSound(minecraft.getSoundManager(), sound, 0.25F);
    }

    private static void playClickSound(SoundManager soundHandler, Supplier<SoundEvent> sound, float clickVolume) {
        soundHandler.play(SimpleSoundInstance.forUI(sound.get(), 1.0F, clickVolume));
    }

    protected void drawTiledSprite(GuiGraphicsExtractor guiGraphics, int xPosition, int yPosition, int yOffset, int desiredWidth, int desiredHeight, TextureAtlasSprite sprite,
          TilingDirection tilingDirection, int color) {
        GuiUtils.drawTiledSprite(guiGraphics, xPosition, yPosition, yOffset, desiredWidth, desiredHeight, sprite, 16, 16, 0, tilingDirection, color);
    }

    @Override
    public long getTimeOpened() {
        return guiObj.getTimeOpened();
    }

    @Override
    public final void drawScrollingString(GuiGraphicsExtractor graphics, Component text, int x, int y, TextAlignment alignment, int color, int width, int height, int maxLengthPad,
          boolean shadow, long msVisible) {
        IFancyFontRenderer.super.drawScrollingString(graphics, text, relativeX + x, relativeY + y, alignment, color, width, height, maxLengthPad, shadow, msVisible);
    }

    @Override
    public final void drawScaledScrollingString(GuiGraphicsExtractor graphics, Component text, int x, int y, TextAlignment alignment, int color, int width, int height, int maxLengthPad,
          boolean shadow, float scale, long msVisible) {
        IFancyFontRenderer.super.drawScaledScrollingString(graphics, text, relativeX + x, relativeY + y, alignment, color, width, height, maxLengthPad, shadow, scale, msVisible);
    }

    public enum ButtonBackground {
        DEFAULT(Mekanism.rl("button")),
        DIGITAL(Mekanism.rl("button_digital")),
        NONE(null);

        @Nullable
        private final Identifier base;
        @Nullable
        private final Identifier focus;
        @Nullable
        private final Identifier inactive;//i.e. disabled?

        ButtonBackground(@Nullable Identifier base) {
            this.base = base;
            this.focus = base == null ? null : base.withSuffix("_focus");
            this.inactive = base == null ? null : base.withSuffix("_inactive");
        }

        @Nullable
        public Identifier base() {
            return base;
        }

        @Nullable
        public Identifier focus() {
            return focus;
        }

        @Nullable
        public Identifier inactive() {
            return inactive;
        }
    }

    @FunctionalInterface
    public interface IClickable {

        //TODO: Can we make the element be a generic type? It might allow for making the implementations non-capturing
        boolean onClick(GuiElement element, MouseButtonEvent event, boolean isDoubleClick);
    }
}