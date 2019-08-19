package mekanism.client.gui.element.tab;

import mekanism.api.TileNetworkList;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTileEntityElement;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiAmplifierTab extends GuiTileEntityElement<TileEntityLaserAmplifier> {

    public GuiAmplifierTab(IGuiWrapper gui, TileEntityLaserAmplifier tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "amplifier.png"), gui, def, tile);
    }

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth - 26, guiHeight + 138, 26, 26);
    }

    @Override
    protected boolean inBounds(double xAxis, double yAxis) {
        return xAxis >= -21 && xAxis <= -3 && yAxis >= 142 && yAxis <= 160;
    }

    @Override
    public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(guiWidth - 26, guiHeight + 138, 0, 0, 26, 26);
        int outputOrdinal = tileEntity.outputMode.ordinal();
        guiObj.drawTexturedRect(guiWidth - 21, guiHeight + 142, 26 + 18 * outputOrdinal, inBounds(xAxis, yAxis) ? 0 : 18, 18, 18);
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    @Override
    public void renderForeground(int xAxis, int yAxis) {
        minecraft.textureManager.bindTexture(RESOURCE);
        if (inBounds(xAxis, yAxis)) {
            displayTooltip(TextComponentUtil.build(Translation.of("mekanism.gui.redstoneOutput"), ": ", tileEntity.outputMode), xAxis, yAxis);
        }
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && inBounds(mouseX, mouseY)) {
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(3)));
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}