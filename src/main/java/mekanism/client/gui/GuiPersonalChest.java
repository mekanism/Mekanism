package mekanism.client.gui;

import mekanism.client.gui.element.GuiSecurityTab;
import mekanism.common.inventory.InventoryPersonalChest;
import mekanism.common.inventory.container.ContainerPersonalChest;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiPersonalChest extends GuiMekanism
{
	public TileEntityPersonalChest tileEntity;

	public GuiPersonalChest(InventoryPlayer inventory, TileEntityPersonalChest tentity)
	{
		super(tentity, new ContainerPersonalChest(inventory, tentity, null, true));

		xSize+=26;
		ySize+=64;
		tileEntity = tentity;

		guiElements.add(new GuiSecurityTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiPersonalChest.png")));
	}

	public GuiPersonalChest(InventoryPlayer inventory, InventoryPersonalChest inv)
	{
		super(new ContainerPersonalChest(inventory, null, inv, false));

		xSize+=26;
		ySize+=64;
		
		guiElements.add(new GuiSecurityTab(this, MekanismUtils.getResource(ResourceType.GUI, "GuiPersonalChest.png"), inv.currentHand));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		fontRenderer.drawString(LangUtils.localize("tile.MachineBlock.PersonalChest.name"), 8, 6, 0x404040);
		fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiPersonalChest.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
