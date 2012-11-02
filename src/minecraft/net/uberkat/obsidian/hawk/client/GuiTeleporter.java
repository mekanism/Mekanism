
package net.uberkat.obsidian.hawk.client;

import org.lwjgl.opengl.GL11;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.InventoryPlayer;
import net.uberkat.obsidian.hawk.common.ContainerTeleporter;
import net.uberkat.obsidian.hawk.common.HawksMachinery;
import net.uberkat.obsidian.hawk.common.TileEntityTeleporter;

public class GuiTeleporter extends GuiContainer
{
	public TileEntityTeleporter tileEntity;
	
	private int containerWidth;
	private int containerHeight;
	
	public GuiTeleporter(InventoryPlayer player, TileEntityTeleporter tentity)
	{
		super(new ContainerTeleporter(player));
		tileEntity = tentity;
	}

	public void initGui()
	{
		controlList.add(new GuiButton(0, containerHeight - 9, containerWidth - 9, 16, 16, null));
	}
	
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(mc.renderEngine.getTexture("/gui/GuiEndiumTeleporter.png"));
		containerWidth = (width - xSize) / 2;
		containerHeight = (height - ySize) / 2;
		drawTexturedModalRect(containerWidth, containerHeight, 0, 0, xSize, ySize);
	}
}
