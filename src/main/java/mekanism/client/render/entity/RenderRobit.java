package mekanism.client.render.entity;

import com.mojang.serialization.MapCodec;
import mekanism.client.model.robit.RobitModel;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class RenderRobit extends MobRenderer<EntityRobit, RenderRobit.RobitRenderState, RobitModel> {

    public RenderRobit(EntityRendererProvider.Context context) {
        super(context, new RobitModel(context.bakeLayer(RobitModel.ROBIT_LAYER)), 0.5F);
    }

    @Override
    public RobitRenderState createRenderState() {
        return new RobitRenderState();
    }

    @Override
    public void extractRenderState(EntityRobit robit, RobitRenderState state, float partialTick) {
        super.extractRenderState(robit, state, partialTick);
        state.skinLookup = MekanismRobitSkins.lookup(robit.level().registryAccess(), robit.getSkin());
        state.modelData = robit.getModelData();
    }


    @Override
    public @NonNull Identifier getTextureLocation(@NotNull RobitRenderState state) {
        Identifier texture = state.modelData.get(EntityRobit.SKIN_TEXTURE_PROPERTY);
        if (texture != null) {
            return Mekanism.rl("textures/entity/robit/" + texture.getPath() + ".png");
        }
        return Mekanism.rl("textures/entity/robit/robit.png");
    }

    public static class RobitRenderState extends LivingEntityRenderState {
        @Nullable
        public MekanismRobitSkins.SkinLookup skinLookup;
        public ModelData modelData = ModelData.EMPTY;
        public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
    }

    public static class Unbaked implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public @Nullable SpecialModelRenderer<Void> bake(SpecialModelRenderer.@NonNull BakingContext context) {
            return null;
        }

        @Override
        public @NonNull MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}