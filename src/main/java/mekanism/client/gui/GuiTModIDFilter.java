package mekanism.client.gui;

import java.io.IOException;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismSounds;
import mekanism.common.OreDictCache;
import mekanism.common.content.transporter.TModIDFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class GuiTModIDFilter extends GuiMekanism
{
	public TileEntityLogisticalSorter tileEntity;

	public boolean isNew = false;

	public TModIDFilter origFilter;

	public TModIDFilter filter = new TModIDFilter();

	private GuiTextField modIDText;

	public ItemStack renderStack = ItemStack.EMPTY;

	public int ticker = 0;

	public int stackSwitch = 0;

	public int stackIndex = 0;

	public List<ItemStack> iterStacks;

	public String status = EnumColor.DARK_GREEN + LangUtils.localize("gui.allOK");

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
		buttonList.add(new GuiButton(0, guiWidth + 47, guiHeight + 62, 60, 20, LangUtils.localize("gui.save")));
		buttonList.add(new GuiButton(1, guiWidth + 109, guiHeight + 62, 60, 20, LangUtils.localize("gui.delete")));

		if(isNew)
		{
			buttonList.get(1).enabled = false;
		}

		modIDText = new GuiTextField(2, fontRenderer, guiWidth + 35, guiHeight + 47, 95, 12);
		modIDText.setMaxStringLength(TransporterFilter.MAX_LENGTH);
		modIDText.setFocused(true);
	}

	@Override
	public void keyTyped(char c, int i) throws IOException
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

		if(Character.isLetter(c) || Character.isDigit(c) || TransporterFilter.SPECIAL_CHARS.contains(c) || isTextboxKey(c, i))
		{
			modIDText.textboxKeyTyped(c, i);
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException
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
				status = EnumColor.DARK_RED + LangUtils.localize("gui.modIDFilter.noKey");
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

		fontRenderer.drawString((isNew ? LangUtils.localize("gui.new") : LangUtils.localize("gui.edit")) + " " + LangUtils.localize("gui.modIDFilter"), 43, 6, 0x404040);
		fontRenderer.drawString(LangUtils.localize("gui.status") + ": " + status, 35, 20, 0x00CD00);
		renderScaledText(LangUtils.localize("gui.id") + ": " + filter.modID, 35, 32, 0x00CD00, 107);
		fontRenderer.drawString(LangUtils.localize("gui." + (filter.allowDefault ? "on" : "off")), 24, 66, 0x404040);

		if(!renderStack.isEmpty())
		{
			try {
				GlStateManager.pushMatrix();
				RenderHelper.enableGUIStandardItemLighting();
				itemRender.renderItemAndEffectIntoGUI(renderStack, 12, 19);
				RenderHelper.disableStandardItemLighting();
				GlStateManager.popMatrix();
			} catch(Exception e) {}
		}

		if(filter.color != null)
		{
			GlStateManager.pushMatrix();
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);

			mc.getTextureManager().bindTexture(MekanismRenderer.getBlocksTexture());
			drawTexturedRectFromIcon(12, 44, MekanismRenderer.getColorIcon(filter.color), 16, 16);

			GL11.glDisable(GL11.GL_LIGHTING);
			GlStateManager.popMatrix();
		}
		
		if(xAxis >= 11 && xAxis <= 22 && yAxis >= 64 && yAxis <= 75)
		{
			drawHoveringText(LangUtils.localize("gui.allowDefault"), xAxis, yAxis);
		}

		if(xAxis >= 12 && xAxis <= 28 && yAxis >= 44 && yAxis <= 60)
		{
			if(filter.color != null)
			{
				drawHoveringText(filter.color.getColoredName(), xAxis, yAxis);
			}
			else {
				drawHoveringText(LangUtils.localize("gui.none"), xAxis, yAxis);
			}
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
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
		
		if(xAxis >= 11 && xAxis <= 22 && yAxis >= 64 && yAxis <= 75)
		{
			drawTexturedModalRect(guiWidth + 11, guiHeight + 64, 199, 0, 11, 11);
		}
		else {
			drawTexturedModalRect(guiWidth + 11, guiHeight + 64, 199, 11, 11, 11);
		}

		modIDText.drawTextBox();
		
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
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
			status = EnumColor.DARK_GREEN + LangUtils.localize("gui.allOK");
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
			renderStack = ItemStack.EMPTY;
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, button);

		modIDText.mouseClicked(mouseX, mouseY, button);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(button == 0)
		{
			if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
			{
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
				Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), isNew ? 4 : 0, 0, 0));
			}

			if(xAxis >= 131 && xAxis <= 143 && yAxis >= 47 && yAxis <= 59)
			{
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
				setModID();
			}
			
			if(xAxis >= 11 && xAxis <= 22 && yAxis >= 64 && yAxis <= 75)
			{
				SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
				filter.allowDefault = !filter.allowDefault;
			}
		}

		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && button == 0)
		{
			button = 2;
		}

		if(xAxis >= 12 && xAxis <= 28 && yAxis >= 44 && yAxis <= 60)
		{
			SoundHandler.playSound(MekanismSounds.DING);

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
			status = EnumColor.DARK_RED + LangUtils.localize("gui.modIDFilter.noID");
			return;
		}
		else if(modName.equals(filter.modID))
		{
			status = EnumColor.DARK_RED + LangUtils.localize("gui.modIDFilter.sameID");
			return;
		}

		updateStackList(modName);

		filter.modID = modName;
		modIDText.setText("");
	}
}
