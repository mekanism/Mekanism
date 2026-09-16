package mekanism.tools.client.integration.gender;

import java.util.concurrent.CompletableFuture;
import mekanism.client.integration.gender.IMekGenderDatagen;
import mekanism.tools.common.MekanismTools;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class ToolsGenderDatagen implements IMekGenderDatagen {

    @Override
    public DataProvider armorProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new ToolsGenderArmorProvider(output, registries, modid());
    }

    @Override
    public String modid() {
        return MekanismTools.MODID;
    }
}