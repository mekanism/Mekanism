package mekanism.common.resource;

public enum ResourceType {
    SHARD("shard"),
    CRYSTAL("crystal"),
    DUST("dust"),
    DIRTY_DUST("dirty_dust"),
    CLUMP("clump"),
    INGOT("ingot"),
    RAW("raw", "raw_materials"),
    NUGGET("nugget"),
    ENRICHED("enriched", "enriched");

    private final String registryPrefix;
    private final String baseTagPath;

    ResourceType(String prefix) {
        this(prefix, prefix + "s");
    }

    ResourceType(String prefix, String baseTagPath) {
        this.registryPrefix = prefix;
        this.baseTagPath = baseTagPath;
    }

    public String getRegistryPrefix() {
        return registryPrefix;
    }

    public String getBaseTagPath() {
        return baseTagPath;
    }

    public boolean usedByPrimary(PrimaryResource resource) {
        //Copper doesn't have nuggets
        return this != ENRICHED && (resource != PrimaryResource.COPPER || this != NUGGET);
    }

    public boolean isVanilla() {
        return this == INGOT || this == RAW || this == NUGGET;
    }
}