package mekanism.client.gui;

import mekanism.api.EnumColor;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiScrollList;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.inventory.container.ContainerTeleporter;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTeleporter extends GuiMekanism
{
	public static int MAX_LENGTH = 16;
	
	public TileEntityTeleporter tileEntity;
	
	public GuiButton publicButton;
	public GuiButton privateButton;
	
	public GuiButton setButton;
	public GuiButton deleteButton;
	
	public GuiScrollList scrollList;
	
	public GuiTextField frequencyField;
	
	public boolean privateMode;

	public GuiTeleporter(InventoryPlayer inventory, TileEntityTeleporter tentity)
	{
		super(tentity, new ContainerTeleporter(inventory, tentity));
		tileEntity = tentity;

		guiElements.add(new GuiPowerBar(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiTeleporter.png"), 158, 26));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTeleporter.png"), 152, 6).with(SlotOverlay.POWER));
		guiElements.add(scrollList = new GuiScrollList(this, MekanismUtils.getResource(ResourceType.GUI, "GuiTeleporter.png"), 28, 37, 120, 4));
		
		ySize+=64;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		buttonList.clear();
		
		publicButton = new GuiButton(0, guiWidth + 27, guiHeight + 14, 60, 20, MekanismUtils.localize("gui.public"));
		privateButton = new GuiButton(1, guiWidth + 89, guiHeight + 14, 60, 20, MekanismUtils.localize("gui.private"));
		
		setButton = new GuiButton(2, guiWidth + 27, guiHeight + 116, 60, 20, MekanismUtils.localize("gui.set"));
		deleteButton = new GuiButton(3, guiWidth + 89, guiHeight + 116, 60, 20, MekanismUtils.localize("gui.delete"));
		
		frequencyField = new GuiTextField(fontRendererObj, guiWidth + 50, guiHeight + 104, 86, 11);
		frequencyField.setMaxStringLength(MAX_LENGTH);
		frequencyField.setEnableBackgroundDrawing(false);
		
		updateButtons();

		buttonList.add(publicButton);
		buttonList.add(privateButton);
		buttonList.add(setButton);
		buttonList.add(deleteButton);
	}
	
	public void setFrequency()
	{
		String text = frequencyField.getText();
		
		if(text.isEmpty())
		{
			return;
		}
	}
	
	public String getSecurity()
	{
		return privateMode ? EnumColor.DARK_RED + MekanismUtils.localize("gui.private") : MekanismUtils.localize("gui.public");
	}
	
	public void updateButtons()
	{
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
			setButton.enabled = true;
			deleteButton.enabled = true;
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
				setFrequency();
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
				setFrequency();
			}
		}

		if(Character.isDigit(c) || Character.isLetter(c) || i == Keyboard.KEY_BACK || i == Keyboard.KEY_DELETE || i == Keyboard.KEY_LEFT || i == Keyboard.KEY_RIGHT)
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
			
		}
		else if(guibutton.id == 3)
		{
			
		}
		
		updateButtons();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX-(width-xSize)/2);
		int yAxis = (mouseY-(height-ySize)/2);

		fontRendererObj.drawString(tileEntity.getInventoryName(), (xSize/2)-(fontRendererObj.getStringWidth(tileEntity.getInventoryName())/2), 4, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.owner") + ": " + tileEntity.owner != null ? tileEntity.owner : MekanismUtils.localize("gui.none"), 8, (ySize-96)+4, 0x404040);
		
		fontRendererObj.drawString(MekanismUtils.localize("gui.freq") + ":", 32, 81, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.security") + ":", 32, 91, 0x404040);
		
		fontRendererObj.drawString(" " + (tileEntity.frequency != null ? tileEntity.frequency.name : EnumColor.DARK_RED + MekanismUtils.localize("gui.none")), 32 + fontRendererObj.getStringWidth(MekanismUtils.localize("gui.freq") + ":"), 81, 0x797979);
		fontRendererObj.drawString(" " + getSecurity(), 32 + fontRendererObj.getStringWidth(MekanismUtils.localize("gui.security") + ":"), 91, 0x797979);
		
		String str = MekanismUtils.localize("gui.set") + ":";
		renderScaledText(str, 27, 104, 0x404040, 20);
		
		if(xAxis >= 6 && xAxis <= 24 && yAxis >= 6 && yAxis <= 24)
		{
			if(tileEntity.frequency == null)
			{
				drawCreativeTabHoveringText(EnumColor.DARK_RED + MekanismUtils.localize("gui.teleporter.noFreq"), xAxis, yAxis);
			}
			else {
				drawCreativeTabHoveringText(tileEntity.getStatusDisplay(), xAxis, yAxis);
			}
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiTeleporter.png"));
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
		
		int y = tileEntity.frequency == null ? 94 : (tileEntity.status == 2 ? 22 : (tileEntity.status == 3 ? 40 : 
			(tileEntity.status == 4 ? 58 : 76)));
		
		drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176, y, 18, 18);

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
		
		frequencyField.drawTextBox();
	}
}
