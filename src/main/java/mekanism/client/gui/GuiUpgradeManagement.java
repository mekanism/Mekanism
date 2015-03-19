package mekanism.client.gui;

import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.inventory.container.ContainerUpgradeManagement;
import mekanism.common.network.PacketRemoveUpgrade.RemoveUpgradeMessage;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiUpgradeManagement extends GuiMekanism
{
	public IUpgradeTile tileEntity;
	
	public Upgrade selectedType;
	
	public boolean isDragging = false;

	public int dragOffset = 0;
	
	public int supportedIndex;
	
	public int delay;
	
	public float scroll;
	
	public GuiUpgradeManagement(InventoryPlayer inventory, IUpgradeTile tile) 
	{
		super(new ContainerUpgradeManagement(inventory, tile));
		tileEntity = tile;
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
		
		if(delay < 40)
		{
			delay++;
		}
		else {
			delay = 0;
			supportedIndex = ++supportedIndex%tileEntity.getComponent().getSupportedTypes().size();
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiUpgradeManagement.png"));
		GL11.glColor4f(1, 1, 1, 1);
		drawTexturedModalRect(84, 8+getScroll(), 202, 0, 4, 4);
		
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.upgrades.supported") + ":", 26, 59, 0x404040);
		
		if(selectedType == null)
		{
			renderText(MekanismUtils.localize("gui.upgrades.noSelection") + ".", 92, 8, 0.8F, true);
		}
		else {
			int amount = tileEntity.getComponent().getUpgrades(selectedType);
			
			renderText(selectedType.getName() + " " + MekanismUtils.localize("gui.upgrade"), 92, 8, 0.6F, true);
			renderText(MekanismUtils.localize("gui.upgrades.amount") + ": " + amount + "/" + selectedType.getMax(), 92, 16, 0.6F, true);
			
			int text = 0;
			
			for(String s : selectedType.getInfo((TileEntity)tileEntity))
			{
				renderText(s, 92, 22+(6*text++), 0.6F, true);
			}
		}
		
		if(!tileEntity.getComponent().getSupportedTypes().isEmpty())
		{
			Upgrade[] supported = tileEntity.getComponent().getSupportedTypes().toArray(new Upgrade[tileEntity.getComponent().getSupportedTypes().size()]);
			
			if(supported.length > supportedIndex)
			{
				renderUpgrade(supported[supportedIndex], 80, 57, 0.8F, true);
				fontRendererObj.drawString(supported[supportedIndex].getName(), 96, 59, 0x404040);
			}
		}
		
		Upgrade[] upgrades = getCurrentUpgrades().toArray(new Upgrade[getCurrentUpgrades().size()]);
		
		for(int i = 0; i < 4; i++)
		{
			int index = getUpgradeIndex()+i;
			
			if(index > upgrades.length-1)
			{
				break;
			}
			
			Upgrade upgrade = upgrades[index];
			
			int xPos = 25;
			int yPos = 7 + (i*12);
			int yRender = 0;
			
			fontRendererObj.drawString(upgrade.getName(), xPos + 12, yPos + 2, 0x404040);
			
			renderUpgrade(upgrade, xPos + 2, yPos + 2, 0.5F, true);
			
			if(xAxis >= xPos && xAxis <= xPos+58 && yAxis >= yPos && yAxis <= yPos+12)
			{
				func_146283_a(MekanismUtils.splitLines(upgrade.getDescription()), xAxis, yAxis);
			}
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}
	
	private void renderText(String text, int x, int y, float size, boolean scale)
	{
		GL11.glPushMatrix();
		GL11.glScalef(size, size, size);
		fontRendererObj.drawString(text, scale ? (int)((1F/size)*x) : x, scale ? (int)((1F/size)*y) : y, 0x00CD00);
		GL11.glPopMatrix();
	}
	
	private void renderUpgrade(Upgrade type, int x, int y, float size, boolean scale)
	{
		GL11.glPushMatrix();
		GL11.glScalef(size, size, size);
		GL11.glEnable(GL11.GL_LIGHTING);
		itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), type.getStack(), scale ? (int)((1F/size)*x) : x, scale ? (int)((1F/size)*y) : y);
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
		
		if(selectedType == null)
		{
			drawTexturedModalRect(guiWidth + 136, guiHeight + 57, 176 + 14, 24, 12, 12);
		}
		else if(xAxis >= 136 && xAxis <= 148 && yAxis >= 57 && yAxis <= 69)
		{
			drawTexturedModalRect(guiWidth + 136, guiHeight + 57, 176 + 14, 0, 12, 12);
		}
		else {
			drawTexturedModalRect(guiWidth + 136, guiHeight + 57, 176 + 14, 12, 12, 12);
		}
		
		int displayInt = tileEntity.getComponent().getScaledUpgradeProgress(14);
		drawTexturedModalRect(guiWidth + 154, guiHeight + 26, 176, 28, 10, displayInt);
		
		if(selectedType != null && tileEntity.getComponent().getUpgrades(selectedType) == 0)
		{
			selectedType = null;
		}
		
		Upgrade[] upgrades = getCurrentUpgrades().toArray(new Upgrade[getCurrentUpgrades().size()]);
		
		for(int i = 0; i < 4; i++)
		{
			int index = getUpgradeIndex()+i;
			
			if(index > upgrades.length-1)
			{
				break;
			}
			
			Upgrade upgrade = upgrades[index];
			
			int xPos = 25;
			int yPos = 7 + (i*12);
			int yRender = 0;
			
			if(upgrade == selectedType)
			{
				yRender = 166 + 24;
			}
			else if(xAxis >= xPos && xAxis <= xPos+58 && yAxis >= yPos && yAxis <= yPos+12)
			{
				yRender = 166;
			}
			else {
				yRender = 166 + 12;
			}
			
			MekanismRenderer.color(upgrade.getColor(), 1.0F, 2.5F);
			drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 0, yRender, 58, 12);
			MekanismRenderer.resetColor();
		}

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
	
	private Set<Upgrade> getCurrentUpgrades()
	{
		return tileEntity.getComponent().getInstalledTypes();
	}
	
	public int getScroll()
	{
		return Math.max(Math.min((int)(scroll*42), 42), 0);
	}

	public int getUpgradeIndex()
	{
		if(getCurrentUpgrades().size() <= 4)
		{
			return 0;
		}

		return (int)((getCurrentUpgrades().size()*scroll) - ((4F/(float)getCurrentUpgrades().size()))*scroll);
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int button, long ticks)
	{
		super.mouseClickMove(mouseX, mouseY, button, ticks);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(isDragging)
		{
			scroll = Math.min(Math.max((float)(yAxis-8-dragOffset)/42F, 0), 1);
		}
	}

	@Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int type)
	{
		super.mouseMovedOrUp(mouseX, mouseY, type);

		if(type == 0 && isDragging)
		{
			dragOffset = 0;
			isDragging = false;
		}
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
			if(xAxis >= 84 && xAxis <= 88 && yAxis >= getScroll()+8 && yAxis <= getScroll()+8+4)
			{
				if(getCurrentUpgrades().size()>4)
				{
					dragOffset = yAxis - (getScroll()+8);
					isDragging = true;
				}
				else {
					scroll = 0;
				}
			}
			
			if(xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20)
			{
				int guiId = MachineType.get(tile.getBlockType(), tile.getBlockMetadata()).guiId;
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tile), guiId));
			}
			
			if(selectedType != null && xAxis >= 136 && xAxis <= 148 && yAxis >= 57 && yAxis <= 69)
			{
				SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new RemoveUpgradeMessage(Coord4D.get(tile), selectedType.ordinal()));
			}
			
			int counter = 0;
			
			for(Upgrade upgrade : getCurrentUpgrades())
			{
				int xPos = 25;
				int yPos = 7 + (counter++*12);
				int yRender = 0;
				
				if(xAxis >= xPos && xAxis <= xPos+58 && yAxis >= yPos && yAxis <= yPos+12)
				{
					selectedType = upgrade;
					break;
				}
			}
		}
	}
}
