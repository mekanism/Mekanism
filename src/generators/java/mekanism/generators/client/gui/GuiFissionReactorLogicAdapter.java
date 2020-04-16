package mekanism.generators.client.gui;

import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.generators.client.gui.element.button.ReactorLogicButton;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract.GeneratorsGuiInteraction;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.FissionReactorLogic;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFissionReactorLogicAdapter extends GuiMekanismTile<TileEntityFissionReactorLogicAdapter, EmptyTileContainer<TileEntityFissionReactorLogicAdapter>> {

    public GuiFissionReactorLogicAdapter(EmptyTileContainer<TileEntityFissionReactorLogicAdapter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiElementHolder(this, 23, 31, 130, 90));
        for (FissionReactorLogic type : FissionReactorLogic.values()) {
            int typeShift = 22 * type.ordinal();
            addButton(new ReactorLogicButton<>(this, getGuiLeft() + 24, getGuiTop() + 32 + typeShift, type, tile,
                  () -> MekanismGenerators.packetHandler.sendToServer(new PacketGeneratorsGuiInteract(GeneratorsGuiInteraction.FISSION_LOGIC_TYPE, tile, type.ordinal()))));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        renderScaledText(GeneratorsLang.REACTOR_LOGIC_REDSTONE_MODE.translate(tile.logicType.getColor(), tile.logicType), 23, 123, 0x404040, 130);
        drawCenteredText(MekanismLang.STATUS.translate(EnumColor.RED, tile.getStatus()),
              0, getXSize(), 136, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}
