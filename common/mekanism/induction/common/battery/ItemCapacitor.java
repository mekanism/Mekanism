/**
 * 
 */
package mekanism.induction.common.battery;

import mekanism.api.induction.ICapacitor;
import mekanism.common.Mekanism;
import mekanism.induction.common.MekanismInduction;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import universalelectricity.compatibility.ItemUniversalElectric;

/**
 * Stores power.
 * 
 * @author Calclavia
 * 
 */
public class ItemCapacitor extends ItemUniversalElectric implements ICapacitor
{
	public ItemCapacitor(int id)
	{
		super(MekanismInduction.CONFIGURATION.get(Configuration.CATEGORY_ITEM, "capacitor", id).getInt(id));
		this.setCreativeTab(Mekanism.tabMekanism);
		this.setUnlocalizedName(MekanismInduction.PREFIX + "capacitor");
		this.setTextureName(MekanismInduction.PREFIX + "capacitor");
		this.setMaxStackSize(1);
		this.setMaxDamage(100);
	}

	@Override
	public float getTransfer(ItemStack itemStack)
	{
		return this.getMaxEnergyStored(itemStack) * 0.05F;
	}

	@Override
	public float getMaxElectricityStored(ItemStack theItem)
	{
		return 500;
	}

}
