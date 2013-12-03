package universalelectricity.compatibility;

import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.core.item.ItemElectric;
import cofh.api.energy.IEnergyContainerItem;

public abstract class ItemUniversalElectric extends ItemElectric implements ISpecialElectricItem, IEnergyContainerItem
{
	public static final float CHARGE_RATE = 0.005f;

	public ItemUniversalElectric(int id)
	{
		super(id);
	}

	/**
	 * IC2
	 */
	@Override
	public int getChargedItemId(ItemStack itemStack)
	{
		return this.itemID;
	}

	@Override
	public int getEmptyItemId(ItemStack itemStack)
	{
		return this.itemID;
	}

	@Override
	public int getMaxCharge(ItemStack itemStack)
	{
		return (int) (this.getMaxElectricityStored(itemStack) * Compatibility.TO_IC2_RATIO);
	}

	@Override
	public int getTier(ItemStack itemStack)
	{
		return 1;
	}

	@Override
	public int getTransferLimit(ItemStack itemStack)
	{
		return (int) ((this.getMaxElectricityStored(itemStack) * CHARGE_RATE) * Compatibility.TO_IC2_RATIO);
	}

	@Override
	public IElectricItemManager getManager(ItemStack itemStack)
	{
		return IC2ElectricItemManager.MANAGER;
	}

	@Override
	public boolean canProvideEnergy(ItemStack itemStack)
	{
		return this.recharge(itemStack, 1, false) > 0;
	}

	/**
	 * Thermal Expansion
	 */
	@Override
	public int receiveEnergy(ItemStack theItem, int energy, boolean doReceive)
	{
		return (int) (this.recharge(theItem, energy * Compatibility.BC3_RATIO, doReceive) * Compatibility.TO_BC_RATIO);
	}

	@Override
	public int extractEnergy(ItemStack theItem, int energy, boolean doTransfer)
	{
		return (int) (this.discharge(theItem, energy * Compatibility.BC3_RATIO, doTransfer) * Compatibility.TO_BC_RATIO);
	}

	@Override
	public int getEnergyStored(ItemStack theItem)
	{
		return (int) (this.getElectricityStored(theItem) * Compatibility.TO_BC_RATIO);
	}

	@Override
	public int getMaxEnergyStored(ItemStack theItem)
	{
		return (int) (this.getMaxElectricityStored(theItem) * Compatibility.TO_BC_RATIO);
	}

	public static class IC2ElectricItemManager implements IElectricItemManager
	{
		public static final IElectricItemManager MANAGER = new IC2ElectricItemManager();

		private IItemElectric getElectricItem(ItemStack itemStack)
		{
			if (itemStack.getItem() instanceof IItemElectric)
			{
				return ((IItemElectric) itemStack.getItem());
			}
			return null;
		}

		@Override
		public int charge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
		{
			float inputElectricity = amount * Compatibility.IC2_RATIO;
			return (int) (getElectricItem(itemStack).recharge(itemStack, inputElectricity, !simulate) * Compatibility.TO_IC2_RATIO);
		}

		@Override
		public int discharge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
		{
			float outputElectricity = amount * Compatibility.IC2_RATIO;
			return (int) (getElectricItem(itemStack).discharge(itemStack, outputElectricity, !simulate) * Compatibility.TO_IC2_RATIO);
		}

		@Override
		public boolean canUse(ItemStack itemStack, int amount)
		{
			return false;
		}

		@Override
		public int getCharge(ItemStack itemStack)
		{
			return 0;
		}

		@Override
		public boolean use(ItemStack itemStack, int amount, EntityLivingBase entity)
		{
			return false;
		}

		@Override
		public void chargeFromArmor(ItemStack itemStack, EntityLivingBase entity)
		{

		}

		@Override
		public String getToolTip(ItemStack itemStack)
		{
			return null;
		}

	}
}
