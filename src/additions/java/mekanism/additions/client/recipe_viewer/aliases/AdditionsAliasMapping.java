package mekanism.additions.client.recipe_viewer.aliases;

import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.client.recipe_viewer.alias.IAliasMapping;
import mekanism.client.recipe_viewer.alias.RVAliasHelper;

public class AdditionsAliasMapping implements IAliasMapping {

    @Override
    public <ITEM, FLUID, CHEMICAL> void addAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addAliases(AdditionsItems.WALKIE_TALKIE, AdditionsAliases.WALKIE_TALKIE_RADIO);
        rv.addAliases(AdditionsBlocks.GLOW_PANELS.values(), AdditionsAliases.GLOW_PANEL_LIGHT_SOURCE);
        rv.addAliases(AdditionsBlocks.PLASTIC_ROADS.values(), AdditionsAliases.PLASTIC_ROAD_PATH);
    }
}