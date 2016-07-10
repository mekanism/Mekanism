package mekanism.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.gui.element.GuiScrollList;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerSecurityDesk;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSecurityDesk extends GuiMekanism
{
	public static int MAX_LENGTH = 24;
	
	public ResourceLocation resource;
	
	public TileEntitySecurityDesk tileEntity;
	
	public EntityPlayer entityPlayer;
	
	public GuiButton removeButton;
	
	public GuiScrollList scrollList;
	
	public GuiTextField trustedField;
	
	public static final List<Character> SPECIAL_CHARS = Arrays.asList('-', '|', '_');

	public GuiSecurityDesk(InventoryPlayer inventory, TileEntitySecurityDesk tentity)
	{
		super(tentity, new ContainerSecurityDesk(inventory, tentity));
		tileEntity = tentity;
		resource = MekanismUtils.getResource(ResourceType.GUI, "GuiSecurityDesk.png");

		guiElements.add(scrollList = new GuiScrollList(this, resource, 14, 14, 120, 4));
		
		ySize+=64;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		buttonList.clear();

		removeButton = new GuiButton(0, guiWidth + 13, guiHeight + 81, 122, 20, LangUtils.localize("gui.remove"));
		
		trustedField = new GuiTextField(fontRendererObj, guiWidth + 35, guiHeight + 69, 86, 11);
		trustedField.setMaxStringLength(MAX_LENGTH);
		trustedField.setEnableBackgroundDrawing(false);
		
		updateButtons();

		buttonList.add(removeButton);
	}
	
	public void addTrusted(String trusted)
	{
		if(trusted.isEmpty())
		{
			return;
		}
		
		ArrayList data = new ArrayList();
		data.add(0);
		data.add(trusted);
		
		Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
	}
	
	public void updateButtons()
	{
		if(tileEntity.owner == null)
		{
			return;
		}
		
		List<String> text = new ArrayList<String>();
		
		if(tileEntity.frequency != null)
		{
			for(String s : tileEntity.frequency.trusted)
			{
				text.add(s);
			}
		}
		
		scrollList.setText(text);
		
		if(scrollList.hasSelection())
		{
			removeButton.enabled = true;
		}
		else {
			removeButton.enabled = false;
		}
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
		
		updateButtons();
		
		trustedField.updateCursorCounter();
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);
		
		updateButtons();

		trustedField.mouseClicked(mouseX, mouseY, button);
		
		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);
			
			if(tileEntity.frequency != null && tileEntity.owner != null && tileEntity.owner.equals(mc.thePlayer.getCommandSenderName()))
			{
				if(xAxis >= 123 && xAxis <= 134 && yAxis >= 68 && yAxis <= 79)
				{
					addTrusted(trustedField.getText());
					trustedField.setText("");
		            SoundHandler.playSound("gui.button.press");
				}
				
				ArrayList data = new ArrayList();
				
				if(xAxis >= 146 && xAxis <= 162 && yAxis >= 59 && yAxis <= 75)
				{
					data.add(2);
				}
				
				if(tileEntity.frequency.securityMode != SecurityMode.PUBLIC)
				{
					if(xAxis >= 13 && xAxis <= 53 && yAxis >= 113 && yAxis <= 129)
					{
						data.add(3);
						data.add(0);
					}
				}
				
				if(tileEntity.frequency.securityMode != SecurityMode.PRIVATE)
				{
					if(xAxis >= 54 && xAxis <= 94 && yAxis >= 113 && yAxis <= 129)
					{
						data.add(3);
						data.add(1);
					}
				}
				
				if(tileEntity.frequency.securityMode != SecurityMode.TRUSTED)
				{
					if(xAxis >= 95 && xAxis <= 135 && yAxis >= 113 && yAxis <= 129)
					{
						data.add(3);
						data.add(2);
					}
				}
				
				if(!data.isEmpty())
				{
					Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
				}
			}
		}
	}
	
	@Override
	public void keyTyped(char c, int i)
	{
		if(!trustedField.isFocused() || i == Keyboard.KEY_ESCAPE)
		{
			super.keyTyped(c, i);
		}

		if(i == Keyboard.KEY_RETURN)
		{
			if(trustedField.isFocused())
			{
				addTrusted(trustedField.getText());
				trustedField.setText("");
			}
		}

		if(SPECIAL_CHARS.contains(c) || Character.isDigit(c) || Character.isLetter(c) || isTextboxKey(c, i))
		{
			trustedField.textboxKeyTyped(c, i);
		}
		
		updateButtons();
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);

		if(guibutton.id == 0)
		{
			int selection = scrollList.getSelection();
			
			if(tileEntity.frequency != null && selection != -1)
			{
				if(tileEntity != null)
				{
					ArrayList data = new ArrayList();
					data.add(1);
					data.add(tileEntity.frequency.trusted.get(selection));
					
					Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
				}
				
				scrollList.selected = -1;
			}
		}
		
		updateButtons();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX-(width-xSize)/2);
		int yAxis = (mouseY-(height-ySize)/2);

		String ownerText = EnumColor.RED + tileEntity.owner != null ? (LangUtils.localize("gui.owner") + ": " + tileEntity.owner) : LangUtils.localize("gui.noOwner");
		fontRendererObj.drawString(tileEntity.getInventoryName(), (xSize/2)-(fontRendererObj.getStringWidth(tileEntity.getInventoryName())/2), 4, 0x404040);
		fontRendererObj.drawString(ownerText, (xSize - 7) - fontRendererObj.getStringWidth(ownerText), (ySize - 96) + 2, 0x404040);
		fontRendererObj.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
		
		String trusted = LangUtils.localize("gui.trustedPlayers");
		fontRendererObj.drawString(trusted, 74-(fontRendererObj.getStringWidth(trusted)/2), 57, 0x787878);
		
		String security = EnumColor.RED + LangUtils.localize("gui.securityOffline");
		
		if(tileEntity.frequency != null)
		{
			security = LangUtils.localize("gui.security") + ": " + tileEntity.frequency.securityMode.getDisplay();
		}
		
		fontRendererObj.drawString(security, 13, 103, 0x404040);
		
		renderScaledText(LangUtils.localize("gui.add") + ":", 13, 70, 0x404040, 20);
		
		if(tileEntity.frequency != null && xAxis >= 146 && xAxis <= 162 && yAxis >= 59 && yAxis <= 75)
		{
			displayTooltip(LangUtils.localize("gui.securityOverride") + ": " + LangUtils.transOnOff(tileEntity.frequency.override), xAxis, yAxis);
		}
		
		if(xAxis >= 13 && xAxis <= 53 && yAxis >= 113 && yAxis <= 129)
		{
			displayTooltip(LangUtils.localize("gui.publicMode"), xAxis, yAxis);
		}
		
		if(xAxis >= 54 && xAxis <= 94 && yAxis >= 113 && yAxis <= 129)
		{
			displayTooltip(LangUtils.localize("gui.privateMode"), xAxis, yAxis);
		}
		
		if(xAxis >= 95 && xAxis <= 135 && yAxis >= 113 && yAxis <= 129)
		{
			displayTooltip(LangUtils.localize("gui.trustedMode"), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(resource);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width-xSize)/2;
		int guiHeight = (height-ySize)/2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
		
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		if(tileEntity.frequency != null && tileEntity.owner != null && mc.thePlayer.getCommandSenderName().equals(tileEntity.owner))
		{
			drawTexturedModalRect(guiWidth + 145, guiHeight + 78, xSize + (tileEntity.frequency.override ? 0 : 6), 22, 6, 6);
			
			if(xAxis >= 146 && xAxis <= 162 && yAxis >= 59 && yAxis <= 75)
			{
				drawTexturedModalRect(guiWidth + 146, guiHeight + 59, xSize + 12, 0, 16, 16);
			}
			else {
				drawTexturedModalRect(guiWidth + 146, guiHeight + 59, xSize + 12, 16, 16, 16);
			}
			
			if(tileEntity.frequency.securityMode != SecurityMode.PUBLIC)
			{
				if(xAxis >= 13 && xAxis <= 53 && yAxis >= 113 && yAxis <= 129)
				{
					drawTexturedModalRect(guiWidth + 13, guiHeight + 113, xSize, 48, 40, 16);
				}
				else {
					drawTexturedModalRect(guiWidth + 13, guiHeight + 113, xSize, 64, 40, 16);
				}
			}
			else {
				drawTexturedModalRect(guiWidth + 13, guiHeight + 113, xSize, 80, 40, 16);
			}
			
			if(tileEntity.frequency.securityMode != SecurityMode.PRIVATE)
			{
				if(xAxis >= 54 && xAxis <= 94 && yAxis >= 113 && yAxis <= 129)
				{
					drawTexturedModalRect(guiWidth + 54, guiHeight + 113, xSize + 40, 48, 40, 16);
				}
				else {
					drawTexturedModalRect(guiWidth + 54, guiHeight + 113, xSize + 40, 64, 40, 16);
				}
			}
			else {
				drawTexturedModalRect(guiWidth + 54, guiHeight + 113, xSize + 40, 80, 40, 16);
			}
			
			if(tileEntity.frequency.securityMode != SecurityMode.TRUSTED)
			{
				if(xAxis >= 95 && xAxis <= 135 && yAxis >= 113 && yAxis <= 129)
				{
					drawTexturedModalRect(guiWidth + 95, guiHeight + 113, xSize, 96, 40, 16);
				}
				else {
					drawTexturedModalRect(guiWidth + 95, guiHeight + 113, xSize, 112, 40, 16);
				}
			}
			else {
				drawTexturedModalRect(guiWidth + 95, guiHeight + 113, xSize, 128, 40, 16);
			}
		}
		else {
			drawTexturedModalRect(guiWidth + 145, guiHeight + 78, xSize, 28, 6, 6);
			drawTexturedModalRect(guiWidth + 146, guiHeight + 59, xSize + 12, 32, 16, 16);
			drawTexturedModalRect(guiWidth + 13, guiHeight + 113, xSize, 80, 40, 16);
			drawTexturedModalRect(guiWidth + 54, guiHeight + 113, xSize + 40, 80, 40, 16);
			drawTexturedModalRect(guiWidth + 95, guiHeight + 113, xSize, 128, 40, 16);
		}
		
		if(xAxis >= 123 && xAxis <= 134 && yAxis >= 68 && yAxis <= 79)
		{
			drawTexturedModalRect(guiWidth + 123, guiHeight + 68, xSize, 0, 11, 11);
		}
		else {
			drawTexturedModalRect(guiWidth + 123, guiHeight + 68, xSize, 11, 11, 11);
		}

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
		
		trustedField.drawTextBox();
	}
}
