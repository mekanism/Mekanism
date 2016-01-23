package mekanism.generators.client.gui;

import mekanism.client.gui.GuiMekanism;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.InventoryPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiIndustrialTurbine extends GuiMekanism
{
	public TileEntityTurbineCasing tileEntity;

	public GuiIndustrialTurbine(InventoryPlayer inventory, TileEntityTurbineCasing tentity)
	{
		super(tentity, new ContainerFilter(inventory, tentity));
		tileEntity = tentity;
	}
}
