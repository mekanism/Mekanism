package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.content.transporter.TOreDictFilter;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTOreDictFilter extends GuiOreDictFilter<TOreDictFilter, TileEntityLogisticalSorter> {

    public GuiTOreDictFilter(EntityPlayer player, TileEntityLogisticalSorter tile, int index) {
        super(player, tile);
        origFilter = (TOreDictFilter) tileEntity.filters.get(index);
        filter = ((TOreDictFilter) tileEntity.filters.get(index)).clone();
        updateStackList(filter.getOreDictName());
    }

    public GuiTOreDictFilter(EntityPlayer player, TileEntityLogisticalSorter tile) {
        super(player, tile);
        isNew = true;
        filter = new TOreDictFilter();
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
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString((isNew ? LangUtils.localize("gui.new") : LangUtils.localize("gui.edit")) + " " +
                                LangUtils.localize("gui.oredictFilter"), 43, 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.status") + ": " + status, 35, 20, 0x00CD00);
        renderScaledText(LangUtils.localize("gui.key") + ": " + filter.getOreDictName(), 35, 32, 0x00CD00, 107);
        fontRenderer.drawString(LangUtils.transOnOff(filter.allowDefault), 24, 66, 0x404040);
        renderItem(renderStack, 12, 19);
        drawColorIcon(12, 44, filter.color, 1);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 11 && xAxis <= 22 && yAxis >= 64 && yAxis <= 75) {
            drawHoveringText(LangUtils.localize("gui.allowDefault"), xAxis, yAxis);
        } else if (xAxis >= 12 && xAxis <= 28 && yAxis >= 44 && yAxis <= 60) {
            if (filter.color != null) {
                drawHoveringText(filter.color.getColoredName(), xAxis, yAxis);
            } else {
                drawHoveringText(LangUtils.localize("gui.none"), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTOreDictFilter.png");
    }

    @Override
    protected void updateStackList(String oreName) {
        iterStacks = OreDictCache.getOreDictStacks(oreName, false);
        stackSwitch = 0;
        stackIndex = -1;
    }
}