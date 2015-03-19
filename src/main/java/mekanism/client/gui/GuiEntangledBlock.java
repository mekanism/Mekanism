package mekanism.client.gui;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityEntangledBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiEntangledBlock extends GuiMekanism
{
	public TileEntityEntangledBlock tileEntity;

	public GuiTextField frequencyField;

	public boolean isCreative;

	public GuiEntangledBlock(InventoryPlayer inventory, TileEntityEntangledBlock tentity)
	{
		super(tentity, new ContainerNull(inventory.player, tentity));
		tileEntity = tentity;
		isCreative = inventory.player.capabilities.isCreativeMode;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		fontRendererObj.drawString(tileEntity.getInventoryName(), 55, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);

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

		frequencyField.drawTextBox();
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		frequencyField.updateCursorCounter();
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		frequencyField.mouseClicked(mouseX, mouseY, button);
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

		frequencyField.textboxKeyTyped(c, i);
	}

	private void setFrequency()
	{
		if(!frequencyField.getText().isEmpty())
		{
			String toUse;

			toUse = frequencyField.getText();

			ArrayList data = new ArrayList();
			data.add(0);
			data.add(toUse);

			if(!toUse.startsWith("creative.") || isCreative)
			{
				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
			}

			frequencyField.setText("");
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		String prevFreq = frequencyField != null ? frequencyField.getText() : "";

		frequencyField = new GuiTextField(fontRendererObj, guiWidth + 75, guiHeight + 55, 96, 11);
		frequencyField.setText(prevFreq);
	}
}
