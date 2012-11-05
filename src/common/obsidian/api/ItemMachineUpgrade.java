package obsidian.api;

import net.minecraft.src.*;
import net.uberkat.obsidian.common.ObsidianIngots;

public class ItemMachineUpgrade extends Item
{
	public ItemMachineUpgrade(int id)
	{
		super(id);
		setMaxStackSize(1);
		setCreativeTab(ObsidianIngots.tabOBSIDIAN);
	}
}
