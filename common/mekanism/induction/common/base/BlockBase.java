/**
 * 
 */
package mekanism.induction.common.base;

import mekanism.common.Mekanism;
import mekanism.induction.common.MekanismInduction;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.Configuration;

/**
 * @author Calclavia
 * 
 */
public class BlockBase extends Block
{
	public BlockBase(String name, int id)
	{
		super(MekanismInduction.CONFIGURATION.get(Configuration.CATEGORY_BLOCK, name, id).getInt(id), Material.piston);
		this.setCreativeTab(Mekanism.tabMekanism);
		this.setUnlocalizedName(MekanismInduction.PREFIX + name);
		this.setTextureName(MekanismInduction.PREFIX + name);
		this.setHardness(1f);
	}
}
