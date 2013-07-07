package mekanism.client;

import java.util.ArrayList;

import mekanism.api.Object3D;
import mekanism.common.ContainerMetallurgicInfuser;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.TileEntityMetallurgicInfuser;
import mekanism.common.network.PacketTileEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMetallurgicInfuser extends GuiContainer
{
	public TileEntityMetallurgicInfuser tileEntity;
	
	private int guiWidth;
	private int guiHeight;
	
	public GuiMetallurgicInfuser(InventoryPlayer inventory, TileEntityMetallurgicInfuser tentity)
    {
        super(new ContainerMetallurgicInfuser(inventory, tentity));
        xSize+=26;
        tileEntity = tentity;
    }
	
	@Override
    protected void mouseClicked(int x, int y, int button)
    {
		super.mouseClicked(x, y, button);
		
		if(button == 0)
		{
			int xAxis = (x - (width - xSize) / 2);
			int yAxis = (y - (height - ySize) / 2);
			
			if(xAxis > 148 && xAxis < 168 && yAxis > 73 && yAxis < 82)
			{
				ArrayList data = new ArrayList();
				data.add(0);
				
				PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Object3D.get(tileEntity), data));
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
			}
		}
    }

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(tileEntity.fullName, 45, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
        fontRenderer.drawString("S:" + (tileEntity.speedMultiplier+1) + "x", 179, 47, 0x404040);
        fontRenderer.drawString("E:" + (tileEntity.energyMultiplier+1) + "x", 179, 57, 0x404040);
        
		if(xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(ElectricityDisplay.getDisplayShort(tileEntity.electricityStored, ElectricUnit.JOULES), xAxis, yAxis);
		}
    }

	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        mc.renderEngine.bindTexture("/mods/mekanism/gui/GuiMetallurgicInfuser.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        guiWidth = (width - xSize) / 2;
        guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int displayInt;
        
        displayInt = tileEntity.getScaledProgress(32);
        drawTexturedModalRect(guiWidth + 72, guiHeight + 47, 176 + 26, 52, displayInt + 1, 8);
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176 + 26, 52 - displayInt, 4, displayInt);
        
        displayInt = tileEntity.getScaledUpgradeProgress(14);
        drawTexturedModalRect(guiWidth + 180, guiHeight + 30, 176 + 26, 60, 10, displayInt);
        
        if(tileEntity.type != null)
        {
	        displayInt = tileEntity.getScaledInfuseLevel(52);
	        mc.renderEngine.bindTexture(tileEntity.type.texture);
	        drawTexturedModalRect(guiWidth + 7, guiHeight + 17 + 52 - displayInt, tileEntity.type.texX, tileEntity.type.texY + 52 - displayInt, 4, displayInt);
        }
    }
}
