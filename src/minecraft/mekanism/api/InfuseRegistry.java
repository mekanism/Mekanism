package mekanism.api;

import java.lang.reflect.Field;
import java.util.HashMap;

import net.minecraft.item.ItemStack;

/**
 * Use this class to add a new object that registers as an infuse object.
 * @author AidanBrady
 *
 */
public class InfuseRegistry 
{
	/**
	 * Registers a block or item that serves as an infuse object.  An infuse object will store a certain type and amount of infuse,
	 * and will deliver this amount to the Metallurgic Infuser's buffer of infuse.  The item's stack size will be decremented when
	 * it is placed in the Metallurgic Infuser's infuse slot, and the machine can accept the type and amount of infuse stored in the
	 * object.
	 * @param itemStack - stack the infuse object is linked to -- stack size is ignored
	 * @param infuseObject - the infuse object with the type and amount data
	 * @return whether or not the registration was successful
	 */
	public boolean registerInfuseObject(ItemStack itemStack, InfuseObject infuseObject)
	{
		try {
			Class c = Class.forName("mekanism.common.Mekanism");
			Field field = c.getField("infusions");
			HashMap<ItemStack, InfuseObject> map = (HashMap<ItemStack, InfuseObject>)field.get(null);
			map.put(itemStack, infuseObject);
			return true;
		} catch(Exception e) {
			System.err.println("[Mekanism] Error while adding infuse object: " + e.getMessage());
			return false;
		}
	}
}
