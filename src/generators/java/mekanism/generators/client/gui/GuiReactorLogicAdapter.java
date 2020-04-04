package mekanism.generators.client.gui;

import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.generators.client.gui.element.button.ReactorLogicButton;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter.ReactorLogic;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiReactorLogicAdapter extends GuiMekanismTile<TileEntityReactorLogicAdapter, EmptyTileContainer<TileEntityReactorLogicAdapter>> {

    public GuiReactorLogicAdapter(EmptyTileContainer<TileEntityReactorLogicAdapter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiElementHolder(this, 23, 31, 130, 90));
        addButton(new MekanismImageButton(this, getGuiLeft() + 23, getGuiTop() + 19, 11, 18, getButtonLocation("toggle"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.TOGGLE_MODE_BUTTON, tile)), getOnHover(GeneratorsLang.REACTOR_LOGIC_TOGGLE_COOLING)));
        for (ReactorLogic type : ReactorLogic.values()) {
            int typeShift = 22 * type.ordinal();
            addButton(new ReactorLogicButton(this, getGuiLeft() + 24, getGuiTop() + 32 + typeShift, type, tile,
                  () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(type)))));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        renderScaledText(GeneratorsLang.REACTOR_LOGIC_ACTIVE_COOLING.translate(EnumColor.RED, OnOff.of(tile.activeCooled)), 36, 20, 0x404040, 117);
        renderScaledText(GeneratorsLang.REACTOR_LOGIC_REDSTONE_OUTPUT_MODE.translate(EnumColor.RED, tile.logicType), 23, 123, 0x404040, 130);
        drawCenteredText(MekanismLang.STATUS.translate(EnumColor.RED, tile.checkMode() ? GeneratorsLang.REACTOR_LOGIC_OUTPUTTING : MekanismLang.IDLE),
              0, getXSize(), 136, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}