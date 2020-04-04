package mekanism.additions.common;

import mekanism.additions.client.AdditionsLangProvider;
import mekanism.additions.client.AdditionsSoundProvider;
import mekanism.additions.common.loot.AdditionsLootProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = MekanismAdditions.MODID, bus = Bus.MOD)
public class AdditionsDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        if (event.includeClient()) {
            //Client side data generators
            gen.addProvider(new AdditionsLangProvider(gen));
            gen.addProvider(new AdditionsSoundProvider(gen, existingFileHelper));
        }
        if (event.includeServer()) {
            //Server side data generators
            gen.addProvider(new AdditionsLootProvider(gen));
            gen.addProvider(new AdditionsTagProvider(gen));
            gen.addProvider(new AdditionsRecipeProvider(gen));
        }
    }
}