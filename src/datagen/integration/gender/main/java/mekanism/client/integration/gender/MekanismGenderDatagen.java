package mekanism.client.integration.gender;

import java.util.concurrent.CompletableFuture;
import mekanism.common.Mekanism;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class MekanismGenderDatagen implements IMekGenderDatagen {

    @Override
    public DataProvider armorProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new MekanismGenderArmorProvider(output, registries, modid());
    }

    @Override
    public String modid() {
        return Mekanism.MODID;
    }
}