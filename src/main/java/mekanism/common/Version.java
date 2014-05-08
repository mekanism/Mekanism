package mekanism.common;

/**
 * Version v1.0.4. Simple version handling for Mekanism.
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

	/**
	 * 1: greater than
	 * 0: equal to
	 * -1: less than
	 * @param version
	 * @return
	 */
	public byte comparedState(Version version)
	{
		if(version.major > major)
		{
			return -1;
		}
		else if(version.major == major)
		{
			if(version.minor > minor)
			{
				return -1;
			}
			else if(version.minor == minor)
			{
				if(version.build > build)
				{
					return -1;
				}
				else if(version.build == build)
				{
					return 0;
				}
				else {
					return 1;
				}
			}
			else {
				return 1;
			}
		}
		else {
			return 1;
		}
	}

	/**
	 * Gets a version object from a string.
	 * @param s - string object
	 * @return version if applicable, otherwise null
	 */
	public static Version get(String s)
	{
		String[] split = s.replace('.', ':').split(":");

		if(split.length != 3)
		{
			return null;
		}

		for(String i : split)
		{
			for(Character c : i.toCharArray())
			{
				if(!Character.isDigit(c))
				{
					return null;
				}
			}
		}

		int[] digits = new int[3];

		for(int i = 0; i < 3; i++)
		{
			digits[i] = Integer.parseInt(split[i]);
		}

		return new Version(digits[0], digits[1], digits[2]);
	}

	@Override
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
