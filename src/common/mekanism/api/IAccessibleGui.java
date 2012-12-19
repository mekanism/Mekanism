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
	 * @return what the @Instance field is named in your mod.
	 */
	public String getInstanceName();
	
	/**
	 * Gets the mod's main class path as a string.  For Mekanism I would return "mekanism.common.Mekanism"
	 * @return
	 */
	public String getClassPath();
}
