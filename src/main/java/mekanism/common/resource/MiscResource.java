package mekanism.common.resource;

public enum MiscResource implements INamedResource {
    DIAMOND("diamond"),
    STEEL("steel"),
    REFINED_OBSIDIAN("refined_obsidian"),
    OBSIDIAN("obsidian"),
    BRONZE("bronze"),
    REFINED_GLOWSTONE("refined_glowstone"),
    SULFUR("sulfur"),
    LITHIUM("lithium"),
    CARBON("carbon"),
    REDSTONE("redstone");

    private final String registrySuffix;

    MiscResource(String registrySuffix) {
        this.registrySuffix = registrySuffix;
    }

    @Override
    public String getRegistrySuffix() {
        return registrySuffix;
    }
}