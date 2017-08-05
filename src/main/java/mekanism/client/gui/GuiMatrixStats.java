package mekanism.client.gui;

import mekanism.client.gui.element.GuiEnergyGauge;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiMatrixTab;
import mekanism.client.gui.element.GuiMatrixTab.MatrixTab;
import mekanism.client.gui.element.GuiRateBar;
import mekanism.client.gui.element.GuiRateBar.IRateInfoHandler;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.LangUtils;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiMatrixStats extends GuiMekanism
{
	public TileEntityInductionCasing tileEntity;

	public GuiMatrixStats(InventoryPlayer inventory, TileEntityInductionCasing tentity)
	{
		super(tentity, new ContainerNull(inventory.player, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiMatrixTab(this, tileEntity, MatrixTab.MAIN, 6, MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png")));
		guiElements.add(new GuiEnergyGauge(() -> tileEntity, GuiEnergyGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png"), 6, 13));
		guiElements.add(new GuiRateBar(this, new IRateInfoHandler()
		{
			@Override
			public String getTooltip()
			{
				return LangUtils.localize("gui.receiving") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.lastInput) + "/t";
			}
			
			@Override
			public double getLevel()
			{
				return tileEntity.structure.lastInput/tileEntity.structure.transferCap;
			}
		}, MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png"), 30, 13));
		guiElements.add(new GuiRateBar(this, new IRateInfoHandler()
		{
			@Override
			public String getTooltip()
			{
				return LangUtils.localize("gui.outputting") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.lastOutput) + "/t";
			}
			
			@Override
			public double getLevel()
			{
				return tileEntity.structure.lastOutput/tileEntity.structure.transferCap;
			}
		}, MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png"), 38, 13));
		guiElements.add(new GuiEnergyInfo(() -> ListUtils.asList(
                LangUtils.localize("gui.storing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
                LangUtils.localize("gui.input") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.lastInput) + "/t",
                LangUtils.localize("gui.output") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.lastOutput) + "/t"), this, MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png")));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		String stats = LangUtils.localize("gui.matrixStats");
		
		fontRenderer.drawString(stats, (xSize/2)-(fontRenderer.getStringWidth(stats)/2), 6, 0x404040);
		fontRenderer.drawString(LangUtils.localize("gui.input") + ":", 53, 26, 0x797979);
		fontRenderer.drawString(MekanismUtils.getEnergyDisplay(tileEntity.structure.lastInput) + "/" + MekanismUtils.getEnergyDisplay(tileEntity.structure.transferCap), 59, 35, 0x404040);
		fontRenderer.drawString(LangUtils.localize("gui.output") + ":", 53, 46, 0x797979);
		fontRenderer.drawString(MekanismUtils.getEnergyDisplay(tileEntity.structure.lastOutput) + "/" + MekanismUtils.getEnergyDisplay(tileEntity.structure.transferCap), 59, 55, 0x404040);
		
		fontRenderer.drawString(LangUtils.localize("gui.dimensions") + ":", 8, 82, 0x797979);
		fontRenderer.drawString(tileEntity.structure.volWidth + " x " + tileEntity.structure.volHeight + " x " + tileEntity.structure.volLength, 14, 91, 0x404040);
		fontRenderer.drawString(LangUtils.localize("gui.constituents") + ":", 8, 102, 0x797979);
		fontRenderer.drawString(tileEntity.clientCells + " " + LangUtils.localize("gui.cells"), 14, 111, 0x404040);
		fontRenderer.drawString(tileEntity.clientProviders + " " + LangUtils.localize("gui.providers"), 14, 120, 0x404040);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
		
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
