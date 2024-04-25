package mekanism.defense.common;

import java.util.concurrent.CompletableFuture;
import mekanism.common.BasePackMetadataGenerator;
import mekanism.common.MekanismDataGenerator;
import mekanism.defense.client.DefenseLangProvider;
import mekanism.defense.common.loot.DefenseLootProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MekanismDefense.MODID, bus = EventBusSubscriber.Bus.MOD)
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
        gen.addProvider(event.includeClient(), new DefenseLangProvider(output));
        //Server side data generators
        gen.addProvider(event.includeServer(), new DefenseTagProvider(output, lookupProvider, existingFileHelper));
        gen.addProvider(event.includeServer(), new DefenseLootProvider(output));
        gen.addProvider(event.includeServer(), new DefenseRecipeProvider(output, existingFileHelper));
    }
}