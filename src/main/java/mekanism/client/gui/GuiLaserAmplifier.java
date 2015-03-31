package mekanism.client.gui;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.client.gui.element.GuiDetectionTab;
import mekanism.client.gui.element.GuiGauge.Type;
import mekanism.client.gui.element.GuiNumberGauge;
import mekanism.client.gui.element.GuiNumberGauge.INumberInfoHandler;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerLaserAmplifier;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.IIcon;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLaserAmplifier extends GuiMekanism
{
	public TileEntityLaserAmplifier tileEntity;

	public GuiTextField minField;
	public GuiTextField maxField;
	public GuiTextField timerField;

	public GuiLaserAmplifier(InventoryPlayer inventory, TileEntityLaserAmplifier tentity)
	{
		super(tentity, new ContainerLaserAmplifier(inventory, tentity));
		tileEntity = tentity;

		guiElements.add(new GuiNumberGauge(new INumberInfoHandler()
		{
			@Override
			public IIcon getIcon()
			{
				return MekanismRenderer.energyIcon;
			}

			@Override
			public double getLevel()
			{
				return tileEntity.collectedEnergy;
			}

			@Override
			public double getMaxLevel()
			{
				return tileEntity.MAX_ENERGY;
			}

			@Override
			public String getText(double level)
			{
				return MekanismUtils.localize("gui.storing") + ": " + MekanismUtils.getEnergyDisplay(level);
			}
		}, Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 6, 10));
		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png")));
		guiElements.add(new GuiDetectionTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png")));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(tileEntity.getInventoryName(), 55, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);

		fontRendererObj.drawString(tileEntity.time > 0 ? MekanismUtils.localize("gui.delay") + ": " + tileEntity.time + "t" : MekanismUtils.localize("gui.noDelay"), 26, 30, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.min") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.minThreshold), 26, 45, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.max") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.maxThreshold), 26, 60, 0x404040);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		minField.drawTextBox();
		maxField.drawTextBox();
		timerField.drawTextBox();
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		minField.updateCursorCounter();
		maxField.updateCursorCounter();
		timerField.updateCursorCounter();
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		minField.mouseClicked(mouseX, mouseY, button);
		maxField.mouseClicked(mouseX, mouseY, button);
		timerField.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void keyTyped(char c, int i)
	{
		if(!(minField.isFocused() || maxField.isFocused() || timerField.isFocused()) || i == Keyboard.KEY_ESCAPE)
		{
			super.keyTyped(c, i);
		}

		if(i == Keyboard.KEY_RETURN)
		{
			if(minField.isFocused())
			{
				setMinThreshold();
			}
			if(maxField.isFocused())
			{
				setMaxThreshold();
			}
			if(timerField.isFocused())
			{
				setTime();
			}
		}

		if(Character.isDigit(c) || c == '.' || c == 'E' || i == Keyboard.KEY_BACK || i == Keyboard.KEY_DELETE || i == Keyboard.KEY_LEFT || i == Keyboard.KEY_RIGHT)
		{
			minField.textboxKeyTyped(c, i);
			maxField.textboxKeyTyped(c, i);
			timerField.textboxKeyTyped(c, i);
		}
	}

	private void setMinThreshold()
	{
		if(!minField.getText().isEmpty())
		{
			double toUse;

			try {
				toUse = Math.max(0, Double.parseDouble(minField.getText()));
			} catch(Exception e) {
				minField.setText("");
				return;
			}

			ArrayList data = new ArrayList();
			data.add(0);
			data.add(toUse);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));

			minField.setText("");
		}
	}

	private void setMaxThreshold()
	{
		if(!maxField.getText().isEmpty())
		{
			double toUse;

			try {
				toUse = Math.max(0, Double.parseDouble(maxField.getText()));
			} catch(Exception e) {
				maxField.setText("");
				return;
			}

			ArrayList data = new ArrayList();
			data.add(1);
			data.add(toUse);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));

			maxField.setText("");
		}
	}

	private void setTime()
	{
		if(!timerField.getText().isEmpty())
		{
			int toUse = Math.max(0, Integer.parseInt(timerField.getText()));

			ArrayList data = new ArrayList();
			data.add(2);
			data.add(toUse);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));

			timerField.setText("");
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		String prevTime = timerField != null ? timerField.getText() : "";

		timerField = new GuiTextField(fontRendererObj, guiWidth + 96, guiHeight + 28, 36, 11);
		timerField.setMaxStringLength(4);
		timerField.setText(prevTime);

		String prevMin = minField != null ? minField.getText() : "";
		minField = new GuiTextField(fontRendererObj, guiWidth + 96, guiHeight + 43, 72, 11);
		minField.setMaxStringLength(10);
		minField.setText(prevMin);

		String prevMax = maxField != null ? maxField.getText() : "";

		maxField = new GuiTextField(fontRendererObj, guiWidth + 96, guiHeight + 58, 72, 11);
		maxField.setMaxStringLength(10);
		maxField.setText(prevMax);
	}
}
