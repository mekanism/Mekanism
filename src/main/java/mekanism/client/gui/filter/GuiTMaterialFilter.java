package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.transporter.TMaterialFilter;
import mekanism.common.inventory.container.tile.filter.LSMaterialFilterContainer;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiTMaterialFilter extends GuiMaterialFilter<TMaterialFilter, TileEntityLogisticalSorter, LSMaterialFilterContainer> {

    public GuiTMaterialFilter(LSMaterialFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        origFilter = container.getOrigFilter();
        filter = container.getFilter();
        isNew = container.isNew();
    }

    @Override
    protected void addButtons() {
        addButton(new GuiInnerScreen(this, 33, 18, 93, 43));
        addButton(new GuiSlot(SlotType.NORMAL, this, 11, 18));
        addButton(new GuiSlot(SlotType.NORMAL, this, 11, 43));
        addButton(saveButton = new TranslationButton(this, getGuiLeft() + 47, getGuiTop() + 62, 60, 20, MekanismLang.BUTTON_SAVE, () -> {
            if (!filter.getMaterialItem().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(Coord4D.get(tile), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tile), false, origFilter, filter));
                }
                sendPacketToServer(ClickedTileButton.BACK_BUTTON);
            } else {
                status = MekanismLang.ITEM_FILTER_NO_ITEM.translateColored(EnumColor.DARK_RED);
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
        addButton(new ColorButton(this, getGuiLeft() + 12, getGuiTop() + 44, 16, 16, () -> filter.color,
              () -> filter.color = InputMappings.isKeyDown(minecraft.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ? null : TransporterUtils.increment(filter.color),
              () -> filter.color = TransporterUtils.decrement(filter.color)));
    }

    @Override
    protected void drawForegroundLayer(int mouseX, int mouseY) {
        if (!filter.getMaterialItem().isEmpty()) {
            renderScaledText(filter.getMaterialItem().getDisplayName(), 35, 41, 0x00CD00, 107);
        }
        drawTransporterForegroundLayer(filter.getMaterialItem());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && overTypeInput(mouseX - getGuiLeft(), mouseY - getGuiTop())) {
            materialMouseClicked();
        }
        return true;
    }
}