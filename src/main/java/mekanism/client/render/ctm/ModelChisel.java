package mekanism.client.render.ctm;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition.Variant;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class ModelChisel implements IModel {

    private static StateMapperBase mapper = new DefaultStateMapper();
    
    private Variant model;
    private Map<String, Variant> models = Maps.newHashMap();;
    
    private String face;
    private Map<EnumFacing, String> overrides = Maps.newHashMap();
    
    private transient ChiselFace faceObj;
    private transient Map<EnumFacing, ChiselFace> overridesObj = new EnumMap<>(EnumFacing.class);
    private transient IBakedModel modelObj;
    
    private transient Map<String, IBakedModel> modelsObj = Maps.newHashMap();
    
    private transient Map<IBlockState, IBakedModel> stateMap = Maps.newHashMap();
    
    private transient List<ResourceLocation> textures = Lists.newArrayList();
    
    @Override
    public Collection<ResourceLocation> getDependencies() {
        List<ResourceLocation> list = Lists.newArrayList(model.getModelLocation());
        list.addAll(models.values().stream().map(v -> v.getModelLocation()).collect(Collectors.toList()));
        return list;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return ImmutableList.copyOf(textures);
    }

    @Override
    @SneakyThrows
    public IFlexibleBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        Function<ResourceLocation, TextureAtlasSprite> dummyGetter = t -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TextureMap.LOCATION_MISSING_TEXTURE.toString());
        modelObj = ModelLoaderRegistry.getModel(model.getModelLocation()).bake(model.isUvLocked() ? new ModelLoader.UVLock(model.getState()) : model.getState(), format, dummyGetter);
        for (Entry<String, Variant> e : models.entrySet()) {
            Variant v = e.getValue();
            modelsObj.put(e.getKey(), ModelLoaderRegistry.getModel(v.getModelLocation()).bake(v.isUvLocked() ? new ModelLoader.UVLock(v.getState()) : v.getState(), format, dummyGetter));
        }
        return new ModelChiselBlock(this);
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }

    public ChiselFace getDefaultFace() {
        return faceObj;
    }

    public ChiselFace getFace(EnumFacing facing) {
        return overridesObj.getOrDefault(facing, faceObj);
    }

    public IBakedModel getModel(IBlockState state) {
        if (state instanceof IExtendedBlockState) {
            state = ((IExtendedBlockState)state).getClean();
        }
        String stateStr = mapper.getPropertyString(state.getProperties());
        stateStr = stateStr.substring(stateStr.indexOf(",") + 1, stateStr.length());
        
        final String capture = stateStr;
        if (modelsObj.containsKey(stateStr)) {
            stateMap.computeIfAbsent(state, s -> modelsObj.get(capture));
        }
        
        return stateMap.getOrDefault(state, modelObj);
    }
}
