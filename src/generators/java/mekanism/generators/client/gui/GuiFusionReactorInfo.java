package mekanism.generators.client.gui;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import mekanism.common.network.to_server.PacketGuiButtonPress.ClickedTileButton;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class GuiFusionReactorInfo extends GuiMekanismTile<TileEntityFusionReactorController, EmptyTileContainer<TileEntityFusionReactorController>> {

    protected GuiFusionReactorInfo(EmptyTileContainer<TileEntityFusionReactorController> container, Inventory inv, Component title) {
        super(container, inv, title);
        titleLabelY = 5;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new MekanismImageButton(this, 6, 6, 14, getButtonLocation("back"),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tile))));
    }
}