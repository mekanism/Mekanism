package mekanism.client.gui;

import mekanism.client.gui.element.GuiFluidGauge;
import mekanism.client.gui.element.GuiGauge;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiUpgradeTab;
import mekanism.client.gui.element.GuiFluidGauge.IFluidInfoHandler;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.common.inventory.container.ContainerElectricPump;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidTank;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiElectricPump extends GuiMekanism
{
	public TileEntityElectricPump tileEntity;

	public ResourceLocation guiLocation = MekanismUtils.getResource(ResourceType.GUI, "GuiElectricPump.png");

	public GuiElectricPump(InventoryPlayer inventory, TileEntityElectricPump tentity)
	{
		super(tentity, new ContainerElectricPump(inventory, tentity));
		tileEntity = tentity;

		guiElements.add(new GuiSlot(SlotType.NORMAL, this, guiLocation, 27, 19));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, guiLocation, 27, 50));
		guiElements.add(new GuiSlot(SlotType.POWER, this, guiLocation, 142, 34).with(SlotOverlay.POWER));
		guiElements.add(new GuiPowerBar(this, tileEntity, guiLocation, 164, 15));
		guiElements.add(new GuiFluidGauge(new IFluidInfoHandler() {
			@Override
			public FluidTank getTank()
			{
				return tileEntity.fluidTank;
			}
		}, GuiGauge.Type.STANDARD, this, guiLocation, 6, 13));
		guiElements.add(new GuiRedstoneControl(this, tileEntity, guiLocation));
		guiElements.add(new GuiUpgradeTab(this, tileEntity, guiLocation));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		fontRendererObj.drawString(tileEntity.getInventoryName(), 45, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
		fontRendererObj.drawString(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), 51, 26, 0x00CD00);
		
		String text = tileEntity.fluidTank.getFluid() != null ? LangUtils.localizeFluidStack(tileEntity.fluidTank.getFluid()) + ": " + tileEntity.fluidTank.getFluid().amount : MekanismUtils.localize("gui.noFluid");
		renderScaledText(text, 51, 35, 0x00CD00, 74);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(guiLocation);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
