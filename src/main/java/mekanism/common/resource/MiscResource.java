package mekanism.common.resource;

public enum MiscResource implements INamedResource {
    DIAMOND("diamond", "Diamond"),
    STEEL("steel", "Steel"),
    REFINED_OBSIDIAN("refined_obsidian", "RefinedObsidian"),
    OBSIDIAN("obsidian", "Obsidian"),
    BRONZE("bronze", "Bronze"),
    REFINED_GLOWSTONE("refined_glowstone", "RefinedGlowstone"),
    SULFUR("sulfur", "Sulfur"),
    LITHIUM("lithium", "Lithium"),
    CARBON("carbon", "Carbon"),
    REDSTONE("redstone", "Redstone");

    private final String registrySuffix;
    private final String oreSuffix;

    MiscResource(String registrySuffix, String oreSuffix) {
        this.registrySuffix = registrySuffix;
        this.oreSuffix = oreSuffix;
    }

    @Override
    public String getRegistrySuffix() {
        return registrySuffix;
    }

    @Override
    public String getOreSuffix() {
        return oreSuffix;
    }
}