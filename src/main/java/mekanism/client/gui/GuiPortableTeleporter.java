package mekanism.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.network.PacketDigitUpdate.DigitUpdateMessage;
import mekanism.common.network.PacketPortableTeleport.PortableTeleportMessage;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiPortableTeleporter extends GuiScreen
{
	public EntityPlayer entityPlayer;
	public ItemStack itemStack;

	public int xSize = 176;
	public int ySize = 166;

	public GuiPortableTeleporter(EntityPlayer player, ItemStack itemstack)
	{
		entityPlayer = player;
		itemStack = itemstack;
	}

	@Override
	public void initGui()
	{
		buttonList.clear();
		buttonList.add(new GuiButton(0, 173, 105, 80, 20, MekanismUtils.localize("gui.teleport")));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTick)
	{
		if(mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemPortableTeleporter)
		{
			itemStack = mc.thePlayer.getCurrentEquippedItem();
		}

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiPortableTeleporter.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width-xSize)/2;
		int guiHeight = (height-ySize)/2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int displayInt;

		displayInt = getYAxisForNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 0));
		drawTexturedModalRect(guiWidth+23, guiHeight+44, 176, displayInt, 13, 13);

		displayInt = getYAxisForNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 1));
		drawTexturedModalRect(guiWidth+62, guiHeight+44, 176, displayInt, 13, 13);

		displayInt = getYAxisForNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 2));
		drawTexturedModalRect(guiWidth+101, guiHeight+44, 176, displayInt, 13, 13);

		displayInt = getYAxisForNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 3));
		drawTexturedModalRect(guiWidth+140, guiHeight+44, 176, displayInt, 13, 13);

		ItemPortableTeleporter item = (ItemPortableTeleporter)itemStack.getItem();

		((GuiButton)buttonList.get(0)).xPosition = guiWidth+48;
		((GuiButton)buttonList.get(0)).yPosition = guiHeight+68;

		fontRendererObj.drawString(MekanismUtils.localize("gui.portableTeleporter"), guiWidth+39, guiHeight+6, 0x404040);
		fontRendererObj.drawString(item.getStatusAsString(item.getStatus(itemStack)), guiWidth+53, guiHeight+19, 0x00CD00);

		super.drawScreen(mouseX, mouseY, partialTick);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if(guibutton.id == 0)
		{
			Mekanism.packetHandler.sendToServer(new PortableTeleportMessage());
			mc.setIngameFocus();
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		int xAxis = (mouseX-(width-xSize)/2);
		int yAxis = (mouseY-(height-ySize)/2);

		handleButtonClick(xAxis, yAxis, button, 0, 23, 37, 44, 58);
		handleButtonClick(xAxis, yAxis, button, 1, 62, 76, 44, 58);
		handleButtonClick(xAxis, yAxis, button, 2, 101, 115, 44, 58);
		handleButtonClick(xAxis, yAxis, button, 3, 140, 154, 44, 58);
	}

	private void handleButtonClick(int xAxis, int yAxis, int mouseButton, int clickedButtonIndex, int xmin, int xmax, int ymin, int ymax)
	{
		if(xAxis > xmin && xAxis < xmax && yAxis > ymin && yAxis < ymax)
		{
			int currentDigit = ((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, clickedButtonIndex);
			int updatedDigit = getUpdatedNumber(currentDigit, mouseButton);
			Mekanism.packetHandler.sendToServer(new DigitUpdateMessage(clickedButtonIndex, updatedDigit));
			((ItemPortableTeleporter)itemStack.getItem()).setDigit(itemStack, clickedButtonIndex, updatedDigit);
			SoundHandler.playSound("gui.button.press");
		}
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

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
}
