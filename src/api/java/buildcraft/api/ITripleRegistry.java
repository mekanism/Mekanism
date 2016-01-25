package buildcraft.api;

import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.item.Item;

public interface ITripleRegistry<T extends ObjectDefinition> extends ISimpleRegistry<T> {
    /** @return An unmodifiable set containing all the current mappings of the registry */
    Set<Triple<String, Item, T>> getDefinitions();

    // Specifics

    /** @param definition The pipe definition to look up. Can be null.
     * @return The item the has been mapped to the definition, or null if the definition was not mapped */
    Item getItem(T definition);

    /** @param item The item to look up the definition of. Can be null.
     * @return The definition associated with that item, or null if the item was not mapped. */
    T getDefinition(Item item);

    /** @param item The item to look up the tag of. Can be null.
     * @return The unique tag associated with that item, or null if the item was not mapped. */
    String getUniqueTag(Item item);

    /** @param tag The tag to look up the item with.
     * @return The item associated with that tag, or null if the tag was not mapped. */
    Item getItem(String tag);
}
