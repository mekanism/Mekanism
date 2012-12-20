package mekanism.client;

import mekanism.api.InfusionType;
import mekanism.common.ContainerMetallurgicInfuser;
import mekanism.common.PacketHandler;
import mekanism.common.TileEntityMetallurgicInfuser;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

public class GuiMetallurgicInfuser extends GuiContainer
{
	public TileEntityMetallurgicInfuser tileEntity;
	
	private int guiWidth;
	private int guiHeight;
	
	public GuiMetallurgicInfuser(InventoryPlayer inventory, TileEntityMetallurgicInfuser tentity)
    {
        super(new ContainerMetallurgicInfuser(inventory, tentity));
        tileEntity = tentity;
    }
	
	@Override
    protected void mouseClicked(int x, int y, int button)
    {
		super.mouseClicked(x, y, button);
		
		int xAxis = (x - (width - xSize) / 2);
		int yAxis = (y - (height - ySize) / 2);
		
		if(xAxis > 148 && xAxis < 168 && yAxis > 73 && yAxis < 82)
		{
			tileEntity.infuseStored = 0;
			PacketHandler.sendTileEntityPacketToServer(tileEntity, tileEntity.infuseStored);
			mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		}
    }

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        fontRenderer.drawString(tileEntity.fullName, 45, 6, 0x404040);
        fontRenderer.drawString("Inventory", 15, (ySize - 96) + 2, 0x404040);
    }

	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int texture = mc.renderEngine.getTexture("/resources/mekanism/gui/GuiMetallurgicInfuser.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(texture);
        guiWidth = (width - xSize) / 2;
        guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int displayInt;
        
        displayInt = tileEntity.getScaledInfuseLevel(52);
        drawTexturedModalRect(guiWidth + 7, guiHeight + 27 + 52 - displayInt, 176 + (tileEntity.type == InfusionType.COAL ? 4 : 0), 52 + (tileEntity.type == InfusionType.TIN ? 52 : 0) - displayInt, 4, displayInt);
        
        displayInt = tileEntity.getScaledProgress(32);
        drawTexturedModalRect(guiWidth + 72, guiHeight + 47, 176, 52 + 52, displayInt + 1, 8);
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
    }
}
