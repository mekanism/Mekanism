package mekanism.client.gui.filter;

import java.util.Collections;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.TagCache;
import mekanism.common.content.transporter.TModIDFilter;
import mekanism.common.inventory.container.tile.filter.LSModIDFilterContainer;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiTModIDFilter extends GuiModIDFilter<TModIDFilter, TileEntityLogisticalSorter, LSModIDFilterContainer> {

    public GuiTModIDFilter(LSModIDFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        origFilter = container.getOrigFilter();
        filter = container.getFilter();
        isNew = container.isNew();
    }

    @Override
    public List<ItemStack> getRenderStacks() {
        if (filter.getModID() == null || filter.getModID().isEmpty()) {
            return Collections.emptyList();
        }
        return TagCache.getBlockTagStacks(filter.getModID());
    }

    @Override
    protected void addButtons() {
        addButton(new GuiInnerScreen(this, 33, 18, 111, 43));
        addButton(new GuiSlot(SlotType.NORMAL, this, 11, 18));
        addButton(new GuiSlot(SlotType.NORMAL, this, 11, 43));
        addButton(saveButton = new TranslationButton(this, getGuiLeft() + 47, getGuiTop() + 62, 60, 20, MekanismLang.BUTTON_SAVE, () -> {
            if (!text.getText().isEmpty()) {
                setText();
            }
            if (filter.getModID() != null && !filter.getModID().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(tile.getPos(), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), false, origFilter, filter));
                }
                sendPacketToServer(ClickedTileButton.BACK_BUTTON);
            } else {
                status = MekanismLang.MODID_FILTER_NO_ID.translateColored(EnumColor.DARK_RED);
                ticker = 20;
            }
        }));
        addButton(deleteButton = new TranslationButton(this, getGuiLeft() + 109, getGuiTop() + 62, 60, 20, MekanismLang.BUTTON_DELETE, () -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), true, origFilter, null));
            sendPacketToServer(ClickedTileButton.BACK_BUTTON);
        }));
        addButton(new MekanismImageButton(this, getGuiLeft() + 5, getGuiTop() + 5, 11, 14, getButtonLocation("back"),
              () -> sendPacketToServer(isNew ? ClickedTileButton.LS_SELECT_FILTER_TYPE : ClickedTileButton.BACK_BUTTON)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 11, getGuiTop() + 64, 11, getButtonLocation("default"),
              () -> filter.allowDefault = !filter.allowDefault, getOnHover(MekanismLang.FILTER_ALLOW_DEFAULT)));
        addButton(new ColorButton(this, getGuiLeft() + 12, getGuiTop() + 44, 16, 16, () -> filter.color,
              () -> filter.color = hasShiftDown() ? null : TransporterUtils.increment(filter.color), () -> filter.color = TransporterUtils.decrement(filter.color)));
    }
}