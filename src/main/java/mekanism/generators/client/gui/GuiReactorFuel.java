package mekanism.generators.client.gui;

import java.io.IOException;
import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiGasGauge;
import mekanism.client.gui.element.GuiGauge.Type;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.gui.element.GuiHeatTab;
import mekanism.generators.client.gui.element.GuiStatTab;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiReactorFuel extends GuiMekanism
{
	public TileEntityReactorController tileEntity;

	public GuiTextField injectionRateField;

	public GuiReactorFuel(InventoryPlayer inventory, final TileEntityReactorController tentity)
	{
		super(new ContainerNull(inventory.player, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiEnergyInfo(() -> tileEntity.isFormed() ? ListUtils.asList(
                LangUtils.localize("gui.storing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
                LangUtils.localize("gui.producing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(false, true)) + "/t") : new ArrayList<>(), this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png")));
		guiElements.add(new GuiGasGauge(() -> tentity.deuteriumTank, Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 25, 64));
		guiElements.add(new GuiGasGauge(() -> tentity.fuelTank, Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 79, 50));
		guiElements.add(new GuiGasGauge(() -> tentity.tritiumTank, Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 133, 64));
		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.getActive() ? 1 : 0;
			}
		}, ProgressBar.SMALL_RIGHT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 45, 75));
		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.getActive() ? 1 : 0;
			}
		}, ProgressBar.SMALL_LEFT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 99, 75));
		guiElements.add(new GuiHeatTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png")));
		guiElements.add(new GuiStatTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png")));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		fontRenderer.drawString(tileEntity.getName(), 46, 6, 0x404040);
		String str = LangUtils.localize("gui.reactor.injectionRate") + ": " + (tileEntity.getReactor() == null ? "None" : tileEntity.getReactor().getInjectionRate());
		fontRenderer.drawString(str, (xSize / 2) - (fontRenderer.getStringWidth(str) / 2), 35, 0x404040);
		fontRenderer.drawString("Edit Rate" + ":", 50, 117, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20)
		{
			drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176, 14, 14, 14);
		}

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		injectionRateField.drawTextBox();
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		injectionRateField.updateCursorCounter();
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, button);

		injectionRateField.mouseClicked(mouseX, mouseY, button);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(button == 0)
		{
			if(xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20)
			{
				SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
				Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 1, 10));
			}
		}
	}

	@Override
	public void keyTyped(char c, int i) throws IOException
	{
		if(!injectionRateField.isFocused() || i == Keyboard.KEY_ESCAPE)
		{
			super.keyTyped(c, i);
		}

		if(i == Keyboard.KEY_RETURN)
		{
			if(injectionRateField.isFocused())
			{
				setInjection();
			}
		}

		if(Character.isDigit(c) || isTextboxKey(c, i))
		{
			injectionRateField.textboxKeyTyped(c, i);
		}
	}

	private void setInjection()
	{
		if(!injectionRateField.getText().isEmpty())
		{
			int toUse = Math.max(0, Integer.parseInt(injectionRateField.getText()));
			toUse -= toUse%2;
			
			ArrayList<Object> data = new ArrayList<>();
			data.add(0);
			data.add(toUse);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));

			injectionRateField.setText("");
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		String prevRad = injectionRateField != null ? injectionRateField.getText() : "";

		injectionRateField = new GuiTextField(0, fontRenderer, guiWidth + 98, guiHeight + 115, 26, 11);
		injectionRateField.setMaxStringLength(2);
		injectionRateField.setText(prevRad);
	}
}
