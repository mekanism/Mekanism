package mekanism.client.gui;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.content.transporter.TMaterialFilter;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;

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
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class GuiTMaterialFilter extends GuiMekanism
{
	public TileEntityLogisticalSorter tileEntity;

	public boolean isNew = false;

	public TMaterialFilter origFilter;

	public TMaterialFilter filter = new TMaterialFilter();

	public String status = EnumColor.DARK_GREEN + MekanismUtils.localize("gui.allOK");

	public int ticker;

	public GuiTMaterialFilter(EntityPlayer player, TileEntityLogisticalSorter tentity, int index)
	{
		super(tentity, new ContainerFilter(player.inventory, tentity));
		tileEntity = tentity;

		origFilter = (TMaterialFilter)tileEntity.filters.get(index);
		filter = ((TMaterialFilter)tileEntity.filters.get(index)).clone();
	}

	public GuiTMaterialFilter(EntityPlayer player, TileEntityLogisticalSorter tentity)
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
			if(filter.materialItem != null)
			{
				if(isNew)
				{
					Mekanism.packetHandler.sendToServer(new NewFilterMessage(Coord4D.get(tileEntity), filter));
				}
				else {
					Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), false, origFilter, filter));
				}

				Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), 0, 0, 0));
			}
			else if(filter.materialItem == null)
			{
				status = EnumColor.DARK_RED + MekanismUtils.localize("gui.itemFilter.noItem");
				ticker = 20;
			}
		}
		else if(guibutton.id == 1)
		{
			Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), true, origFilter, null));
			Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), 0, 0, 0));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString((isNew ? MekanismUtils.localize("gui.new") : MekanismUtils.localize("gui.edit")) + " " + MekanismUtils.localize("gui.materialFilter"), 43, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.status") + ": " + status, 35, 20, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.localize("gui.materialFilter.details") + ":", 35, 32, 0x00CD00);

		if(filter.materialItem != null)
		{
			fontRendererObj.drawString(filter.materialItem.getDisplayName(), 35, 41, 0x00CD00);
		}

		if(filter.materialItem != null)
		{
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_LIGHTING);
			itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), filter.materialItem, 12, 19);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}
		
		if(filter.color != null)
		{
			GL11.glPushMatrix();
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);

			mc.getTextureManager().bindTexture(MekanismRenderer.getBlocksTexture());
			itemRender.renderIcon(12, 44, MekanismRenderer.getColorIcon(filter.color), 16, 16);

			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}

		if(xAxis >= 12 && xAxis <= 28 && yAxis >= 44 && yAxis <= 60)
		{
			if(filter.color != null)
			{
				drawCreativeTabHoveringText(filter.color.getName(), xAxis, yAxis);
			}
			else {
				drawCreativeTabHoveringText(MekanismUtils.localize("gui.none"), xAxis, yAxis);
			}
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

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiTMaterialFilter.png"));
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
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);
		
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(button == 0)
		{
			if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), isNew ? 4 : 0, 0, 0));
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
							filter.materialItem = stack.copy();
							filter.materialItem.stackSize = 1;
						}
					}
				}
				else if(stack == null && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					filter.materialItem = null;
				}

                SoundHandler.playSound("gui.button.press");
			}
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && button == 0)
		{
			button = 2;
		}

		if(xAxis >= 12 && xAxis <= 28 && yAxis >= 44 && yAxis <= 60)
		{
			SoundHandler.playSound("mekanism:etc.Ding");

			if(button == 0)
			{
				filter.color = TransporterUtils.increment(filter.color);
			}
			else if(button == 1)
			{
				filter.color = TransporterUtils.decrement(filter.color);
			}
			else if(button == 2)
			{
				filter.color = null;
			}
		}
	}
}
