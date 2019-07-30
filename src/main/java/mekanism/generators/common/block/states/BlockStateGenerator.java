package mekanism.generators.common.block.states;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import mekanism.common.base.IBlockType;
import mekanism.common.config.MekanismConfig;
import net.minecraft.block.Block;

public class BlockStateGenerator {

    public enum GeneratorType implements IBlockType {
        HEAT_GENERATOR(false),
        SOLAR_GENERATOR(false),
        GAS_GENERATOR(false),
        BIO_GENERATOR(false),
        ADVANCED_SOLAR_GENERATOR(false),
        WIND_GENERATOR(false),

        TURBINE_ROTOR(true),
        ROTATIONAL_COMPLEX(true),
        ELECTROMAGNETIC_COIL(true),
        TURBINE_CASING(true),
        TURBINE_VALVE(true),
        TURBINE_VENT(true),
        SATURATING_CONDENSER(true);

        private boolean notGenerator;

        GeneratorType(boolean notGenerator) {
            this.notGenerator = notGenerator;
        }

        public static List<GeneratorType> getGeneratorsForConfig() {
            return Arrays.stream(values()).filter(type -> !type.notGenerator).collect(Collectors.toList());
        }

        public static GeneratorType get(Block block, int meta) {
            return null;
        }

        @Override
        public String getBlockName() {
            return "";
        }

        @Override
        public boolean isEnabled() {
            return notGenerator || MekanismConfig.current().generators.generatorsManager.isEnabled(this);
        }
    }
}