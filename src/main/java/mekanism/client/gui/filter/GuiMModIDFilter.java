package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.content.miner.MModIDFilter;
import mekanism.common.network.PacketDigitalMinerGui.DigitalMinerGuiMessage;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMModIDFilter extends GuiModIDFilter<MModIDFilter, TileEntityDigitalMiner> {

    public GuiMModIDFilter(EntityPlayer player, TileEntityDigitalMiner tile, int index) {
        super(player, tile);
        origFilter = (MModIDFilter) tileEntity.filters.get(index);
        filter = ((MModIDFilter) tileEntity.filters.get(index)).clone();
        updateStackList(filter.getModID());
    }

    public GuiMModIDFilter(EntityPlayer player, TileEntityDigitalMiner tile) {
        super(player, tile);
        isNew = true;
        filter = new MModIDFilter();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString((isNew ? LangUtils.localize("gui.new") : LangUtils.localize("gui.edit")) + " " +
                                LangUtils.localize("gui.modIDFilter"), 43, 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.status") + ": " + status, 35, 20, 0x00CD00);
        renderScaledText(LangUtils.localize("gui.id") + ": " + filter.getModID(), 35, 32, 0x00CD00, 107);
        renderItem(renderStack, 12, 19);
        renderItem(filter.replaceStack, 149, 19);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59) {
            drawHoveringText(LangUtils.localize("gui.digitalMiner.requireReplace") + ": " + LangUtils.transYesNo(filter.requireStack), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiMModIDFilter.png");
    }

    @Override
    protected void updateStackList(String modName) {
        iterStacks = OreDictCache.getModIDStacks(modName, true);
        stackSwitch = 0;
        stackIndex = -1;
    }

    @Override
    protected void addButtons() {
        buttonList.add(new GuiButton(0, guiLeft + 27, guiTop + 62, 60, 20, LangUtils.localize("gui.save")));
        buttonList.add(new GuiButton(1, guiLeft + 89, guiTop + 62, 60, 20, LangUtils.localize("gui.delete")));
    }

    @Override
    protected void sendPacketToServer(int guiID) {
        Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), guiID, 0, 0));
    }
}