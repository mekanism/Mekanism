package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.miner.MModIDFilter;
import mekanism.common.network.PacketDigitalMinerGui.DigitalMinerGuiMessage;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMModIDFilter extends GuiMekanism
{
	public TileEntityDigitalMiner tileEntity;

	public boolean isNew = false;

	public MModIDFilter origFilter;

	public MModIDFilter filter = new MModIDFilter();

	private GuiTextField modIDText;

	public ItemStack renderStack;

	public int ticker = 0;

	public int stackSwitch = 0;

	public int stackIndex = 0;

	public List<ItemStack> iterStacks;

	public String status = EnumColor.DARK_GREEN + MekanismUtils.localize("gui.allOK");

	public GuiMModIDFilter(EntityPlayer player, TileEntityDigitalMiner tentity, int index)
	{
		super(new ContainerFilter(player.inventory, tentity));
		tileEntity = tentity;

		origFilter = (MModIDFilter)tileEntity.filters.get(index);
		filter = ((MModIDFilter)tentity.filters.get(index)).clone();

		updateStackList(filter.modID);
	}

	public GuiMModIDFilter(EntityPlayer player, TileEntityDigitalMiner tentity)
	{
		super(new ContainerFilter(player.inventory, tentity));
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
		modIDText.setMaxStringLength(12);
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

		if(Character.isLetter(c) || Character.isDigit(c) || c == '*' || i == Keyboard.KEY_BACK || i == Keyboard.KEY_DELETE || i == Keyboard.KEY_LEFT || i == Keyboard.KEY_RIGHT)
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

				Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), 0, 0, 0));
			}
			else {
				status = EnumColor.DARK_RED + MekanismUtils.localize("gui.modIDFilter.noKey");
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

		fontRendererObj.drawString((isNew ? MekanismUtils.localize("gui.new") : MekanismUtils.localize("gui.edit")) + " " + MekanismUtils.localize("gui.modIDFilter"), 43, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.status") + ": " + status, 35, 20, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.localize("gui.id") + ": " + filter.modID, 35, 32, 0x00CD00);

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

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiMModIDFilter.png"));
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
				setModID();
			}
		}
	}

	private void updateStackList(String modName)
	{
		if(iterStacks == null)
		{
			iterStacks = new ArrayList<ItemStack>();
		}
		else {
			iterStacks.clear();
		}

		for(String key : OreDictionary.getOreNames())
		{
			for(ItemStack stack : OreDictionary.getOres(key))
			{
				ItemStack toAdd = stack.copy();
				String s = MekanismUtils.getMod(toAdd);

				if(!iterStacks.contains(stack) && toAdd.getItem() instanceof ItemBlock)
				{
					if(modName.equals(s) || modName.equals("*"))
					{
						iterStacks.add(stack.copy());
					}
					else if(modName.endsWith("*") && !modName.startsWith("*"))
					{
						if(s.startsWith(modName.substring(0, modName.length()-1)))
						{
							iterStacks.add(stack.copy());
						}
					}
					else if(modName.startsWith("*") && !modName.endsWith("*"))
					{
						if(s.endsWith(modName.substring(1)))
						{
							iterStacks.add(stack.copy());
						}
					}
					else if(modName.startsWith("*") && modName.endsWith("*"))
					{
						if(s.contains(modName.substring(1, modName.length()-1)))
						{
							iterStacks.add(stack.copy());
						}
					}
				}
			}
		}

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
