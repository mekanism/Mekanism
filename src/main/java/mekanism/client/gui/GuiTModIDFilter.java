package mekanism.client.gui;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.content.transporter.TModIDFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTModIDFilter extends GuiMekanism
{
	public TileEntityLogisticalSorter tileEntity;

	public boolean isNew = false;

	public TModIDFilter origFilter;

	public TModIDFilter filter = new TModIDFilter();

	private GuiTextField modIDText;

	public ItemStack renderStack;

	public int ticker = 0;

	public int stackSwitch = 0;

	public int stackIndex = 0;

	public List<ItemStack> iterStacks;

	public String status = EnumColor.DARK_GREEN + MekanismUtils.localize("gui.allOK");

	public GuiTModIDFilter(EntityPlayer player, TileEntityLogisticalSorter tentity, int index)
	{
		super(tentity, new ContainerFilter(player.inventory, tentity));
		tileEntity = tentity;

		origFilter = (TModIDFilter)tileEntity.filters.get(index);
		filter = ((TModIDFilter)tentity.filters.get(index)).clone();

		updateStackList(filter.modID);
	}

	public GuiTModIDFilter(EntityPlayer player, TileEntityLogisticalSorter tentity)
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

		modIDText = new GuiTextField(fontRendererObj, guiWidth + 35, guiHeight + 47, 95, 12);
		modIDText.setMaxStringLength(TransporterFilter.MAX_LENGTH);
		modIDText.setFocused(true);
	}

	@Override
	public void keyTyped(char c, int i)
	{
		if(!modIDText.isFocused() || i == Keyboard.KEY_ESCAPE)
		{
			super.keyTyped(c, i);
		}

		if(modIDText.isFocused() && i == Keyboard.KEY_RETURN)
		{
			setModID();
			return;
		}

		if(Character.isLetter(c) || Character.isDigit(c) || TransporterFilter.SPECIAL_CHARS.contains(c) || i == Keyboard.KEY_BACK || i == Keyboard.KEY_DELETE || i == Keyboard.KEY_LEFT || i == Keyboard.KEY_RIGHT)
		{
			modIDText.textboxKeyTyped(c, i);
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);

		if(guibutton.id == 0)
		{
			if(!modIDText.getText().isEmpty())
			{
				setModID();
			}

			if(filter.modID != null && !filter.modID.isEmpty())
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
			else {
				status = EnumColor.DARK_RED + MekanismUtils.localize("gui.modIDFilter.noKey");
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

		fontRendererObj.drawString((isNew ? MekanismUtils.localize("gui.new") : MekanismUtils.localize("gui.edit")) + " " + MekanismUtils.localize("gui.modIDFilter"), 43, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.status") + ": " + status, 35, 20, 0x00CD00);
		renderScaledText(MekanismUtils.localize("gui.id") + ": " + filter.modID, 35, 32, 0x00CD00, 107);

		if(renderStack != null)
		{
			try {
				GL11.glPushMatrix();
				GL11.glEnable(GL11.GL_LIGHTING);
				itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), renderStack, 12, 19);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glPopMatrix();
			} catch(Exception e) {}
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
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiTModIDFilter.png"));
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

		if(xAxis >= 131 && xAxis <= 143 && yAxis >= 47 && yAxis <= 59)
		{
			drawTexturedModalRect(guiWidth + 131, guiHeight + 47, 176 + 11, 0, 12, 12);
		}
		else {
			drawTexturedModalRect(guiWidth + 131, guiHeight + 47, 176 + 11, 12, 12, 12);
		}

		modIDText.drawTextBox();
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		modIDText.updateCursorCounter();

		if(ticker > 0)
		{
			ticker--;
		}
		else {
			status = EnumColor.DARK_GREEN + MekanismUtils.localize("gui.allOK");
		}

		if(stackSwitch > 0)
		{
			stackSwitch--;
		}

		if(stackSwitch == 0 && iterStacks != null && iterStacks.size() > 0)
		{
			stackSwitch = 20;

			if(stackIndex == -1 || stackIndex == iterStacks.size()-1)
			{
				stackIndex = 0;
			}
			else if(stackIndex < iterStacks.size()-1)
			{
				stackIndex++;
			}

			renderStack = iterStacks.get(stackIndex);
		}
		else if(iterStacks != null && iterStacks.size() == 0)
		{
			renderStack = null;
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		modIDText.mouseClicked(mouseX, mouseY, button);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(button == 0)
		{
			if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), isNew ? 4 : 0, 0, 0));
			}

			if(xAxis >= 131 && xAxis <= 143 && yAxis >= 47 && yAxis <= 59)
			{
                SoundHandler.playSound("gui.button.press");
				setModID();
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

	private void updateStackList(String modName)
	{
		iterStacks = OreDictCache.getModIDStacks(modName, false);

		stackSwitch = 0;
		stackIndex = -1;
	}

	private void setModID()
	{
		String modName = modIDText.getText();

		if(modName == null || modName.isEmpty())
		{
			status = EnumColor.DARK_RED + MekanismUtils.localize("gui.modIDFilter.noID");
			return;
		}
		else if(modName.equals(filter.modID))
		{
			status = EnumColor.DARK_RED + MekanismUtils.localize("gui.modIDFilter.sameID");
			return;
		}

		updateStackList(modName);

		filter.modID = modName;
		modIDText.setText("");
	}
}
