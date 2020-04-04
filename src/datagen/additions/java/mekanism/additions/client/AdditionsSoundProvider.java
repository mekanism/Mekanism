package mekanism.additions.client;

import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsSounds;
import mekanism.client.sound.BaseSoundProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class AdditionsSoundProvider extends BaseSoundProvider {

    public AdditionsSoundProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen, existingFileHelper, MekanismAdditions.MODID);
    }

    @Override
    protected void addSoundEvents() {
        addSoundEvent(AdditionsSounds.POP, MekanismAdditions.rl("pop"));
    }
}