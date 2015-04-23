package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.util.ListUtils;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiUpgradeTab;
import mekanism.client.gui.element.GuiVisualsTab;
import mekanism.client.gui.element.GuiEnergyInfo.IInfoHandler;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.content.miner.ThreadMinerSearch.State;
import mekanism.common.inventory.container.ContainerDigitalMiner;
import mekanism.common.network.PacketDigitalMinerGui.DigitalMinerGuiMessage;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class GuiDigitalMiner extends GuiMekanism
{
	public TileEntityDigitalMiner tileEntity;

	public GuiButton startButton;
	public GuiButton stopButton;
	public GuiButton configButton;

	public ResourceLocation guiLocation = MekanismUtils.getResource(ResourceType.GUI, "GuiDigitalMiner.png");

	public GuiDigitalMiner(InventoryPlayer inventory, TileEntityDigitalMiner tentity)
	{
		super(tentity, new ContainerDigitalMiner(inventory, tentity));
		tileEntity = tentity;

		//guiLocation = MekanismUtils.getResource(ResourceType.GUI, "GuiDigitalMiner.png");

		guiElements.add(new GuiRedstoneControl(this, tileEntity, guiLocation, 176, 107));
		guiElements.add(new GuiUpgradeTab(this, tileEntity, guiLocation, 176, 5));
		guiElements.add(new GuiPowerBar(this, tileEntity, guiLocation, 7, 23));
		guiElements.add(new GuiVisualsTab(this, tileEntity, guiLocation, -26, 5));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
			@Override
			public List<String> getInfo()
			{
				String multiplier = MekanismUtils.getEnergyDisplay(tileEntity.getPerTick());
				return ListUtils.asList(MekanismUtils.localize("gui.using") + ": " + multiplier + "/t", MekanismUtils.localize("gui.needed") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
			}
		}, this, guiLocation,-26, 107));

		guiElements.add(new GuiSlot(SlotType.POWER, this, guiLocation, 13, 59).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, guiLocation, 13, 17));

		ySize+=64;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		buttonList.clear();
		startButton = new GuiButton(0, guiWidth + 49, guiHeight + 17, 60, 20, MekanismUtils.localize("gui.start"));

		if(tileEntity.searcher.state != State.IDLE && tileEntity.running)
		{
			startButton.enabled = false;
		}

		stopButton = new GuiButton(1, guiWidth + 49, guiHeight + 37, 60, 20, MekanismUtils.localize("gui.stop"));

		if(tileEntity.searcher.state == State.IDLE || !tileEntity.running)
		{
			stopButton.enabled = false;
		}

		configButton = new GuiButton(2, guiWidth + 49, guiHeight + 57, 60, 20, MekanismUtils.localize("gui.config"));

		if(tileEntity.searcher.state != State.IDLE)
		{
			configButton.enabled = false;
		}

		buttonList.add(startButton);
		buttonList.add(stopButton);
		buttonList.add(configButton);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);

		if(guibutton.id == 0)
		{
			ArrayList data = new ArrayList();
			data.add(3);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
		}
		else if(guibutton.id == 1)
		{
			ArrayList data = new ArrayList();
			data.add(4);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
		}
		else if(guibutton.id == 2)
		{
			Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), 0, 0, 0));
		}
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		if(tileEntity.searcher.state != State.IDLE && tileEntity.running)
		{
			startButton.enabled = false;
		}
		else {
			startButton.enabled = true;
		}

		if(tileEntity.searcher.state == State.IDLE || !tileEntity.running)
		{
			stopButton.enabled = false;
		}
		else {
			stopButton.enabled = true;
		}

		if(tileEntity.searcher.state != State.IDLE)
		{
			configButton.enabled = false;
		}
		else {
			configButton.enabled = true;
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = mouseX - guiLeft;
		int yAxis = mouseY - guiTop;

		fontRendererObj.drawString(tileEntity.getInventoryName(), 8, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);

		fontRendererObj.drawString(tileEntity.running ? MekanismUtils.localize("gui.digitalMiner.running") : MekanismUtils.localize("gui.idle"), 113, 10, 0x00CD00);
		fontRendererObj.drawString(tileEntity.searcher.state.desc, 113, 19, 0x00CD00);

		fontRendererObj.drawString(MekanismUtils.localize("gui.eject") + ": " + MekanismUtils.localize("gui." + (tileEntity.doEject ? "on" : "off")), 113, 30, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.localize("gui.digitalMiner.pull") + ": " + MekanismUtils.localize("gui." + (tileEntity.doPull ? "on" : "off")), 113, 39, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.localize("gui.digitalMiner.silk") + ": " + MekanismUtils.localize("gui." + (tileEntity.silkTouch ? "on" : "off")), 113, 48, 0x00CD00);

		fontRendererObj.drawString(MekanismUtils.localize("gui.digitalMiner.toMine") + ":", 113, 59, 0x00CD00);
		fontRendererObj.drawString("" + tileEntity.clientToMine, 113, 68, 0x00CD00);

		if(tileEntity.missingStack != null)
		{
			GL11.glPushMatrix();
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);

			mc.getTextureManager().bindTexture(MekanismRenderer.getBlocksTexture());
			
			itemRender.renderIcon(14, 18, MekanismRenderer.getColorIcon(EnumColor.DARK_RED), 16, 16);
			itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), tileEntity.missingStack, 14, 18);
			
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}
		else {
			mc.getTextureManager().bindTexture(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSlot.png"));
			drawTexturedModalRect(13, 17, SlotOverlay.CHECK.textureX, SlotOverlay.CHECK.textureY, 18, 18);
		}

		if(xAxis >= 164 && xAxis <= 168 && yAxis >= 25 && yAxis <= 77)
		{
			drawCreativeTabHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), xAxis, yAxis);
		}

		// Auto eject
		if(xAxis >= 33 && xAxis <= 46 && yAxis >= 32 && yAxis <= 45)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.autoEject"), xAxis, yAxis);
		}

		// Auto pull
		if(xAxis >= 33 && xAxis <= 46 && yAxis >= 48 && yAxis <= 61)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.digitalMiner.autoPull"), xAxis, yAxis);
		}

		if(xAxis >= 13 && xAxis <= 30 && yAxis >= 17 && yAxis <= 34)
		{
			if(tileEntity.missingStack != null)
			{
				drawCreativeTabHoveringText(MekanismUtils.localize("gui.digitalMiner.missingBlock"), xAxis, yAxis);
			}
			else {
				drawCreativeTabHoveringText(MekanismUtils.localize("gui.well"), xAxis, yAxis);
			}
		}

		// Reset
		if(xAxis >= 33 && xAxis <= 46 && yAxis >= 17 && yAxis <= 30)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.digitalMiner.reset"), xAxis, yAxis);
		}

		// Silk touch
		if(xAxis >= 33 && xAxis <= 46 && yAxis >= 63 && yAxis <= 76)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.digitalMiner.silkTouch"), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiDigitalMiner.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int xAxis = mouseX - guiLeft;
		int yAxis = mouseY - guiTop;

		int displayInt;

		displayInt = tileEntity.getScaledEnergyLevel(52);
		drawTexturedModalRect(guiLeft + 164, guiTop + 25 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);

		// Reset
		boolean mouseOver = xAxis >= 33 && xAxis <= 46 && yAxis >= 17 && yAxis <= 30;
		drawTexturedModalRect(guiLeft + 33, guiTop + 17, 180, mouseOver?  0: 14, 14, 14);

		// Auto eject
		mouseOver = xAxis >= 33 && xAxis <= 46 && yAxis >= 32 && yAxis <= 45;
		drawTexturedModalRect(guiLeft + 33, guiTop + 32, tileEntity.doEject ? 194 : 180, mouseOver?  28: 42, 14, 14);

		// Auto pull
		mouseOver = xAxis >= 33 && xAxis <= 46 && yAxis >= 48 && yAxis <= 61;
		drawTexturedModalRect(guiLeft + 33, guiTop + 48, tileEntity.doPull ? 194 : 180, mouseOver?  56: 70, 14, 14);

		// Silk touch
		mouseOver = xAxis >= 33 && xAxis <= 46 && yAxis >= 63 && yAxis <= 76;
		drawTexturedModalRect(guiLeft + 33, guiTop + 63, tileEntity.silkTouch ? 194 : 180, mouseOver?  84: 98, 14, 14);

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		if(button == 0)
		{
			int xAxis = mouseX - guiLeft;
			int yAxis = mouseY - guiTop;

			// Auto eject
			if(xAxis >= 33 && xAxis <= 46 && yAxis >= 32 && yAxis <= 45)
			{
                SoundHandler.playSound("gui.button.press");

				ArrayList data = new ArrayList();
				data.add(0);

				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
			}

			// Auto pull
			if(xAxis >= 33 && xAxis <= 46 && yAxis >= 48 && yAxis <= 61)
			{
                SoundHandler.playSound("gui.button.press");

				ArrayList data = new ArrayList();
				data.add(1);

				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
			}

			// Reset
			if(xAxis >= 33 && xAxis <= 46 && yAxis >= 17 && yAxis <= 30)
			{
                SoundHandler.playSound("gui.button.press");

				ArrayList data = new ArrayList();
				data.add(5);

				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
			}

			// Silk touch
			if(xAxis >= 33 && xAxis <= 46 && yAxis >= 63 && yAxis <= 76)
			{
                SoundHandler.playSound("gui.button.press");

				ArrayList data = new ArrayList();
				data.add(9);

				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
			}
		}
	}
}
