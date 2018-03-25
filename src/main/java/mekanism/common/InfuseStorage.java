package mekanism.common;

import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.common.base.ISustainedData;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;

public class InfuseStorage implements ISustainedData
{
	public InfuseType type;

	public int amount;

	public InfuseStorage() {}

	public InfuseStorage(InfuseType infuseType, int infuseAmount)
	{
		type = infuseType;
		amount = infuseAmount;
	}

	public boolean contains(InfuseStorage storage)
	{
		return type == storage.type && amount >= storage.amount;
	}

	public void subtract(InfuseStorage storage)
	{
		if(contains(storage))
		{
			amount -= storage.amount;
		} 
		else if(type == storage.type)
		{
			amount = 0;
		}
	}

	@Override
	public void writeSustainedData(ItemStack itemStack)
	{
		if (type != null && amount > 0)
		{
			ItemDataUtils.setString(itemStack, "infuseType", type.name);
			ItemDataUtils.setInt(itemStack, "infuseAmount", amount);
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack)
	{
		if (ItemDataUtils.hasData(itemStack, "infuseType") && ItemDataUtils.hasData(itemStack, "infuseAmount")){
			type = InfuseRegistry.get(ItemDataUtils.getString(itemStack, "infuseType"));
			if (type != null){
				amount = ItemDataUtils.getInt(itemStack, "infuseAmount");
			}
		} else {
			type = null;
			amount = 0;
		}
	}
}
