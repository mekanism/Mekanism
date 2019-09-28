package mekanism.common;

import mekanism.api.gas.Gas;
import mekanism.api.infuse.InfuseType;
import mekanism.common.entity.MekanismEntityTypes;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.MekanismRecipeEnabledCondition;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tags.MekanismTagManager;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Mekanism.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registration {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void buildRegistry(RegistryEvent.NewRegistry event) {
        //TODO: Come up with a better way than just doing it on low to make sure this happens AFTER the registries are initialized?
        Mekanism.instance.setTagManager(new MekanismTagManager());
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        MekanismBlock.registerBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        MekanismItem.registerItems(registry);
        MekanismBlock.registerItemBlocks(registry);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        //TODO: Is this supposed to just be on the client side
        MekanismSounds.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerGases(RegistryEvent.Register<Gas> event) {
        MekanismGases.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerInfuseTypes(RegistryEvent.Register<InfuseType> event) {
        MekanismInfuseTypes.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerFluids(RegistryEvent.Register<Fluid> event) {
        MekanismGases.registerFluids(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        MekanismRecipeType.registerRecipeTypes(event.getRegistry());
        MekanismRecipeSerializers.registerRecipeSerializers(event.getRegistry());
        //TODO: Register a custom shaped crafting recipe serializer if needed
        //TODO: Move this to MekanismRecipeSerializers??
        CraftingHelper.register(MekanismRecipeEnabledCondition.Serializer.INSTANCE);
    }

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        MekanismTileEntityTypes.registerTileEntities(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        MekanismContainerTypes.registerContainers(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        MekanismEntityTypes.registerEntities(event.getRegistry());
    }
}