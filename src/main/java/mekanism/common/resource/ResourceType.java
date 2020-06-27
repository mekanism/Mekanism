package mekanism.common.resource;

public enum ResourceType {
    SHARD("shard"),
    CRYSTAL("crystal"),
    DUST("dust"),
    DIRTY_DUST("dirty_dust"),
    CLUMP("clump"),
    INGOT("ingot"),
    NUGGET("nugget"),
    ENRICHED("enriched", "enriched");

    private final String registryPrefix;
    private final String pluralPrefix;

    ResourceType(String prefix) {
        this(prefix, prefix + "s");
    }

    ResourceType(String prefix, String pluralPrefix) {
        this.registryPrefix = prefix;
        this.pluralPrefix = pluralPrefix;
    }

    public String getRegistryPrefix() {
        return registryPrefix;
    }

    public String getPluralPrefix() {
        return pluralPrefix;
    }

    public boolean usedByPrimary() {
        return this != ENRICHED;
    }
}