package mekanism.additions.client.recipe_viewer.aliases;

import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.client.recipe_viewer.alias.IAliasMapping;
import mekanism.client.recipe_viewer.alias.RVAliasHelper;

public class AdditionsAliasMapping implements IAliasMapping {

    @Override
    public <ITEM, FLUID, CHEMICAL> void addAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addItemAliases(AdditionsItems.WALKIE_TALKIE, AdditionsAliases.WALKIE_TALKIE_RADIO);
        rv.addItemAliases(AdditionsTags.Items.GLOW_PANELS, AdditionsAliases.GLOW_PANEL_LIGHT_SOURCE);
        rv.addItemAliases(AdditionsTags.Items.PLASTIC_BLOCKS_ROAD, AdditionsAliases.PLASTIC_ROAD_PATH);
    }
}