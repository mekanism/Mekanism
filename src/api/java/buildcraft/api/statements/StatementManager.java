/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.statements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.BCLog;

public final class StatementManager {

    public static Map<String, IStatement> statements = new HashMap<>();
    public static Map<String, Class<? extends IStatementParameter>> parameters = new HashMap<>();
    private static List<ITriggerProvider> triggerProviders = new LinkedList<>();
    private static List<IActionProvider> actionProviders = new LinkedList<>();

    static {
        registerParameterClass(StatementParameterItemStack.class);
    }

    /** Deactivate constructor */
    private StatementManager() {}

    public static void registerTriggerProvider(ITriggerProvider provider) {
        if (provider != null && !triggerProviders.contains(provider)) {
            triggerProviders.add(provider);
        }
    }

    public static void registerActionProvider(IActionProvider provider) {
        if (provider != null && !actionProviders.contains(provider)) {
            actionProviders.add(provider);
        }
    }

    public static void registerStatement(IStatement statement) {
        statements.put(statement.getUniqueTag(), statement);
    }

    public static void registerParameterClass(Class<? extends IStatementParameter> param) {
        parameters.put(createParameter(param).getUniqueTag(), param);
    }

    public static List<ITriggerExternal> getExternalTriggers(EnumFacing side, TileEntity entity) {
        if (entity instanceof IOverrideDefaultStatements) {
            List<ITriggerExternal> result = ((IOverrideDefaultStatements) entity).overrideTriggers();
            if (result != null) {
                return result;
            }
        }

        LinkedHashSet<ITriggerExternal> triggers = new LinkedHashSet<>();

        for (ITriggerProvider provider : triggerProviders) {
            provider.addExternalTriggers(triggers, side, entity);
        }

        return new ArrayList<>(triggers);
    }

    public static List<IActionExternal> getExternalActions(EnumFacing side, TileEntity entity) {
        if (entity instanceof IOverrideDefaultStatements) {
            List<IActionExternal> result = ((IOverrideDefaultStatements) entity).overrideActions();
            if (result != null) {
                return result;
            }
        }

        LinkedHashSet<IActionExternal> actions = new LinkedHashSet<>();

        for (IActionProvider provider : actionProviders) {
            provider.addExternalActions(actions, side, entity);
        }

        return new ArrayList<>(actions);
    }

    public static List<ITriggerInternal> getInternalTriggers(IStatementContainer container) {
        LinkedHashSet<ITriggerInternal> triggers = new LinkedHashSet<>();

        for (ITriggerProvider provider : triggerProviders) {
            provider.addInternalTriggers(triggers, container);
        }

        return new ArrayList<>(triggers);
    }

    public static List<IActionInternal> getInternalActions(IStatementContainer container) {
        LinkedHashSet<IActionInternal> actions = new LinkedHashSet<>();

        for (IActionProvider provider : actionProviders) {
            provider.addInternalActions(actions, container);
        }

        return new ArrayList<>(actions);
    }

    public static List<ITriggerInternalSided> getInternalSidedTriggers(IStatementContainer container, EnumFacing side) {
        LinkedHashSet<ITriggerInternalSided> triggers = new LinkedHashSet<>();

        for (ITriggerProvider provider : triggerProviders) {
            provider.addInternalSidedTriggers(triggers, container, side);
        }

        return new ArrayList<>(triggers);
    }

    public static List<IActionInternalSided> getInternalSidedActions(IStatementContainer container, EnumFacing side) {
        LinkedHashSet<IActionInternalSided> actions = new LinkedHashSet<>();

        for (IActionProvider provider : actionProviders) {
            provider.addInternalSidedActions(actions, container, side);
        }

        return new ArrayList<>(actions);
    }

    public static IStatementParameter createParameter(String kind) {
        return createParameter(parameters.get(kind));
    }

    private static IStatementParameter createParameter(Class<? extends IStatementParameter> param) {
        if (param == null) {
            return null;
        }

        try {
            return param.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (Error error) {
            BCLog.logErrorAPI(error, IStatementParameter.class);
            throw error;
        }

        return null;
    }
}
