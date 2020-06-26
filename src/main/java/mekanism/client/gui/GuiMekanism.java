package mekanism.client.gui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElement.IHoverable;
import mekanism.client.gui.element.GuiWindow;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.render.IFancyFontRenderer;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.LRU;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

//TODO: Add our own "func_230480_a_" type thing for elements that are just "drawn" but don't actually have any logic behind them
public abstract class GuiMekanism<CONTAINER extends Container> extends ContainerScreen<CONTAINER> implements IGuiWrapper, IFancyFontRenderer {

    private static final NumberFormat intFormatter = NumberFormat.getIntegerInstance();
    public static final ResourceLocation BASE_BACKGROUND = MekanismUtils.getResource(ResourceType.GUI, "base.png");
    public static final ResourceLocation SHADOW = MekanismUtils.getResource(ResourceType.GUI, "shadow.png");
    public static final ResourceLocation BLUR = MekanismUtils.getResource(ResourceType.GUI, "blur.png");
    //TODO: Look into defaulting this to true
    protected boolean dynamicSlots;
    protected final LRU<GuiWindow> windows = new LRU<>();
    protected final List<GuiElement> focusListeners = new ArrayList<>();

    private boolean hasClicked = false;

    public static int maxZOffset;

    protected GuiMekanism(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        initPreSlots();
        if (dynamicSlots) {
            addSlots();
        }
    }

    @Override
    public void func_231023_e_() {
        super.func_231023_e_();
        field_230705_e_.stream().filter(child -> child instanceof GuiElement).map(child -> (GuiElement) child).forEach(GuiElement::tick);
    }

    protected void initPreSlots() {
    }

    protected IHoverable getOnHover(ILangEntry translationHelper) {
        return getOnHover((Supplier<ITextComponent>) translationHelper::translate);
    }

    protected IHoverable getOnHover(Supplier<ITextComponent> componentSupplier) {
        return (onHover, xAxis, yAxis) -> displayTooltip(componentSupplier.get(), xAxis, yAxis);
    }

    protected ResourceLocation getButtonLocation(String name) {
        return MekanismUtils.getResource(ResourceType.GUI_BUTTON, name + ".png");
    }

    @Override
    public void addFocusListener(GuiElement element) {
        focusListeners.add(element);
    }

    @Override
    public void removeFocusListener(GuiElement element) {
        focusListeners.remove(element);
    }

    @Override
    public void focusChange(GuiElement changed) {
        focusListeners.stream().filter(e -> e != changed).forEach(e -> e.setFocused(false));
    }

    @Override
    public void incrementFocus(GuiElement current) {
        int index = focusListeners.indexOf(current);
        if (index != -1) {
            GuiElement next = focusListeners.get((index + 1) % focusListeners.size());
            next.setFocused(true);
            focusChange(next);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
        return getWindowHovering(mouseX, mouseY) == null && super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn, mouseButton);
    }

    @Override
    public void resize(@Nonnull Minecraft minecraft, int sizeX, int sizeY) {
        List<Pair<Integer, GuiElement>> prevElements = new ArrayList<>();
        for (int i = 0; i < field_230710_m_.size(); i++) {
            Widget widget = field_230710_m_.get(i);
            if (widget instanceof GuiElement && ((GuiElement) widget).hasPersistentData()) {
                prevElements.add(Pair.of(i, (GuiElement) widget));
            }
        }
        // flush the focus listeners list unless it's an overlay
        focusListeners.removeIf(element -> !element.isOverlay);
        int prevLeft = getGuiLeft(), prevTop = getGuiTop();
        super.resize(minecraft, sizeX, sizeY);

        windows.forEach(window -> {
            window.resize(prevLeft, prevTop, getGuiLeft(), getGuiTop());
            field_230705_e_.add(window);
        });

        prevElements.forEach(e -> {
            if (e.getLeft() < field_230710_m_.size()) {
                Widget widget = field_230710_m_.get(e.getLeft());
                // we're forced to assume that the children list is the same before and after the resize.
                // for verification, we run a lightweight class equality check
                // Note: We do not perform an instance check on widget to ensure it is a GuiElement, as that is
                // ensured by the class comparison, and the restrictions of what can go in prevElements
                if (widget.getClass() == e.getRight().getClass()) {
                    ((GuiElement) widget).syncFrom(e.getRight());
                }
            }
        });
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int xAxis = mouseX - getGuiLeft();
        int yAxis = mouseY - getGuiTop();
        // first render general foregrounds
        maxZOffset = 200;
        int zOffset = 200;
        for (Widget widget : this.field_230710_m_) {
            if (widget instanceof GuiElement) {
                RenderSystem.pushMatrix();
                ((GuiElement) widget).onRenderForeground(mouseX, mouseY, zOffset, zOffset);
                RenderSystem.popMatrix();
            }
        }
        // now render overlays in reverse-order (i.e. back to front)
        zOffset = maxZOffset;
        for (LRU<GuiWindow>.LRUIterator iter = getWindowsDescendingIterator(); iter.hasNext(); ) {
            GuiWindow overlay = iter.next();
            zOffset += 150;
            RenderSystem.pushMatrix();
            overlay.onRenderForeground(mouseX, mouseY, zOffset, zOffset);
            if (iter.hasNext()) {
                // if this isn't the focused window, render a 'blur' effect over it
                overlay.renderBlur();
            }
            RenderSystem.popMatrix();
        }
        // then render tooltips, translating above max z offset to prevent clashing
        GuiElement tooltipElement = getWindowHovering(mouseX, mouseY);
        if (tooltipElement == null) {
            for (int i = field_230710_m_.size() - 1; i >= 0; i--) {
                Widget widget = field_230710_m_.get(i);
                if (widget instanceof GuiElement && widget.isMouseOver(mouseX, mouseY)) {
                    tooltipElement = (GuiElement) widget;
                    break;
                }
            }
        }
        if (tooltipElement != null) {
            RenderSystem.translated(0, 0, maxZOffset + 50);
            tooltipElement.renderToolTip(xAxis, yAxis);
            RenderSystem.translated(0, 0, maxZOffset - 50);
        }
    }

    @Nonnull
    @Override
    public Optional<IGuiEventListener> getEventListenerForPos(double mouseX, double mouseY) {
        GuiWindow window = getWindowHovering(mouseX, mouseY);
        return window != null ? Optional.of(window) : super.getEventListenerForPos(mouseX, mouseY);
    }

    @Override
    public boolean func_231044_a_(double mouseX, double mouseY, int button) {
        hasClicked = true;
        // first try to send the mouse event to our overlays
        GuiWindow focused = windows.stream().filter(overlay -> overlay.func_231044_a_(mouseX, mouseY, button)).findFirst().orElse(null);
        if (focused != null) {
            setFocused(focused);
            if (button == 0) {
                setDragging(true);
            }
            windows.moveUp(focused);
            return true;
        }
        // otherwise we send it to the current element
        for (int i = field_230710_m_.size() - 1; i >= 0; i--) {
            IGuiEventListener listener = field_230710_m_.get(i);
            if (listener.func_231044_a_(mouseX, mouseY, button)) {
                setFocused(listener);
                if (button == 0) {
                    setDragging(true);
                }
                return true;
            }
        }
        return super.func_231044_a_(mouseX, mouseY, button);
    }

    @Override
    public boolean func_231048_c_(double mouseX, double mouseY, int button) {
        if (hasClicked) {
            return super.func_231048_c_(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean func_231046_a_(int keyCode, int scanCode, int modifiers) {
        return windows.stream().anyMatch(window -> window.func_231046_a_(keyCode, scanCode, modifiers)) ||
               GuiUtils.checkChildren(field_230710_m_, (child) -> child.func_231046_a_(keyCode, scanCode, modifiers)) ||
               super.func_231046_a_(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean func_231042_a_(char c, int keyCode) {
        return windows.stream().anyMatch(window -> window.func_231042_a_(c, keyCode)) ||
               GuiUtils.checkChildren(field_230710_m_, (child) -> child.func_231042_a_(c, keyCode)) ||
               super.func_231042_a_(c, keyCode);
    }

    /**
     * @apiNote mouseXOld and mouseYOld are just guessed mappings I couldn't find any usage from a quick glance.
     */
    @Override
    public boolean func_231045_a_(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
        super.func_231045_a_(mouseX, mouseY, button, mouseXOld, mouseYOld);
        return getFocused() != null && isDragging() && button == 0 && getFocused().mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
    }

    protected boolean isMouseOverSlot(Slot slot, double mouseX, double mouseY) {
        return isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY);
    }

    @Override
    protected boolean isPointInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
        // overridden to prevent slot interactions when a GuiElement is blocking
        return super.isPointInRegion(x, y, width, height, mouseX, mouseY) &&
               getWindowHovering(mouseX, mouseY) == null &&
               field_230710_m_.stream().noneMatch(button -> button.isMouseOver(mouseX, mouseY));
    }

    protected void addSlots() {
        int size = container.inventorySlots.size();
        for (int i = 0; i < size; i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot instanceof InventoryContainerSlot) {
                InventoryContainerSlot containerSlot = (InventoryContainerSlot) slot;
                ContainerSlotType slotType = containerSlot.getSlotType();
                DataType dataType = findDataType(containerSlot);
                //Shift the slots by one as the elements include the border of the slot
                SlotType type;
                if (dataType != null) {
                    type = SlotType.get(dataType);
                } else if (slotType == ContainerSlotType.INPUT || slotType == ContainerSlotType.OUTPUT || slotType == ContainerSlotType.EXTRA) {
                    type = SlotType.NORMAL;
                } else if (slotType == ContainerSlotType.POWER) {
                    type = SlotType.POWER;
                } else if (slotType == ContainerSlotType.NORMAL || slotType == ContainerSlotType.VALIDITY) {
                    type = SlotType.NORMAL;
                } else {//slotType == ContainerSlotType.IGNORED: don't do anything
                    continue;
                }
                GuiSlot guiSlot = new GuiSlot(type, this, slot.xPos - 1, slot.yPos - 1);
                SlotOverlay slotOverlay = containerSlot.getSlotOverlay();
                if (slotOverlay != null) {
                    guiSlot.with(slotOverlay);
                }
                if (slotType == ContainerSlotType.VALIDITY) {
                    int index = i;
                    guiSlot.validity(() -> checkValidity(index));
                }
                func_230480_a_(guiSlot);
            } else {
                func_230480_a_(new GuiSlot(SlotType.NORMAL, this, slot.xPos - 1, slot.yPos - 1));
            }
        }
    }

    @Nullable
    protected DataType findDataType(InventoryContainerSlot slot) {
        if (container instanceof MekanismTileContainer) {
            TileEntityMekanism tileEntity = ((MekanismTileContainer<?>) container).getTileEntity();
            if (tileEntity instanceof ISideConfiguration) {
                return ((ISideConfiguration) tileEntity).getActiveDataType(slot.getInventorySlot());
            }
        }
        return null;
    }

    protected ItemStack checkValidity(int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        //Ensure the GL color is white as mods adding an overlay (such as JEI for bookmarks), might have left
        // it in an unexpected state.
        MekanismRenderer.resetColor();
        if (width < 8 || height < 8) {
            Mekanism.logger.warn("Gui: {}, was too small to draw the background of. Unable to draw a background for a gui smaller than 8 by 8.", getClass().getSimpleName());
            return;
        }
        GuiUtils.renderBackgroundTexture(BASE_BACKGROUND, 4, 4, getGuiLeft(), getGuiTop(), getXSize(), getYSize(), 256, 256);
    }

    @Override
    public FontRenderer getFont() {
        return field_230712_o_;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        // shift back a whole lot so we can stack more windows
        RenderSystem.translated(0, 0, -500D);
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        RenderSystem.translated(0, 0, 500D);
    }

    @Override
    public void renderItem(@Nonnull ItemStack stack, int xAxis, int yAxis, float scale) {
        renderItem(stack, xAxis, yAxis, scale, null, false);
    }

    @Override
    public void renderItemWithOverlay(@Nonnull ItemStack stack, int xAxis, int yAxis, float scale, String text) {
        renderItem(stack, xAxis, yAxis, scale, text, true);
    }

    private void renderItem(@Nonnull ItemStack stack, int xAxis, int yAxis, float scale, String text, boolean overlay) {
        if (!stack.isEmpty()) {
            try {
                RenderSystem.pushMatrix();
                RenderSystem.enableDepthTest();
                RenderHelper.enableStandardItemLighting();
                if (scale != 1) {
                    RenderSystem.scalef(scale, scale, scale);
                }
                field_230707_j_.renderItemAndEffectIntoGUI(stack, xAxis, yAxis);
                if (overlay) {
                    field_230707_j_.renderItemOverlayIntoGUI(getFont(), stack, xAxis, yAxis, text);
                }
                RenderHelper.disableStandardItemLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.popMatrix();
            } catch (Exception e) {
                Mekanism.logger.error("Failed to render stack into gui: " + stack, e);
            }
        }
    }

    @Override
    public void renderItemTooltip(@Nonnull ItemStack stack, int xAxis, int yAxis) {
        renderTooltip(stack, xAxis, yAxis);
    }

    @Override
    public ItemRenderer getItemRenderer() {
        return field_230707_j_;
    }

    protected static String formatInt(long l) {
        return intFormatter.format(l);
    }

    @Override
    public void addWindow(GuiWindow window) {
        windows.add(window);
    }

    @Override
    public void removeWindow(GuiWindow window) {
        windows.remove(window);
    }

    @Nullable
    @Override
    public GuiWindow getWindowHovering(double mouseX, double mouseY) {
        return windows.stream().filter(w -> w.isMouseOver(mouseX, mouseY)).findFirst().orElse(null);
    }

    public Collection<GuiWindow> getWindows() {
        return windows;
    }

    public LRU<GuiWindow>.LRUIterator getWindowsDescendingIterator() {
        return windows.descendingIterator();
    }

    //Some blit param namings
    //blit(int x, int y, int textureX, int textureY, int width, int height);
    //blit(int x, int y, TextureAtlasSprite icon, int width, int height);
    //blit(int x, int y, int textureX, int textureY, int width, int height, int textureWidth, int textureHeight);
    //blit(int x, int y, int zLevel, float textureX, float textureY, int width, int height, int textureWidth, int textureHeight);
    //blit(int x, int y, int desiredWidth, int desiredHeight, int textureX, int textureY, int width, int height, int textureWidth, int textureHeight);
    //innerBlit(int x, int endX, int y, int endY, int zLevel, int width, int height, float textureX, float textureY, int textureWidth, int textureHeight);
    //    * calls innerBlit(x, endX, y, endY, zLevel, (textureX + 0.0F) / textureWidth, (textureX + width) / textureWidth, (textureY + 0.0F) / textureHeight, (textureY + height) / textureHeight);
    //innerBlit(int x, int endX, int y, int endY, int zLevel, float uMin, float uMax, float vMin, float vMax);
}