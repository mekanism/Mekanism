package mekanism.generators.client.gui;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiFusionReactorInfo extends GuiMekanismTile<TileEntityFusionReactorController, EmptyTileContainer<TileEntityFusionReactorController>> {

    protected GuiFusionReactorInfo(EmptyTileContainer<TileEntityFusionReactorController> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new MekanismImageButton(this, getGuiLeft() + 6, getGuiTop() + 6, 14, getButtonLocation("back"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tile))));
    }
}