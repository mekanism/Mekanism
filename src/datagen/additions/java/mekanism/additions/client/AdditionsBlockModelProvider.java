package mekanism.additions.client;

import mekanism.additions.common.MekanismAdditions;
import mekanism.client.model.BaseBlockModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class AdditionsBlockModelProvider extends BaseBlockModelProvider {

    //TODO: Add helpers for the color block stuff
    public AdditionsBlockModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MekanismAdditions.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }
}