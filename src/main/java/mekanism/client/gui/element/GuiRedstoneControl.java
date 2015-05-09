package mekanism.client.gui.element;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.network.PacketRedstoneControl.RedstoneControlMessage;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class GuiRedstoneControl extends GuiElement
{
	TileEntity tileEntity;

	public GuiRedstoneControl(IGuiWrapper gui, TileEntity tile, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiRedstoneControl.png"), gui, def);

		lmntLeft = 176;
		lmntTop = 138;

		tileEntity = tile;
	}
	
	public GuiRedstoneControl( IGuiWrapper gui, TileEntity tile, ResourceLocation def, int guiLeft, int guiTop )
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiRedstoneControl.png"), gui, def );

		lmntLeft = guiLeft;
		lmntTop = guiTop;

		tileEntity = tile;
	}
	
	@Override
	public void renderBackground(int xAxis, int yAxis, int guiLeft, int guiTop)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiLeft + lmntLeft, guiTop + lmntTop, 0, 0, lmntWidth, lmntHeight);

		IRedstoneControl control = (IRedstoneControl)tileEntity;
		int renderX = 26 + (18*control.getControlType().ordinal());
		boolean mouseOver = xAxis >= lmntLeft + 3 && xAxis <= lmntLeft + 21 && yAxis >= lmntTop + 4 && yAxis <= lmntTop + 22;

		guiObj.drawTexturedRect(guiLeft + lmntLeft + 3, guiTop + lmntTop + 4, renderX, mouseOver?  0: 18, 18, 18);

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		IRedstoneControl control = (IRedstoneControl)tileEntity;

		if(xAxis >= lmntLeft + 3 && xAxis <= lmntLeft + 21 && yAxis >= lmntTop + 4 && yAxis <= lmntTop + 22)
		{
			displayTooltip(control.getControlType().getDisplay(), xAxis, yAxis);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button)
	{
		IRedstoneControl control = (IRedstoneControl)tileEntity;

		if(button == 0)
		{
			if(xAxis >= lmntLeft + 3 && xAxis <= lmntLeft + 21 && yAxis >= lmntTop + 4 && yAxis <= lmntTop + 22)
			{
				RedstoneControl current = control.getControlType();
				int ordinalToSet = current.ordinal() < (RedstoneControl.values().length-1) ? current.ordinal()+1 : 0;
				if(ordinalToSet == RedstoneControl.PULSE.ordinal() && !control.canPulse()) ordinalToSet = 0;

				SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new RedstoneControlMessage(Coord4D.get(tileEntity), RedstoneControl.values()[ordinalToSet]));
			}
		}
	}
}
