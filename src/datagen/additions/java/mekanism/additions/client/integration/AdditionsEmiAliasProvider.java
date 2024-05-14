package mekanism.additions.client.integration;

import java.util.concurrent.CompletableFuture;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.integration.emi.BaseEmiAliasProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

@NothingNullByDefault
public class AdditionsEmiAliasProvider extends BaseEmiAliasProvider {

    public AdditionsEmiAliasProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, MekanismAdditions.MODID);
    }

    @Override
    protected void addAliases(HolderLookup.Provider lookupProvider) {
        addAliases(AdditionsItems.WALKIE_TALKIE, AdditionsAliases.WALKIE_TALKIE_RADIO);
        addAliases(AdditionsBlocks.GLOW_PANELS.values(), AdditionsAliases.GLOW_PANEL_LIGHT_SOURCE);
        addAliases(AdditionsBlocks.PLASTIC_ROADS.values(), AdditionsAliases.PLASTIC_ROAD_PATH);
    }
}