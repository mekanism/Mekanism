package mekanism.generators.client.model;

import com.mojang.math.Axis;
import mekanism.generators.client.model.ModelTurbine.TurbineBladeRenderState;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

public class ModelTurbine extends Model<TurbineBladeRenderState> {

    public static final ModelLayerLocation TURBINE_LAYER = new ModelLayerLocation(MekanismGenerators.rl("turbine"), "main");
    private static final Identifier TURBINE_TEXTURE = MekanismGenerators.rl("render/turbine.png");

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        float bladeRotate = 0.418879F;
        PartDefinition north = root.addOrReplaceChild("north", CubeListBuilder.create(), PartPose.offsetAndRotation(0, 20, 0, 0, 0, bladeRotate));
        PartDefinition east = root.addOrReplaceChild("east", CubeListBuilder.create(), PartPose.offsetAndRotation(0, 20, 0, -bladeRotate, 0, 0));
        PartDefinition south = root.addOrReplaceChild("south", CubeListBuilder.create(), PartPose.offsetAndRotation(0, 20, 0, 0, 0, -bladeRotate));
        PartDefinition west = root.addOrReplaceChild("west", CubeListBuilder.create(), PartPose.offsetAndRotation(0, 20, 0, bladeRotate, 0, 0));

        north.addOrReplaceChild("extension", CubeListBuilder.create()
                    .texOffs(0, 9)
                    .addBox(-1, 0, -4, 2, 1, 3),
              PartPose.ZERO
        );
        north.addOrReplaceChild("blade", CubeListBuilder.create()
                    .addBox(-1.5F, 0, -8, 3, 1, 4),
              PartPose.ZERO
        );

        east.addOrReplaceChild("extension", CubeListBuilder.create()
                    .texOffs(0, 13)
                    .addBox(1, 0, -1, 3, 1, 2),
              PartPose.ZERO
        );
        east.addOrReplaceChild("blade", CubeListBuilder.create()
                    .texOffs(0, 5)
                    .addBox(4, 0, -1.5F, 4, 1, 3),
              PartPose.ZERO
        );

        south.addOrReplaceChild("extension", CubeListBuilder.create()
                    .texOffs(0, 9)
                    .addBox(-1, 0, 1, 2, 1, 3),
              PartPose.ZERO
        );
        south.addOrReplaceChild("blade", CubeListBuilder.create()
                    .addBox(-1.5F, 0, 4, 3, 1, 4),
              PartPose.ZERO
        );

        west.addOrReplaceChild("extension", CubeListBuilder.create()
                    .texOffs(0, 13)
                    .addBox(-4, 0, -1, 3, 1, 2),
              PartPose.ZERO
        );
        west.addOrReplaceChild("blade", CubeListBuilder.create()
                    .texOffs(0, 5)
                    .addBox(-8, 0, -1.5F, 4, 1, 3),
              PartPose.ZERO
        );
        return LayerDefinition.create(mesh, 16, 16);
    }

    private final RenderType RENDER_TYPE = RenderTypes.entitySolid(TURBINE_TEXTURE);
    private final ModelPart bladeWest;
    private final ModelPart bladeEast;
    private final ModelPart bladeNorth;
    private final ModelPart bladeSouth;

    public ModelTurbine(EntityModelSet entityModelSet) {
        super(entityModelSet.bakeLayer(TURBINE_LAYER), RenderTypes::entitySolid);
        bladeNorth = root().getChild("north").getChild("blade");
        bladeEast = root().getChild("east").getChild("blade");
        bladeSouth = root().getChild("south").getChild("blade");
        bladeWest = root().getChild("west").getChild("blade");
    }

    public RenderType getRenderType() {
        return RENDER_TYPE;
    }

    @Override
    public void setupAnim(TurbineBladeRenderState state) {
        super.setupAnim(state);
        root.rotateBy(Axis.YP.rotationDegrees(5 * state.index));
        float scale = state.index * 0.5F;
        float adjustedScale = scale / 16;
        float transAmount = 2f;
        setupAnim(bladeWest, state.index, scale, adjustedScale, -transAmount, 0);
        setupAnim(bladeEast, state.index, scale, adjustedScale, transAmount, 0);
        setupAnim(bladeNorth, state.index, adjustedScale, scale, 0, -transAmount);
        setupAnim(bladeSouth, state.index, adjustedScale, scale, 0, transAmount);
    }

    private void setupAnim(ModelPart blade, int index, float scaleX, float scaleZ, float transX, float transZ) {
        blade.offsetPos(new Vector3f(-transX * index, 0, -transZ * index));
        blade.offsetScale(new Vector3f(scaleX, 0, scaleZ));
    }

    public static class TurbineBladeRenderState {

        public float rotation;
        public int index;
    }
}