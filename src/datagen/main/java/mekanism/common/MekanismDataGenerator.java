package mekanism.common;

import mekanism.client.MekanismLangProvider;
import mekanism.common.loot.MekanismLootProvider;
import mekanism.common.recipe.MekanismRecipeProvider;
import mekanism.common.tag.MekanismTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = Mekanism.MODID, bus = Bus.MOD)
public class MekanismDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        if (event.includeClient()) {
            //Client side data generators
            gen.addProvider(new MekanismLangProvider(gen));
        }
        if (event.includeServer()) {
            //Server side data generators
            gen.addProvider(new MekanismLootProvider(gen));
            gen.addProvider(new MekanismTagProvider(gen));
            gen.addProvider(new MekanismRecipeProvider(gen));
        }
    }
}