package mekanism.client.model.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import mekanism.api.MekanismRegistries;
import mekanism.api.upgrade.IUpgradeHelper;
import mekanism.api.upgrade.Upgrade;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
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

    private final ItemTransforms itemTransforms;
    private final Matrix4fc transformation;
    private final BakingContext context;

    private UpgradeItemModel(BakingContext context, Matrix4fc transformation) {
        this.context = context;
        this.itemTransforms = context.blockModelBaker().getModel(Unbaked.GENERATED_MODEL).getTopTransforms();
        this.transformation = transformation;
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver modelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level,
          @Nullable ItemOwner owner, int seed) {
        ItemModel model = context.missingItemModel();
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
        ModelBaker modelBaker = context.blockModelBaker();
        Material.Baked bakedMaterial = modelBaker.materials().get(new Material(texture), DEBUG_NAME);
        ModelRenderProperties properties = new ModelRenderProperties(false, bakedMaterial, this.itemTransforms);
        QuadCollection quads = modelBaker.compute(new ItemModelGenerator.ItemLayerKey(bakedMaterial, BlockModelRotation.IDENTITY, 0));
        return new CuboidItemModelWrapper(Collections.emptyList(), quads, properties, transformation);
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
