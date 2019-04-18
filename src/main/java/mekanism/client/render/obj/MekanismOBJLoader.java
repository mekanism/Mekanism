package mekanism.client.render.obj;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.client.render.obj.MekanismOBJModel.OBJModelType;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MekanismOBJLoader implements ICustomModelLoader {

    public static final MekanismOBJLoader INSTANCE = new MekanismOBJLoader();
    public static final ImmutableMap<String, String> flipData = ImmutableMap.of("flip-v", String.valueOf(true));
    public static final String[] OBJ_RENDERS = new String[]{"glow_panel"};
    private final Map<ResourceLocation, MekanismOBJModel> modelCache = new HashMap<>();

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        GlowPanelModel.forceRebake();

        IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        for (String s : OBJ_RENDERS) {
            ModelResourceLocation model = new ModelResourceLocation("mekanism:" + s, "inventory");
            IBakedModel bakedModel = modelRegistry.getObject(model);
            modelRegistry.putObject(model, createBakedObjItemModel(bakedModel, "mekanism:models/block/" + s +
                  ".obj.mek", new OBJState(Lists.newArrayList(OBJModel.Group.ALL), true), DefaultVertexFormats.ITEM));
        }
    }

    public OBJBakedModel createBakedObjItemModel(IBakedModel existingModel, String name, IModelState state,
          VertexFormat format) {
        try {
            Function<ResourceLocation, TextureAtlasSprite> textureGetter = location -> Minecraft.getMinecraft()
                  .getTextureMapBlocks().getAtlasSprite(location.toString());

            ResourceLocation modelLocation = new ResourceLocation(name);
            OBJModel objModel = (OBJModel) OBJLoader.INSTANCE.loadModel(modelLocation);
            objModel = (OBJModel) objModel.process(flipData);
            ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
            builder.put(ModelLoader.White.LOCATION.toString(), ModelLoader.White.INSTANCE);
            TextureAtlasSprite missing = textureGetter.apply(new ResourceLocation("missingno"));

            for (String s : objModel.getMatLib().getMaterialNames()) {
                ResourceLocation texture = objModel.getMatLib().getMaterial(s).getTexture().getTextureLocation();
                if (texture.getPath().startsWith("#")) {
                    Mekanism.logger.error("OBJLoader: Unresolved texture '{}' for obj model '{}'", texture.getPath(),
                          modelLocation);
                    builder.put(s, missing);
                } else {
                    builder.put(s, textureGetter.apply(texture));
                }
            }

            builder.put("missingno", missing);

            return new GlowPanelModel(existingModel, objModel, state, format, builder.build(), new HashMap<>());
        } catch (Exception e) {
            Mekanism.logger.error("Failed to load OBJ", e);
        }

        return null;
    }

    @Override
    public boolean accepts(@Nonnull ResourceLocation modelLocation) {
        return modelLocation.getPath().endsWith(".obj.mek");
    }

    @Nonnull
    @Override
    public IModel loadModel(@Nonnull ResourceLocation loc) throws Exception {
        ResourceLocation file = new ResourceLocation(loc.getNamespace(), loc.getPath());

        if (!modelCache.containsKey(file)) {
            IModel model = OBJLoader.INSTANCE.loadModel(file);

            if (model instanceof OBJModel) {
                if (file.getPath().contains("glow_panel")) {
                    MekanismOBJModel mekModel = new MekanismOBJModel(OBJModelType.GLOW_PANEL,
                          ((OBJModel) model).getMatLib(), file);
                    modelCache.put(file, mekModel);
                } else if (file.getPath().contains("transmitter")) {
                    MekanismOBJModel mekModel = new MekanismOBJModel(OBJModelType.TRANSMITTER,
                          ((OBJModel) model).getMatLib(), file);
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
}
