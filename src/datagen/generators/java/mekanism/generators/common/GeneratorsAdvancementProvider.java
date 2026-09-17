package mekanism.generators.common;

import mekanism.common.advancements.BaseAdvancementProvider;
import mekanism.generators.common.advancements.GeneratorsAdvancements;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.data.worldgen.BootstrapContext;

public class GeneratorsAdvancementProvider extends BaseAdvancementProvider {

    protected GeneratorsAdvancementProvider(BootstrapContext<Advancement> output) {
        super(output);
    }

    @Override
    public void generate() {
        advancement(GeneratorsAdvancements.HEAT_GENERATOR)
              .displayAndCriterion(GeneratorsBlocks.HEAT_GENERATOR, AdvancementType.TASK, true)
              .save(output);
        advancement(GeneratorsAdvancements.SOLAR_GENERATOR)
              .displayAndCriterion(GeneratorsBlocks.SOLAR_GENERATOR, AdvancementType.TASK, false)
              .save(output);
        advancement(GeneratorsAdvancements.WIND_GENERATOR)
              .displayAndCriterion(GeneratorsBlocks.WIND_GENERATOR, AdvancementType.TASK, false)
              .save(output);
        advancement(GeneratorsAdvancements.BURN_THE_GAS)
              .displayAndCriterion(GeneratorsBlocks.GAS_BURNING_GENERATOR, AdvancementType.GOAL, true)
              .save(output);
    }
}