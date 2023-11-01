package mekanism.generators.client;

import mekanism.client.sound.BaseSoundProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class GeneratorsSoundProvider extends BaseSoundProvider {

    public GeneratorsSoundProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, existingFileHelper, MekanismGenerators.MODID);
    }

    @Override
    public void registerSounds() {
        addSoundEventWithSubtitle(GeneratorsSounds.FUSION_REACTOR, "fusion_reactor");
        addSoundEventWithSubtitle(GeneratorsSounds.FISSION_REACTOR, "fission_reactor");
        addGeneratorSoundEvents();
    }

    private void addGeneratorSoundEvents() {
        String basePath = "generator/";
        addSoundEventWithSubtitle(GeneratorsSounds.BIO_GENERATOR, basePath + "bio");
        addSoundEventWithSubtitle(GeneratorsSounds.GAS_BURNING_GENERATOR, basePath + "gas_burning");
        addSoundEventWithSubtitle(GeneratorsSounds.HEAT_GENERATOR, basePath + "heat");
        //Use a reduced attenuation range for passive generators
        addSoundEventWithSubtitle(GeneratorsSounds.SOLAR_GENERATOR, basePath + "solar", sound -> sound.attenuationDistance(8));
        addSoundEventWithSubtitle(GeneratorsSounds.WIND_GENERATOR, basePath + "wind", sound -> sound.attenuationDistance(8));
    }
}