package mekanism.generators.common;

import java.util.function.Consumer;
import mekanism.common.advancements.BaseAdvancementProvider;
import mekanism.generators.common.advancements.GeneratorsAdvancements;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class GeneratorsAdvancementProvider extends BaseAdvancementProvider {

    public GeneratorsAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper, MekanismGenerators.MODID);
    }

    @Override
    protected void registerAdvancements(@NotNull Consumer<Advancement> consumer) {
        advancement(GeneratorsAdvancements.HEAT_GENERATOR)
              .displayAndCriterion(GeneratorsBlocks.HEAT_GENERATOR, FrameType.TASK, true)
              .save(consumer);
        advancement(GeneratorsAdvancements.SOLAR_GENERATOR)
              .displayAndCriterion(GeneratorsBlocks.SOLAR_GENERATOR, FrameType.TASK, false)
              .save(consumer);
        advancement(GeneratorsAdvancements.WIND_GENERATOR)
              .displayAndCriterion(GeneratorsBlocks.WIND_GENERATOR, FrameType.TASK, false)
              .save(consumer);
        advancement(GeneratorsAdvancements.BURN_THE_GAS)
              .displayAndCriterion(GeneratorsBlocks.GAS_BURNING_GENERATOR, FrameType.GOAL, true)
              .save(consumer);
    }
}