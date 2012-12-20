package mekanism.api;

/**
 * Implement this if your tile entity can store some form of gas.
 * @author AidanBrady
 *
 */
public interface IGasStorage
{
	/**
	 * Get the gas of a declared type.
	 * @param type - type of gas
	 * @return gas stored
	 */
	public int getGas(EnumGas type);
	
	/**
	 * Set the gas of a declared type to a new amount;
	 * @param type - type of gas
	 * @param amount - amount to store
	 */
	public void setGas(EnumGas type, int amount);
}
