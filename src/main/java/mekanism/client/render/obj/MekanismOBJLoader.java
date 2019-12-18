//TODO: 1.15
/*package mekanism.client.render.obj;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MekanismOBJLoader implements ICustomModelLoader {

    public static final MekanismOBJLoader INSTANCE = new MekanismOBJLoader();
    public static final ImmutableMap<String, String> flipData = ImmutableMap.of("flip-v", String.valueOf(true));
    public static final String[] SMALL_OBJ_RENDERS = new String[]{"basic_universal_cable", "advanced_universal_cable", "elite_universal_cable",
                                                                  "ultimate_universal_cable", "basic_pressurized_tube", "advanced_pressurized_tube",
                                                                  "elite_pressurized_tube", "ultimate_pressurized_tube", "basic_thermodynamic_conductor",
                                                                  "advanced_thermodynamic_conductor", "elite_thermodynamic_conductor",
                                                                  "ultimate_thermodynamic_conductor"};
    public static final String[] LARGE_OBJ_RENDERS = new String[]{"basic_mechanical_pipe", "advanced_mechanical_pipe", "elite_mechanical_pipe",
                                                                  "ultimate_mechanical_pipe", "diversion_transporter", "restrictive_transporter",
                                                                  "basic_logistical_transporter", "advanced_logistical_transporter", "elite_logistical_transporter",
                                                                  "ultimate_logistical_transporter"};
    private final Map<ResourceLocation, MekanismOBJModel> modelCache = new HashMap<>();

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        OBJLoader.INSTANCE.addDomain(Mekanism.MODID);
        ModelLoaderRegistry.registerLoader(INSTANCE);
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
        if (!event.getMap().getBasePath().equals("textures")) {
            return;
        }
        for (String name : SMALL_OBJ_RENDERS) {
            event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/" + name + "_vertical"));
            event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/" + name));
            event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/opaque/" + name + "_vertical"));
        }
        for (String name : LARGE_OBJ_RENDERS) {
            event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/" + name + "_vertical"));
            event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/" + name));
            event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/opaque/" + name + "_vertical"));
            event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/opaque/" + name));
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        for (String name : SMALL_OBJ_RENDERS) {
            registerModel(modelRegistry, "models/block/transmitter_small.obj.mek", name, true);
        }
        for (String name : LARGE_OBJ_RENDERS) {
            registerModel(modelRegistry, "models/block/transmitter_large.obj.mek", name, false);
        }
    }

    private static void registerModel(Map<ResourceLocation, IBakedModel> modelRegistry, String objFile, String name, boolean small) {
        ResourceLocation resource = new ResourceLocation(Mekanism.MODID, name);

        ImmutableMap<String, String> textures = small ? getSmallTextures(name) : getLargeTextures(name);
        registerModel(modelRegistry, objFile, textures, new ModelResourceLocation(resource, "inventory"));
        registerModel(modelRegistry, objFile, textures, new ModelResourceLocation(resource, "waterlogged=false"));
        registerModel(modelRegistry, objFile, textures, new ModelResourceLocation(resource, "waterlogged=true"));
        //TODO: Is this needed given we have the waterlogged
        registerModel(modelRegistry, objFile, textures, new ModelResourceLocation(resource, ""));
    }

    private static void registerModel(Map<ResourceLocation, IBakedModel> modelRegistry, String objFile, ImmutableMap<String, String> textures, ModelResourceLocation mrl) {
        IBakedModel bakedNormal = modelRegistry.get(mrl);
        modelRegistry.put(mrl, createBakedObjModel(bakedNormal, objFile, new OBJState(Lists.newArrayList(OBJModel.Group.ALL), true),
              DefaultVertexFormats.field_227849_i_, textures));
    }

    public static OBJBakedModel createBakedObjModel(IBakedModel existingModel, String name, IModelState state, VertexFormat format, ImmutableMap<String, String> textures) {
        try {
            Function<ResourceLocation, TextureAtlasSprite> textureGetter = location -> Minecraft.getInstance().getTextureMap().getAtlasSprite(location.toString());

            ResourceLocation modelLocation = new ResourceLocation(Mekanism.MODID, name);
            OBJModel objModel = (OBJModel) OBJLoader.INSTANCE.loadModel(modelLocation);
            objModel = (OBJModel) objModel.process(flipData);
            objModel = (OBJModel) objModel.retexture(textures);
            ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
            builder.put(ModelLoader.White.LOCATION.toString(), ModelLoader.White.INSTANCE);
            TextureAtlasSprite missing = textureGetter.apply(new ResourceLocation("missingno"));

            for (String s : objModel.getMatLib().getMaterialNames()) {
                Texture tex = objModel.getMatLib().getMaterial(s).getTexture();
                String texture = tex.getPath();
                if (texture.startsWith("#")) {
                    Mekanism.logger.error("OBJLoader: Unresolved texture '{}' for obj model '{}'", texture, modelLocation);
                    builder.put(s, missing);
                } else {
                    builder.put(s, textureGetter.apply(tex.getTextureLocation()));
                }
            }

            builder.put("missingno", missing);
            return new TransmitterModel(existingModel, objModel, state, format, builder.build(), new HashMap<>());
        } catch (Exception e) {
            Mekanism.logger.error("Failed to load OBJ", e);
        }
        return null;
    }

    private static ImmutableMap<String, String> getSmallTextures(String name) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("#side", "mekanism:block/models/multipart/" + name + "_vertical");
        builder.put("#center", "mekanism:block/models/multipart/" + name);
        builder.put("#side_opaque", "mekanism:block/models/multipart/opaque/" + name + "_vertical");
        builder.put("#center_opaque", "mekanism:block/models/multipart/" + name);
        return builder.build();
    }

    private static ImmutableMap<String, String> getLargeTextures(String name) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("#side", "mekanism:block/models/multipart/" + name + "_vertical");
        builder.put("#center", "mekanism:block/models/multipart/" + name);
        builder.put("#side_opaque", "mekanism:block/models/multipart/opaque/" + name + "_vertical");
        builder.put("#center_opaque", "mekanism:block/models/multipart/opaque/" + name);
        return builder.build();
    }

    @Override
    public boolean accepts(@Nonnull ResourceLocation modelLocation) {
        return modelLocation.getPath().endsWith(".obj.mek");
    }

    @Nonnull
    @Override
    public IUnbakedModel loadModel(@Nonnull ResourceLocation loc) throws Exception {
        ResourceLocation file = new ResourceLocation(loc.getNamespace(), loc.getPath());
        if (!modelCache.containsKey(file)) {
            IUnbakedModel model = OBJLoader.INSTANCE.loadModel(file);
            if (model instanceof OBJModel) {
                if (file.getPath().contains("transmitter")) {
                    MekanismOBJModel mekModel = new MekanismOBJModel(((OBJModel) model).getMatLib(), file);
                    modelCache.put(file, mekModel);
                }
            }
        }

        MekanismOBJModel mekModel = modelCache.get(file);
        if (mekModel == null) {
            return ModelLoaderRegistry.getMissingModel();
        }
        return mekModel;
    }

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
        modelCache.clear();
    }
}*/