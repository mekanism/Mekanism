package mekanism.generators.common;

import java.util.function.Consumer;
import mekanism.common.advancements.BaseAdvancementProvider;
import mekanism.generators.common.advancements.GeneratorsAdvancements;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.core.HolderLookup;

public class GeneratorsAdvancementProvider extends BaseAdvancementProvider {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer) {
        advancement(GeneratorsAdvancements.HEAT_GENERATOR)
              .displayAndCriterion(GeneratorsBlocks.HEAT_GENERATOR, AdvancementType.TASK, true)
              .save(consumer);
        advancement(GeneratorsAdvancements.SOLAR_GENERATOR)
              .displayAndCriterion(GeneratorsBlocks.SOLAR_GENERATOR, AdvancementType.TASK, false)
              .save(consumer);
        advancement(GeneratorsAdvancements.WIND_GENERATOR)
              .displayAndCriterion(GeneratorsBlocks.WIND_GENERATOR, AdvancementType.TASK, false)
              .save(consumer);
        advancement(GeneratorsAdvancements.BURN_THE_GAS)
              .displayAndCriterion(GeneratorsBlocks.GAS_BURNING_GENERATOR, AdvancementType.GOAL, true)
              .save(consumer);
    }
}