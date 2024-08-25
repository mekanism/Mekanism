package mekanism.client.recipe_viewer.alias;

public interface IAliasMapping {

    <ITEM, FLUID, CHEMICAL> void addAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv);
}