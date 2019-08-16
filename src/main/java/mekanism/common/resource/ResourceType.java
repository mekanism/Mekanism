package mekanism.common.resource;

public enum ResourceType {
    SHARD("shard"),
    CRYSTAL("crystal"),
    DUST("dust"),
    DIRTY_DUST("dirty_dust"),
    CLUMP("clump"),
    INGOT("ingot"),
    NUGGET("nugget"),
    ENRICHED("enriched");

    private final String registryPrefix;

    ResourceType(String prefix) {
        this.registryPrefix = prefix;
    }

    public String getRegistryPrefix() {
        return registryPrefix;
    }
}