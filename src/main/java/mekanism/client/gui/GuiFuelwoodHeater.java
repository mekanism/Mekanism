package mekanism.client.gui;

import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.GuiSecurityTab;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerFuelwoodHeater;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import mekanism.common.util.LangUtils;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiFuelwoodHeater extends GuiMekanism
{
	public TileEntityFuelwoodHeater tileEntity;
	
	public GuiFuelwoodHeater(InventoryPlayer inventory, TileEntityFuelwoodHeater tentity)
	{
		super(tentity, new ContainerFuelwoodHeater(inventory, tentity));
		tileEntity = tentity;
		
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiFuelwoodHeater.png"), 14, 28));
		guiElements.add(new GuiSecurityTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiFuelwoodHeater.png")));
		guiElements.add(new GuiHeatInfo(() ->
        {
            TemperatureUnit unit = TemperatureUnit.values()[MekanismConfig.current().general.tempUnit.val().ordinal()];
            String environment = UnitDisplayUtils.getDisplayShort(tileEntity.lastEnvironmentLoss*unit.intervalSize, false, unit);
            return ListUtils.asList(LangUtils.localize("gui.dissipated") + ": " + environment + "/t");
        }, this, MekanismUtils.getResource(ResourceType.GUI, "GuiFuelwoodHeater.png")));
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		fontRenderer.drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
		fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
		
		renderScaledText(LangUtils.localize("gui.temp") + ": " + MekanismUtils.getTemperatureDisplay(tileEntity.temperature, TemperatureUnit.AMBIENT), 50, 25, 0x00CD00, 76);
		renderScaledText(LangUtils.localize("gui.fuel") + ": " + tileEntity.burnTime, 50, 41, 0x00CD00, 76);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiFuelwoodHeater.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
		
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		if(tileEntity.burnTime > 0)
		{
			int displayInt = tileEntity.burnTime*13 / tileEntity.maxBurnTime;
			drawTexturedModalRect(guiWidth + 143, guiHeight + 30 + 12 - displayInt, 176, 12 - displayInt, 14, displayInt + 1);
		}
		
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
