package mekanism.common;

/**
 * Implement this if your machine/generator has some form of active state.
 * @author aidancbrady
 *
 */
public interface IActiveState 
{
	/**
	 * Gets the active state as a boolean.
	 * @return active state
	 */
	public boolean getActive();
	
	/**
	 * Sets the active state to a new value.
	 * @param active - new active state
	 */
	public void setActive(boolean active);
}
