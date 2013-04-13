
package thermalexpansion.api.item;

import java.util.Map;
import java.util.TreeMap;

import net.minecraft.item.ItemStack;

public final class ItemRegistry {

    private static final Map<String, ItemStack> registry = new TreeMap<String, ItemStack>();

    /**
     * Returns an ItemStack containing the item that corresponds to the provided name.
     * 
     * @param name
     *            Name of the item.
     * @param qty
     *            Requested quantity of the item.
     */
    public static ItemStack getItem(String name, int qty) {

        ItemStack result = registry.get(name);
        if (result != null) {
            result = result.copy();
            result.stackSize = qty;
        }
        return result;
    }

    /**
     * Registers a new item with the ItemRegistry.
     * 
     * @param name
     *            Name of the item.
     * @param item
     *            ItemStack representing the item.
     */
    public static void registerItem(String name, ItemStack item) {

        registry.put(name, item);
    }

    /**
     * Print a list of all currently registered items to the console.
     */
    public static void printItemNames() {

        System.out.println("Printing all registered Thermal Expansion items:");
        for (String itemName : registry.keySet()) {
            System.out.println(itemName);
        }
    }

}
