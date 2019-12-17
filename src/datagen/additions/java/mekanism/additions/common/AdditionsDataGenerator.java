package mekanism.additions.common;

import mekanism.additions.client.AdditionsLangGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = MekanismAdditions.MODID, bus = Bus.MOD)
public class AdditionsDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        if (event.includeClient()) {
            //Client side data generators
            gen.addProvider(new AdditionsLangGenerator(gen));
        }
        if (event.includeServer()) {
            //Server side data generators
        }
    }
}