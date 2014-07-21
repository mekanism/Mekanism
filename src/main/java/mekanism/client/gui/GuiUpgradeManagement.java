package mekanism.client.gui;

import mekanism.common.IUpgradeTile;
import mekanism.common.inventory.container.ContainerUpgradeManagement;
import net.minecraft.entity.player.InventoryPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiUpgradeManagement extends GuiMekanism
{
	public IUpgradeTile tileEntity;
	
	public GuiUpgradeManagement(InventoryPlayer inventory, IUpgradeTile tileEntity) 
	{
		super(new ContainerUpgradeManagement(inventory, tileEntity));
	}
}
