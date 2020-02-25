package mekanism.client.gui.filter;

import java.util.Collections;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.filter.OredictionificatorFilterContainer;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiOredictionificatorFilter extends GuiTextFilterBase<OredictionificatorFilter, TileEntityOredictionificator, OredictionificatorFilterContainer> {

    private GuiSlot slot;

    public GuiOredictionificatorFilter(OredictionificatorFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        origFilter = container.getOrigFilter();
        filter = container.getFilter();
        isNew = container.isNew();
        updateRenderStack();
    }

    @Override
    protected boolean wasTextboxKey(char c, int i) {
        return super.wasTextboxKey(c, i) || c == '_' || c == ':' || c == '/';
    }

    @Override
    protected void addButtons() {
        addButton(new GuiInnerScreen(this, 129, 47, 14, 14));
        addButton(slot = new GuiSlot(SlotType.NORMAL, this, 44, 18));
        addButton(saveButton = new TranslationButton(this, getGuiLeft() + 31, getGuiTop() + 62, 54, 20, MekanismLang.BUTTON_SAVE, () -> {
            if (!text.getText().isEmpty()) {
                setText();
            }
            if (filter.hasFilter()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(Coord4D.get(tile), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tile), false, origFilter, filter));
                }
                sendPacketToServer(ClickedTileButton.BACK_BUTTON);
            }
        }));
        addButton(deleteButton = new TranslationButton(this, getGuiLeft() + 89, getGuiTop() + 62, 54, 20, MekanismLang.BUTTON_DELETE, () -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tile), true, origFilter, null));
            sendPacketToServer(ClickedTileButton.BACK_BUTTON);
        }));
        addButton(new MekanismImageButton(this, getGuiLeft() + 5, getGuiTop() + 5, 11, 14, getButtonLocation("back"),
              () -> sendPacketToServer(ClickedTileButton.BACK_BUTTON)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 31, getGuiTop() + 21, 12, getButtonLocation("left"), () -> {
            if (filter.hasFilter()) {
                List<Item> matchingItems = filter.getMatchingItems();
                if (filter.index > 0) {
                    filter.index--;
                } else {
                    filter.index = matchingItems.size() - 1;
                }
                updateRenderStack();
            }
        }, getOnHover(MekanismLang.LAST_ITEM)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 63, getGuiTop() + 21, 12, getButtonLocation("right"), () -> {
            if (filter.hasFilter()) {
                List<Item> matchingItems = filter.getMatchingItems();
                if (filter.index < matchingItems.size() - 1) {
                    filter.index++;
                } else {
                    filter.index = 0;
                }
                updateRenderStack();
            }
        }, getOnHover(MekanismLang.NEXT_ITEM)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 130, getGuiTop() + 48, 12, getButtonLocation("checkmark"), this::setText));
    }

    @Override
    public void setText() {
        String newFilter = text.getText().toLowerCase();
        String modid = "forge";
        if (newFilter.contains(":")) {
            String[] split = newFilter.split(":");
            modid = split[0];
            newFilter = split[1];
        }
        List<String> possibleFilters = TileEntityOredictionificator.possibleFilters.getOrDefault(modid, Collections.emptyList());
        if (possibleFilters.stream().anyMatch(newFilter::startsWith)) {
            filter.setFilter(new ResourceLocation(modid, newFilter));
            filter.index = 0;
            text.setText("");
            updateRenderStack();
        }
        updateButtons();
    }

    private void updateButtons() {
        saveButton.active = filter.hasFilter();
        deleteButton.active = !isNew;
    }

    @Override
    protected TextFieldWidget createTextField() {
        return new TextFieldWidget(font, getGuiLeft() + 33, getGuiTop() + 48, 96, 12, "");
    }

    @Override
    public void init() {
        super.init();
        updateButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawCenteredText((isNew ? MekanismLang.FILTER_NEW : MekanismLang.FILTER_EDIT).translate(MekanismLang.FILTER), 0, getXSize(), 6, 0x404040);
        drawString(MekanismLang.FILTER_INDEX.translate(filter.index), 79, 23, 0x404040);
        if (filter.hasFilter()) {
            renderScaledText(filter.getFilterText(), 32, 38, 0x404040, 111);
        }
        renderItem(renderStack, 45, 19);
        int xAxis = mouseX - getGuiLeft();
        int yAxis = mouseY - getGuiTop();
        if (text.isMouseOver(mouseX, mouseY)) {
            displayTooltip(MekanismLang.TAG_COMPAT.translate(), xAxis, yAxis);
        } else if (xAxis >= 45 && xAxis <= 61 && yAxis >= 19 && yAxis <= 35 && !renderStack.isEmpty()) {
            displayTooltip(MekanismLang.GENERIC_WITH_PARENTHESIS.translate(renderStack, renderStack.getItem().getRegistryName().getNamespace()), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        text.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    private void updateRenderStack() {
        if (!filter.hasFilter()) {
            renderStack = ItemStack.EMPTY;
            return;
        }
        List<Item> matchingItems = filter.getMatchingItems();
        if (matchingItems.isEmpty()) {
            renderStack = ItemStack.EMPTY;
            return;
        }
        if (matchingItems.size() - 1 >= filter.index) {
            renderStack = new ItemStack(matchingItems.get(filter.index));
        } else {
            renderStack = ItemStack.EMPTY;
        }
    }
}