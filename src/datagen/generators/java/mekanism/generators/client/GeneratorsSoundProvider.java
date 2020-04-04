package mekanism.generators.client;

import mekanism.client.sound.BaseSoundProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsSounds;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class GeneratorsSoundProvider extends BaseSoundProvider {

    public GeneratorsSoundProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen, existingFileHelper, MekanismGenerators.MODID);
    }

    @Override
    protected void addSoundEvents() {
        addSoundEvent(GeneratorsSounds.FUSION_REACTOR, MekanismGenerators.rl("fusion_reactor"));
        addGeneratorSoundEvents();
    }

    private void addGeneratorSoundEvents() {
        String basePath = "generator/";
        addSoundEvent(GeneratorsSounds.BIO_GENERATOR, MekanismGenerators.rl(basePath + "bio"));
        addSoundEvent(GeneratorsSounds.GAS_BURNING_GENERATOR, MekanismGenerators.rl(basePath + "gas_burning"));
        addSoundEvent(GeneratorsSounds.HEAT_GENERATOR, MekanismGenerators.rl(basePath + "heat"));
        addSoundEvent(GeneratorsSounds.SOLAR_GENERATOR, MekanismGenerators.rl(basePath + "solar"));
        addSoundEvent(GeneratorsSounds.WIND_GENERATOR, MekanismGenerators.rl(basePath + "wind"));
    }
}