package mekanism.common;

import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.RecipeCacheManager;
import mekanism.common.tags.MekanismTagManager;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

//TODO: We may want to just move this stuff into the main Mekanism class
@Mod.EventBusSubscriber(modid = Mekanism.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registration {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void buildRegistry(RegistryEvent.NewRegistry event) {
        //TODO: Come up with a better way than just doing it on low to make sure this happens AFTER the registries are initialized?
        Mekanism.instance.setTagManager(new MekanismTagManager());
    }

    @SubscribeEvent
    public static void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        MekanismRecipeType.registerRecipeTypes(event.getRegistry());
        //TODO: Register a custom shaped crafting recipe serializer if needed

        Mekanism.instance.setRecipeCacheManager(new RecipeCacheManager());
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.Reloading configEvent) {
        //TODO: Handle reloading
    }
}