package mekanism.client.gui.element;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class GuiUpgradeTab extends GuiElement
{
	TileEntity tileEntity;

	public GuiUpgradeTab(IGuiWrapper gui, TileEntity tile, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiUpgradeTab.png"), gui, def);

		lmntLeft = 176;
		lmntTop = 6;

		tileEntity = tile;
	}

	public GuiUpgradeTab(IGuiWrapper gui, TileEntity tile, ResourceLocation def, int guiLeft, int guiTop)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiUpgradeTab.png"), gui, def);

		lmntLeft = guiLeft;
		lmntTop = guiTop;

		tileEntity = tile;
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiLeft, int guiTop)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiLeft + lmntLeft, guiTop + lmntTop, 0, 0, lmntWidth, lmntHeight);
		
		boolean mouseOver = xAxis >= lmntLeft + 3 && xAxis <= lmntLeft + 21 && yAxis >= lmntTop + 4 && yAxis <= lmntTop + 22;

		guiObj.drawTexturedRect(guiLeft + lmntLeft + 3, guiTop + lmntTop + 4, 26, mouseOver?  0: 18, 18, 18);

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		if(xAxis >= lmntLeft + 3 && xAxis <= lmntLeft + 21 && yAxis >= lmntTop + 4 && yAxis <= lmntTop + 22)
		{
			displayTooltip(LangUtils.localize("gui.upgrades"), xAxis, yAxis);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button)
	{
		if(button == 0)
		{
			if(xAxis >= lmntLeft + 3 && xAxis <= lmntLeft + 21 && yAxis >= lmntTop + 4 && yAxis <= lmntTop + 22)
			{
				Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 43));
	            SoundHandler.playSound("gui.button.press");
			}
		}
	}
}
