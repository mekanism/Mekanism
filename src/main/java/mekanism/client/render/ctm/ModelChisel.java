package mekanism.client.render.ctm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.TRSRTransformation;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class ModelChisel implements IModel {

    private static StateMapperBase mapper = new DefaultStateMapper();
    
    private Map<EnumFacing, String> overrides = Maps.newHashMap();
    
    private transient ChiselFace faceObj;
    private transient Map<EnumFacing, ChiselFace> overridesObj = new EnumMap<>(EnumFacing.class);
    
    private transient IBakedModel modelObj;
    
    private transient Map<IBlockState, IBakedModel> stateMap = Maps.newHashMap();
    
    private transient List<ResourceLocation> textures = Lists.newArrayList();
    
    public String modelName;
    
    public ModelChisel(IBakedModel model, String name)
    {
    	modelObj = model;
    	modelName = name;
    }
    
    @Override
    public Collection<ResourceLocation> getDependencies() {
    	return new ArrayList<ResourceLocation>();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return ImmutableList.copyOf(textures);
    }

    @Override
    public IFlexibleBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return null;
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }
    
    void load() {
        if (faceObj != null) {
            return;
        }
        System.out.println("LOADING MODEL");
        faceObj = TextureStitcher.createFace(modelName);
        faceObj.getTextureList().forEach(t -> textures.addAll(t.getTextures()));
        overridesObj.values().forEach(f -> f.getTextureList().forEach(t -> textures.addAll(t.getTextures())));
    }

    public ChiselFace getDefaultFace() {
        return faceObj;
    }

    public ChiselFace getFace(EnumFacing facing) {
        return overridesObj.getOrDefault(facing, faceObj);
    }

    public IBakedModel getModel(IBlockState state) {
        return modelObj;
    }
}
