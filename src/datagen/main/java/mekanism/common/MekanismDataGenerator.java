package mekanism.common;

import mekanism.client.lang.MekanismLangProvider;
import mekanism.client.model.MekanismBlockModelProvider;
import mekanism.client.model.MekanismItemModelProvider;
import mekanism.client.sound.MekanismSoundProvider;
import mekanism.common.loot.MekanismLootProvider;
import mekanism.common.recipe.MekanismRecipeProvider;
import mekanism.common.tag.MekanismTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = Mekanism.MODID, bus = Bus.MOD)
public class MekanismDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        if (event.includeClient()) {
            //Client side data generators
            gen.addProvider(new MekanismLangProvider(gen));
            gen.addProvider(new MekanismSoundProvider(gen, existingFileHelper));
            gen.addProvider(new MekanismBlockModelProvider(gen, existingFileHelper));
            gen.addProvider(new MekanismItemModelProvider(gen, existingFileHelper));
        }
        if (event.includeServer()) {
            //Server side data generators
            gen.addProvider(new MekanismLootProvider(gen));
            gen.addProvider(new MekanismTagProvider(gen));
            gen.addProvider(new MekanismRecipeProvider(gen));
        }
    }
}