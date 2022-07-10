package mekanism.additions.client;

import java.util.Map;
import mekanism.additions.client.model.AdditionsModelCache;
import mekanism.additions.client.model.ModelBabyCreeper;
import mekanism.additions.client.render.entity.RenderBabyCreeper;
import mekanism.additions.client.render.entity.RenderBabyEnderman;
import mekanism.additions.client.render.entity.RenderBalloon;
import mekanism.additions.client.render.entity.RenderObsidianTNTPrimed;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.item.ItemBalloon;
import mekanism.additions.common.item.ItemWalkieTalkie;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.text.EnumColor;
import mekanism.client.ClientRegistrationUtil;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.StrayRenderer;
import net.minecraft.client.renderer.entity.WitherSkeletonRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MekanismAdditions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AdditionsClientRegistration {

    private AdditionsClientRegistration() {
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ClientRegistrationUtil.setPropertyOverride(AdditionsItems.WALKIE_TALKIE, MekanismAdditions.rl("channel"), (stack, world, entity, seed) -> {
            ItemWalkieTalkie item = (ItemWalkieTalkie) stack.getItem();
            return item.getOn(stack) ? item.getChannel(stack) : 0;
        }));
    }

    @SubscribeEvent
    public static void registerKeybindings(RegisterKeyMappingsEvent event) {
        AdditionsKeyHandler.registerKeybindings(event);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        //Register entity rendering handlers
        event.registerEntityRenderer(AdditionsEntityTypes.OBSIDIAN_TNT.get(), RenderObsidianTNTPrimed::new);
        event.registerEntityRenderer(AdditionsEntityTypes.BALLOON.get(), RenderBalloon::new);
        event.registerEntityRenderer(AdditionsEntityTypes.BABY_CREEPER.get(), RenderBabyCreeper::new);
        event.registerEntityRenderer(AdditionsEntityTypes.BABY_ENDERMAN.get(), RenderBabyEnderman::new);
        event.registerEntityRenderer(AdditionsEntityTypes.BABY_SKELETON.get(), SkeletonRenderer::new);
        event.registerEntityRenderer(AdditionsEntityTypes.BABY_STRAY.get(), StrayRenderer::new);
        event.registerEntityRenderer(AdditionsEntityTypes.BABY_WITHER_SKELETON.get(), WitherSkeletonRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModelBabyCreeper.CREEPER_LAYER, () -> ModelBabyCreeper.createBodyLayer(CubeDeformation.NONE));
        //Note: Use 1 instead of 2 for size
        event.registerLayerDefinition(ModelBabyCreeper.ARMOR_LAYER, () -> ModelBabyCreeper.createBodyLayer(new CubeDeformation(1)));
    }

    @SubscribeEvent
    public static void registerAdditionalModels(RegisterAdditional event) {
        AdditionsModelCache.INSTANCE.setup(event);
    }

    @SubscribeEvent
    public static void onModelBake(BakingCompleted event) {
        AdditionsModelCache.INSTANCE.onBake(event);
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        registerIColoredBlocks(event);
    }

    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        registerIColoredBlocks(event);
        ItemColor balloonColorHandler = (stack, tintIndex) -> stack.getItem() instanceof ItemBalloon balloon ? MekanismRenderer.getColorARGB(balloon.getColor(), 1) : -1;
        for (ItemRegistryObject<ItemBalloon> balloon : AdditionsItems.BALLOONS.values()) {
            ClientRegistrationUtil.registerItemColorHandler(event, balloonColorHandler, balloon);
        }
    }

    private static void registerIColoredBlocks(RegisterColorHandlersEvent event) {
        registerBlockColorHandles(event, AdditionsBlocks.GLOW_PANELS, AdditionsBlocks.PLASTIC_BLOCKS,
              AdditionsBlocks.SLICK_PLASTIC_BLOCKS, AdditionsBlocks.PLASTIC_GLOW_BLOCKS, AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS, AdditionsBlocks.PLASTIC_ROADS,
              AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS, AdditionsBlocks.PLASTIC_STAIRS, AdditionsBlocks.PLASTIC_SLABS, AdditionsBlocks.PLASTIC_FENCES,
              AdditionsBlocks.PLASTIC_FENCE_GATES, AdditionsBlocks.PLASTIC_GLOW_STAIRS, AdditionsBlocks.PLASTIC_GLOW_SLABS, AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS,
              AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS);
    }

    @SafeVarargs
    private static void registerBlockColorHandles(RegisterColorHandlersEvent event, Map<EnumColor, ? extends BlockRegistryObject<?, ?>>... blocks) {
        for (Map<EnumColor, ? extends BlockRegistryObject<?, ?>> blockMap : blocks) {
            for (BlockRegistryObject<?, ?> block : blockMap.values()) {
                ClientRegistrationUtil.registerIColoredBlockHandler(event, block);
            }
        }
    }
}