/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.core;

import java.util.Locale;

import net.minecraft.world.World;

/** Provides a way to quickly enable or disable certain debug conditions via VM arguments or whether the client/server
 * is in a dev environment */
public class BCDebugging {
    public enum DebugStatus {
        NONE,
        ENABLE,
        LOGGING_ONLY,
        ALL;
    }

    enum DebugLevel {
        LOG,
        COMPLEX;

        final String name = name().toLowerCase(Locale.ROOT);
        boolean isAllOn;
    }

    private static final DebugStatus DEBUG_STATUS;

    static {
        // Basically we enable debugging for the dev-environment, and disable everything for normal players.
        // However if you provide VM arguments then the behaviour changes somewhat:
        // (VM argument is "-Dbuildcraft.debug=...")
        // - "enable" Enables debugging even if you are not in a dev environment
        // - "disable" Disables ALL debugging (and doesn't output any messages even in the dev environment)
        // - "log" Major debug options are turned on. Registry setup + API usage etc
        // - "all" All possible debug options are turned on. Lots of spam. Not recommended.

        String value = System.getProperty("buildcraft.debug");
        if ("enable".equals(value)) DEBUG_STATUS = DebugStatus.ENABLE;
        else if ("all".equals(value)) DEBUG_STATUS = DebugStatus.ALL;
        else if ("disable".equals(value)) {
            // let people disable the messages if they are in a dev environment but don't want messages.
            DEBUG_STATUS = DebugStatus.NONE;
        } else if ("log".equals(value)) {
            // Some debugging options are more than just logging, so we will differentiate between them
            DEBUG_STATUS = DebugStatus.LOGGING_ONLY;
        } else if (World.class.getName().contains("World")) {
            // Dev environment
            DEBUG_STATUS = DebugStatus.ENABLE;
        } else {
            // Most likely a built jar - don't spam people with info they probably don't need
            DEBUG_STATUS = DebugStatus.NONE;
        }

        if (DEBUG_STATUS == DebugStatus.ALL) {
            BCLog.logger.info("[debugger] Debugging automatically enabled for ALL of buildcraft. Prepare for log spam.");
        } else if (DEBUG_STATUS == DebugStatus.LOGGING_ONLY) {
            BCLog.logger.info("[debugger] Debugging automatically enabled for some non-spammy parts of buildcraft.");
        } else if (DEBUG_STATUS == DebugStatus.ENABLE) {
            BCLog.logger.info("[debugger] Debugging not automatically enabled for all of buildcraft. Logging all possible debug options.");
            BCLog.logger.info("              To enable it for only logging messages add \"-Dbuildcraft.debug=log\" to your launch VM arguments");
            BCLog.logger.info("              To enable it for ALL debugging \"-Dbuildcraft.debug=all\" to your launch VM arguments");
            BCLog.logger.info("              To remove this message and all future ones add \"-Dbuildcraft.debug=disable\" to your launch VM arguments");
        }

        DebugLevel.COMPLEX.isAllOn = DEBUG_STATUS == DebugStatus.ALL;
        DebugLevel.LOG.isAllOn = DEBUG_STATUS == DebugStatus.ALL || DEBUG_STATUS == DebugStatus.LOGGING_ONLY;
    }

    public static boolean shouldDebugComplex(String string) {
        return shouldDebug(string, DebugLevel.COMPLEX);
    }

    public static boolean shouldDebugLog(String string) {
        return shouldDebug(string, DebugLevel.LOG);
    }

    private static boolean shouldDebug(String option, DebugLevel type) {
        String prop = getProp(option);
        String actual = System.getProperty(prop);
        if ("false".equals(actual)) {
            BCLog.logger.info("[debugger] Debugging manually disabled for \"" + option + "\" (" + type + ").");
            return false;
        } else if ("true".equals(actual)) {
            BCLog.logger.info("[debugger] Debugging enabled for \"" + option + "\" (" + type + ").");
            return true;
        }
        if (type.isAllOn) {
            BCLog.logger.info("[debugger] Debugging automatically enabled for \"" + option + "\" (" + type + ").");
            return true;
        }
        if ("complex".equals(actual) || type.name.equals(actual)) {
            BCLog.logger.info("[debugger] Debugging enabled for \"" + option + "\" (" + type + ").");
            return true;
        } else {
            StringBuilder log = new StringBuilder();
            log.append("[debugger] To enable debugging for ");
            log.append(option);
            log.append(" add the option \"-D");
            log.append(prop);
            log.append("=true\" to your launch config as a VM argument (" + type + ").");
            BCLog.logger.info(log);
        }
        return false;
    }

    private static String getProp(String string) {
        return "buildcraft." + string + ".debug";
    }
}
