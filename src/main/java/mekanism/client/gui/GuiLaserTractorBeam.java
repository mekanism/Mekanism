package mekanism.client.gui;

import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.inventory.container.tile.LaserTractorBeamContainer;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiLaserTractorBeam extends GuiMekanismTile<TileEntityLaserTractorBeam, LaserTractorBeamContainer> {

    public GuiLaserTractorBeam(LaserTractorBeamContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, getGuiLocation()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiFullInv.png");
    }
}