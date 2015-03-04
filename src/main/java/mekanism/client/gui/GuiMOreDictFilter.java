package mekanism.client.gui;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.content.miner.MOreDictFilter;
import mekanism.common.content.transporter.TransporterFilter;
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
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMOreDictFilter extends GuiMekanism
{
	public TileEntityDigitalMiner tileEntity;

	public boolean isNew = false;

	public MOreDictFilter origFilter;

	public MOreDictFilter filter = new MOreDictFilter();

	private GuiTextField oreDictText;

	public ItemStack renderStack;

	public int ticker = 0;

	public int stackSwitch = 0;

	public int stackIndex = 0;

	public List<ItemStack> iterStacks;

	public String status = EnumColor.DARK_GREEN + MekanismUtils.localize("gui.allOK");

	public GuiMOreDictFilter(EntityPlayer player, TileEntityDigitalMiner tentity, int index)
	{
		super(tentity, new ContainerFilter(player.inventory, tentity));
		tileEntity = tentity;

		origFilter = (MOreDictFilter)tileEntity.filters.get(index);
		filter = ((MOreDictFilter)tentity.filters.get(index)).clone();

		updateStackList(filter.oreDictName);
	}

	public GuiMOreDictFilter(EntityPlayer player, TileEntityDigitalMiner tentity)
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

		oreDictText = new GuiTextField(fontRendererObj, guiWidth + 35, guiHeight + 47, 95, 12);
		oreDictText.setMaxStringLength(TransporterFilter.MAX_LENGTH);
		oreDictText.setFocused(true);
	}

	@Override
	public void keyTyped(char c, int i)
	{
		if(!oreDictText.isFocused() || i == Keyboard.KEY_ESCAPE)
		{
			super.keyTyped(c, i);
		}

		if(oreDictText.isFocused() && i == Keyboard.KEY_RETURN)
		{
			setOreDictKey();
			return;
		}

		if(Character.isLetter(c) || Character.isDigit(c) || TransporterFilter.SPECIAL_CHARS.contains(c) || i == Keyboard.KEY_BACK || i == Keyboard.KEY_DELETE || i == Keyboard.KEY_LEFT || i == Keyboard.KEY_RIGHT)
		{
			oreDictText.textboxKeyTyped(c, i);
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);

		if(guibutton.id == 0)
		{
			if(!oreDictText.getText().isEmpty())
			{
				setOreDictKey();
			}

			if(filter.oreDictName != null && !filter.oreDictName.isEmpty())
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
			else {
				status = EnumColor.DARK_RED + MekanismUtils.localize("gui.oredictFilter.noKey");
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

		fontRendererObj.drawString((isNew ? MekanismUtils.localize("gui.new") : MekanismUtils.localize("gui.edit")) + " " + MekanismUtils.localize("gui.oredictFilter"), 43, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.status") + ": " + status, 35, 20, 0x00CD00);
		renderScaledText(MekanismUtils.localize("gui.key") + ": " + filter.oreDictName, 35, 32, 0x00CD00, 107);

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

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiMOreDictFilter.png"));
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
		
		if(xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59)
		{
			drawTexturedModalRect(guiWidth + 148, guiHeight + 45, 176 + 23, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 148, guiHeight + 45, 176 + 23, 14, 14, 14);
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

		oreDictText.drawTextBox();
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		oreDictText.updateCursorCounter();

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

		oreDictText.mouseClicked(mouseX, mouseY, button);

		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);

			if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), isNew ? 5 : 0, 0, 0));
			}

			if(xAxis >= 131 && xAxis <= 143 && yAxis >= 47 && yAxis <= 59)
			{
                SoundHandler.playSound("gui.button.press");
				setOreDictKey();
			}
			
			if(xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59)
			{
				SoundHandler.playSound("gui.button.press");
				filter.requireStack = !filter.requireStack;
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

	private void updateStackList(String oreName)
	{
		iterStacks = OreDictCache.getOreDictStacks(oreName, true);

		stackSwitch = 0;
		stackIndex = -1;
	}

	private void setOreDictKey()
	{
		String oreName = oreDictText.getText();

		if(oreName == null || oreName.isEmpty())
		{
			status = EnumColor.DARK_RED + MekanismUtils.localize("gui.oredictFilter.noKey");
			return;
		}
		else if(oreName.equals(filter.oreDictName))
		{
			status = EnumColor.DARK_RED + MekanismUtils.localize("gui.oredictFilter.sameKey");
			return;
		}

		updateStackList(oreName);

		filter.oreDictName = oreName;
		oreDictText.setText("");
	}
}
