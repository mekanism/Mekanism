package mekanism.client;

import java.util.ArrayList;

import mekanism.api.Object3D;
import mekanism.common.ContainerMetallurgicInfuser;
import mekanism.common.MekanismUtils;
import mekanism.common.PacketHandler;
import mekanism.common.MekanismUtils.ResourceType;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.TileEntityMetallurgicInfuser;
import mekanism.common.network.PacketRemoveUpgrade;
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
	
	public GuiRedstoneControl redstoneControl;
	public GuiUpgradeManagement upgradeManagement;
	
	public GuiMetallurgicInfuser(InventoryPlayer inventory, TileEntityMetallurgicInfuser tentity)
    {
        super(new ContainerMetallurgicInfuser(inventory, tentity));
        tileEntity = tentity;
        
        redstoneControl = new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"));
        upgradeManagement = new GuiUpgradeManagement(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"));
    }

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(tileEntity.fullName, 45, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
        
		if(xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(ElectricityDisplay.getDisplayShort(tileEntity.getEnergyStored(), ElectricUnit.JOULES), xAxis, yAxis);
		}
		
		redstoneControl.renderForeground(xAxis, yAxis);
		upgradeManagement.renderForeground(xAxis, yAxis);
    }

	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY)
    {
		mc.renderEngine.func_110577_a(MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int xAxis = (mouseX - (width - xSize) / 2);
 		int yAxis = (mouseY - (height - ySize) / 2);
        
        int displayInt;
        
        displayInt = tileEntity.getScaledProgress(32);
        drawTexturedModalRect(guiWidth + 72, guiHeight + 47, 176, 52, displayInt + 1, 8);
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
        
        if(tileEntity.type != null)
        {
	        displayInt = tileEntity.getScaledInfuseLevel(52);
	        mc.renderEngine.func_110577_a(tileEntity.type.texture);
	        drawTexturedModalRect(guiWidth + 7, guiHeight + 17 + 52 - displayInt, tileEntity.type.texX, tileEntity.type.texY + 52 - displayInt, 4, displayInt);
        }
        
        redstoneControl.renderBackground(xAxis, yAxis, guiWidth, guiHeight);
        upgradeManagement.renderBackground(xAxis, yAxis, guiWidth, guiHeight);
    }
	
	@Override
    protected void mouseClicked(int x, int y, int button)
    {
		xSize += 26;
		super.mouseClicked(x, y, button);
		xSize -= 26;
		
		if(button == 0)
		{
			int xAxis = (x - (width - xSize) / 2);
			int yAxis = (y - (height - ySize) / 2);
			
			redstoneControl.mouseClicked(xAxis, yAxis);
			upgradeManagement.mouseClicked(xAxis, yAxis);
			
			if(xAxis > 148 && xAxis < 168 && yAxis > 73 && yAxis < 82)
			{
				ArrayList data = new ArrayList();
				data.add(0);
				
				PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Object3D.get(tileEntity), data));
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
			}
		}
    }
}
