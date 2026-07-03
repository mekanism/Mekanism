package mekanism.client.model.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import mekanism.api.MekanismRegistries;
import mekanism.api.upgrade.Upgrade;
import mekanism.common.component.UpgradeType;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

/// Copied and adapted from [CuboidItemModelWrapper]
public class UpgradeItemModel implements ItemModel {

    private final Map<ResourceKey<Upgrade>, ItemModel> cache = new IdentityHashMap<>(); // contains all the baked models since they'll never change

    private final ResolvedModel baseModel;
    private final Matrix4fc transformation;
    private final BakingContext context;

    private UpgradeItemModel(BakingContext context, Matrix4fc transformation) {
        this.context = context;
        this.baseModel = context.blockModelBaker().getModel(Unbaked.GENERATED_MODEL);
        this.transformation = transformation;
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver modelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level,
          @Nullable ItemOwner owner, int seed) {
        ItemModel model = context.missingItemModel();
        UpgradeType upgradeType = stack.get(MekanismDataComponents.UPGRADE_TYPE);
        if (upgradeType != null) {
            Either<ResourceKey<Upgrade>, Upgrade> value = upgradeType.type().unwrap();
            Optional<ResourceKey<Upgrade>> upgradeResourceKey = value.left();
            Optional<Upgrade> optionalUpgrade = value.right();
            if (optionalUpgrade.isPresent() && level != null) {
                upgradeResourceKey = level.registryAccess().lookupOrThrow(MekanismRegistries.Keys.UPGRADES).getResourceKey(optionalUpgrade.get());
            }
            if (upgradeResourceKey.isPresent()) {
                model = cache.computeIfAbsent(upgradeResourceKey.get(), this::bakeModelForUpgrade);
            }
        }
        model.update(renderState, stack, modelResolver, displayContext, level, owner, seed);
    }

    private ItemModel bakeModelForUpgrade(ResourceKey<Upgrade> upgrade) {
        TextureSlots textureSlots = makeTextureSlots(upgrade.identifier().withPrefix("item/upgrade/"));
        QuadCollection quads = baseModel.bakeTopGeometry(textureSlots, context.blockModelBaker(), BlockModelRotation.IDENTITY);
        ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(context.blockModelBaker(), baseModel, textureSlots);
        return new CuboidItemModelWrapper(Collections.emptyList(), quads, properties, transformation);
    }

    /// from [ResolvedModel#findTopTextureSlots(ResolvedModel)]
    private TextureSlots makeTextureSlots(Identifier texture) {
        ResolvedModel current = baseModel;
        TextureSlots.Resolver resolver;
        for (resolver = new TextureSlots.Resolver(); current != null; current = current.parent()) {
            resolver.addLast(current.wrapped().textureSlots());
        }
        resolver.addLast(new TextureSlots.Data.Builder()
              .addTexture("layer0", new Material(texture))
              .build()
        );
        return resolver.resolve(baseModel);
    }

    public record Unbaked() implements ItemModel.Unbaked {

        private static final Identifier GENERATED_MODEL = ModelLocationUtils.decorateItemModelLocation("generated");
        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(GENERATED_MODEL);
        }

        @Override
        public ItemModel bake(BakingContext context, Matrix4fc transformation) {
            return new UpgradeItemModel(context, transformation);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
