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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BCLog {

	public static final Logger logger = LogManager.getLogger("BuildCraft");

	/**
	 * Deactivate constructor
	 */
	private BCLog() {
	}

	public static void initLog() {

		logger.info("Starting BuildCraft " + getVersion());
		logger.info("Copyright (c) SpaceToad, 2011-2014");
		logger.info("http://www.mod-buildcraft.com");
	}

	public static void logErrorAPI(String mod, Throwable error, Class<?> classFile) {
		StringBuilder msg = new StringBuilder(mod);
		msg.append(" API error, please update your mods. Error: ").append(error);
		StackTraceElement[] stackTrace = error.getStackTrace();
		if (stackTrace.length > 0) {
			msg.append(", ").append(stackTrace[0]);
		}

		logger.log(Level.ERROR, msg.toString());

		if (classFile != null) {
			msg = new StringBuilder(mod);
			msg.append(" API error: ").append(classFile.getSimpleName()).append(" is loaded from ").append(classFile.getProtectionDomain().getCodeSource().getLocation());
			logger.log(Level.ERROR, msg.toString());
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
