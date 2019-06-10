package mekanism.client.gui.filter;

import java.io.IOException;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.LangUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiModIDFilter<FILTER extends IModIDFilter, TILE extends TileEntityContainerBlock> extends GuiTextFilter<FILTER, TILE> {

    protected GuiModIDFilter(EntityPlayer player, TILE tile) {
        super(player, tile);
    }

    protected abstract void updateStackList(String modName);

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == 0) {
            if (!text.getText().isEmpty()) {
                setText();
            }
            if (filter.getModID() != null && !filter.getModID().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new NewFilterMessage(Coord4D.get(tileEntity), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), false, origFilter, filter));
                }
                sendPacketToServer(0);
            } else {
                status = EnumColor.DARK_RED + LangUtils.localize("gui.modIDFilter.noID");
                ticker = 20;
            }
        } else if (guibutton.id == 1) {
            Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), true, origFilter, null));
            sendPacketToServer(0);
        }
    }

    @Override
    protected void setText() {
        String name = text.getText();
        if (name.isEmpty()) {
            status = EnumColor.DARK_RED + LangUtils.localize("gui.modIDFilter.noID");
            return;
        } else if (name.equals(filter.getModID())) {
            status = EnumColor.DARK_RED + LangUtils.localize("gui.modIDFilter.sameID");
            return;
        }
        updateStackList(name);
        filter.setModID(name);
        text.setText("");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString((isNew ? LangUtils.localize("gui.new") : LangUtils.localize("gui.edit")) + " " +
                                LangUtils.localize("gui.modIDFilter"), 43, 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.status") + ": " + status, 35, 20, 0x00CD00);
        renderScaledText(LangUtils.localize("gui.id") + ": " + filter.getModID(), 35, 32, 0x00CD00, 107);

        if (tileEntity instanceof TileEntityDigitalMiner) {
            drawMinerForegroundLayer(mouseX, mouseY, renderStack);
        } else if (tileEntity instanceof TileEntityLogisticalSorter) {
            drawTransporterForegroundLayer(mouseX, mouseY, renderStack);
        }

        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}