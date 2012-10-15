package net.uberkat.obsidian.common;

/**
 * Version v1.0.3. Simple version handling for Obsidian Ingots.
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
	 * Creates a version number with 3 digits from a string, by splitting with the char '.'
	 * @param version - version number as a String
	 */
	public Version(String version)
	{
		String[] numbers = version.split(".");
		major = Integer.getInteger(numbers[0]);
		minor = Integer.getInteger(numbers[1]);
		build = Integer.getInteger(numbers[2]);
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
		else {
			return major + "." + minor + "." + build;
		}
	}
}
