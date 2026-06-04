package mekanism.additions.client.render.entity;

import mekanism.additions.client.model.ModelBabyEnderman;
import mekanism.additions.client.render.entity.layer.BabyEndermanEyesLayer;
import mekanism.additions.client.render.entity.layer.BabyEndermanHeldBlockLayer;
import mekanism.additions.common.entity.baby.EntityBabyEnderman;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Copy of vanilla's enderman render, modified to use our own model/layer that is properly scaled, so that the block is held in the correct spot and the head is in the
 * proper place.
 */
@NothingNullByDefault
public class RenderBabyEnderman extends MobRenderer<EntityBabyEnderman, EndermanRenderState, EndermanModel<EndermanRenderState>> {

    public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private static final Identifier ENDERMAN_TEXTURES = Identifier.withDefaultNamespace("textures/entity/enderman/enderman.png");
    private final RandomSource random = RandomSource.create();
    private final BlockModelResolver blockModelResolver;

    public RenderBabyEnderman(EntityRendererProvider.Context context) {
        super(context, new ModelBabyEnderman(context.bakeLayer(ModelBabyEnderman.BABY_ENDERMAN_LAYER)), 0.5F);
        this.blockModelResolver = context.getBlockModelResolver();
        this.addLayer(new BabyEndermanEyesLayer(this));
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
        BlockState carriedBlock = enderman.getCarriedBlock();
        if (carriedBlock != null) {
            this.blockModelResolver.update(state.carriedBlock, carriedBlock, BLOCK_DISPLAY_CONTEXT);
        } else {
            state.carriedBlock.clear();
        }
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