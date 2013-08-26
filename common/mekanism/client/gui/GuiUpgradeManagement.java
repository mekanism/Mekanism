package mekanism.client.gui;

import mekanism.api.Object3D;
import mekanism.common.IUpgradeTile;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketRemoveUpgrade;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class GuiUpgradeManagement extends GuiElement
{
	public GuiUpgradeManagement(GuiContainer gui, TileEntity tile, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI, "GuiUpgradeManagement.png"), gui, tile, def);
	}
	
	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.func_110577_a(RESOURCE);
		
		guiContainer.drawTexturedModalRect(guiWidth + 176, guiHeight + 6, 0, 0, 26, 63);
		
		IUpgradeTile upgradeTile = (IUpgradeTile)tileEntity;
		int displayInt = upgradeTile.getComponent().getScaledUpgradeProgress(14);
		
	    guiContainer.drawTexturedModalRect(guiWidth + 180, guiHeight + 30, 26, 0, 10, displayInt);
		
		mc.renderEngine.func_110577_a(defaultLocation);
	}
	
	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.func_110577_a(RESOURCE);
		
		IUpgradeTile upgradeTile = (IUpgradeTile)tileEntity;
		
		getFontRenderer().drawString("S:" + (upgradeTile.getSpeedMultiplier()+1) + "x", 179, 47, 0x404040);
        getFontRenderer().drawString("E:" + (upgradeTile.getEnergyMultiplier()+1) + "x", 179, 57, 0x404040);
		
		if(xAxis >= 179 && xAxis <= 198 && yAxis >= 47 && yAxis <= 54)
		{
			displayTooltip("Remove speed upgrade", xAxis, yAxis);
		}
		
		if(xAxis >= 179 && xAxis <= 198 && yAxis >= 57 && yAxis <= 64)
		{
			displayTooltip("Remove energy upgrade", xAxis, yAxis);
		}
		
		mc.renderEngine.func_110577_a(defaultLocation);
	}
	
	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button)
	{
		if(button == 0)
		{
			if(xAxis >= 180 && xAxis <= 196 && yAxis >= 11 && yAxis <= 27)
			{
				offsetX(26);
			}
		}
	}
	
	@Override
	public void mouseClicked(int xAxis, int yAxis, int button)
	{
		if(button == 0)
		{
			if(xAxis >= 179 && xAxis <= 198 && yAxis >= 47 && yAxis <= 54)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketRemoveUpgrade().setParams(Object3D.get(tileEntity), (byte)0));
			}
			
			if(xAxis >= 179 && xAxis <= 198 && yAxis >= 57 && yAxis <= 64)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketRemoveUpgrade().setParams(Object3D.get(tileEntity), (byte)1));
			}
			
			if(xAxis >= 180 && xAxis <= 196 && yAxis >= 11 && yAxis <= 27)
			{
				offsetX(-26);
			}
		}
	}
}
