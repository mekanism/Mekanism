package mekanism.api;

/**
 * Implement this if you want your GUI to be accessible by the Control Panel.
 * @author AidanBrady
 *
 */
public interface IAccessibleGui 
{
	/**
	 * The block's GUI's specific access ID. 
	 * @return gui id
	 */
	public int getGuiID();
	
	/**
	 * The mod's instance object
	 * @return mod instance
	 */
	public Object getModInstance();
}
