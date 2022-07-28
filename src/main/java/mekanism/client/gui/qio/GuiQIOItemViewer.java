package mekanism.client.gui.qio;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiDigitalIconToggle;
import mekanism.client.gui.element.GuiDropdown;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.custom.GuiResizeControls;
import mekanism.client.gui.element.custom.GuiResizeControls.ResizeType;
import mekanism.client.gui.element.scroll.GuiSlotScroll;
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
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public abstract class GuiQIOItemViewer<CONTAINER extends QIOItemViewerContainer> extends GuiMekanism<CONTAINER> {

    private static final Set<Character> ALLOWED_SPECIAL_CHARS = Sets.newHashSet('_', ' ', '-', '/', '.', '\"', '\'', '|', '(', ')', ':');

    static {
        // include all search prefix chars
        ALLOWED_SPECIAL_CHARS.addAll(QueryType.getPrefixChars());
    }

    protected final Inventory inv;
    private GuiTextField searchField;
    private GuiCraftingWindowTab craftingWindowTab;

    protected GuiQIOItemViewer(CONTAINER container, Inventory inv, Component title) {
        super(container, inv, title);
        this.inv = inv;
        imageWidth = 16 + MekanismConfig.client.qioItemViewerSlotsX.get() * 18 + 18;
        imageHeight = QIOItemViewerContainer.SLOTS_START_Y + MekanismConfig.client.qioItemViewerSlotsY.get() * 18 + 96;
        inventoryLabelY = imageHeight - 94;
        titleLabelY = 5;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        int slotsY = MekanismConfig.client.qioItemViewerSlotsY.get();
        getMinecraft().keyboardHandler.setSendRepeatsToGui(true);
        addRenderableWidget(new GuiInnerScreen(this, 7, 15, imageWidth - 16, 12, () -> {
            FrequencyIdentity freq = getFrequency();
            if (freq == null) {
                return List.of(MekanismLang.NO_FREQUENCY.translate());
            }
            return List.of(MekanismLang.FREQUENCY.translate(freq.key()));
        }).tooltip(() -> {
            if (getFrequency() == null) {
                return List.of();
            }
            return List.of(MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO, TextUtils.format(menu.getTotalItems()),
                        TextUtils.format(menu.getCountCapacity())),
                  MekanismLang.QIO_TYPES_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO, TextUtils.format(menu.getTotalTypes()),
                        TextUtils.format(menu.getTypeCapacity()))
            );
        }));
        searchField = addRenderableWidget(new GuiTextField(this, 50, 15 + 12 + 3, imageWidth - 50 - 10, 10));
        searchField.setOffset(0, -1);
        searchField.setInputValidator(this::isValidSearchChar);
        searchField.setResponder(menu::updateSearch);
        searchField.setMaxLength(50);
        searchField.setBackground(BackgroundType.ELEMENT_HOLDER);
        searchField.setVisible(true);
        searchField.setTextColor(0xFFFFFF);
        searchField.setFocused(true);
        addRenderableWidget(new GuiSlotScroll(this, 7, QIOItemViewerContainer.SLOTS_START_Y, MekanismConfig.client.qioItemViewerSlotsX.get(), slotsY,
              menu::getQIOItemList, menu));
        addRenderableWidget(new GuiDropdown<>(this, imageWidth - 9 - 54, QIOItemViewerContainer.SLOTS_START_Y + slotsY * 18 + 1,
              41, ListSortType.class, menu::getSortType, menu::setSortType));
        addRenderableWidget(new GuiDigitalIconToggle<>(this, imageWidth - 9 - 12, QIOItemViewerContainer.SLOTS_START_Y + slotsY * 18 + 1,
              12, 12, SortDirection.class, menu::getSortDirection, menu::setSortDirection));
        addRenderableWidget(new GuiResizeControls(this, (getMinecraft().getWindow().getGuiScaledHeight() / 2) - 20 - topPos, this::resize));
        craftingWindowTab = addRenderableWidget(new GuiCraftingWindowTab(this, () -> craftingWindowTab, menu));
    }

    @Override
    protected void drawForegroundText(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        drawTextScaledBound(matrix, MekanismLang.LIST_SEARCH.translate(), 7, 31, titleTextColor(), 41);
        Component text = MekanismLang.LIST_SORT.translate();
        drawString(matrix, text, imageWidth - 66 - getStringWidth(text), imageHeight - 92, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public void init(@NotNull Minecraft minecraft, int sizeX, int sizeY) {
        super.init(minecraft, sizeX, sizeY);
        menu.updateSearch(searchField.getText());
        //Validate the height is still valid, and if it isn't recreate it
        int maxY = QIOItemViewerContainer.getSlotsYMax();
        if (MekanismConfig.client.qioItemViewerSlotsY.get() > maxY) {
            //Note: We need to update it here to ensure that it refreshes when recreating the viewer on the client when connected to a server
            MekanismConfig.client.qioItemViewerSlotsY.set(maxY);
            // save the updated config info
            MekanismConfig.client.save();
            recreateViewer();
        }
    }

    @Override
    public void removed() {
        super.removed();
        getMinecraft().keyboardHandler.setSendRepeatsToGui(false);
    }

    private boolean isValidSearchChar(char c) {
        return ALLOWED_SPECIAL_CHARS.contains(c) || Character.isDigit(c) || Character.isAlphabetic(c);
    }

    public abstract FrequencyIdentity getFrequency();

    private void resize(ResizeType type) {
        int sizeX = MekanismConfig.client.qioItemViewerSlotsX.get(), sizeY = MekanismConfig.client.qioItemViewerSlotsY.get();
        boolean changed = false;
        if (type == ResizeType.EXPAND_X && sizeX < QIOItemViewerContainer.SLOTS_X_MAX) {
            MekanismConfig.client.qioItemViewerSlotsX.set(sizeX + 1);
            changed = true;
        } else if (type == ResizeType.EXPAND_Y && sizeY < QIOItemViewerContainer.getSlotsYMax()) {
            MekanismConfig.client.qioItemViewerSlotsY.set(sizeY + 1);
            changed = true;
        } else if (type == ResizeType.SHRINK_X && sizeX > QIOItemViewerContainer.SLOTS_X_MIN) {
            MekanismConfig.client.qioItemViewerSlotsX.set(sizeX - 1);
            changed = true;
        } else if (type == ResizeType.SHRINK_Y && sizeY > QIOItemViewerContainer.SLOTS_Y_MIN) {
            MekanismConfig.client.qioItemViewerSlotsY.set(sizeY - 1);
            changed = true;
        }
        if (changed) {
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
        getMinecraft().screen = null;
        getMinecraft().player.containerMenu = s.getMenu();
        getMinecraft().setScreen(s);
        s.searchField.setText(searchField.getText());
        c.updateSearch(searchField.getText());
        //Transfer all the windows to the new GUI
        s.transferWindows(windows);
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
}
