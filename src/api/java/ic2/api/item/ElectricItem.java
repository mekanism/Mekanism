package ic2.api.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * Allows for charging, discharging and using electric items (IElectricItem).
 */
public final class ElectricItem {
	/**
	 * IElectricItemManager to use for interacting with IElectricItem ItemStacks.
	 * 
	 * This manager will act as a gateway and delegate the tasks to the final implementation
	 * (rawManager or a custom one) as necessary.
	 */
	public static IElectricItemManager manager;

	/**
	 * Standard IElectricItemManager implementation, only call it directly from another
	 * IElectricItemManager. Use manager instead.
	 */
	public static IElectricItemManager rawManager;

	/**
	 * Register an electric item manager for items not implementing IElectricItem.
	 * 
	 * This method is only designed for special purposes, implementing IElectricItem or
	 * ISpecialElectricItem instead of using this is faster.
	 * 
	 * Managers used with ISpecialElectricItem shouldn't be registered.
	 * 
	 * @param manager Manager to register.
	 */
	public static void registerBackupManager(IBackupElectricItemManager manager) {
		backupManagers.add(manager);
	}

	/**
	 * Get the electric item manager suitable for the supplied item stack.
	 * 
	 * This method is for internal use only.
	 * 
	 * @param stack ItemStack to be handled.
	 * @return First suitable electric item manager.
	 */
	public static IBackupElectricItemManager getBackupManager(ItemStack stack) {
		for (IBackupElectricItemManager manager : backupManagers) {
			if (manager.handles(stack)) return manager;
		}

		return null;
	}


	private static final List<IBackupElectricItemManager> backupManagers = new ArrayList<IBackupElectricItemManager>();
}

