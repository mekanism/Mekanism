/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.api.robots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import buildcraft.api.core.BCLog;

public abstract class RobotManager {
    public static IRobotRegistryProvider registryProvider;
    public static ArrayList<Class<? extends AIRobot>> aiRobots = new ArrayList<Class<? extends AIRobot>>();

    static {
        aiRobotsByNames = new HashMap<String, Class<? extends AIRobot>>();
        aiRobotsNames = new HashMap<Class<? extends AIRobot>, String>();
        aiRobotsByLegacyClassNames = new HashMap<String, Class<? extends AIRobot>>();
        resourceIdNames = new HashMap<Class<? extends ResourceId>, String>();
        resourceIdByNames = new HashMap<String, Class<? extends ResourceId>>();
        resourceIdLegacyClassNames = new HashMap<String, Class<? extends ResourceId>>();
        dockingStationNames = new HashMap<Class<? extends DockingStation>, String>();
        dockingStationByNames = new HashMap<String, Class<? extends DockingStation>>();

        registerResourceId(ResourceIdBlock.class, "resourceIdBlock", "buildcraft.core.robots.ResourceIdBlock");
        registerResourceId(ResourceIdRequest.class, "resourceIdRequest", "buildcraft.core.robots.ResourceIdRequest");
    }

    private static Map<Class<? extends AIRobot>, String> aiRobotsNames;
    private static Map<String, Class<? extends AIRobot>> aiRobotsByNames;
    private static Map<String, Class<? extends AIRobot>> aiRobotsByLegacyClassNames;

    private static Map<Class<? extends ResourceId>, String> resourceIdNames;
    private static Map<String, Class<? extends ResourceId>> resourceIdByNames;
    private static Map<String, Class<? extends ResourceId>> resourceIdLegacyClassNames;

    private static Map<Class<? extends DockingStation>, String> dockingStationNames;
    private static Map<String, Class<? extends DockingStation>> dockingStationByNames;

    public static void registerAIRobot(Class<? extends AIRobot> aiRobot, String name) {
        registerAIRobot(aiRobot, name, null);
    }

    public static void registerAIRobot(Class<? extends AIRobot> aiRobot, String name, String legacyClassName) {
        if (aiRobotsByNames.containsKey(name)) {
            BCLog.logger.info("Overriding " + aiRobotsByNames.get(name).getName() + " with " + aiRobot.getName());
        }

        // Check if NBT-load constructor is present
        try {
            aiRobot.getConstructor(EntityRobotBase.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("AI class " + aiRobot.getName() + " lacks NBT load construtor! This is a bug!");
        }

        aiRobots.add(aiRobot);
        aiRobotsByNames.put(name, aiRobot);
        aiRobotsNames.put(aiRobot, name);
        if (legacyClassName != null) {
            aiRobotsByLegacyClassNames.put(legacyClassName, aiRobot);
        }
    }

    public static Class<?> getAIRobotByName(String aiRobotName) {
        return aiRobotsByNames.get(aiRobotName);
    }

    public static String getAIRobotName(Class<? extends AIRobot> aiRobotClass) {
        return aiRobotsNames.get(aiRobotClass);
    }

    public static Class<?> getAIRobotByLegacyClassName(String aiRobotLegacyClassName) {
        return aiRobotsByLegacyClassNames.get(aiRobotLegacyClassName);
    }

    public static void registerResourceId(Class<? extends ResourceId> resourceId, String name) {
        registerResourceId(resourceId, name, null);
    }

    public static void registerResourceId(Class<? extends ResourceId> resourceId, String name, String legacyClassName) {
        resourceIdByNames.put(name, resourceId);
        resourceIdNames.put(resourceId, name);
        if (legacyClassName != null) {
            resourceIdLegacyClassNames.put(legacyClassName, resourceId);
        }
    }

    public static Class<?> getResourceIdByName(String resourceIdName) {
        return resourceIdByNames.get(resourceIdName);
    }

    public static String getResourceIdName(Class<? extends ResourceId> resouceIdClass) {
        return resourceIdNames.get(resouceIdClass);
    }

    public static Class<?> getResourceIdByLegacyClassName(String resourceIdLegacyClassName) {
        return resourceIdLegacyClassNames.get(resourceIdLegacyClassName);
    }

    public static void registerDockingStation(Class<? extends DockingStation> dockingStation, String name) {
        dockingStationByNames.put(name, dockingStation);
        dockingStationNames.put(dockingStation, name);
    }

    public static Class<? extends DockingStation> getDockingStationByName(String dockingStationTypeName) {
        return dockingStationByNames.get(dockingStationTypeName);
    }

    public static String getDockingStationName(Class<? extends DockingStation> dockingStation) {
        return dockingStationNames.get(dockingStation);
    }
}
