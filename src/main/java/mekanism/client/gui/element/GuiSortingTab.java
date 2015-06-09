package mekanism.client.gui.element;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import codechicken.lib.vec.Rectangle4i;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSortingTab extends GuiElement
{
	public TileEntityFactory tileEntity;

	public GuiSortingTab(IGuiWrapper gui, TileEntityFactory tile, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSortingTab.png"), gui, def);

		lmntLeft = -26;
		lmntTop = 62;

		tileEntity = tile;
	}

	public GuiSortingTab(IGuiWrapper gui, TileEntityFactory tile, ResourceLocation def, int guiLeft, int guiTop )
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSortingTab.png"), gui, def);

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
		boolean mouseOver = xAxis >= lmntLeft + 5 && xAxis <= lmntLeft + 23 && yAxis >= lmntTop + 4 && yAxis <= lmntTop + 22;
		boolean sortingOn = ((TileEntityFactory)tileEntity).sorting;

		guiObj.drawTexturedRect(guiLeft + lmntLeft + 5, guiTop + lmntTop + 4, sortingOn? 44: 26, mouseOver?  0: 18, 18, 18);

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		//getFontRenderer().drawString(LangUtils.transOnOff(((TileEntityFactory)tileEntity).sorting), -21, 86, 0x0404040);

		if(xAxis >= lmntLeft + 5 && xAxis <= lmntLeft + 23 && yAxis >= lmntTop + 4 && yAxis <= lmntTop + 22)
		{
			displayTooltip(LangUtils.localize("gui.factory.autoSort") + " (" + LangUtils.transOnOff(((TileEntityFactory)tileEntity).sorting) + ")", xAxis, yAxis);
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
			if(xAxis >= lmntLeft + 5 && xAxis <= lmntLeft + 23 && yAxis >= lmntTop + 4 && yAxis <= lmntTop + 22)
			{
				ArrayList data = new ArrayList();
				data.add(0);
				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
                SoundHandler.playSound("gui.button.press");
			}
		}
	}
}
