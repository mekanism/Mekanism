package universalelectricity.core.item;

import net.minecraft.item.ItemStack;
import universalelectricity.core.electricity.ElectricityPack;

/**
 * An interface applied to all electrical items. Should be applied to the Item class.
 * 
 * @author Calclavia
 * 
 */
public interface IItemElectric extends IItemElectricityStorage, IItemVoltage
{
	/**
	 * Called when this item receives electricity; being charged.
	 * 
	 * @return The amount of electricity that was added to the electric item.
	 */
	public ElectricityPack onReceive(ElectricityPack electricityPack, ItemStack itemStack);

	/**
	 * Called when something requests electricity from this item; being decharged.
	 * 
	 * @return - The amount of electricity that was removed from the electric item.
	 */
	public ElectricityPack onProvide(ElectricityPack electricityPack, ItemStack itemStack);

	/**
	 * @return How much electricity does this item want to receive/take? This will affect the speed
	 * in which items get charged per tick.
	 */
	public ElectricityPack getReceiveRequest(ItemStack itemStack);

	/**
	 * 
	 * @return How much electricity does this item want to provide/give out? This will affect the
	 * speed in which items get decharged per tick.
	 */
	public ElectricityPack getProvideRequest(ItemStack itemStack);
}
