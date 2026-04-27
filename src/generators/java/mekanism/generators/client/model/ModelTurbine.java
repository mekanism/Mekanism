package mekanism.generators.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.client.model.MekanismJavaModel;
import mekanism.client.model.ModelPartData;
import mekanism.generators.client.model.ModelTurbine.TurbineBladeRenderState;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class ModelTurbine extends MekanismJavaModel<TurbineBladeRenderState> {

    public static final ModelLayerLocation TURBINE_LAYER = new ModelLayerLocation(MekanismGenerators.rl("turbine"), "main");
    private static final Identifier TURBINE_TEXTURE = MekanismGenerators.rl("render/turbine.png");
    private static final float BLADE_ROTATE = 0.418879F;

    private static final ModelPartData EXTENSION_NORTH = new ModelPartData("extensionNorth", CubeListBuilder.create()
          .texOffs(0, 9)
          .addBox(-1, 0, -4, 2, 1, 3),
          PartPose.offsetAndRotation(0, 20, 0, 0, 0, BLADE_ROTATE));
    private static final ModelPartData EXTENSION_EAST = new ModelPartData("extensionEast", CubeListBuilder.create()
          .texOffs(0, 13)
          .addBox(1, 0, -1, 3, 1, 2),
          PartPose.offsetAndRotation(0, 20, 0, -BLADE_ROTATE, 0, 0));
    private static final ModelPartData EXTENSION_SOUTH = new ModelPartData("extensionSouth", CubeListBuilder.create()
          .texOffs(0, 9)
          .addBox(-1, 0, 1, 2, 1, 3),
          PartPose.offsetAndRotation(0, 20, 0, 0, 0, -BLADE_ROTATE));
    private static final ModelPartData EXTENSION_WEST = new ModelPartData("extensionWest", CubeListBuilder.create()
          .texOffs(0, 13)
          .addBox(-4, 0, -1, 3, 1, 2),
          PartPose.offsetAndRotation(0, 20, 0, BLADE_ROTATE, 0, 0));
    private static final ModelPartData BLADE_NORTH = new ModelPartData("bladeNorth", CubeListBuilder.create()
          .addBox(-1.5F, 0, -8, 3, 1, 4),
          PartPose.offsetAndRotation(0, 20, 0, 0, 0, BLADE_ROTATE));
    private static final ModelPartData BLADE_EAST = new ModelPartData("bladeEast", CubeListBuilder.create()
          .texOffs(0, 5)
          .addBox(4, 0, -1.5F, 4, 1, 3),
          PartPose.offsetAndRotation(0, 20, 0, -BLADE_ROTATE, 0, 0));
    private static final ModelPartData BLADE_SOUTH = new ModelPartData("bladeSouth", CubeListBuilder.create()
          .addBox(-1.5F, 0, 4, 3, 1, 4),
          PartPose.offsetAndRotation(0, 20, 0, 0, 0, -BLADE_ROTATE));
    private static final ModelPartData BLADE_WEST = new ModelPartData("bladeWest", CubeListBuilder.create()
          .texOffs(0, 5)
          .addBox(-8, 0, -1.5F, 4, 1, 3),
          PartPose.offsetAndRotation(0, 20, 0, BLADE_ROTATE, 0, 0));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(16, 16, EXTENSION_NORTH, EXTENSION_EAST, EXTENSION_SOUTH, EXTENSION_WEST, BLADE_NORTH, BLADE_EAST, BLADE_SOUTH,
              BLADE_WEST);
    }

    private final RenderType RENDER_TYPE = RenderTypes.entitySolid(TURBINE_TEXTURE);
    private final ModelPart bladeWest;
    private final ModelPart bladeEast;
    private final ModelPart bladeNorth;
    private final ModelPart bladeSouth;

    public ModelTurbine(EntityModelSet entityModelSet) {
        super(entityModelSet.bakeLayer(TURBINE_LAYER));
        bladeWest = BLADE_WEST.getFromRoot(root);
        bladeEast = BLADE_EAST.getFromRoot(root);
        bladeNorth = BLADE_NORTH.getFromRoot(root);
        bladeSouth = BLADE_SOUTH.getFromRoot(root);
    }

    public RenderType getRenderType() {
        return RENDER_TYPE;
    }

    @Override
    public void setupAnim(TurbineBladeRenderState state) {
        super.setupAnim(state);
        //TODO - 26.1: Can we rotate it here instead of having to do so to the pose stack?
        root().rotateBy(Axis.YP.rotationDegrees(state.rotation));
        float scale = state.index * 0.5F;
        float adjustedScale = scale / 16;
        setupAnim(bladeWest, state.index, scale, adjustedScale, -0.25F, 0);
        setupAnim(bladeEast, state.index, scale, adjustedScale, 0.25F, 0);
        setupAnim(bladeNorth, state.index, adjustedScale, scale, 0, -0.25F);
        setupAnim(bladeSouth, state.index, adjustedScale, scale, 0, 0.25F);
    }

    private void setupAnim(ModelPart blade, int index, float scaleX, float scaleZ, float transX, float transZ) {
        //TODO - 26.1: Can we rotate it here instead of having to do so to the pose stack?
        blade.rotateBy(Axis.YP.rotationDegrees(5 * index));
        //TODO - 26.1: Validate that this is equivalent to the transforms that we previously had
        /*poseStack.translate(transX, 0, transZ);
        poseStack.scale(1 + scaleX, 1, 1 + scaleZ);
        poseStack.translate(-transX, 0, -transZ);*/
        blade.offsetRotation(new Vector3f(-transX * scaleX, 0, -transZ * scaleZ));
        blade.offsetScale(new Vector3f(scaleX, 0, scaleZ));
    }

    @Override
    public void collect(TurbineBladeRenderState turbineBladeRenderState, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, int light, int overlayLight, boolean hasEffect) {
        setupAnim(turbineBladeRenderState);
        collectParts(allParts, poseStack, RENDER_TYPE, submitNodeCollector, light, overlayLight, -1, null, hasEffect);
    }

    public static class TurbineBladeRenderState {

        public float rotation;
        public int index;
    }
}