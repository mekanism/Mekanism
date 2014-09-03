package mekanism.client.gui;

import java.util.List;

import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerRobitRepair;
import mekanism.common.network.PacketRobit.RobitMessage;
import mekanism.common.network.PacketRobit.RobitPacketType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import org.apache.commons.io.Charsets;

@SideOnly(Side.CLIENT)
public class GuiRobitRepair extends GuiMekanism implements ICrafting
{
	public int entityId;
	private ContainerRepair repairContainer;
	private GuiTextField itemNameField;
	private InventoryPlayer playerInventory;

	public GuiRobitRepair(InventoryPlayer inventory, World world, int id)
	{
		super(new ContainerRobitRepair(inventory, world));
		xSize += 25;
		entityId = id;
		playerInventory = inventory;
		repairContainer = (ContainerRobitRepair)inventorySlots;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		Keyboard.enableRepeatEvents(true);

		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;

		itemNameField = new GuiTextField(fontRendererObj, i + 62, j + 24, 103, 12);
		itemNameField.setTextColor(-1);
		itemNameField.setDisabledTextColour(-1);
		itemNameField.setEnableBackgroundDrawing(false);
		itemNameField.setMaxStringLength(30);
		inventorySlots.removeCraftingFromCrafters(this);
		inventorySlots.addCraftingToCrafters(this);
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
		inventorySlots.removeCraftingFromCrafters(this);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		GL11.glDisable(GL11.GL_LIGHTING);
		fontRendererObj.drawString(MekanismUtils.localize("container.repair"), 60, 6, 4210752);

		if(repairContainer.maximumCost > 0)
		{
			int k = 8453920;
			boolean flag = true;
			String s = StatCollector.translateToLocalFormatted("container.repair.cost", new Object[] {Integer.valueOf(repairContainer.maximumCost)});

			if(repairContainer.maximumCost >= 40 && !mc.thePlayer.capabilities.isCreativeMode)
			{
				s = MekanismUtils.localize("container.repair.expensive");
				k = 16736352;
			}
			else if(!repairContainer.getSlot(2).getHasStack())
			{
				flag = false;
			}
			else if(!repairContainer.getSlot(2).canTakeStack(playerInventory.player))
			{
				k = 16736352;
			}

			if(flag)
			{
				int l = -16777216 | (k & 16579836) >> 2 | k & -16777216;
				int i1 = (xSize - 25) - 8 - fontRendererObj.getStringWidth(s);
				byte b0 = 67;

				if(fontRendererObj.getUnicodeFlag())
				{
					drawRect(i1 - 3, b0 - 2, (xSize - 25) - 7, b0 + 10, -16777216);
					drawRect(i1 - 2, b0 - 1, (xSize - 25) - 8, b0 + 9, -12895429);
				}
				else {
					fontRendererObj.drawString(s, i1, b0 + 1, l);
					fontRendererObj.drawString(s, i1 + 1, b0, l);
					fontRendererObj.drawString(s, i1 + 1, b0 + 1, l);
				}

				fontRendererObj.drawString(s, i1, b0, k);
			}
		}

		GL11.glEnable(GL11.GL_LIGHTING);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void keyTyped(char c, int i)
	{
		if(itemNameField.textboxKeyTyped(c, i))
		{
			repairContainer.updateItemName(itemNameField.getText());
			mc.thePlayer.sendQueue.addToSendQueue(new C17PacketCustomPayload("MC|ItemName", itemNameField.getText().getBytes()));
		}
		else {
			super.keyTyped(c, i);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		itemNameField.mouseClicked(mouseX, mouseY, button);

		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);

			if(xAxis >= 179 && xAxis <= 197 && yAxis >= 10 && yAxis <= 28)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.GUI, 0, entityId, null));
				mc.thePlayer.openGui(Mekanism.instance, 21, mc.theWorld, entityId, 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 30 && yAxis <= 48)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.GUI, 1, entityId, null));
				mc.thePlayer.openGui(Mekanism.instance, 22, mc.theWorld, entityId, 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 50 && yAxis <= 68)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.GUI, 2, entityId, null));
				mc.thePlayer.openGui(Mekanism.instance, 23, mc.theWorld, entityId, 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 70 && yAxis <= 88)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.GUI, 3, entityId, null));
				mc.thePlayer.openGui(Mekanism.instance, 24, mc.theWorld, entityId, 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 90 && yAxis <= 108)
			{
                SoundHandler.playSound("gui.button.press");
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTick)
	{
		super.drawScreen(mouseX, mouseY, partialTick);

		GL11.glDisable(GL11.GL_LIGHTING);
		itemNameField.drawTextBox();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiRobitRepair.png"));
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 10 && yAxis <= 28)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 10, 176 + 25, 0, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 10, 176 + 25, 18, 18, 18);
		}

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 30 && yAxis <= 48)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 30, 176 + 25, 36, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 30, 176 + 25, 54, 18, 18);
		}

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 50 && yAxis <= 68)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 50, 176 + 25, 72, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 50, 176 + 25, 90, 18, 18);
		}

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 70 && yAxis <= 88)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 70, 176 + 25, 108, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 70, 176 + 25, 126, 18, 18);
		}

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 90 && yAxis <= 108)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 90, 176 + 25, 144, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 90, 176 + 25, 162, 18, 18);
		}

		drawTexturedModalRect(guiWidth + 59, guiHeight + 20, 0, ySize + (repairContainer.getSlot(0).getHasStack() ? 0 : 16), 110, 16);

		if((repairContainer.getSlot(0).getHasStack() || repairContainer.getSlot(1).getHasStack()) && !repairContainer.getSlot(2).getHasStack())
		{
			drawTexturedModalRect(guiWidth + 99, guiHeight + 45, xSize + 18, 36, 28, 21);
		}
	}

	@Override
	public void sendContainerAndContentsToPlayer(Container container, List list)
	{
		sendSlotContents(container, 0, container.getSlot(0).getStack());
	}

	@Override
	public void sendSlotContents(Container container, int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			itemNameField.setText(itemstack == null ? "" : itemstack.getDisplayName());
			itemNameField.setEnabled(itemstack != null);

			if(itemstack != null)
			{
				repairContainer.updateItemName(itemNameField.getText());
				mc.thePlayer.sendQueue.addToSendQueue(new C17PacketCustomPayload("MC|ItemName", itemNameField.getText().getBytes(Charsets.UTF_8)));
			}
		}
	}

	@Override
	public void sendProgressBarUpdate(Container par1Container, int par2, int par3) {}
}
