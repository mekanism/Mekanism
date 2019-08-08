package mekanism.client.gui.filter;

import java.io.IOException;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.LangUtils;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiItemStackFilter<FILTER extends IItemStackFilter, TILE extends TileEntityMekanism> extends GuiTypeFilter<FILTER, TILE> {

    protected GuiItemStackFilter(PlayerEntity player, TILE tile) {
        super(player, tile);
    }

    @Override
    public void tick() {
        super.tick();
        if (ticker > 0) {
            ticker--;
        } else {
            status = EnumColor.DARK_GREEN + LangUtils.localize("gui.allOK");
        }
    }

    @Override
    protected void actionPerformed(Button guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == deleteButton.id) {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), true, origFilter, null));
            sendPacketToServer(0);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString((isNew ? LangUtils.localize("gui.new") : LangUtils.localize("gui.edit")) + " " + LangUtils.localize("gui.itemFilter"), 43, 6, 0x404040);
        font.drawString(LangUtils.localize("gui.status") + ": " + status, 35, 20, 0x00CD00);
        font.drawString(LangUtils.localize("gui.itemFilter.details") + ":", 35, 32, 0x00CD00);
        drawForegroundLayer(mouseX, mouseY);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}