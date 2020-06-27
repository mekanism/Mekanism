package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.tileentity.TileEntity;

public class GuiUpgradeTab extends GuiInsetElement<TileEntity> {

    public GuiUpgradeTab(IGuiWrapper gui, TileEntity tile) {
        super(MekanismUtils.getResource(ResourceType.GUI, "upgrade.png"), gui, tile, gui.getWidth(), 6, 26, 18, false);
    }

    @Override
    public void func_230443_a_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        displayTooltip(matrix, MekanismLang.UPGRADES.translate(), mouseX, mouseY);
    }

    @Override
    public void func_230982_a_(double mouseX, double mouseY) {
        Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.UPGRADE_MANAGEMENT, tile));
    }
}