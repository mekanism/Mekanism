package mekanism.defense.common;

import mekanism.defense.client.DefenseLangProvider;
import mekanism.defense.common.loot.DefenseLootProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = MekanismDefense.MODID, bus = Bus.MOD)
public class DefenseDataGenerator {

    private DefenseDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        if (event.includeClient()) {
            //Client side data generators
            gen.addProvider(new DefenseLangProvider(gen));
        }
        if (event.includeServer()) {
            //Server side data generators
            gen.addProvider(new DefenseTagProvider(gen, existingFileHelper));
            gen.addProvider(new DefenseLootProvider(gen));
            gen.addProvider(new DefenseRecipeProvider(gen));
        }
    }
}