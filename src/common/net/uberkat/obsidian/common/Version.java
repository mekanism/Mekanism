package net.uberkat.obsidian.common;

/**
 * Version class. Simple version handling for Obsidian Ingots.
 * @author AidanBrady
 *
 */
public class Version
{
	/** Major number for version */
	public int major;
	/** Minor number for version */
	public int minor;
	/** Build number for version */
	public int build;
	
	/**
	 * Creates a version number with 3 digits.
	 * @param majorNum - major version
	 * @param minorNum - minor version
	 * @param buildNum - build version
	 */
	public Version(int majorNum, int minorNum, int buildNum)
	{
		major = majorNum;
		minor = minorNum;
		build = buildNum;
	}
	
	/** 
	 * Resets the version number to "0.0.0." 
	 */
	public void reset()
	{
		major = 0;
		minor = 0;
		build = 0;
	}
	
	public String toString()
	{
		if(major == 0 && minor == 0 && build == 0)
		{
			return "";
		}
		else if(major != 0 && minor != 0 && build == 0)
		{
			return major + "." + minor;
		}
		else if(major != 0 && minor == 0 && build == 0)
		{
			return major + "." + minor;
		}
		else {
			return major + "." + minor + "." + build;
		}
	}
}
