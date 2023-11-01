package mekanism.additions.client;

import mekanism.additions.common.MekanismAdditions;
import mekanism.client.model.BaseBlockModelProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class AdditionsBlockModelProvider extends BaseBlockModelProvider {

    //TODO: Add helpers for the color block stuff
    public AdditionsBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MekanismAdditions.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }
}