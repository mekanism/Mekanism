package mekanism.client.model.item;

import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import net.minecraft.client.color.item.ItemTintSource;
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
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

/// Copied and adapted from [CuboidItemModelWrapper]
public class ComponentItemModel<TYPE> implements ItemModel {

    private static final ModelDebugName DEBUG_NAME = () -> "MekanismComponentItemModel";

    private final Map<ResourceKey<TYPE>, ItemModel> cache = new IdentityHashMap<>(); // contains all the baked models since they'll never change

    private final ResourceKey<? extends Registry<TYPE>> componentRegistry;
    private final String componentName;
    private final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<TYPE>>> componentType;

    private final List<ItemTintSource> tints;
    private final ItemModel missingItemModel;
    private final ResolvedModel resolvedBaseModel;
    private final Matrix4fc transformation;
    private final ModelBaker baker;

    private ComponentItemModel(ModelBaker baker, List<ItemTintSource> tints, ResolvedModel resolvedBaseModel, Matrix4fc transformation, ItemModel missingItemModel,
          ResourceKey<? extends Registry<?>> componentRegistry) {
        this.baker = baker;
        this.tints = tints;
        this.resolvedBaseModel = resolvedBaseModel;
        this.missingItemModel = missingItemModel;
        this.transformation = transformation;
        this.componentRegistry = (ResourceKey<? extends Registry<TYPE>>) componentRegistry;
        this.componentName = this.componentRegistry.identifier().getPath();
        componentType = DeferredHolder.create(ResourceKey.create(Registries.DATA_COMPONENT_TYPE, this.componentRegistry.identifier().withSuffix("_type")));
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver modelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level,
          @Nullable ItemOwner owner, int seed) {
        ItemModel model = missingItemModel;
        Holder<TYPE> typeHolder = stack.get(componentType);
        if (typeHolder != null) {
            Either<ResourceKey<TYPE>, TYPE> value = typeHolder.unwrap();
            Optional<ResourceKey<TYPE>> resourceKey = value.left();
            Optional<TYPE> optionalElement = value.right();
            if (optionalElement.isPresent() && level != null) {//Theoretically the other path is what will be taken the majority of times
                resourceKey = level.registryAccess().lookupOrThrow(componentRegistry).getResourceKey(optionalElement.get());
            }
            if (resourceKey.isPresent()) {
                model = cache.computeIfAbsent(resourceKey.get(), this::bakeModelForComponent);
            }
        }
        model.update(renderState, stack, modelResolver, displayContext, level, owner, seed);
    }

    private ItemModel bakeModelForComponent(ResourceKey<TYPE> key) {
        Identifier texture = key.identifier().withPrefix("item/" + componentName + "/");
        TextureSlots textureSlots = makeTextureSlots(texture);
        ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolvedBaseModel, textureSlots);
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
              .addTexture(componentName, new Material(texture))
              .build()
        );
        return resolver.resolve(DEBUG_NAME);
    }

    public record Unbaked(CuboidItemModelWrapper.Unbaked cuboidUnbaked, ResourceKey<? extends Registry<?>> componentRegistry) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
              CuboidItemModelWrapper.Unbaked.MAP_CODEC.forGetter(Unbaked::cuboidUnbaked),
              Identifier.CODEC.<ResourceKey<? extends Registry<?>>>xmap(ResourceKey::createRegistryKey, ResourceKey::identifier).fieldOf(SerializationConstants.MEK_DATA).forGetter(Unbaked::componentRegistry)
        ).apply(i, Unbaked::new));

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(cuboidUnbaked.model());
        }

        @Override
        public ItemModel bake(BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            Matrix4fc modelTransform = Transformation.compose(transformation, cuboidUnbaked.transformation());
            return new ComponentItemModel<>(context.blockModelBaker(), cuboidUnbaked.tints(), baker.getModel(cuboidUnbaked.model()), modelTransform,
                  context.missingItemModel(), componentRegistry);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
