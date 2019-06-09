package mekanism.client.gui.filter;

import java.io.IOException;
import mekanism.client.sound.SoundHandler;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.LangUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiFilterSelect<TILE extends TileEntityContainerBlock> extends GuiFilter<TILE> {

    protected GuiFilterSelect(EntityPlayer player, TILE tile) {
        super(tile, new ContainerNull(player, tile));
    }

    protected boolean inBounds(int xAxis, int yAxis) {
        return xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16;
    }

    @Override
    protected void addButtons() {
        buttonList.add(new GuiButton(0, guiLeft + 24, guiTop + 32, 128, 20, LangUtils.localize("gui.itemstack")));
        buttonList.add(new GuiButton(1, guiLeft + 24, guiTop + 52, 128, 20, LangUtils.localize("gui.oredict")));
        buttonList.add(new GuiButton(2, guiLeft + 24, guiTop + 72, 128, 20, LangUtils.localize("gui.material")));
        buttonList.add(new GuiButton(3, guiLeft + 24, guiTop + 92, 128, 20, LangUtils.localize("gui.modID")));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(LangUtils.localize("gui.filterSelect.title"), 43, 6, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        drawTexturedModalRect(guiLeft + 5, guiTop + 5, 176, inBounds(xAxis, yAxis), 11);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int xAxis = mouseX - guiLeft;
            int yAxis = mouseY - guiTop;
            if (inBounds(xAxis, yAxis)) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                sendPacketToServer(0);
            }
        }
    }
}