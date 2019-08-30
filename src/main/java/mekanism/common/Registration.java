package mekanism.common;

import mekanism.api.MekanismAPI;
import mekanism.api.gas.Gas;
import mekanism.common.entity.MekanismEntityTypes;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.MekanismRecipeEnabledCondition;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = Mekanism.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registration {

    @SubscribeEvent
    public static void buildRegistry(RegistryEvent.NewRegistry event) {
        //TODO: Should this be declared in the API package
        MekanismAPI.GAS_REGISTRY = new RegistryBuilder<Gas>().setName(new ResourceLocation(Mekanism.MODID, "gas")).setType(Gas.class).create();
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
        //TODO: Is this supposed to just be on the client side
        MekanismGases.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerFluids(RegistryEvent.Register<Fluid> event) {
        MekanismGases.registerFluids(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        //TODO: Register recipe serializers
        //event.getRegistry().register(ShapedMekanismRecipe.CRAFTING_SHAPED);
        //TODO: Is this the correct place to register this
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