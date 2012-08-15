package net.uberkat.obsidian.common;

public class Version {
	
	public int major;
	public int minor;
	public int build;
	
	public Version(int majorNum, int minorNum, int buildNum)
	{
		major = majorNum;
		minor = minorNum;
		build = buildNum;
	}
	
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
