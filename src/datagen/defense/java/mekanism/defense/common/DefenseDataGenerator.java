package mekanism.defense.common;

import java.util.concurrent.CompletableFuture;
import mekanism.common.BasePackMetadataGenerator;
import mekanism.common.MekanismDataGenerator;
import mekanism.defense.client.DefenseLangProvider;
import mekanism.defense.common.loot.DefenseLootProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = MekanismDefense.MODID, bus = Bus.MOD)
public class DefenseDataGenerator {

    private DefenseDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismDefense.MODID);
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        addProvider(gen, true, output -> new BasePackMetadataGenerator(output, DefenseLang.PACK_DESCRIPTION));
        //Client side data generators
        addProvider(gen, event.includeClient(), DefenseLangProvider::new);
        //Server side data generators
        addProvider(gen, event.includeServer(), output -> new DefenseTagProvider(output, lookupProvider, existingFileHelper));
        addProvider(gen, event.includeServer(), DefenseLootProvider::new);
        addProvider(gen, event.includeServer(), output -> new DefenseRecipeProvider(output, existingFileHelper));
    }

    private static <PROVIDER extends DataProvider> void addProvider(DataGenerator gen, boolean run, DataProvider.Factory<PROVIDER> factory) {
        gen.addProvider(run, factory);
    }
}