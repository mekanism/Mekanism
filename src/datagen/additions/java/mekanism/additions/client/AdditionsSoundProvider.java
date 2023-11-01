package mekanism.additions.client;

import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsSounds;
import mekanism.client.sound.BaseSoundProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class AdditionsSoundProvider extends BaseSoundProvider {

    public AdditionsSoundProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, existingFileHelper, MekanismAdditions.MODID);
    }

    @Override
    public void registerSounds() {
        addSoundEventWithSubtitle(AdditionsSounds.POP, "pop");
    }
}