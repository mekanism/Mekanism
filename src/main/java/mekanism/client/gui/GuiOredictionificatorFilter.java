package mekanism.client.gui;

import java.util.Arrays;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOredictionificatorFilter extends GuiMekanism
{
	public TileEntityOredictionificator tileEntity;
	
	public OredictionificatorFilter origFilter;

	public OredictionificatorFilter filter = new OredictionificatorFilter();
	
	public GuiTextField filterText;
	
	public boolean isNew;
	
	public ItemStack renderStack;
	
	public GuiOredictionificatorFilter(EntityPlayer player, TileEntityOredictionificator tentity, int index)
	{
		super(tentity, new ContainerFilter(player.inventory, tentity));
		tileEntity = tentity;
		
		origFilter = tileEntity.filters.get(index);
		filter = ((OredictionificatorFilter)tentity.filters.get(index)).clone();
		
		updateRenderStack();
	}
	
	public GuiOredictionificatorFilter(EntityPlayer player, TileEntityOredictionificator tentity)
	{
		super(tentity, new ContainerFilter(player.inventory, tentity));
		tileEntity = tentity;
		
		isNew = true;
	}
	
	public void setFilter()
	{
		String newFilter = filterText.getText();
		boolean has = false;
		
		for(String s : TileEntityOredictionificator.possibleFilters)
		{
			if(newFilter.startsWith(s))
			{
				has = true;
				break;
			}
		}
		
		if(has)
		{
			filter.filter = newFilter;
			filter.index = 0;
			filterText.setText("");
			
			updateRenderStack();
		}
		
		updateButtons();
	}
	
	public void updateButtons()
	{
		if(filter.filter != null && !filter.filter.isEmpty())
		{
			((GuiButton)buttonList.get(0)).enabled = true;
		}
		else {
			((GuiButton)buttonList.get(0)).enabled = false;
		}
		
		if(!isNew)
		{
			((GuiButton)buttonList.get(1)).enabled = true;
		}
		else {
			((GuiButton)buttonList.get(1)).enabled = false;
		}
	}
	
	@Override
	public void initGui()
	{
		super.initGui();

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 31, guiHeight + 62, 54, 20, MekanismUtils.localize("gui.save")));
		buttonList.add(new GuiButton(1, guiWidth + 89, guiHeight + 62, 54, 20, MekanismUtils.localize("gui.delete")));

		if(isNew)
		{
			((GuiButton)buttonList.get(1)).enabled = false;
		}

		filterText = new GuiTextField(fontRendererObj, guiWidth + 33, guiHeight + 48, 96, 12);
		filterText.setMaxStringLength(TileEntityOredictionificator.MAX_LENGTH);
		filterText.setFocused(true);
		
		updateButtons();
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		String text = (isNew ? MekanismUtils.localize("gui.new") : MekanismUtils.localize("gui.edit")) + " " + MekanismUtils.localize("gui.filter");
		fontRendererObj.drawString(text, (xSize/2)-(fontRendererObj.getStringWidth(text)/2), 6, 0x404040);
		
		fontRendererObj.drawString(MekanismUtils.localize("gui.index") + ": " + filter.index, 79, 23, 0x404040);
		
		if(filter.filter != null)
		{
			renderScaledText(filter.filter, 32, 38, 0x404040, 111);
		}

		if(renderStack != null)
		{
			try {
				GL11.glPushMatrix();
				GL11.glEnable(GL11.GL_LIGHTING);
				itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), renderStack, 45, 19);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glPopMatrix();
			} catch(Exception e) {}
		}
		
		if(xAxis >= 31 && xAxis <= 43 && yAxis >= 21 && yAxis <= 33)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.lastItem"), xAxis, yAxis);
		}
		
		if(xAxis >= 63 && xAxis <= 75 && yAxis >= 21 && yAxis <= 33)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.nextItem"), xAxis, yAxis);
		}
		
		if(xAxis >= 33 && xAxis <= 129 && yAxis >= 48 && yAxis <= 60)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.oreDictCompat"), xAxis, yAxis);
		}
		
		if(xAxis >= 45 && xAxis <= 61 && yAxis >= 19 && yAxis <= 35)
		{
			if(renderStack != null)
			{
				String name = MekanismUtils.getMod(renderStack);
				String extra = name.equals("null") ? "" : " (" + name + ")";
				
				drawCreativeTabHoveringText(renderStack.getDisplayName() + extra, xAxis, yAxis);
			}
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiOredictionificatorFilter.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
		{
			drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176 + 36, 0, 11, 11);
		}
		else {
			drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176 + 36, 11, 11, 11);
		}

		if(xAxis >= 31 && xAxis <= 43 && yAxis >= 21 && yAxis <= 33)
		{
			drawTexturedModalRect(guiWidth + 31, guiHeight + 21, 176 + 24, 0, 12, 12);
		}
		else {
			drawTexturedModalRect(guiWidth + 31, guiHeight + 21, 176 + 24, 12, 12, 12);
		}
		
		if(xAxis >= 63 && xAxis <= 75 && yAxis >= 21 && yAxis <= 33)
		{
			drawTexturedModalRect(guiWidth + 63, guiHeight + 21, 176 + 12, 0, 12, 12);
		}
		else {
			drawTexturedModalRect(guiWidth + 63, guiHeight + 21, 176 + 12, 12, 12, 12);
		}
		
		if(xAxis >= 130 && xAxis <= 142 && yAxis >= 48 && yAxis <= 60)
		{
			drawTexturedModalRect(guiWidth + 130, guiHeight + 48, 176, 0, 12, 12);
		}
		else {
			drawTexturedModalRect(guiWidth + 130, guiHeight + 48, 176, 12, 12, 12);
		}

		filterText.drawTextBox();
	}
	
	@Override
	public void keyTyped(char c, int i)
	{
		if(!filterText.isFocused() || i == Keyboard.KEY_ESCAPE)
		{
			super.keyTyped(c, i);
		}

		if(filterText.isFocused() && i == Keyboard.KEY_RETURN)
		{
			setFilter();
			return;
		}

		if(Character.isLetter(c) || Character.isDigit(c) || i == Keyboard.KEY_BACK || i == Keyboard.KEY_DELETE || i == Keyboard.KEY_LEFT || i == Keyboard.KEY_RIGHT)
		{
			filterText.textboxKeyTyped(c, i);
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);
		
		if(guibutton.id == 0)
		{
			if(!filterText.getText().isEmpty())
			{
				setFilter();
			}

			if(filter.filter != null && !filter.filter.isEmpty())
			{
				if(isNew)
				{
					Mekanism.packetHandler.sendToServer(new NewFilterMessage(Coord4D.get(tileEntity), filter));
				}
				else {
					Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), false, origFilter, filter));
				}

				Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 52));
			}
		}
		else if(guibutton.id == 1)
		{
			Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), true, origFilter, null));
			Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 52));
		}
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();

		filterText.updateCursorCounter();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		filterText.mouseClicked(mouseX, mouseY, button);
		
		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);

			if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 52));
			}
			
			if(xAxis >= 130 && xAxis <= 142 && yAxis >= 48 && yAxis <= 60)
			{
                SoundHandler.playSound("gui.button.press");
				setFilter();
			}

			if(xAxis >= 31 && xAxis <= 43 && yAxis >= 21 && yAxis <= 33)
			{
				SoundHandler.playSound("gui.button.press");
				
				if(filter.filter != null)
				{
					List<ItemStack> ores = OreDictionary.getOres(filter.filter);
					
					if(filter.index > 0)
					{
						filter.index--;
					}
					else {
						filter.index = ores.size()-1;
					}
					
					updateRenderStack();
				}
			}
			
			if(xAxis >= 63 && xAxis <= 75 && yAxis >= 21 && yAxis <= 33)
			{
				SoundHandler.playSound("gui.button.press");
				
				if(filter.filter != null)
				{
					List<ItemStack> ores = OreDictionary.getOres(filter.filter);
					
					if(filter.index < ores.size()-1)
					{
						filter.index++;
					}
					else {
						filter.index = 0;
					}
					
					updateRenderStack();
				}
			}
		}
	}
	
	public void updateRenderStack()
	{
		if(filter.filter == null || filter.filter.isEmpty())
		{
			renderStack = null;
			return;
		}
		
		List<ItemStack> stacks = OreDictionary.getOres(filter.filter);
		
		if(stacks.isEmpty())
		{
			renderStack = null;
			return;
		}
		
		if(stacks.size()-1 >= filter.index)
		{
			renderStack = stacks.get(filter.index).copy();
		}
		else {
			renderStack = null;
			return;
		}
	}
}
