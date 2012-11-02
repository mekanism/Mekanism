
package net.uberkat.obsidian.hawk.client;

import org.lwjgl.opengl.GL11;
import universalelectricity.electricity.ElectricInfo;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.StatCollector;
import net.uberkat.obsidian.hawk.common.ContainerWasher;
import net.uberkat.obsidian.hawk.common.HawksMachinery;
import net.uberkat.obsidian.hawk.common.TileEntityWasher;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class GuiWasher extends GuiContainer
{
	private TileEntityWasher tileEntity;
	
	private int containerWidth;
	private int containerHeight;	
	
	public GuiWasher(InventoryPlayer playerInv, TileEntityWasher tentity)
	{
		super(new ContainerWasher(playerInv, tentity));
		tileEntity = tentity;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		fontRenderer.drawString(ElectricInfo.getDisplayShort(tileEntity.getVoltage(), ElectricInfo.ElectricUnit.VOLTAGE), 116, 60, 4210752);
		fontRenderer.drawString(ElectricInfo.getDisplayShort(tileEntity.energyStored, ElectricInfo.ElectricUnit.WATT), 116, 70, 4210752);
	}
	
	/**
	 * Draw the background layer for the GuiContainer (everything behind the items)
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		int var4 = mc.renderEngine.getTexture("/gui/GuiWasher.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(var4);
		containerWidth = (width - xSize) / 2;
		containerHeight = (height - ySize) / 2;
		drawTexturedModalRect(containerWidth, containerHeight, 0, 0, xSize, ySize);
		
		if (tileEntity.operatingTicks > 0)
		{
			int scale = tileEntity.getWashingStatus(tileEntity.TICKS_REQUIRED);
			drawTexturedModalRect(containerWidth + 52, containerHeight + 28, 176, 0, 52 - scale, 20);
		}

		drawTexturedModalRect(containerWidth - 32, containerHeight - 16, 0, 232, 240, 16);
	}
}
