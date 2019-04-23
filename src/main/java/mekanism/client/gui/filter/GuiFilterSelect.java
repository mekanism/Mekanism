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
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public abstract class GuiFilterSelect<TILE extends TileEntityContainerBlock> extends GuiFilter<TILE> {

    protected GuiFilterSelect(EntityPlayer player, TILE tile) {
        super(tile, new ContainerNull(player, tile));
    }

    protected boolean inBounds(int xAxis, int yAxis) {
        return xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16;
    }

    @Override
    public void initGui() {
        super.initGui();
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        buttonList.clear();
        addButtons(guiWidth, guiHeight);
    }

    @Override
    protected void addButtons(int guiWidth, int guiHeight) {
        buttonList.add(new GuiButton(0, guiWidth + 24, guiHeight + 32, 128, 20, LangUtils.localize("gui.itemstack")));
        buttonList.add(new GuiButton(1, guiWidth + 24, guiHeight + 52, 128, 20, LangUtils.localize("gui.oredict")));
        buttonList.add(new GuiButton(2, guiWidth + 24, guiHeight + 72, 128, 20, LangUtils.localize("gui.material")));
        buttonList.add(new GuiButton(3, guiWidth + 24, guiHeight + 92, 128, 20, LangUtils.localize("gui.modID")));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(LangUtils.localize("gui.filterSelect.title"), 43, 6, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(getGuiLocation());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176, inBounds(xAxis, yAxis) ? 0 : 11, 11, 11);
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int xAxis = (mouseX - (width - xSize) / 2);
            int yAxis = (mouseY - (height - ySize) / 2);
            if (inBounds(xAxis, yAxis)) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                sendPacketToServer(0);
            }
        }
    }
}