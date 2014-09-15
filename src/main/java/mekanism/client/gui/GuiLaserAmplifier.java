package mekanism.client.gui;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.client.gui.GuiGauge.Type;
import mekanism.client.gui.GuiNumberGauge.INumberInfoHandler;
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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiLaserAmplifier extends GuiMekanism
{
	public TileEntityLaserAmplifier tileEntity;

	public GuiTextField thresholdField;
	public GuiTextField timerField;

	public GuiLaserAmplifier(InventoryPlayer inventory, TileEntityLaserAmplifier tentity)
	{
		super(new ContainerLaserAmplifier(inventory, tentity));
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
				return "Stored: " + MekanismUtils.getEnergyDisplay(level);
			}
		}, Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 6, 10));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(tileEntity.getInventoryName(), 55, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);

		fontRendererObj.drawString(MekanismUtils.localize("gui.threshold" + ": " + MekanismUtils.getEnergyDisplay(tileEntity.threshold)), 75, 45, 0x404040);

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

		thresholdField.drawTextBox();
		timerField.drawTextBox();
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		thresholdField.updateCursorCounter();
		timerField.updateCursorCounter();
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		thresholdField.mouseClicked(mouseX, mouseY, button);
		timerField.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void keyTyped(char c, int i)
	{
		if(!(thresholdField.isFocused() || timerField.isFocused()) || i == Keyboard.KEY_ESCAPE)
		{
			super.keyTyped(c, i);
		}

		if(i == Keyboard.KEY_RETURN)
		{
			if(thresholdField.isFocused())
			{
				setThreshold();
			}
			if(timerField.isFocused())
			{
				setTime();
			}
		}

		if(Character.isDigit(c) || c == '.' || c == 'E' || i == Keyboard.KEY_BACK || i == Keyboard.KEY_DELETE || i == Keyboard.KEY_LEFT || i == Keyboard.KEY_RIGHT)
		{
			thresholdField.textboxKeyTyped(c, i);
			timerField.textboxKeyTyped(c, i);
		}
	}

	private void setThreshold()
	{
		if(!thresholdField.getText().isEmpty())
		{
			double toUse;

			try
			{
				toUse = Math.max(0, Double.parseDouble(thresholdField.getText()));
			}
			catch(Exception e)
			{
				toUse = 0;
			}

			ArrayList data = new ArrayList();
			data.add(1);
			data.add(toUse);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));

			thresholdField.setText("");
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

		String prevThresh = thresholdField != null ? thresholdField.getText() : "";

		thresholdField = new GuiTextField(fontRendererObj, guiWidth + 75, guiHeight + 55, 96, 11);
		thresholdField.setMaxStringLength(10);
		thresholdField.setText(prevThresh);

		String prevTime = timerField != null ? timerField.getText() : "";

		timerField = new GuiTextField(fontRendererObj, guiWidth + 75, guiHeight + 15, 26, 11);
		timerField.setMaxStringLength(4);
		timerField.setText(prevTime);
	}
}
