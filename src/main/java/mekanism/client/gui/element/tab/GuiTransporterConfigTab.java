package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiTransporterConfigTab extends GuiInsetElement<TileEntity> {

    public GuiTransporterConfigTab(IGuiWrapper gui, TileEntity tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "transporter_config.png"), gui, def, tile, -26, 34, 26, 18);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(TextComponentUtil.translate("gui.mekanism.configuration.transporter"), mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.TRANSPORTER_CONFIGURATION, tileEntity.getPos()));
    }

    @Override
    protected int getTextureWidth() {
        return 18;
    }

    @Override
    protected int getTextureHeight() {
        return 36;
    }
}