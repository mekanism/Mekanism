package mekanism.client.gui.filter;

import java.io.IOException;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.content.filter.IOreDictFilter;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.tile.base.TileEntityContainer;
import mekanism.common.util.LangUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiOreDictFilter<FILTER extends IOreDictFilter, TILE extends TileEntityContainer> extends GuiTextFilter<FILTER, TILE> {

    protected GuiOreDictFilter(EntityPlayer player, TILE tile) {
        super(player, tile);
    }

    protected abstract void updateStackList(String oreName);

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == 0) {
            if (!text.getText().isEmpty()) {
                setText();
            }
            if (filter.getOreDictName() != null && !filter.getOreDictName().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new NewFilterMessage(Coord4D.get(tileEntity), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), false, origFilter, filter));
                }
                sendPacketToServer(0);
            } else {
                status = EnumColor.DARK_RED + LangUtils.localize("gui.oredictFilter.noKey");
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
            status = EnumColor.DARK_RED + LangUtils.localize("gui.oredictFilter.noKey");
            return;
        } else if (name.equals(filter.getOreDictName())) {
            status = EnumColor.DARK_RED + LangUtils.localize("gui.oredictFilter.sameKey");
            return;
        }
        updateStackList(name);
        filter.setOreDictName(name);
        text.setText("");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString((isNew ? LangUtils.localize("gui.new") : LangUtils.localize("gui.edit")) + " " +
                                LangUtils.localize("gui.oredictFilter"), 43, 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.status") + ": " + status, 35, 20, 0x00CD00);
        renderScaledText(LangUtils.localize("gui.key") + ": " + filter.getOreDictName(), 35, 32, 0x00CD00, 107);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}