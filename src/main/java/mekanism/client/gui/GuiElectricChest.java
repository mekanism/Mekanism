package mekanism.client.gui;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.energy.IEnergizedItem;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.IElectricChest;
import mekanism.common.inventory.container.ContainerElectricChest;
import mekanism.common.network.PacketElectricChest.ElectricChestMessage;
import mekanism.common.network.PacketElectricChest.ElectricChestPacketType;
import mekanism.common.tile.TileEntityElectricChest;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiElectricChest extends GuiMekanism
{
	public TileEntityElectricChest tileEntity;
	public IInventory itemInventory;
	public boolean isBlock;

	public GuiElectricChest(InventoryPlayer inventory, TileEntityElectricChest tentity)
	{
		super(tentity, new ContainerElectricChest(inventory, tentity, null, true));

		xSize+=26;
		ySize+=64;
		tileEntity = tentity;
		isBlock = true;
	}

	public GuiElectricChest(InventoryPlayer inventory, IInventory inv)
	{
		super(new ContainerElectricChest(inventory, null, inv, false));

		xSize+=26;
		ySize+=64;
		itemInventory = inv;
		isBlock = false;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 93, guiHeight + 4, 76, 20, MekanismUtils.localize("gui.electricChest.editPassword")));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if(guibutton.id == 0)
		{
			if(isBlock)
			{
				mc.thePlayer.openGui(Mekanism.instance, 20, mc.theWorld, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
			}
			else {
				FMLClientHandler.instance().displayGuiScreen(mc.thePlayer, new GuiPasswordModify(mc.thePlayer.getCurrentEquippedItem()));
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(MekanismUtils.localize("tile.MachineBlock.ElectricChest.name"), 8, 6, 0x404040);
		fontRendererObj.drawString(getLocked() ? EnumColor.DARK_RED + "Locked" : EnumColor.BRIGHT_GREEN + "Unlocked", 97, 137, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);

		if(xAxis >= 180 && xAxis <= 184 && yAxis >= 32 && yAxis <= 84)
		{
			drawCreativeTabHoveringText(MekanismUtils.getEnergyDisplay(getEnergy()), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);

			if(xAxis >= 179 && xAxis <= 197 && yAxis >= 88 && yAxis <= 106)
			{
                SoundHandler.playSound("gui.button.press");

				if(isBlock)
				{
					Mekanism.packetHandler.sendToServer(new ElectricChestMessage(ElectricChestPacketType.LOCK, !getLocked(), true, 0, 0, null, Coord4D.get(tileEntity)));
				}
				else {
					Mekanism.packetHandler.sendToServer(new ElectricChestMessage(ElectricChestPacketType.LOCK, !getLocked(), false, 0, 0, null, null));

					ItemStack stack = mc.thePlayer.getCurrentEquippedItem();
					((IElectricChest)stack.getItem()).setLocked(stack, !getLocked());
				}
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiElectricChest.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 88 && yAxis <= 106)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 88, 176 + 26, 52, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 88, 176 + 26, 70, 18, 18);
		}

		int displayInt = getScale();
		drawTexturedModalRect(guiWidth + 180, guiHeight + 32 + 52 - displayInt, 176 + 26, 52 - displayInt, 4, displayInt);
	}

	public boolean getLocked()
	{
		if(isBlock)
		{
			return tileEntity.locked;
		}
		else {
			ItemStack stack = mc.thePlayer.getCurrentEquippedItem();
			return ((IElectricChest)stack.getItem()).getLocked(stack);
		}
	}

	public int getScale()
	{
		if(isBlock)
		{
			return tileEntity.getScaledEnergyLevel(52);
		}
		else {
			ItemStack stack = mc.thePlayer.getCurrentEquippedItem();
			return (int)(((IEnergizedItem)stack.getItem()).getEnergy(stack)*52 / ((IEnergizedItem)stack.getItem()).getMaxEnergy(stack));
		}
	}

	public double getEnergy()
	{
		if(isBlock)
		{
			return tileEntity.getEnergy();
		}
		else {
			ItemStack stack = mc.thePlayer.getCurrentEquippedItem();
			return ((IEnergizedItem)stack.getItem()).getEnergy(stack);
		}
	}
}
