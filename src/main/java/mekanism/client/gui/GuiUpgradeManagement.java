package mekanism.client.gui;

import java.util.Arrays;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.IUpgradeTile;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.inventory.container.ContainerUpgradeManagement;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiUpgradeManagement extends GuiMekanism
{
	public IUpgradeTile tileEntity;
	
	public Upgrade selectedType;
	
	public GuiUpgradeManagement(InventoryPlayer inventory, IUpgradeTile tile) 
	{
		super(new ContainerUpgradeManagement(inventory, tile));
		tileEntity = tile;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.upgrades.supported") + ":", 26, 60, 0x404040);
		
		if(selectedType == null)
		{
			renderText(MekanismUtils.localize("gui.upgrades.noSelection") + ".", 96, 9, 0.8F);
		}
		
		int rendered = 0;
		
		for(Upgrade upgrade : tileEntity.getComponent().getSupportedTypes())
		{
			renderUpgrade(upgrade, 84 + (rendered++*12), 60, 0.8F);
		}
		
		int counter = 0;
		
		for(Upgrade upgrade : getCurrentUpgrades())
		{
			int xPos = 25;
			int yPos = 7 + (counter++*12);
			int yRender = 0;
			
			if(xAxis >= xPos && xAxis <= xPos+64 && yAxis >= yPos && yAxis <= yPos+12)
			{
				func_146283_a(MekanismUtils.splitLines(upgrade.getDescription()), xAxis, yAxis);
			}
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}
	
	private void renderText(String text, int x, int y, float size)
	{
		GL11.glPushMatrix();
		GL11.glScalef(size, size, size);
		fontRendererObj.drawString(text, (int)(x*(1+(1-size))), (int)(y*(1+(1-size))), 0x00CD00);
		GL11.glPopMatrix();
	}
	
	private void renderUpgrade(Upgrade type, int x, int y, float size)
	{
		GL11.glPushMatrix();
		GL11.glScalef(size, size, size);
		GL11.glEnable(GL11.GL_LIGHTING);
		itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), type.getStack(), (int)(x*(1+(1-size))), (int)(y*(1+(1-size))));
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiUpgradeManagement.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		if(xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20)
		{
			drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176, 14, 14, 14);
		}
		
		int displayInt = tileEntity.getComponent().getScaledUpgradeProgress(14);
		drawTexturedModalRect(guiWidth + 154, guiHeight + 26, 176, 28, 10, displayInt);
		
		if(selectedType != null && tileEntity.getComponent().getUpgrades(selectedType) == 0)
		{
			selectedType = null;
		}
		
		int rendered = 0;
		
		for(Upgrade upgrade : getCurrentUpgrades())
		{
			int xPos = 25;
			int yPos = 7 + (rendered++*12);
			int yRender = 0;
			
			if(upgrade == selectedType)
			{
				yRender = 166 + 24;
			}
			else if(xAxis >= xPos && xAxis <= xPos+64 && yAxis >= yPos && yAxis <= yPos+12)
			{
				yRender = 166;
			}
			else {
				yRender = 166 + 12;
			}
			
			MekanismRenderer.color(upgrade.getColor(), 1.0F, 2.5F);
			drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 0, yRender, 64, 12);
			MekanismRenderer.resetColor();
		}

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
	
	/* Here for scrolling in the future */
	private Set<Upgrade> getCurrentUpgrades()
	{
		return tileEntity.getComponent().getInstalledTypes();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		TileEntity tile = (TileEntity)tileEntity;
		
		if(button == 0)
		{
			if(xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20)
			{
				int guiId = MachineType.get(tile.getBlockType(), tile.getBlockMetadata()).guiId;
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tile), guiId));
			}
			
			int counter = 0;
			
			for(Upgrade upgrade : getCurrentUpgrades())
			{
				int xPos = 25;
				int yPos = 7 + (counter++*12);
				int yRender = 0;
				
				if(xAxis >= xPos && xAxis <= xPos+64 && yAxis >= yPos && yAxis <= yPos+12)
				{
					selectedType = upgrade;
					break;
				}
			}
		}
	}
}
