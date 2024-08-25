package mekanism.generators.client.recipe_viewer.alias;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.recipe_viewer.alias.IAliasedTranslation;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.Util;

@NothingNullByDefault
public enum GeneratorsAliases implements IAliasedTranslation {
    FUSION_FUEL("fusion_fuel", "Fusion Fuel"),
    GBG_ETHYLENE("gbg.ethylene", "Ethylene Generator"),
    GBG_ETHENE("gbg.ethene", "Ethene Generator"),
    //Multiblock
    FISSION_COMPONENT("multiblock.fission", "Fission Reactor Multiblock Component"),
    FUSION_COMPONENT("multiblock.fusion", "Fusion Reactor Multiblock Component"),
    TURBINE_COMPONENT("multiblock.turbine", "Turbine Multiblock Component"),
    ;

    private final String key;
    private final String alias;

    GeneratorsAliases(String path, String alias) {
        this.key = Util.makeDescriptionId("alias", MekanismGenerators.rl(path));
        this.alias = alias;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public String getAlias() {
        return alias;
    }
}