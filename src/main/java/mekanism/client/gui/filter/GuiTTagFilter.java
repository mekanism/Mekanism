package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.OreDictCache;
import mekanism.common.content.transporter.TTagFilter;
import mekanism.common.inventory.container.tile.filter.LSTagFilterContainer;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiTTagFilter extends GuiTagFilter<TTagFilter, TileEntityLogisticalSorter, LSTagFilterContainer> {

    public GuiTTagFilter(LSTagFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        origFilter = container.getOrigFilter();
        filter = container.getFilter();
        isNew = container.isNew();
    }

    @Override
    protected void addButtons() {
        addButton(new GuiInnerScreen(this, 33, 18, 111, 43));
        addButton(new GuiInnerScreen(this, 130, 46, 14, 14));
        addButton(new GuiSlot(SlotType.NORMAL, this, 11, 18));
        addButton(new GuiSlot(SlotType.NORMAL, this, 11, 43));
        addButton(saveButton = new TranslationButton(this, getGuiLeft() + 47, getGuiTop() + 62, 60, 20, MekanismLang.BUTTON_SAVE, () -> {
            if (!text.getText().isEmpty()) {
                setText();
            }
            if (filter.getTagName() != null && !filter.getTagName().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(Coord4D.get(tile), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tile), false, origFilter, filter));
                }
                sendPacketToServer(ClickedTileButton.BACK_BUTTON);
            } else {
                status = MekanismLang.TAG_FILTER_NO_TAG.translateColored(EnumColor.DARK_RED);
                ticker = 20;
            }
        }));
        addButton(deleteButton = new TranslationButton(this, getGuiLeft() + 109, getGuiTop() + 62, 60, 20, MekanismLang.BUTTON_DELETE, () -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tile), true, origFilter, null));
            sendPacketToServer(ClickedTileButton.BACK_BUTTON);
        }));
        addButton(new MekanismImageButton(this, getGuiLeft() + 5, getGuiTop() + 5, 11, 14, getButtonLocation("back"),
              () -> sendPacketToServer(isNew ? ClickedTileButton.LS_SELECT_FILTER_TYPE : ClickedTileButton.BACK_BUTTON)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 11, getGuiTop() + 64, 11, getButtonLocation("default"),
              () -> filter.allowDefault = !filter.allowDefault, getOnHover(MekanismLang.FILTER_ALLOW_DEFAULT)));
        addButton(checkboxButton = new MekanismImageButton(this, getGuiLeft() + 131, getGuiTop() + 47, 12, getButtonLocation("checkmark"),
              this::setText));
        addButton(new ColorButton(this, getGuiLeft() + 12, getGuiTop() + 44, 16, 16, () -> filter.color,
              () -> filter.color = InputMappings.isKeyDown(minecraft.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ? null : TransporterUtils.increment(filter.color),
              () -> filter.color = TransporterUtils.decrement(filter.color)));
    }

    @Override
    protected void updateStackList(String oreName) {
        iterStacks = OreDictCache.getItemTagStacks(oreName);
        stackSwitch = 0;
        stackIndex = -1;
    }
}