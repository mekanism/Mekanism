package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.additions.client.model.ModelBabyEnderman;
import mekanism.additions.client.render.entity.layer.BabyEndermanEyesLayer;
import mekanism.additions.client.render.entity.layer.BabyEndermanHeldBlockLayer;
import mekanism.additions.common.entity.baby.EntityBabyEnderman;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Copy of vanilla's enderman render, modified to use our own model/layer that is properly scaled, so that the block is held in the correct spot and the head is in the
 * proper place.
 */
@NothingNullByDefault
public class RenderBabyEnderman extends MobRenderer<EntityBabyEnderman, EndermanRenderState, ModelBabyEnderman> {

    private static final Identifier ENDERMAN_TEXTURES = Identifier.withDefaultNamespace("textures/entity/enderman/enderman.png");
    private final RandomSource random = RandomSource.create();

    public RenderBabyEnderman(EntityRendererProvider.Context context) {
        super(context, new ModelBabyEnderman(context.bakeLayer(ModelLayers.ENDERMAN)), 0.5F);
        this.addLayer(new EnderEyesLayer(this));
        this.addLayer(new BabyEndermanHeldBlockLayer(this));
    }

    @Override
    public EndermanRenderState createRenderState() {
        return new EndermanRenderState();
    }

    @Override
    public void extractRenderState(EntityBabyEnderman enderman, EndermanRenderState state, float partialTicks) {
        super.extractRenderState(enderman, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(enderman, state, partialTicks, this.itemModelResolver);
        state.isCreepy = enderman.isCreepy();
        state.carriedBlock = enderman.getCarriedBlock();
    }

    @Override
    public Vec3 getRenderOffset(EndermanRenderState state) {
        if (state.isCreepy) {
            double offset = 0.02 * state.scale;
            return new Vec3(this.random.nextGaussian() * offset, 0, this.random.nextGaussian() * offset);
        }
        return super.getRenderOffset(state);
    }

    @Override
    public Identifier getTextureLocation(EndermanRenderState state) {
        return ENDERMAN_TEXTURES;
    }
}