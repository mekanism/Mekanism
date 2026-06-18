package mekanism.additions.client;

import java.util.Map;
import mekanism.additions.client.model.AdditionsModelCache;
import mekanism.additions.client.model.BabyModelLayers;
import mekanism.additions.client.model.ModelBabyCreeper;
import mekanism.additions.client.model.ModelBabyEnderman;
import mekanism.additions.client.render.entity.BabyBoggedRenderer;
import mekanism.additions.client.render.entity.BabyCreeperRenderer;
import mekanism.additions.client.render.entity.BabyEndermanRenderer;
import mekanism.additions.client.render.entity.BabyParchedRenderer;
import mekanism.additions.client.render.entity.BabySkeletonRenderer;
import mekanism.additions.client.render.entity.BabyStrayRenderer;
import mekanism.additions.client.render.entity.BabyWitherSkeletonRenderer;
import mekanism.additions.client.render.entity.BalloonRenderer;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.api.text.EnumColor;
import mekanism.client.ClientRegistrationUtil;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.model.monster.skeleton.BoggedModel;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = MekanismAdditions.MODID, value = Dist.CLIENT)
public class AdditionsClientRegistration {

    private AdditionsClientRegistration() {
    }

    @SubscribeEvent
    public static void registerKeybindings(RegisterKeyMappingsEvent event) {
        AdditionsKeyHandler.registerKeybindings(event);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        //Register entity rendering handlers
        event.registerEntityRenderer(AdditionsEntityTypes.OBSIDIAN_TNT.get(), TntRenderer::new);
        event.registerEntityRenderer(AdditionsEntityTypes.BALLOON.get(), BalloonRenderer::new);
        event.registerEntityRenderer(AdditionsEntityTypes.BABY_BOGGED.get(), BabyBoggedRenderer::new);
        event.registerEntityRenderer(AdditionsEntityTypes.BABY_CREEPER.get(), BabyCreeperRenderer::new);
        event.registerEntityRenderer(AdditionsEntityTypes.BABY_ENDERMAN.get(), BabyEndermanRenderer::new);
        event.registerEntityRenderer(AdditionsEntityTypes.BABY_PARCHED.get(), BabyParchedRenderer::new);
        event.registerEntityRenderer(AdditionsEntityTypes.BABY_SKELETON.get(), BabySkeletonRenderer::new);
        event.registerEntityRenderer(AdditionsEntityTypes.BABY_STRAY.get(), BabyStrayRenderer::new);
        event.registerEntityRenderer(AdditionsEntityTypes.BABY_WITHER_SKELETON.get(), BabyWitherSkeletonRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ArmorModelSet<LayerDefinition> humanoidBabyArmor = HumanoidModel.createBabyArmorMeshSet(
              LayerDefinitions.BABY_INNER_ARMOR_DEFORMATION, LayerDefinitions.BABY_OUTER_ARMOR_DEFORMATION, PartPose.ZERO
        ).map(mesh -> LayerDefinition.create(mesh, 64, 64));
        LayerDefinition babySkeletonBodyLayer = SkeletonModel.createBodyLayer().apply(HumanoidModel.BABY_TRANSFORMER);

        event.registerLayerDefinition(BabyModelLayers.BABY_BOGGED, () -> BoggedModel.createBodyLayer().apply(HumanoidModel.BABY_TRANSFORMER));
        addBabyArmors(event, BabyModelLayers.BABY_BOGGED_ARMOR, humanoidBabyArmor);
        //Copy of vanilla's bogged outer layer, but with the baby transformation applied
        event.registerLayerDefinition(BabyModelLayers.BABY_BOGGED_OUTER_LAYER, () -> LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.2F), 0.0F), 64, 32)
              .apply(HumanoidModel.BABY_TRANSFORMER));

        event.registerLayerDefinition(BabyModelLayers.BABY_PARCHED, () -> SkeletonModel.createSingleModelDualBodyLayer().apply(HumanoidModel.BABY_TRANSFORMER));
        addBabyArmors(event, BabyModelLayers.BABY_PARCHED_ARMOR, humanoidBabyArmor);
        //Note: It seems vanilla doesn't use the outer layer that they add?
        //addBabyArmors(event, BabyModelLayers.BABY_PARCHED_OUTER_LAYER, LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.25F), 0.0F), 64, 32).apply(HumanoidModel.BABY_TRANSFORMER));

        event.registerLayerDefinition(BabyModelLayers.BABY_SKELETON, () -> babySkeletonBodyLayer);
        addBabyArmors(event, BabyModelLayers.BABY_SKELETON_ARMOR, humanoidBabyArmor);

        event.registerLayerDefinition(BabyModelLayers.BABY_STRAY, () -> babySkeletonBodyLayer);
        addBabyArmors(event, BabyModelLayers.BABY_STRAY_ARMOR, humanoidBabyArmor);
        //Copy of vanilla's stray outer layer, but with the baby transformation applied
        event.registerLayerDefinition(BabyModelLayers.BABY_STRAY_OUTER_LAYER, () -> LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.25F), 0.0F), 64, 32)
              .apply(HumanoidModel.BABY_TRANSFORMER));

        MeshTransformer witherSkeletonScale = MeshTransformer.scaling(1.2F);
        event.registerLayerDefinition(BabyModelLayers.BABY_WITHER_SKELETON, () -> babySkeletonBodyLayer.apply(witherSkeletonScale));
        addBabyArmors(event, BabyModelLayers.BABY_WITHER_SKELETON_ARMOR, humanoidBabyArmor.map(layer -> layer.apply(witherSkeletonScale)));

        event.registerLayerDefinition(BabyModelLayers.BABY_CREEPER, () -> ModelBabyCreeper.createBodyLayer(CubeDeformation.NONE));
        //Note: Use 1 instead of 2 for size
        event.registerLayerDefinition(BabyModelLayers.BABY_CREEPER_ARMOR, () -> ModelBabyCreeper.createBodyLayer(new CubeDeformation(1)));

        event.registerLayerDefinition(BabyModelLayers.BABY_ENDERMAN, () -> EndermanModel.createBodyLayer().apply(ModelBabyEnderman.BABY_MODEL_TRANSFORM));
    }

    private static void addBabyArmors(EntityRenderersEvent.RegisterLayerDefinitions event, ArmorModelSet<ModelLayerLocation> armor, ArmorModelSet<LayerDefinition> values) {
        event.registerLayerDefinition(armor.head(), values::head);
        event.registerLayerDefinition(armor.chest(), values::chest);
        event.registerLayerDefinition(armor.legs(), values::legs);
        event.registerLayerDefinition(armor.feet(), values::feet);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        ClientRegistrationUtil.registerBlockExtensions(event, AdditionsBlocks.BLOCKS);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterStandalone event) {
        AdditionsModelCache.INSTANCE.setup(event);
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.BakingCompleted event) {
        AdditionsModelCache.INSTANCE.onBake(event);
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.BlockTintSources event) {
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