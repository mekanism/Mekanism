package mekanism.generators.common;

import mekanism.generators.client.GeneratorsLangGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = MekanismGenerators.MODID, bus = Bus.MOD)
public class GeneratorsDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        if (event.includeClient()) {
            //Client side data generators
            gen.addProvider(new GeneratorsLangGenerator(gen));
        }
        if (event.includeServer()) {
            //Server side data generators
        }
    }
}