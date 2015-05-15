package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.util.ListUtils;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiEnergyInfo.IInfoHandler;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSideConfigurationTab;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.GuiTransporterConfigTab;
import mekanism.client.gui.element.GuiUpgradeTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerMetallurgicInfuser;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMetallurgicInfuser extends GuiMekanism
{
	// Buttons
	int BUTTON_DRAIN = 0;

	public TileEntityMetallurgicInfuser tileEntity;

	public GuiMetallurgicInfuser(InventoryPlayer inventory, TileEntityMetallurgicInfuser tentity)
	{
		super(tentity, new ContainerMetallurgicInfuser(inventory, tentity));
		tileEntity = tentity;
		ySize = 185;

		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"),176,63));
		guiElements.add(new GuiUpgradeTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"),176,5));
		guiElements.add(new GuiSideConfigurationTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"),-26,5));
		guiElements.add(new GuiTransporterConfigTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"),-26,34));
		guiElements.add(new GuiPowerBar(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"), 7, 35));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
			@Override
			public List<String> getInfo()
			{
				String multiplier = MekanismUtils.getEnergyDisplay(tileEntity.energyPerTick);
				return ListUtils.asList(MekanismUtils.localize("gui.using") + ": " + multiplier + "/t", MekanismUtils.localize("gui.needed") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
			}
		}, this, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"),-26, 63));

		guiElements.add(new GuiSlot(SlotType.EXTRA, this, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"), 19, 17));
		guiElements.add(new GuiSlot(SlotType.INPUT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"), 52, 44));
		guiElements.add(new GuiSlot(SlotType.POWER, this, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"), 13, 71).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.OUTPUT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"), 124, 44));

		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.getScaledProgress();
			}
		}, ProgressBar.INFUSE, this, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"), 70, 45));
	}

	@Override
	public void initGui()
	{
		super.initGui();

		// Add buttons to gui
		buttonList.clear();
		buttonList.add(new GuiButton(BUTTON_DRAIN, guiLeft + 37, guiTop + 69, 32, 20, MekanismUtils.localize("gui.drain")));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);

		if(guibutton.id == BUTTON_DRAIN)
		{
			ArrayList data = new ArrayList();
			data.add(0);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
			SoundHandler.playSound("gui.button.press");
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = mouseX - guiLeft;
		int yAxis = mouseY - guiTop;

		fontRendererObj.drawString(tileEntity.getInventoryName(), 45, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);

		if(xAxis >= 31 && xAxis <= 36 && yAxis >= 35 && yAxis <= 88)
		{
			drawCreativeTabHoveringText(tileEntity.infuseStored.type != null ? tileEntity.infuseStored.type.getLocalizedName() + ": " + tileEntity.infuseStored.amount : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(tileEntity.infuseStored.type != null)
		{
			int displayInt = tileEntity.getScaledInfuseLevel(52);
			mc.renderEngine.bindTexture(tileEntity.infuseStored.type.texture);
			drawTexturedModalRect(guiLeft + 32, guiTop + 36 + 52 - displayInt, tileEntity.infuseStored.type.texX, tileEntity.infuseStored.type.texY + 52 - displayInt, 4, displayInt);
		}

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}

	@Override
	protected void mouseClicked(int x, int y, int button)
	{
		super.mouseClicked(x, y, button);

		if(button == 0)
		{
			int xAxis = (x - (width - xSize) / 2);
			int yAxis = (y - (height - ySize) / 2);

			if(xAxis > 148 && xAxis < 168 && yAxis > 73 && yAxis < 82)
			{
				ArrayList data = new ArrayList();
				data.add(0);

				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
				SoundHandler.playSound("gui.button.press");
			}
		}
	}
}
