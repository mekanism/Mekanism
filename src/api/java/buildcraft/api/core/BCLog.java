/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BCLog {

	public static final Logger logger = Logger.getLogger("Buildcraft");

	/**
	 * Deactivate constructor
	 */
	private BCLog() {
	}

	public static void initLog() {
		// TODO: check if the code below is still useful and remove otherwise.
		//logger.setParent(FMLLog.getLogger());

		logger.info("Starting BuildCraft " + getVersion());
		logger.info("Copyright (c) SpaceToad, 2011");
		logger.info("http://www.mod-buildcraft.com");
	}

	public static void logErrorAPI(String mod, Throwable error, Class classFile) {
		StringBuilder msg = new StringBuilder(mod);
		msg.append(" API error, please update your mods. Error: ").append(error);
		StackTraceElement[] stackTrace = error.getStackTrace();
		if (stackTrace.length > 0) {
			msg.append(", ").append(stackTrace[0]);
		}

		logger.log(Level.SEVERE, msg.toString());

		if (classFile != null) {
			msg = new StringBuilder(mod);
			msg.append(" API error: ").append(classFile.getSimpleName()).append(" is loaded from ").append(classFile.getProtectionDomain().getCodeSource().getLocation());
			logger.log(Level.SEVERE, msg.toString());
		}
	}

    public static String getVersion() {
        try {
            Class<?> clazz = Class.forName("buildcraft.core.Version");
            Method method = clazz.getDeclaredMethod("getVersion");
            return String.valueOf(method.invoke(null));
        } catch (Exception e) {
            return "UNKNOWN VERSION";
        }
    }
}
