package mekanism.defense.common;

import mekanism.common.MekanismDataGenerator;
import mekanism.defense.client.DefenseLangProvider;
import mekanism.defense.common.loot.DefenseLootProvider;
import net.minecraft.data.DataGenerator;
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
        //Client side data generators
        gen.addProvider(event.includeClient(), new DefenseLangProvider(gen));
        //Server side data generators
        gen.addProvider(event.includeServer(), new DefenseTagProvider(gen, existingFileHelper));
        gen.addProvider(event.includeServer(), new DefenseLootProvider(gen));
        gen.addProvider(event.includeServer(), new DefenseRecipeProvider(gen, existingFileHelper));
    }
}