package mekanism.api.infuse;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Use this class to add a new object that registers as an infuse object.
 *
 * @author AidanBrady
 */
//TODO: Make this act more like GasConversionHandler
public class InfuseRegistry {

    /**
     * The (private) map of ItemStacks and their related InfuseObjects.
     */
    private static Map<ItemStack, InfuseObject> infuseObjects = new HashMap<>();

    /**
     * Registers a block or item that serves as an infuse object.  An infuse object will store a certain type and amount of infuse, and will deliver this amount to the
     * Metallurgic Infuser's buffer of infuse.  The item's stack size will be decremented when it is placed in the Metallurgic Infuser's infuse slot, and the machine can
     * accept the type and amount of infuse stored in the object.
     *
     * @param itemStack    - stack the infuse object is linked to -- stack size is ignored
     * @param infuseObject - the infuse object with the type and amount data
     */
    public static void registerInfuseObject(ItemStack itemStack, InfuseObject infuseObject) {
        if (getObject(itemStack) != null) {
            return;
        }
        infuseObjects.put(itemStack, infuseObject);
    }

    /**
     * Gets the InfuseObject data from an ItemStack.
     *
     * @param itemStack - the ItemStack to check
     *
     * @return the ItemStack's InfuseObject
     */
    public static InfuseObject getObject(ItemStack itemStack) {
        for (Entry<ItemStack, InfuseObject> obj : infuseObjects.entrySet()) {
            if (ItemHandlerHelper.canItemStacksStack(obj.getKey(), itemStack)) {
                return obj.getValue();
            }
        }
        return null;
    }

    /**
     * Gets the private map for InfuseObjects.
     *
     * @return private InfuseObject map
     */
    public static Map<ItemStack, InfuseObject> getObjectMap() {
        return infuseObjects;
    }
}