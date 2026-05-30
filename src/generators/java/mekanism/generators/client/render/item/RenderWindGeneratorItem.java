package mekanism.generators.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import mekanism.api.MekanismAPITags;
import mekanism.client.render.MekanismRenderer;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.client.model.ModelWindGenerator.WindGeneratorRotationRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class RenderWindGeneratorItem implements SpecialModelRenderer<WindGeneratorRotationRenderState> {

    private static final int SPEED = 16;
    private static int lastTicksUpdated = 0;
    private static int angle = 0;
    private final ModelWindGenerator windGenerator;
    private static final WindGeneratorRotationRenderState ZERO_ANGLE = new WindGeneratorRotationRenderState(0);

    public RenderWindGeneratorItem(EntityModelSet entityModelSet) {
        windGenerator = new ModelWindGenerator(entityModelSet);
    }

    @Nullable
    @Override
    public WindGeneratorRotationRenderState extractArgument(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean tickingNormally = MekanismRenderer.isRunningNormally();
        if (tickingNormally && minecraft.level != null) {
            //Only update the angle if we are in a world and that world is not blacklisted
            if (minecraft.level.dimensionTypeRegistration().is(MekanismAPITags.DimensionTypes.NO_WIND)) {
                //If the dimension is blacklisted, don't try to tick it at all
                tickingNormally = false;
            } else {
                int ticks = Minecraft.getInstance().levelRenderer.getTicks();
                if (lastTicksUpdated != ticks) {
                    angle = (angle + SPEED) % 360;
                    lastTicksUpdated = ticks;
                }
            }
        }
        WindGeneratorRotationRenderState state = new WindGeneratorRotationRenderState(angle);
        if (tickingNormally) {
            state.angle = (state.angle + SPEED * MekanismRenderer.getPartialTick()) % 360;
        }
        return state;
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        windGenerator.setupAnim(ZERO_ANGLE);
        windGenerator.root().getExtentsForGui(new PoseStack(), output);
    }

    @Override
    public void submit(@Nullable WindGeneratorRotationRenderState argument, PoseStack matrix, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (argument == null) {
            return;
        }
        windGenerator.setupAnim(argument);
        matrix.pushPose();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.mulPose(Axis.ZP.rotationDegrees(180));
        windGenerator.collect(argument, matrix, submitNodeCollector, lightCoords, overlayCoords, hasFoil);
        matrix.popPose();
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked<WindGeneratorRotationRenderState> {

        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        @Nullable
        public SpecialModelRenderer<WindGeneratorRotationRenderState> bake(BakingContext context) {
            return new RenderWindGeneratorItem(context.entityModelSet());
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}