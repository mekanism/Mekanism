package mekanism.api.infuse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Use this class to add a new object that registers as an infuse object.
 *
 * @author AidanBrady
 */
public class InfuseRegistry {

    /**
     * The (private) map of infuse names and their corresponding InfuseTypes.
     */
    private static final Map<String, InfuseType> infuseTypes = new HashMap<>();

    private static final Map<String, InfuseType> infuseTypesImmutable = Collections.unmodifiableMap(infuseTypes);

    private static final List<Pair<InfuseObject, Ingredient>> infuseObjects = new ArrayList<>();

    private static final Collection<Pair<InfuseObject, Ingredient>> infuseObjectsImmutable = Collections.unmodifiableCollection(infuseObjects);

    /**
     * Registers an InfuseType into the registry. Call this in PreInit!
     *
     * @param infuse InfuseType to register
     */
    public static void registerInfuseType(InfuseType infuse) {
        if (infuseTypes.containsKey(infuse.name)) {
            return;
        }
        infuseTypes.put(infuse.name, infuse);
    }

    /**
     * Gets an InfuseType from it's name, or null if it doesn't exist.
     *
     * @param name - the name of the InfuseType to get
     *
     * @return the name's corresponding InfuseType
     */
    public static InfuseType get(String name) {
        if (name.equals("null")) {
            return null;
        }
        return infuseTypes.get(name);
    }

    /**
     * Whether or not the registry contains a correspondent InfuseType to a name.
     *
     * @param name - the name to check
     *
     * @return if the name has a coorespondent InfuseType
     */
    public static boolean contains(String name) {
        return get(name) != null;
    }

    /**
     * Registers a block or item that serves as an infuse object.  An infuse object will store a certain type and amount of infuse, and will deliver this amount to the
     * Metallurgic Infuser's buffer of infuse.  The item's stack size will be decremented when it is placed in the Metallurgic Infuser's infuse slot, and the machine can
     * accept the type and amount of infuse stored in the object.
     *
     * @param ingredient   - ingredient the infuse object is linked to
     * @param infuseObject - the infuse object with the type and amount data
     */
    public static void registerInfuseObject(Ingredient ingredient, InfuseObject infuseObject) {
        //noinspection ConstantConditions
        if (infuseObject.type == null || infuseObject.stored <= 0){
            throw new IllegalArgumentException("Bad infuseObject");
        }
        infuseObjects.add(Pair.of(infuseObject, ingredient));
    }

    /**
     * Gets the InfuseObject data from an ItemStack.
     *
     * @param itemStack - the ItemStack to check
     *
     * @return the ItemStack's InfuseObject
     */
    public static InfuseObject getObject(ItemStack itemStack) {
        for (Pair<InfuseObject, Ingredient> obj : infuseObjects) {
            if (obj.getRight().apply(itemStack)) {
                return obj.getLeft();
            }
        }
        return null;
    }

    /**
     * Gets the private map for InfuseTypes.
     *
     * @return private InfuseType map
     */
    public static Map<String, InfuseType> getInfuseMap() {
        return infuseTypesImmutable;
    }

    public static Collection<Pair<InfuseObject, Ingredient>> getInfuseObjects() {
        return Collections.unmodifiableCollection(infuseObjectsImmutable);
    }
}