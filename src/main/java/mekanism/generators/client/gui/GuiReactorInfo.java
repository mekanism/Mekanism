package mekanism.generators.client.gui;

import java.io.IOException;
import mekanism.api.Coord4D;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiReactorInfo extends GuiMekanismTile<TileEntityReactorController> {

    private Button backButton;

    public GuiReactorInfo(TileEntityReactorController tile, Container container) {
        super(tile, container);
    }

    @Override
    public void init() {
        super.init();
        buttons.clear();
        buttons.add(backButton = new GuiButtonDisableableImage(0, guiLeft + 6, guiTop + 6, 14, 14, 176, 14, -14, getGuiLocation()));
    }

    @Override
    protected void actionPerformed(Button guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == backButton.id) {
            Mekanism.packetHandler.sendToServer(new PacketSimpleGui(Coord4D.get(tileEntity), 1, 10));
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png");
    }
}