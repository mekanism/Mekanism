package mekanism.chemistry.client;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.client.sound.BaseSoundProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ChemistrySoundProvider extends BaseSoundProvider {
    public ChemistrySoundProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen, existingFileHelper, MekanismChemistry.MODID);
    }

    @Override
    public void registerSounds() {
    }
}
