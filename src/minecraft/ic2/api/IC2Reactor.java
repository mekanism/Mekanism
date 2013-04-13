package ic2.api;

import java.lang.reflect.Field;

public class IC2Reactor {
	private static Field energyGeneratorNuclear;
	
	public static int getEUOutput() {
		try {
			if (energyGeneratorNuclear == null) energyGeneratorNuclear = Class.forName(getPackage() + ".core.IC2").getDeclaredField("energyGeneratorNuclear");
		
			return energyGeneratorNuclear.getInt(null);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get the base IC2 package name, used internally.
	 * 
	 * @return IC2 package name, if unable to be determined defaults to ic2
	 */
	private static String getPackage() {
		Package pkg = IC2Reactor.class.getPackage();
		if (pkg != null) return pkg.getName().substring(0, pkg.getName().lastIndexOf('.'));
		else return "ic2";
	}
}
