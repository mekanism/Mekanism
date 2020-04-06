package mekanism.additions.client;

import mekanism.additions.common.MekanismAdditions;
import mekanism.client.model.BaseItemModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class AdditionsItemModelProvider extends BaseItemModelProvider {

    public AdditionsItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MekanismAdditions.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }
}