package railcraft.common.api.core.items;

import java.util.Map;
import java.util.TreeMap;
import net.minecraft.src.ItemStack;

/**
 * This class contains a registry of all currently active Railcraft items.
 * Which items are registered depends on the user's settings in "railcraft.cfg",
 * so the available items may vary from one installation to the next.
 *
 * Initialization of the registry will occur during the BaseMod.load()
 * function. It is strongly recommended you wait until the BaseMod.modsLoaded()
 * function to reference the registry.
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public final class ItemRegistry
{

    private static final Map<String, ItemStack> registry = new TreeMap<String, ItemStack>();

    private ItemRegistry()
    {
    }

    /**
     * This function will return an ItemStack containing the item that
     * corresponds to the provided tag.
     *
     * Generally item tags will correspond to the tags used in "railcraft.cfg",
     * but there will be some exceptions.
     *
     * This function can and will return null for just about every item
     * if the item is disabled via the configuration files.
     * You must test the return value for safety.
     *
     * For list of available tags see the printItemTags() function.
     *
     * @param tag The item tag
     * @param qty The stackSize of the returned item
     * @return The ItemStack or null if no item exists for that tag
     */
    public static ItemStack getItem(String tag, int qty)
    {
        ItemStack stack = registry.get(tag);
        if(stack != null) {
            stack = stack.copy();
            stack.stackSize = qty;
        }
        return stack;
    }

    /**
     * Registers a new item with the Registry.
     *
     * This should generally only be called by Railcraft itself
     * while the mod is initializing during the mod_Railcraft.load() call.
     *
     * @param tag The tag name
     * @param item The item
     */
    public static void registerItem(String tag, ItemStack item)
    {
        tag = tag.replace("rc.", "");
        registry.put(tag, item);
    }

    /**
     * This function will print a list of all currently registered items
     * to the console.
     *
     * Use this for development purposes.
     */
    public static void printItemTags()
    {
        System.out.println();
        System.out.println("Printing all registered Railcraft items:");
        for(String tag : registry.keySet()) {
            System.out.println(tag);
        }
        System.out.println();
    }

    /**
     * Returns the entire mapping of items.
     *
     * @return
     */
    public static Map<String, ItemStack> getItems()
    {
        return registry;
    }
}
