package mekanism.client.integration.gender;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPI;
import mekanism.api.ModBasedService;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public interface IMekGenderDatagen extends ModBasedService {

    Map<String, IMekGenderDatagen> INSTANCES = MekanismAPI.getModBasedServices(IMekGenderDatagen.class);

    DataProvider armorProvider(PackOutput output, CompletableFuture<Provider> registries);
}