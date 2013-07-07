package buildcraft.api.gates;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.transport.IPipe;

public class ActionManager {

	public static ITrigger[] triggers = new ITrigger[1024];
	public static IAction[] actions = new IAction[1024];

	private static LinkedList<ITriggerProvider> triggerProviders = new LinkedList<ITriggerProvider>();
	private static LinkedList<IActionProvider> actionProviders = new LinkedList<IActionProvider>();

	public static void registerTriggerProvider(ITriggerProvider provider) {
		if (provider != null && !triggerProviders.contains(provider)) {
			triggerProviders.add(provider);
		}
	}

	public static LinkedList<ITrigger> getNeighborTriggers(Block block, TileEntity entity) {
		LinkedList<ITrigger> triggers = new LinkedList<ITrigger>();

		for (ITriggerProvider provider : triggerProviders) {
			LinkedList<ITrigger> toAdd = provider.getNeighborTriggers(block, entity);

			if (toAdd != null) {
				for (ITrigger t : toAdd) {
					if (!triggers.contains(t)) {
						triggers.add(t);
					}
				}
			}
		}

		return triggers;
	}

	public static void registerActionProvider(IActionProvider provider) {
		if (provider != null && !actionProviders.contains(provider)) {
			actionProviders.add(provider);
		}
	}

	public static LinkedList<IAction> getNeighborActions(Block block, TileEntity entity) {
		LinkedList<IAction> actions = new LinkedList<IAction>();

		for (IActionProvider provider : actionProviders) {
			LinkedList<IAction> toAdd = provider.getNeighborActions(block, entity);

			if (toAdd != null) {
				for (IAction t : toAdd) {
					if (!actions.contains(t)) {
						actions.add(t);
					}
				}
			}
		}

		return actions;
	}

	public static LinkedList<ITrigger> getPipeTriggers(IPipe pipe) {
		LinkedList<ITrigger> triggers = new LinkedList<ITrigger>();

		for (ITriggerProvider provider : triggerProviders) {
			LinkedList<ITrigger> toAdd = provider.getPipeTriggers(pipe);

			if (toAdd != null) {
				for (ITrigger t : toAdd) {
					if (!triggers.contains(t)) {
						triggers.add(t);
					}
				}
			}
		}

		return triggers;
	}

}
