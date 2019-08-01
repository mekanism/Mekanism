package mekanism.generators.common.block.states;

import java.util.Arrays;
import java.util.List;
import mekanism.common.base.IBlockType;
import mekanism.common.config.MekanismConfig;

public enum GeneratorType implements IBlockType {
    HEAT_GENERATOR,
    SOLAR_GENERATOR,
    GAS_GENERATOR,
    BIO_GENERATOR,
    ADVANCED_SOLAR_GENERATOR,
    WIND_GENERATOR;

    public static List<GeneratorType> getGeneratorsForConfig() {
        return Arrays.asList(values());
    }

    @Override
    public String getBlockName() {
        return "";
    }

    @Override
    public boolean isEnabled() {
        return MekanismConfig.current().generators.generatorsManager.isEnabled(this);
    }
}