package mekanism.common;

/**
 * Implement in your main class if your mod happens to be completely reliant on Mekanism, or in other words, a Mekanism module.
 * @author aidancbrady
 *
 */
public interface IModule 
{
	/**
	 * Gets the version of the module.
	 * @return the module's version
	 */
	public Version getVersion();
	
	/**
	 * Gets the name of the module.  Note that this doesn't include "Mekanism" like the actual module's name does, just the
	 * unique name.  For example, MekanismGenerators returns "Generators" here.
	 * @return unique name of the module
	 */
	public String getName();
}
