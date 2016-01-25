package buildcraft.api;

interface ISimpleRegistry<T extends ObjectDefinition> {
    /** @param uniqueTag The tag to look up. Can be null.
     * @return The unique definition associated with the unique tag, or null if the tag was not mapped */
    T getDefinition(String uniqueTag);

    /** @param definition The definition to look up. Can be null.
     * @return The unique to for that definition. Will never be null, but will be an empty string if the definition did
     *         not exist. */
    String getTag(T definition);
}
