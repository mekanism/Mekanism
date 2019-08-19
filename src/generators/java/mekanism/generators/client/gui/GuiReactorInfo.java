package mekanism.generators.client.gui;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.inventory.container.reactor.info.ReactorInfoContainer;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiReactorInfo<CONTAINER extends ReactorInfoContainer> extends GuiMekanismTile<TileEntityReactorController, CONTAINER> {

    private Button backButton;

    protected GuiReactorInfo(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        buttons.clear();
        buttons.add(backButton = new GuiButtonDisableableImage(guiLeft + 6, guiTop + 6, 14, 14, 176, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tileEntity.getPos()))));
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "tall.png");
    }
}