package mekanism.client.render.obj;

import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.obj.ObjModel;
import org.jetbrains.annotations.Nullable;

public class TransmitterModel implements IUnbakedGeometry<TransmitterModel> {

    private final ObjModel internal;
    @Nullable
    private final ObjModel glass;

    public TransmitterModel(ObjModel internalModel, @Nullable ObjModel glass) {
        this.internal = internalModel;
        this.glass = glass;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
          ItemOverrides overrides, ResourceLocation modelLocation) {
        return new TransmitterBakedModel(internal, glass, owner, bakery, spriteGetter, modelTransform, overrides, modelLocation);
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        IGeometryBakingContext opaqueContext = new OpaqueModelConfiguration(owner);
        Set<Material> combined = new HashSet<>(internal.getMaterials(owner, modelGetter, missingTextureErrors));
        //Add the opaque versions of the textures as well
        combined.addAll(internal.getMaterials(opaqueContext, modelGetter, missingTextureErrors));
        if (glass != null) {
            combined.addAll(glass.getMaterials(owner, modelGetter, missingTextureErrors));
            //Add the opaque versions of the textures as well
            combined.addAll(glass.getMaterials(opaqueContext, modelGetter, missingTextureErrors));
        }
        return combined;
    }
}