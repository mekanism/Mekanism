package mekanism.client.gui.qio;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.lwjgl.glfw.GLFW;
import com.google.common.collect.Sets;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiSlotScroll;
import mekanism.client.gui.element.custom.GuiItemListSortScreen;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.frequency.Frequency.FrequencyIdentity;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiQIOItemViewer<CONTAINER extends QIOItemViewerContainer> extends GuiMekanism<CONTAINER> {

    private TextFieldWidget searchField;
    private Set<Character> ALLOWED_SPECIAL_CHARS = Sets.newHashSet('_', ' ', '-', '/', '.');

    protected GuiQIOItemViewer(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        xSize = 16 + MekanismConfig.client.qioItemViewerSlotsX.get() * 18 + 18;
        ySize = QIOItemViewerContainer.SLOTS_START_Y + MekanismConfig.client.qioItemViewerSlotsY.get() * 18 + 96;
    }

    @Override
    public void init() {
        super.init();
        int slotsY = MekanismConfig.client.qioItemViewerSlotsY.get();
        minecraft.keyboardListener.enableRepeatEvents(true);
        addButton(new GuiInnerScreen(this, 7, 15, xSize - 16, 12, () -> {
            List<ITextComponent> list = new ArrayList<>();
            FrequencyIdentity freq = getFrequency();
            if (freq != null) {
                list.add(MekanismLang.FREQUENCY.translate(getFrequency().getKey()));
            } else {
                list.add(MekanismLang.NO_FREQUENCY.translate());
            }
            return list;
        }).tooltip(() -> {
            List<ITextComponent> list = new ArrayList<>();
            if (getFrequency() != null) {
                list.add(MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      QIOFrequency.formatItemCount(container.getTotalItems()), QIOFrequency.formatItemCount(container.getCountCapacity())));
                list.add(MekanismLang.QIO_TYPES_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      QIOFrequency.formatItemCount(container.getTotalTypes()), QIOFrequency.formatItemCount(container.getTypeCapacity())));
            }
            return list;
        }));
        addButton(new GuiElementHolder(this, 48, 15 + 12 + 2, xSize - 48 - 9, 12));
        addButton(searchField = new TextFieldWidget(font, getGuiLeft() + 50, getGuiTop() + 15 + 12 + 4, xSize - 50 - 10, 9, I18n.format("itemGroup.search")));
        searchField.setMaxStringLength(50);
        searchField.setEnableBackgroundDrawing(false);
        searchField.setVisible(true);
        searchField.setFocused2(true);
        searchField.setTextColor(0xFFFFFF);
        addButton(new GuiSlotScroll(this, 7, QIOItemViewerContainer.SLOTS_START_Y, MekanismConfig.client.qioItemViewerSlotsX.get(), slotsY,
              () -> container.getQIOItemList(), container));
        addButton(new GuiItemListSortScreen(this, xSize - 9 - 50, QIOItemViewerContainer.SLOTS_START_Y + slotsY * 18 + 1,
              container::getSortType, container::setSortType));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 4, titleTextColor());
        drawString(MekanismLang.LIST_SEARCH.translate(), 7, 31, titleTextColor());
        ITextComponent text = MekanismLang.LIST_SORT.translate();
        int width = getStringWidth(text);
        drawString(text, xSize - 62 - width, (getYSize() - 96) + 4, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        searchField.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick() {
        super.tick();
        searchField.tick();
    }

    @Override
    public void removed() {
        super.removed();
        minecraft.keyboardListener.enableRepeatEvents(false);
    }

    private boolean isValidSearchChar(char c) {
        return ALLOWED_SPECIAL_CHARS.contains(c) || Character.isDigit(c) || Character.isAlphabetic(c);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchField.canWrite()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                searchField.setFocused2(false);
                return true;
            }
            boolean ret = searchField.keyPressed(keyCode, scanCode, modifiers);
            container.updateSearch(searchField.getText());
            return ret;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if (searchField.canWrite()) {
            if (isValidSearchChar(c)) {
                boolean ret = searchField.charTyped(c, keyCode);
                container.updateSearch(searchField.getText());
                return ret;
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }

    public abstract FrequencyIdentity getFrequency();

    private void resize(ResizeType type) {
        int sizeX = MekanismConfig.client.qioItemViewerSlotsX.get(), sizeY = MekanismConfig.client.qioItemViewerSlotsY.get();
        if (type == ResizeType.EXPAND_X && sizeX < QIOItemViewerContainer.SLOTS_X_MAX) {
            MekanismConfig.client.qioItemViewerSlotsX.set(sizeX + 1);
        } else if (type == ResizeType.EXPAND_Y && sizeY < QIOItemViewerContainer.SLOTS_Y_MAX) {
            MekanismConfig.client.qioItemViewerSlotsY.set(sizeY + 1);
        } else if (type == ResizeType.SHRINK_X && sizeX > QIOItemViewerContainer.SLOTS_X_MIN) {
            MekanismConfig.client.qioItemViewerSlotsX.set(sizeX - 1);
        } else if (type == ResizeType.SHRINK_Y && sizeY > QIOItemViewerContainer.SLOTS_Y_MIN) {
            MekanismConfig.client.qioItemViewerSlotsY.set(sizeY - 1);
        }
        // save the updated config info
        MekanismConfig.client.getConfigSpec().save();
        // here we subtly recreate the entire interface + container, maintaining the same window ID
        @SuppressWarnings("unchecked")
        CONTAINER c = (CONTAINER) container.recreate();
        GuiQIOItemViewer<CONTAINER> s = recreate(c);
        // TODO set search box
        minecraft.currentScreen = null;
        minecraft.player.openContainer = ((IHasContainer<?>)s).getContainer();
        minecraft.displayGuiScreen(recreate(c));
    }

    public abstract GuiQIOItemViewer<CONTAINER> recreate(CONTAINER container);

    private enum ResizeType {
        EXPAND_X,
        EXPAND_Y,
        SHRINK_X,
        SHRINK_Y;
    }
}
