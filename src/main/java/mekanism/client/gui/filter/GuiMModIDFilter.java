package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.TagCache;
import mekanism.common.content.miner.MModIDFilter;
import mekanism.common.inventory.container.tile.filter.DMModIDFilterContainer;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiMModIDFilter extends GuiModIDFilter<MModIDFilter, TileEntityDigitalMiner, DMModIDFilterContainer> {

    public GuiMModIDFilter(DMModIDFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        origFilter = container.getOrigFilter();
        filter = container.getFilter();
        isNew = container.isNew();
    }

    @Override
    protected void updateStackList(String modName) {
        iterStacks = TagCache.getModIDStacks(modName, true);
        stackSwitch = 0;
        stackIndex = -1;
    }

    @Override
    protected void addButtons() {
        addButton(new GuiInnerScreen(this, 33, 18, 111, 43));
        addButton(new GuiInnerScreen(this, 130, 46, 14, 14));
        addButton(new GuiSlot(SlotType.NORMAL, this, 11, 18));
        addButton(new GuiSlot(SlotType.NORMAL, this, 148, 18).setRenderHover(true));
        addButton(saveButton = new TranslationButton(this, getGuiLeft() + 27, getGuiTop() + 62, 60, 20, MekanismLang.BUTTON_SAVE, () -> {
            if (!text.getText().isEmpty()) {
                setText();
            }
            if (filter.getModID() != null && !filter.getModID().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(Coord4D.get(tile), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tile), false, origFilter, filter));
                }
                sendPacketToServer(ClickedTileButton.DIGITAL_MINER_CONFIG);
            } else {
                status = MekanismLang.MODID_FILTER_NO_ID.translateColored(EnumColor.DARK_RED);
                ticker = 20;
            }
        }));
        addButton(deleteButton = new TranslationButton(this, getGuiLeft() + 89, getGuiTop() + 62, 60, 20, MekanismLang.BUTTON_DELETE, () -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tile), true, origFilter, null));
            sendPacketToServer(ClickedTileButton.DIGITAL_MINER_CONFIG);
        }));
        addButton(new MekanismImageButton(this, getGuiLeft() + 5, getGuiTop() + 5, 11, 14, getButtonLocation("back"),
              () -> sendPacketToServer(isNew ? ClickedTileButton.DM_SELECT_FILTER_TYPE : ClickedTileButton.DIGITAL_MINER_CONFIG)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 148, getGuiTop() + 45, 14, 16, getButtonLocation("exclamation"),
              () -> filter.requireStack = !filter.requireStack, getOnHoverReplace(filter)));
        addButton(checkboxButton = new MekanismImageButton(this, getGuiLeft() + 131, getGuiTop() + 47, 12, getButtonLocation("checkmark"),
              this::setText));
    }
}