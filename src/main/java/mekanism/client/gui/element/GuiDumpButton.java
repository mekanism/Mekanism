package mekanism.client.gui.element;

import mekanism.api.TileNetworkList;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiDumpButton extends GuiTileEntityElement<TileEntityMetallurgicInfuser> {

    //TODO: Make it some tile that implements a dumping interface
    public GuiDumpButton(IGuiWrapper gui, TileEntityMetallurgicInfuser tile, ResourceLocation def, int x, int y) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "dump.png"), gui, def, tile, x, y, 21, 10);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        guiObj.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        //TODO: Change this to using PacketGuiButtonPress or something with a specific dump button
        Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(0)));
    }
}