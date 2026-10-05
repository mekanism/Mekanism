package mekanism.generators.client.model;

import mekanism.client.model.BaseModelCache;
import mekanism.generators.common.MekanismGenerators;

public class GeneratorsModelCache extends BaseModelCache {

    public static final GeneratorsModelCache INSTANCE = new GeneratorsModelCache();

    public final BlockStateModelPartHelper WIND_GENERATOR_BLADES = registerJSON("block/wind_generator_blades");

    private GeneratorsModelCache() {
        super(MekanismGenerators.MODID);
    }
}