package mekanism.client.gui.qio;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiDigitalIconToggle;
import mekanism.client.gui.element.GuiDropdown;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.custom.GuiResizeControls;
import mekanism.client.gui.element.custom.GuiResizeControls.ResizeController;
import mekanism.client.gui.element.custom.GuiResizeControls.ResizeType;
import mekanism.client.gui.element.scroll.GuiSlotScroll;
import mekanism.client.gui.element.tab.GuiTargetDirectionTab;
import mekanism.client.gui.element.tab.GuiToggleClientConfigTab;
import mekanism.client.gui.element.tab.window.GuiCraftingWindowTab;
import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.GuiCraftingWindow;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.qio.SearchQueryParser.QueryType;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.QIOItemViewerContainer.ListSortType;
import mekanism.common.inventory.container.QIOItemViewerContainer.SortDirection;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.util.text.TextUtils;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public abstract class GuiQIOItemViewer<CONTAINER extends QIOItemViewerContainer> extends GuiMekanism<CONTAINER> implements ResizeController {

    private static final Set<Character> ALLOWED_SPECIAL_CHARS = Util.make(
          Sets.newHashSet('_', ' ', '-', '/', '.', '\"', '\'', '|', '(', ')', ':'),
          // include all search prefix chars
          allowsChars -> allowsChars.addAll(QueryType.getPrefixChars())
    );

    protected final Inventory inv;
    private GuiTextField searchField;
    private GuiCraftingWindowTab craftingWindowTab;
    private GuiDropdown<?> searchDropdown;
    private boolean loadPinned = true;

    protected GuiQIOItemViewer(CONTAINER container, Inventory inv, Component title) {
        super(container, inv, title);
        this.inv = inv;
        imageWidth = 16 + MekanismConfig.client.qioItemViewerSlotsX.get() * 18 + 18;
        imageHeight = QIOItemViewerContainer.SLOTS_START_Y + MekanismConfig.client.qioItemViewerSlotsY.get() * 18 + 96;
        inventoryLabelY = imageHeight - 93;
        titleLabelY = 5;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        int slotsY = MekanismConfig.client.qioItemViewerSlotsY.get();
        addRenderableWidget(new GuiInnerScreen(this, 7, 15, imageWidth - 16, 12, () -> {
            FrequencyIdentity freq = getFrequency();
            if (freq == null) {
                return List.of(MekanismLang.NO_FREQUENCY.translate());
            }
            return List.of(MekanismLang.FREQUENCY.translate(freq.key()));
        }).tooltip(() -> {
            if (getFrequency() == null) {
                return Collections.emptyList();
            }
            return List.of(MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO, TextUtils.format(menu.getTotalItems()),
                        TextUtils.format(menu.getCountCapacity())),
                  MekanismLang.QIO_TYPES_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO, TextUtils.format(menu.getTotalTypes()),
                        TextUtils.format(menu.getTypeCapacity()))
            );
        }));
        searchField = addRenderableWidget(new GuiTextField(this, 50, 15 + 12 + 3, imageWidth - 50 - 10, 10));
        searchField.setOffset(0, -1)
              .setInputValidator(this::isValidSearchChar)
              .setBackground(BackgroundType.ELEMENT_HOLDER)
              //Note: This responder will also be called when the menu is resized/repositioned and the text gets copied
              .setResponder(text -> menu.updateSearch(menu.getLevel(), text, true));
        searchField.setMaxLength(50);
        searchField.setVisible(true);
        searchField.setTextColor(0xFFFFFF);
        if (MekanismConfig.client.qioAutoFocusSearchBar.get()) {
            setInitialFocus(searchField);
        }
        addRenderableWidget(new GuiSlotScroll(this, 7, QIOItemViewerContainer.SLOTS_START_Y, MekanismConfig.client.qioItemViewerSlotsX.get(), slotsY,
              menu::getQIOItemList, menu));
        searchDropdown = addRenderableWidget(new GuiDropdown<>(this, imageWidth - 9 - 54, QIOItemViewerContainer.SLOTS_START_Y + slotsY * 18 + 1,
              41, ListSortType.class, menu::getSortType, menu::setSortType));
        addRenderableWidget(new GuiDigitalIconToggle<>(this, imageWidth - 9 - 12, QIOItemViewerContainer.SLOTS_START_Y + slotsY * 18 + 1,
              12, 12, SortDirection.class, menu::getSortDirection, menu::setSortDirection));
        addRenderableWidget(new GuiTargetDirectionTab(this, menu, 60));
        addRenderableWidget(new GuiToggleClientConfigTab(this, imageHeight - 35, true, getButtonLocation("recipe_viewer_inventory"), getButtonLocation("recipe_viewer_frequency"),
              //Note: This is backwards as it describes what the button will be doing
              MekanismConfig.client.qioRejectsToInventory, MekanismLang.QIO_REJECTS_TO_INVENTORY.translate(), MekanismLang.QIO_REJECTS_TO_FREQUENCY.translate()));
        addRenderableWidget(new GuiToggleClientConfigTab(this, 6, false, getButtonLocation("searchbar_autofocus_off"), getButtonLocation("searchbar_autofocus_on"),
              //Note: This is backwards as it describes what the button will be doing
              MekanismConfig.client.qioAutoFocusSearchBar, MekanismLang.QIO_SEARCH_MANUAL_FOCUS.translate(), MekanismLang.QIO_SEARCH_AUTO_FOCUS.translate()));
        addRenderableWidget(new GuiResizeControls(this, (getMinecraft().getWindow().getGuiScaledHeight() / 2) - topPos));
        craftingWindowTab = addRenderableWidget(new GuiCraftingWindowTab(this, () -> craftingWindowTab, menu));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryTextAndOther(guiGraphics, MekanismLang.LIST_SORT.translate(), imageWidth - searchDropdown.getRelativeX() - 5);
        drawScrollingString(guiGraphics, MekanismLang.LIST_SEARCH.translate(), 4, 31, TextAlignment.RIGHT, titleTextColor(), searchField.getRelativeX() - 4, 3, false);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void repositionElements() {
        boolean wasOnRecipeViewer = switchingToRecipeViewer;
        super.repositionElements();
        //Validate the height is still valid, and if it isn't recreate it
        int maxY = QIOItemViewerContainer.getSlotsYMax();
        if (MekanismConfig.client.qioItemViewerSlotsY.get() > maxY) {
            //Note: We need to update it here to ensure that it refreshes when recreating the viewer on the client when connected to a server
            MekanismConfig.client.qioItemViewerSlotsY.set(maxY);
            // save the updated config info
            MekanismConfig.client.save();
            recreateViewer();
        } else if (wasOnRecipeViewer) {
            //When switching back to the QIO Item Viewer from a recipe viewer, we want to ensure that we update the current pause state
            menu.pauseSorting(hasShiftDown());
        }
    }

    private boolean isValidSearchChar(char c) {
        return ALLOWED_SPECIAL_CHARS.contains(c) || Character.isDigit(c) || Character.isAlphabetic(c);
    }

    public abstract FrequencyIdentity getFrequency();

    @Override
    public void resize(ResizeType type, boolean adjustMax) {
        IntSupplier changeX = null;
        IntSupplier changeY = null;
        switch (type) {
            case EXPAND_X -> changeX = adjustMax ? () -> QIOItemViewerContainer.SLOTS_X_MAX : () -> MekanismConfig.client.qioItemViewerSlotsX.get() + 1;
            case SHRINK_X -> changeX = adjustMax ? () -> QIOItemViewerContainer.SLOTS_X_MIN : () -> MekanismConfig.client.qioItemViewerSlotsX.get() - 1;
            case EXPAND_Y -> changeY = adjustMax ? QIOItemViewerContainer::getSlotsYMax : () -> MekanismConfig.client.qioItemViewerSlotsY.get() + 1;
            case SHRINK_Y -> changeY = adjustMax ? () -> QIOItemViewerContainer.SLOTS_Y_MIN : () -> MekanismConfig.client.qioItemViewerSlotsY.get() - 1;
        }
        if (changeX != null || changeY != null) {
            if (changeX != null) {
                MekanismConfig.client.qioItemViewerSlotsX.set(Mth.clamp(changeX.getAsInt(), QIOItemViewerContainer.SLOTS_X_MIN, QIOItemViewerContainer.SLOTS_X_MAX));
            }
            if (changeY != null) {
                MekanismConfig.client.qioItemViewerSlotsY.set(Mth.clamp(changeY.getAsInt(), QIOItemViewerContainer.SLOTS_Y_MIN, QIOItemViewerContainer.getSlotsYMax()));
            }
            // save the updated config info
            MekanismConfig.client.save();
            // And recreate the viewer
            recreateViewer();
        }
    }

    private void recreateViewer() {
        // here we subtly recreate the entire interface + container, maintaining the same window ID
        @SuppressWarnings("unchecked")
        CONTAINER c = (CONTAINER) menu.recreate();
        GuiQIOItemViewer<CONTAINER> s = recreate(c);
        //Skip loading pinned windows for now on the new viewer as we will transfer any open windows manually (pinned or not)
        s.loadPinned = false;
        getMinecraft().screen = null;
        getMinecraft().player.containerMenu = s.getMenu();
        getMinecraft().setScreen(s);
        s.searchField.setText(searchField.getText());
        //Transfer all the windows to the new GUI
        s.transferWindows(windows);
    }

    @Override
    protected void initPinnedWindows() {
        if (loadPinned) {
            super.initPinnedWindows();
        }
    }

    protected void transferWindows(Collection<GuiWindow> windows) {
        for (GuiWindow window : windows) {
            //Transition all current popup windows over to the new screen.
            if (window instanceof GuiCraftingWindow craftingWindow) {
                //Updating the references for listeners and the like for crafting windows
                craftingWindowTab.adoptWindows(craftingWindow);
                //Update the container the virtual slots point to be correct
                craftingWindow.updateContainer(menu);
            }
            addWindow(window);
            window.transferToNewGui(this);
        }
    }

    public abstract GuiQIOItemViewer<CONTAINER> recreate(CONTAINER container);

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            menu.pauseSorting(true);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        //Note: We only want to unpause sorting if they aren't pressing shift. If they were pressing both shift keys and then released one
        // we want to make sure it stays paused. We just pass the value of if the other key is pressed and let the code that handles
        // pausing/unpausing on value change handle determining if we should stay paused
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT) {
            menu.pauseSorting(InputConstants.isKeyDown(getMinecraft().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT));
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            menu.pauseSorting(InputConstants.isKeyDown(getMinecraft().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT));
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
