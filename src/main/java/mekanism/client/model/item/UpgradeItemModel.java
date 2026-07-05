package mekanism.client.model.item;

import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mekanism.api.MekanismRegistries;
import mekanism.api.upgrade.IUpgradeHelper;
import mekanism.api.upgrade.Upgrade;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

/// Copied and adapted from [CuboidItemModelWrapper]
public class UpgradeItemModel implements ItemModel {

    private static final ModelDebugName DEBUG_NAME = () -> "MekanismUpgradeItemModel";

    private final Map<ResourceKey<Upgrade>, ItemModel> cache = new IdentityHashMap<>(); // contains all the baked models since they'll never change

    private final List<ItemTintSource> tints;
    private final ItemModel missingItemModel;
    private final ResolvedModel resolvedBaseModel;
    private final Matrix4fc transformation;
    private final ModelBaker baker;

    private UpgradeItemModel(ModelBaker baker, List<ItemTintSource> tints, ResolvedModel resolvedBaseModel, Matrix4fc transformation, ItemModel missingItemModel) {
        this.baker = baker;
        this.tints = tints;
        this.resolvedBaseModel = resolvedBaseModel;
        this.missingItemModel = missingItemModel;
        this.transformation = transformation;
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver modelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level,
          @Nullable ItemOwner owner, int seed) {
        ItemModel model = missingItemModel;
        Holder<Upgrade> upgradeType = stack.get(IUpgradeHelper.INSTANCE.dataComponent());
        if (upgradeType != null) {
            Either<ResourceKey<Upgrade>, Upgrade> value = upgradeType.unwrap();
            Optional<ResourceKey<Upgrade>> upgradeResourceKey = value.left();
            Optional<Upgrade> optionalUpgrade = value.right();
            if (optionalUpgrade.isPresent() && level != null) {//Theoretically the other path is what will be taken the majority of times
                upgradeResourceKey = level.registryAccess().lookupOrThrow(MekanismRegistries.Keys.UPGRADES).getResourceKey(optionalUpgrade.get());
            }
            if (upgradeResourceKey.isPresent()) {
                model = cache.computeIfAbsent(upgradeResourceKey.get(), this::bakeModelForUpgrade);
            }
        }
        model.update(renderState, stack, modelResolver, displayContext, level, owner, seed);
    }

    private ItemModel bakeModelForUpgrade(ResourceKey<Upgrade> upgrade) {
        Identifier texture = upgrade.identifier().withPrefix("item/upgrade/");
        TextureSlots textureSlots = makeTextureSlots(texture);
        //Note: We use the primary texture as the particle
        Material.Baked particle = baker.materials().resolveSlot(textureSlots, TextureSlot.LAYER0.getId(), DEBUG_NAME);
        ModelRenderProperties properties = new ModelRenderProperties(resolvedBaseModel.getTopGuiLight().lightLikeBlock(), particle, resolvedBaseModel.getTopTransforms());
        QuadCollection quads = resolvedBaseModel.getTopGeometry().bake(textureSlots, baker, BlockModelRotation.IDENTITY, DEBUG_NAME, resolvedBaseModel.getTopAdditionalProperties());
        return new CuboidItemModelWrapper(tints, quads, properties, transformation);
    }

    /// from [ResolvedModel#findTopTextureSlots(ResolvedModel)]
    private TextureSlots makeTextureSlots(Identifier texture) {
        ResolvedModel current = resolvedBaseModel;
        TextureSlots.Resolver resolver;
        for (resolver = new TextureSlots.Resolver(); current != null; current = current.parent()) {
            resolver.addLast(current.wrapped().textureSlots());
        }
        resolver.addLast(new TextureSlots.Data.Builder()
              .addTexture(TextureSlot.LAYER0.getId(), new Material(texture))
              .build()
        );
        return resolver.resolve(DEBUG_NAME);
    }

    public record Unbaked(CuboidItemModelWrapper.Unbaked cuboidUnbaked) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = CuboidItemModelWrapper.Unbaked.MAP_CODEC.xmap(Unbaked::new, Unbaked::cuboidUnbaked);

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(cuboidUnbaked.model());
        }

        @Override
        public ItemModel bake(BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            Matrix4fc modelTransform = Transformation.compose(transformation, cuboidUnbaked.transformation());
            return new UpgradeItemModel(context.blockModelBaker(), cuboidUnbaked.tints(), baker.getModel(cuboidUnbaked.model()), modelTransform, context.missingItemModel());
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
