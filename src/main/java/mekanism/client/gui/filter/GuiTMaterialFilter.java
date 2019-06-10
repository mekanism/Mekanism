package mekanism.client.gui.filter;

import java.io.IOException;
import mekanism.api.Coord4D;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.content.transporter.TMaterialFilter;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTMaterialFilter extends GuiMaterialFilter<TMaterialFilter, TileEntityLogisticalSorter> {

    public GuiTMaterialFilter(EntityPlayer player, TileEntityLogisticalSorter tile, int index) {
        super(player, tile);
        origFilter = (TMaterialFilter) tileEntity.filters.get(index);
        filter = ((TMaterialFilter) tileEntity.filters.get(index)).clone();
    }

    public GuiTMaterialFilter(EntityPlayer player, TileEntityLogisticalSorter tile) {
        super(player, tile);
        isNew = true;
        filter = new TMaterialFilter();
    }

    @Override
    protected void addButtons() {
        buttonList.add(new GuiButton(0, guiLeft + 47, guiTop + 62, 60, 20, LangUtils.localize("gui.save")));
        buttonList.add(new GuiButton(1, guiLeft + 109, guiTop + 62, 60, 20, LangUtils.localize("gui.delete")));
    }

    @Override
    protected void sendPacketToServer(int guiID) {
        Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), guiID, 0, 0));
    }

    @Override
    protected void drawForegroundLayer(int mouseX, int mouseY) {
        if (!filter.getMaterialItem().isEmpty()) {
            renderScaledText(filter.getMaterialItem().getDisplayName(), 35, 41, 0x00CD00, 107);
        }
        drawTransporterForegroundLayer(mouseX, mouseY, filter.getMaterialItem());
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (button == 0) {
            if (xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                sendPacketToServer(isNew ? 4 : 0);
            } else if (xAxis >= 12 && xAxis <= 28 && yAxis >= 19 && yAxis <= 35) {
                materialMouseClicked();
            } else if (xAxis >= 11 && xAxis <= 22 && yAxis >= 64 && yAxis <= 75) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                filter.allowDefault = !filter.allowDefault;
            }
        }
        transporterMouseClicked(xAxis, yAxis, button, filter);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTMaterialFilter.png");
    }
}