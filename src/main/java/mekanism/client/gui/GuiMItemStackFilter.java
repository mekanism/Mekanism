package mekanism.client.gui;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.content.miner.MItemStackFilter;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.network.PacketDigitalMinerGui.DigitalMinerGuiMessage;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiMItemStackFilter extends GuiMekanism
{
	public TileEntityDigitalMiner tileEntity;

	public boolean isNew = false;

	public MItemStackFilter origFilter;

	public MItemStackFilter filter = new MItemStackFilter();

	public String status = EnumColor.DARK_GREEN + MekanismUtils.localize("gui.allOK");

	public int ticker;

	public GuiMItemStackFilter(EntityPlayer player, TileEntityDigitalMiner tentity, int index)
	{
		super(tentity, new ContainerFilter(player.inventory, tentity));
		tileEntity = tentity;

		origFilter = (MItemStackFilter)tileEntity.filters.get(index);
		filter = ((MItemStackFilter)tileEntity.filters.get(index)).clone();
	}

	public GuiMItemStackFilter(EntityPlayer player, TileEntityDigitalMiner tentity)
	{
		super(tentity, new ContainerFilter(player.inventory, tentity));
		tileEntity = tentity;

		isNew = true;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 27, guiHeight + 62, 60, 20, MekanismUtils.localize("gui.save")));
		buttonList.add(new GuiButton(1, guiWidth + 89, guiHeight + 62, 60, 20, MekanismUtils.localize("gui.delete")));

		if(isNew)
		{
			((GuiButton)buttonList.get(1)).enabled = false;
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);

		if(guibutton.id == 0)
		{
			if(filter.itemType != null)
			{
				if(isNew)
				{
					Mekanism.packetHandler.sendToServer(new NewFilterMessage(Coord4D.get(tileEntity), filter));
				}
				else {
					Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), false, origFilter, filter));
				}

				Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), 0, 0, 0));
			}
			else if(filter.itemType == null)
			{
				status = EnumColor.DARK_RED + MekanismUtils.localize("gui.itemFilter.noItem");
				ticker = 20;
			}
		}
		else if(guibutton.id == 1)
		{
			Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), true, origFilter, null));
			Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), 0, 0, 0));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString((isNew ? MekanismUtils.localize("gui.new") : MekanismUtils.localize("gui.edit")) + " " + MekanismUtils.localize("gui.itemFilter"), 43, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.status") + ": " + status, 35, 20, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.localize("gui.itemFilter.details") + ":", 35, 32, 0x00CD00);

		if(filter.itemType != null)
		{
			fontRendererObj.drawString(filter.itemType.getDisplayName(), 35, 41, 0x00CD00);
		}

		if(filter.itemType != null)
		{
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_LIGHTING);
			itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), filter.itemType, 12, 19);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}
		
		if(filter.replaceStack != null)
		{
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_LIGHTING);
			itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), filter.replaceStack, 149, 19);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}
		
		if(xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.digitalMiner.requireReplace") + ": " + LangUtils.transYesNo(filter.requireStack), xAxis, yAxis);
		}
		
		if(xAxis >= 15 && xAxis <= 29 && yAxis >= 45 && yAxis <= 59)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.digitalMiner.fuzzyMode") + ": " + LangUtils.transYesNo(filter.fuzzy), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		if(ticker > 0)
		{
			ticker--;
		}
		else {
			status = EnumColor.DARK_GREEN + MekanismUtils.localize("gui.allOK");
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiMItemStackFilter.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
		{
			drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176, 0, 11, 11);
		}
		else {
			drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176, 11, 11, 11);
		}
		
		if(xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59)
		{
			drawTexturedModalRect(guiWidth + 148, guiHeight + 45, 176 + 23, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 148, guiHeight + 45, 176 + 23, 14, 14, 14);
		}
		
		if(xAxis >= 15 && xAxis <= 29 && yAxis >= 45 && yAxis <= 59)
		{
			drawTexturedModalRect(guiWidth + 15, guiHeight + 45, 176 + 37, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 15, guiHeight + 45, 176 + 37, 14, 14, 14);
		}

		if(xAxis >= 12 && xAxis <= 28 && yAxis >= 19 && yAxis <= 35)
		{
			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);

			int x = guiWidth + 12;
			int y = guiHeight + 19;
			drawGradientRect(x, y, x + 16, y + 16, -2130706433, -2130706433);

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glPopMatrix();
		}
		
		if(xAxis >= 149 && xAxis <= 165 && yAxis >= 19 && yAxis <= 35)
		{
			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);

			int x = guiWidth + 149;
			int y = guiHeight + 19;
			drawGradientRect(x, y, x + 16, y + 16, -2130706433, -2130706433);

			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);

			if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), isNew ? 5 : 0, 0, 0));
			}
			
			if(xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59)
			{
				SoundHandler.playSound("gui.button.press");
				filter.requireStack = !filter.requireStack;
			}
			
			if(xAxis >= 15 && xAxis <= 29 && yAxis >= 45 && yAxis <= 59)
			{
				SoundHandler.playSound("gui.button.press");
				filter.fuzzy = !filter.fuzzy;
			}

			if(xAxis >= 12 && xAxis <= 28 && yAxis >= 19 && yAxis <= 35)
			{
				ItemStack stack = mc.thePlayer.inventory.getItemStack();

				if(stack != null && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					if(stack.getItem() instanceof ItemBlock)
					{
						if(Block.getBlockFromItem(stack.getItem()) != Blocks.bedrock)
						{
							filter.itemType = stack.copy();
							filter.itemType.stackSize = 1;
						}
					}
				}
				else if(stack == null && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					filter.itemType = null;
				}

                SoundHandler.playSound("gui.button.press");
			}
			
			if(xAxis >= 149 && xAxis <= 165 && yAxis >= 19 && yAxis <= 35)
			{
				boolean doNull = false;
				ItemStack stack = mc.thePlayer.inventory.getItemStack();
				ItemStack toUse = null;

				if(stack != null && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					if(stack.getItem() instanceof ItemBlock)
					{
						if(Block.getBlockFromItem(stack.getItem()) != Blocks.bedrock)
						{
							toUse = stack.copy();
							toUse.stackSize = 1;
						}
					}
				}
				else if(stack == null && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					doNull = true;
				}

				if(toUse != null || doNull)
				{
					filter.replaceStack = toUse;
				}

                SoundHandler.playSound("gui.button.press");
			}
		}
	}
}
