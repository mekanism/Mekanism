package mekanism.client.gui;

import java.util.ArrayList;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import mekanism.api.Coord4D;
import mekanism.client.gui.GuiSlot.SlotOverlay;
import mekanism.client.gui.GuiSlot.SlotType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerTeleporter;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiTeleporter extends GuiMekanism
{
	public TileEntityTeleporter tileEntity;

	public GuiTeleporter(InventoryPlayer inventory, TileEntityTeleporter tentity)
	{
		super(new ContainerTeleporter(inventory, tentity));
		tileEntity = tentity;

		guiElements.add(new GuiPowerBar(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiTeleporter.png"), 164, 15));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTeleporter.png"), 26, 13).with(SlotOverlay.POWER));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX-(width-xSize)/2);
		int yAxis = (mouseY-(height-ySize)/2);

		fontRendererObj.drawString(tileEntity.getInventoryName(), 45, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize-96)+2, 0x404040);
		fontRendererObj.drawString(tileEntity.getStatusDisplay(), 66, 19, 0x00CD00);

		if(xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void mouseClicked(int x, int y, int button)
	{
		super.mouseClicked(x, y, button);

		int xAxis = (x-(width-xSize)/2);
		int yAxis = (y-(height-ySize)/2);

		handleButtonClick(xAxis, yAxis, button, 23, 37, 44, 58, 0);
		handleButtonClick(xAxis, yAxis, button, 62, 76, 44, 58, 1);
		handleButtonClick(xAxis, yAxis, button, 101, 115, 44, 58, 2);
		handleButtonClick(xAxis, yAxis, button, 140, 154, 44, 58, 3);
	}

	private void handleButtonClick(int xAxis, int yAxis, int mouseButton, int xmin, int xmax, int ymin, int ymax, int buttonIndex)
	{
		if(xAxis > xmin && xAxis < xmax && yAxis > ymin && yAxis < ymax)
		{
			ArrayList data = new ArrayList();

			int incrementedNumber = getUpdatedNumber(getButtonValue(buttonIndex), mouseButton);

			data.add(buttonIndex);
			data.add(incrementedNumber);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
			setButton(buttonIndex, incrementedNumber);
			SoundHandler.playSound("gui.button.press");
		}
	}

	public void setButton(int index, int number)
	{
		if(index == 0)
		{
			tileEntity.code.digitOne = number;
		}
		if(index == 1)
		{
			tileEntity.code.digitTwo = number;
		}
		if(index == 2)
		{
			tileEntity.code.digitThree = number;
		}
		if(index == 3)
		{
			tileEntity.code.digitFour = number;
		}
	}

	public int getButtonValue(int index)
	{
		if(index == 0)
		{
			return tileEntity.code.digitOne;
		}
		if(index == 1)
		{
			return tileEntity.code.digitTwo;
		}
		if(index == 2)
		{
			return tileEntity.code.digitThree;
		}
		if(index == 3)
		{
			return tileEntity.code.digitFour;
		}
		return 0;//should never happen
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiTeleporter.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width-xSize)/2;
		int guiHeight = (height-ySize)/2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
		int displayInt;

		displayInt = tileEntity.getScaledEnergyLevel(52);
		drawTexturedModalRect(guiWidth+165, guiHeight+17+52-displayInt, 176+13, 52-displayInt, 4, displayInt);

		displayInt = getYAxisForNumber(tileEntity.code.digitOne);
		drawTexturedModalRect(guiWidth+23, guiHeight+44, 176, displayInt, 13, 13);

		displayInt = getYAxisForNumber(tileEntity.code.digitTwo);
		drawTexturedModalRect(guiWidth+62, guiHeight+44, 176, displayInt, 13, 13);

		displayInt = getYAxisForNumber(tileEntity.code.digitThree);
		drawTexturedModalRect(guiWidth+101, guiHeight+44, 176, displayInt, 13, 13);

		displayInt = getYAxisForNumber(tileEntity.code.digitFour);
		drawTexturedModalRect(guiWidth+140, guiHeight+44, 176, displayInt, 13, 13);

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}

	public int getUpdatedNumber(int i, int mouseButton)
	{
		if(mouseButton == 1) //right click
		{
			return (i-1+10)%10; //add 10 to ensure postive result
		}
		else
		{
			return (i+1)%10;
		}
	}

	public int getYAxisForNumber(int i)
	{
		return i*13;
	}
}
