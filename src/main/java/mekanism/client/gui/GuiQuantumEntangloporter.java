package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.gui.element.GuiScrollList;
import mekanism.client.gui.element.GuiSideConfigurationTab;
import mekanism.client.gui.element.GuiTransporterConfigTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.inventory.container.ContainerQuantumEntangloporter;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterPacketType;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
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
public class GuiQuantumEntangloporter extends GuiMekanism
{
	public ResourceLocation resource;
	
	public TileEntityQuantumEntangloporter tileEntity;
	
	public EntityPlayer entityPlayer;
	
	public GuiButton publicButton;
	public GuiButton privateButton;
	
	public GuiButton setButton;
	public GuiButton deleteButton;
	
	public GuiScrollList scrollList;
	
	public GuiTextField frequencyField;
	
	public boolean privateMode;

	public GuiQuantumEntangloporter(InventoryPlayer inventory, TileEntityQuantumEntangloporter tentity)
	{
		super(tentity, new ContainerQuantumEntangloporter(inventory, tentity));
		tileEntity = tentity;
		resource = MekanismUtils.getResource(ResourceType.GUI, "GuiTeleporter.png");

		guiElements.add(scrollList = new GuiScrollList(this, resource, 28, 37, 120, 4));
		guiElements.add(new GuiSideConfigurationTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiTeleporter.png")));
		guiElements.add(new GuiTransporterConfigTab(this, 34, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiTeleporter.png")));
		
		if(tileEntity.frequency != null)
		{
			privateMode = !tileEntity.frequency.publicFreq;
		}
		
		ySize+=64;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		buttonList.clear();
		
		publicButton = new GuiButton(0, guiWidth + 27, guiHeight + 14, 60, 20, LangUtils.localize("gui.public"));
		privateButton = new GuiButton(1, guiWidth + 89, guiHeight + 14, 60, 20, LangUtils.localize("gui.private"));
		
		setButton = new GuiButton(2, guiWidth + 27, guiHeight + 116, 60, 20, LangUtils.localize("gui.set"));
		deleteButton = new GuiButton(3, guiWidth + 89, guiHeight + 116, 60, 20, LangUtils.localize("gui.delete"));
		
		frequencyField = new GuiTextField(fontRendererObj, guiWidth + 50, guiHeight + 104, 86, 11);
		frequencyField.setMaxStringLength(FrequencyManager.MAX_FREQ_LENGTH);
		frequencyField.setEnableBackgroundDrawing(false);
		
		updateButtons();

		buttonList.add(publicButton);
		buttonList.add(privateButton);
		buttonList.add(setButton);
		buttonList.add(deleteButton);
	}
	
	public void setFrequency(String freq)
	{
		if(freq.isEmpty())
		{
			return;
		}
		
		ArrayList data = new ArrayList();
		data.add(0);
		data.add(freq);
		data.add(!privateMode);
		
		Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
	}
	
	public String getSecurity(Frequency freq)
	{
		return !freq.publicFreq ? EnumColor.DARK_RED + LangUtils.localize("gui.private") : LangUtils.localize("gui.public");
	}
	
	public void updateButtons()
	{
		if(tileEntity.getSecurity().getOwner() == null)
		{
			return;
		}
		
		List<String> text = new ArrayList<String>();
		
		if(privateMode)
		{
			for(Frequency freq : tileEntity.privateCache)
			{
				text.add(freq.name);
			}
		}
		else {
			for(Frequency freq : tileEntity.publicCache)
			{
				text.add(freq.name + " (" + freq.owner + ")");
			}
		}
		
		scrollList.setText(text);
		
		if(privateMode)
		{
			publicButton.enabled = true;
			privateButton.enabled = false;
		}
		else {
			publicButton.enabled = false;
			privateButton.enabled = true;
		}
		
		if(scrollList.hasSelection())
		{
			Frequency freq = privateMode ? tileEntity.privateCache.get(scrollList.selected) : tileEntity.publicCache.get(scrollList.selected);
			
			if(tileEntity.getFrequency(null) == null || !tileEntity.getFrequency(null).equals(freq))
			{
				setButton.enabled = true;
			}
			else {
				setButton.enabled = false;
			}
			
			if(tileEntity.getSecurity().getOwner().equals(freq.owner))
			{
				deleteButton.enabled = true;
			}
			else {
				deleteButton.enabled = false;
			}
		}
		else {
			setButton.enabled = false;
			deleteButton.enabled = false;
		}
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
		
		updateButtons();
		
		frequencyField.updateCursorCounter();
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);
		
		updateButtons();

		frequencyField.mouseClicked(mouseX, mouseY, button);
		
		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);
			
			if(xAxis >= 137 && xAxis <= 148 && yAxis >= 103 && yAxis <= 114)
			{
				setFrequency(frequencyField.getText());
				frequencyField.setText("");
	            SoundHandler.playSound("gui.button.press");
			}
		}
	}
	
	@Override
	public void keyTyped(char c, int i)
	{
		if(!frequencyField.isFocused() || i == Keyboard.KEY_ESCAPE)
		{
			super.keyTyped(c, i);
		}

		if(i == Keyboard.KEY_RETURN)
		{
			if(frequencyField.isFocused())
			{
				setFrequency(frequencyField.getText());
				frequencyField.setText("");
			}
		}

		if(Character.isDigit(c) || Character.isLetter(c) || isTextboxKey(c, i) || FrequencyManager.SPECIAL_CHARS.contains(c))
		{
			frequencyField.textboxKeyTyped(c, i);
		}
		
		updateButtons();
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);

		if(guibutton.id == 0)
		{
			privateMode = false;
		}
		else if(guibutton.id == 1)
		{
			privateMode = true;
		}
		else if(guibutton.id == 2)
		{
			int selection = scrollList.getSelection();
			
			if(selection != -1)
			{
				Frequency freq = privateMode ? tileEntity.privateCache.get(selection) : tileEntity.publicCache.get(selection);
				setFrequency(freq.name);
			}
		}
		else if(guibutton.id == 3)
		{
			int selection = scrollList.getSelection();
			
			if(selection != -1)
			{
				Frequency freq = privateMode ? tileEntity.privateCache.get(selection) : tileEntity.publicCache.get(selection);
				
				if(tileEntity != null)
				{
					ArrayList data = new ArrayList();
					data.add(1);
					data.add(freq.name);
					data.add(freq.publicFreq);
					
					Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
				}
				else {
					Mekanism.packetHandler.sendToServer(new PortableTeleporterMessage(PortableTeleporterPacketType.DEL_FREQ, freq));
					Mekanism.packetHandler.sendToServer(new PortableTeleporterMessage(PortableTeleporterPacketType.DATA_REQUEST, null));
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

		fontRendererObj.drawString(tileEntity.getInventoryName(), (xSize/2)-(fontRendererObj.getStringWidth(tileEntity.getInventoryName())/2), 4, 0x404040);
		fontRendererObj.drawString(LangUtils.localize("gui.owner") + ": " + (tileEntity.getSecurity().getOwner() != null ? tileEntity.getSecurity().getOwner() : LangUtils.localize("gui.none")), 8, (ySize-96)+4, 0x404040);

		fontRendererObj.drawString(LangUtils.localize("gui.freq") + ":", 32, 81, 0x404040);
		fontRendererObj.drawString(LangUtils.localize("gui.security") + ":", 32, 91, 0x404040);
		
		fontRendererObj.drawString(" " + (tileEntity.getFrequency(null) != null ? tileEntity.getFrequency(null).name : EnumColor.DARK_RED + LangUtils.localize("gui.none")), 32 + fontRendererObj.getStringWidth(LangUtils.localize("gui.freq") + ":"), 81, 0x797979);
		fontRendererObj.drawString(" " + (tileEntity.getFrequency(null) != null ? getSecurity(tileEntity.getFrequency(null)) : EnumColor.DARK_RED + LangUtils.localize("gui.none")), 32 + fontRendererObj.getStringWidth(LangUtils.localize("gui.security") + ":"), 91, 0x797979);
		
		String str = LangUtils.localize("gui.set") + ":";
		renderScaledText(str, 27, 104, 0x404040, 20);

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
		
		if(xAxis >= 137 && xAxis <= 148 && yAxis >= 103 && yAxis <= 114)
		{
			drawTexturedModalRect(guiWidth + 137, guiHeight + 103, xSize, 0, 11, 11);
		}
		else {
			drawTexturedModalRect(guiWidth + 137, guiHeight + 103, xSize, 11, 11, 11);
		}

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
		
		frequencyField.drawTextBox();
	}
}
