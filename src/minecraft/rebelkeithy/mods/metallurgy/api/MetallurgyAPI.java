package rebelkeithy.mods.metallurgy.api;

import java.lang.reflect.Field;

public class MetallurgyAPI 
{
	
	// Values for name: "base", "precious", "nether", "fantasy", "ender", "utility"
	public static IMetalSet getMetalSet(String name)
	{
		try {
			Class metallurgyMetals = Class.forName("rebelkeithy.mods.metallurgy.metals.MetallurgyMetals");
			Field set = metallurgyMetals.getField(name + "Set");
			return (IMetalSet) set.get(null);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	public static String[] getMetalSetNames()
	{
		// TODO maybe put something to get runtime list here
		String[] names = {"base", "precious", "nether", "fantasy", "ender", "utility"};
		return names;
	}
}
