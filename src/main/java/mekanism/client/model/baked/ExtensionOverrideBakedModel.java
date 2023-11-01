package mekanism.client.model.baked;

import com.google.common.collect.ImmutableList;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ExtensionOverrideBakedModel<T> extends ExtensionBakedModel<T> {

    private final ItemOverrides overrides;

    public ExtensionOverrideBakedModel(BakedModel original, UnaryOperator<ItemOverrides> wrapper) {
        super(original);
        this.overrides = wrapper.apply(super.getOverrides());
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    public abstract static class ExtendedItemOverrides extends ItemOverrides {

        protected final ItemOverrides original;

        protected ExtendedItemOverrides(ItemOverrides original) {
            this.original = original;
        }

        @Nullable
        @Override
        public abstract BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed);

        protected BakedModel wrap(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed, ModelData modelData,
              BiFunction<BakedModel, ModelData, ModelDataBakedModel> wrapper) {
            // perform any overrides the original may have (most likely it doesn't have any)
            // and then wrap the baked model so that it makes use of the model data
            BakedModel resolved = original.resolve(model, stack, world, entity, seed);
            if (resolved == null) {
                resolved = model;
            }
            return wrapper.apply(resolved, modelData);
        }

        @Override
        public ImmutableList<BakedOverride> getOverrides() {
            return original.getOverrides();
        }
    }
}