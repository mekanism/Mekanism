package mekanism.common.resource;

public enum ResourceType {
    SHARD("shard", "shard"),
    CRYSTAL("crystal"),
    DUST("dust"),
    DIRTY_DUST("dirty_dust", "dustDirty"),
    CLUMP("clump"),
    INGOT("ingot"),
    NUGGET("nugget"),
    COMPRESSED("compressed", "itemCompressed");

    private final String registryPrefix;
    private final String orePrefix;

    ResourceType(String prefix) {
        this(prefix, prefix);
    }

    ResourceType(String registryPrefix, String orePrefix) {
        this.registryPrefix = registryPrefix;
        this.orePrefix = orePrefix;
    }

    public String getRegistryPrefix() {
        return registryPrefix;
    }

    public String getOrePrefix() {
        return orePrefix;
    }
}