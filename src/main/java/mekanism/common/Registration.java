package mekanism.common;

import mekanism.common.entity.MekanismEntityTypes;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Mekanism.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registration {

    //@SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        MekanismBlock.registerBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        MekanismItem.registerItems(registry);
        //MekanismBlock.registerItemBlocks(registry);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        //TODO: Is this supposed to just be on the client side
        MekanismSounds.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        //TODO: Register recipe serializers
        //event.getRegistry().register(ShapedMekanismRecipe.CRAFTING_SHAPED);
    }

    //@SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        MekanismTileEntityTypes.registerTileEntities(event.getRegistry());
    }

    //@SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        MekanismContainerTypes.registerContainers(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        MekanismEntityTypes.registerEntities(event.getRegistry());
    }
}