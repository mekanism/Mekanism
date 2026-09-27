package mekanism.additions.client.render.entity;

import mekanism.additions.client.model.BabyEndermanModel;
import mekanism.additions.client.model.BabyModelLayers;
import mekanism.additions.client.render.entity.layer.BabyEndermanCarriedBlockLayer;
import mekanism.additions.client.render.entity.layer.BabyEndermanEyesLayer;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.baby.EntityBabyEnderman;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/// Copy of vanilla's [enderman render][net.minecraft.client.renderer.entity.EndermanRenderer], modified to use our own model/layer that is properly scaled, so that the
/// block is held in the correct spot and the head is in the proper place.
public class BabyEndermanRenderer extends MobRenderer<EntityBabyEnderman, EndermanRenderState, EndermanModel<EndermanRenderState>> {

    private static final Identifier ENDERMAN_LOCATION = MekanismAdditions.rl("textures/entity/baby/enderman/enderman.png");
    private final RandomSource random = RandomSource.create();
    private final BlockModelResolver blockModelResolver;

    public BabyEndermanRenderer(EntityRendererProvider.Context context) {
        super(context, new BabyEndermanModel(context.bakeLayer(BabyModelLayers.BABY_ENDERMAN)), 0.5F);
        this.blockModelResolver = context.getBlockModelResolver();
        this.addLayer(new BabyEndermanEyesLayer(this));
        this.addLayer(new BabyEndermanCarriedBlockLayer(this));
    }

    @Override
    public Vec3 getRenderOffset(EndermanRenderState state) {
        Vec3 offset = super.getRenderOffset(state);
        if (state.isCreepy) {
            double d = 0.02 * state.scale;
            return offset.add(this.random.nextGaussian() * d, 0, this.random.nextGaussian() * d);
        }
        return offset;
    }

    @Override
    public Identifier getTextureLocation(EndermanRenderState state) {
        return ENDERMAN_LOCATION;
    }

    @Override
    public EndermanRenderState createRenderState() {
        return new EndermanRenderState();
    }

    @Override
    public void extractRenderState(EntityBabyEnderman entity, EndermanRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
        state.isCreepy = entity.isCreepy();
        BlockState carriedBlock = entity.getCarriedBlock();
        if (carriedBlock != null) {
            this.blockModelResolver.update(state.carriedBlock, carriedBlock, EndermanRenderer.BLOCK_DISPLAY_CONTEXT);
        } else {
            state.carriedBlock.clear();
        }
    }
}