package mekanism.client.model.robit;

//todo 26.1 robit model
/*import java.util.List;
import java.util.function.BiPredicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.robit.RobitSkin;
import mekanism.client.RobitSpriteUploader;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.model.baked.ExtensionOverrideBakedModel;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadTransformation.TextureFilteredTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.common.entity.EntityRobit;
import mekanism.common.item.ItemRobit;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.registries.MekanismRobitSkins.SkinLookup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RobitBakedModel extends ExtensionOverrideBakedModel<Identifier> {

    private static final BiPredicate<Identifier, Identifier> DATA_EQUALITY_CHECK = Identifier::equals;

    public RobitBakedModel(BakedModel original) {
        super(original, RobitItemOverrideList::new);
    }

    @Override
    public List<BakedQuad> createQuads(QuadsKey<Identifier> key) {
        List<BakedQuad> quads = key.getQuads();
        //if (RobitSpriteUploader.UPLOADER != null) {
            Identifier selectedTexture = key.getData();
            //Only replace missing textures (which should in general be #robit in the actual json without a mapping to it)
            //TODO: This technically doesn't behave quite right for textures that are not replaced given the sprites on the
            // model likely are on a different atlas than the robit textures, so the render type will be wrong
        SpriteGetter spriteGetter = null;//todo 26.1 - bakery rewrite
        TextureAtlasSprite sprite = spriteGetter.get(RobitSpriteUploader.getSpriteId(selectedTexture));
        QuadTransformation transformation = QuadTransformation.texture(sprite);
            transformation = TextureFilteredTransformation.of(transformation, rl -> rl.getPath().equals("missingno"));
            quads = QuadUtils.transformBakedQuads(quads, transformation);
        //}
        return quads;
    }

    @Nullable
    @Override
    public QuadsKey<Identifier> createKey(QuadsKey<Identifier> key, ModelData data) {
        Identifier skinTexture = data.get(EntityRobit.SKIN_TEXTURE_PROPERTY);
        if (skinTexture == null) {
            return null;
        }
        return key.data(skinTexture, skinTexture.hashCode(), DATA_EQUALITY_CHECK);
    }

    @Override
    protected RobitBakedModel wrapModel(BakedModel model) {
        return new RobitBakedModel(model);
    }

    private static class RobitItemOverrideList extends ExtendedItemOverrides {

        RobitItemOverrideList(ItemOverrides original) {
            super(original);
        }

        @Nullable
        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemRobit) {
                RegistryAccess registryAccess;
                if (world != null) {
                    registryAccess = world.registryAccess();
                } else if (entity != null) {
                    registryAccess = entity.level().registryAccess();
                } else {
                    ClientLevel level = Minecraft.getInstance().level;
                    if (level == null) {
                        //Failed to lookup the skin so don't do any overrides
                        return original.resolve(model, stack, null, null, seed);
                    }
                    registryAccess = level.registryAccess();
                }
                ResourceKey<RobitSkin> skinKey = stack.getOrDefault(MekanismDataComponents.ROBIT_SKIN, MekanismRobitSkins.BASE);
                SkinLookup skinLookup = MekanismRobitSkins.lookup(registryAccess, skinKey);
                if (skinLookup.customModel() != null) {
                    //If the skin has a custom model look it up and if it isn't the model we are currently resolving for
                    // (to avoid stack overflow and recursion), then lookup the overrides of that model
                    BakedModel customModel = MekanismModelCache.INSTANCE.getRobitSkin(skinLookup);
                    if (customModel != null && customModel != model) {
                        return customModel.getOverrides().resolve(customModel, stack, world, entity, seed);
                    }
                }
                List<Identifier> textures = skinLookup.textures();
                if (!textures.isEmpty()) {
                    //Assuming the skin actually has textures (it should), grab the first texture as the model data
                    ModelData modelData = ModelData.of(EntityRobit.SKIN_TEXTURE_PROPERTY, textures.getFirst());
                    return wrap(model, stack, world, entity, seed, modelData, RobitModelDataBakedModel::new);
                }
            }
            return original.resolve(model, stack, world, entity, seed);
        }
    }
}*/