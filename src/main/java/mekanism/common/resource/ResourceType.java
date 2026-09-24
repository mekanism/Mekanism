package mekanism.common.resource;

import java.util.List;
import org.jetbrains.annotations.Unmodifiable;

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

    /// Cached value of [ResourceType#values()].
    @Unmodifiable
    public static final List<ResourceType> VALUES = List.of(values());

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