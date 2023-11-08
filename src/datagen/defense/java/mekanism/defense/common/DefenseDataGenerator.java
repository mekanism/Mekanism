package mekanism.defense.common;

import java.util.concurrent.CompletableFuture;
import mekanism.common.BasePackMetadataGenerator;
import mekanism.common.MekanismDataGenerator;
import mekanism.defense.client.DefenseLangProvider;
import mekanism.defense.common.loot.DefenseLootProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = MekanismDefense.MODID, bus = Bus.MOD)
public class DefenseDataGenerator {

    private DefenseDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismDefense.MODID);
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        gen.addProvider(true, new BasePackMetadataGenerator(output, DefenseLang.PACK_DESCRIPTION));
        //Client side data generators
        MekanismDataGenerator.addProvider(gen, event.includeClient(), DefenseLangProvider::new);
        //Server side data generators
        gen.addProvider(event.includeServer(), new DefenseTagProvider(output, lookupProvider, existingFileHelper));
        MekanismDataGenerator.addProvider(gen, event.includeServer(), DefenseLootProvider::new);
        gen.addProvider(event.includeServer(), new DefenseRecipeProvider(output, existingFileHelper, lookupProvider));
    }
}