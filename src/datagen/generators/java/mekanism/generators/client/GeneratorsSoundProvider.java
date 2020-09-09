package mekanism.generators.client;

import mekanism.client.sound.BaseSoundProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsSounds;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class GeneratorsSoundProvider extends BaseSoundProvider {

    public GeneratorsSoundProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen, existingFileHelper, MekanismGenerators.MODID);
    }

    @Override
    protected void addSoundEvents() {
        addSoundEventWithSubtitle(GeneratorsSounds.FUSION_REACTOR, MekanismGenerators.rl("fusion_reactor"));
        addSoundEventWithSubtitle(GeneratorsSounds.FISSION_REACTOR, MekanismGenerators.rl("fission_reactor"));
        addGeneratorSoundEvents();
    }

    private void addGeneratorSoundEvents() {
        String basePath = "generator/";
        addSoundEventWithSubtitle(GeneratorsSounds.BIO_GENERATOR, MekanismGenerators.rl(basePath + "bio"));
        addSoundEventWithSubtitle(GeneratorsSounds.GAS_BURNING_GENERATOR, MekanismGenerators.rl(basePath + "gas_burning"));
        addSoundEventWithSubtitle(GeneratorsSounds.HEAT_GENERATOR, MekanismGenerators.rl(basePath + "heat"));
        addSoundEventWithSubtitle(GeneratorsSounds.SOLAR_GENERATOR, MekanismGenerators.rl(basePath + "solar"));
        addSoundEventWithSubtitle(GeneratorsSounds.WIND_GENERATOR, MekanismGenerators.rl(basePath + "wind"));
    }
}