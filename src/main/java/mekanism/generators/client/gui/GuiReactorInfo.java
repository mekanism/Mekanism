package mekanism.generators.client.gui;

import java.io.IOException;
import mekanism.api.Coord4D;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiReactorInfo extends GuiMekanismTile<TileEntityReactorController> {

    public GuiReactorInfo(TileEntityReactorController tile, Container container) {
        super(tile, container);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        drawTexturedModalRect(guiLeft + 6, guiTop + 6, 176, xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20, 14);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (button == 0) {
            if (xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 1, 10));
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png");
    }
}