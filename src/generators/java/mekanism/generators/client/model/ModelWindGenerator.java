package mekanism.generators.client.model;

import java.util.Collection;
import mekanism.client.ModelUtil;
import mekanism.client.render.outline.Outlines.Line;
import mekanism.generators.client.model.ModelWindGenerator.WindGeneratorRotationRenderState;
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
import net.minecraft.util.Mth;

public class ModelWindGenerator extends Model<WindGeneratorRotationRenderState> {

    public static final ModelLayerLocation GENERATOR_LAYER = new ModelLayerLocation(MekanismGenerators.rl("wind_generator"), "main");
    public static final Identifier GENERATOR_TEXTURE = MekanismGenerators.rl("render/wind_generator.png");

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("base", CubeListBuilder.create()
                    .texOffs(10, 64)
                    .addBox(-8, 22, -8, 16, 2, 16)
                    //Rim
                    .texOffs(26, 50)
                    .addBox(-6, 21, -6, 12, 2, 12),
              PartPose.ZERO
        );
        root.addOrReplaceChild("head", CubeListBuilder.create()
                    .texOffs(20, 0)
                    .addBox(-3.5F, -51.5F, -4, 7, 7, 9),
              PartPose.ZERO
        );
        root.addOrReplaceChild("plate", CubeListBuilder.create()
                    .texOffs(42, 25)
                    .addBox(-4, 12, -8, 8, 8, 1)
                    //Connectors
                    .texOffs(0, 75)
                    .addBox(-2, 19, -5.5F, 4, 2, 2)
                    .texOffs(42, 34)
                    .addBox(-3, 13, -7, 6, 6, 10),
              PartPose.ZERO
        );
        root.addOrReplaceChild("wire", CubeListBuilder.create()
                    .texOffs(74, 0)
                    .addBox(-1, 0, -1.1F, 2, 65, 2),
              PartPose.offsetAndRotation(0, -46, -1.5F, -0.0349066F, 0, 0)
        );
        root.addOrReplaceChild("rearPlate1", CubeListBuilder.create()
                    .texOffs(20, 16)
                    .addBox(-2.5F, -6, 0, 5, 6, 3),
              PartPose.offsetAndRotation(0, -44.5F, 4, 0.122173F, 0, 0)
        );
        root.addOrReplaceChild("rearPlate2", CubeListBuilder.create()
                    .texOffs(36, 16)
                    .addBox(-1.5F, -5, -1, 3, 5, 2),
              PartPose.offsetAndRotation(0, -45, 7, 0.2094395F, 0, 0)
        );

        root.addOrReplaceChild("post1a", CubeListBuilder.create()
                    .addBox(-2.5F, 0, -2.5F, 5, 68, 5),
              PartPose.offsetAndRotation(0, -46, 0, -0.0349066F, 0, 0.0349066F)
        );
        root.addOrReplaceChild("post1b", CubeListBuilder.create()
                    .addBox(-2.5F, 0, -2.5F, 5, 68, 5),
              PartPose.offsetAndRotation(0, -46, 0, 0.0349066F, 0, -0.0349066F)
        );
        root.addOrReplaceChild("post1c", CubeListBuilder.create()
                    .addBox(-2.5F, 0, -2.5F, 5, 68, 5),
              PartPose.offsetAndRotation(0, -46, 0, 0.0347321F, 0, 0.0347321F)
        );
        root.addOrReplaceChild("post1d", CubeListBuilder.create()
                    .addBox(-2.5F, 0, -2.5F, 5, 68, 5),
              PartPose.offsetAndRotation(0, -46, 0, -0.0347321F, 0, -0.0347321F)
        );

        PartDefinition blades = root.addOrReplaceChild("blades", CubeListBuilder.create()
                    //Center
                    .texOffs(20, 25)
                    .addBox(-2, -2, -7, 4, 4, 3)
                    //Cap
                    .texOffs(22, 0)
                    .addBox(-1, -1, -8, 2, 2, 1),
              PartPose.offset(0, -48, 0)
        );
        blades.addOrReplaceChild("1a", CubeListBuilder.create()
                    .texOffs(20, 32)
                    .addBox(-1, -32, 0, 2, 32, 1),
              PartPose.offset(0, 0, -5.99F)
        );
        blades.addOrReplaceChild("2a", CubeListBuilder.create()
                    .texOffs(20, 32)
                    .addBox(-1, 0, 0, 2, 32, 1),
              PartPose.offsetAndRotation(0, 0, -6, 0, 0, 1.047198F)
        );
        blades.addOrReplaceChild("3a", CubeListBuilder.create()
                    .texOffs(20, 32)
                    .addBox(-1, 0, 0, 2, 32, 1),
              PartPose.offsetAndRotation(0, 0, -6, 0, 0, -1.047198F)
        );
        blades.addOrReplaceChild("1b", CubeListBuilder.create()
                    .texOffs(26, 32)
                    .addBox(-2, -28, 0, 2, 28, 1),
              PartPose.offsetAndRotation(0, 0, -6, 0, 0, 0.0349066F)
        );
        blades.addOrReplaceChild("2b", CubeListBuilder.create()
                    .texOffs(26, 32)
                    .addBox(0, 0, 0, 2, 28, 1),
              PartPose.offsetAndRotation(0, 0, -6.01F, 0, 0, 1.082104F)
        );
        blades.addOrReplaceChild("3b", CubeListBuilder.create()
                    .texOffs(26, 32)
                    .addBox(0, 0, 0, 2, 28, 1),
              PartPose.offsetAndRotation(0, 0, -6.01F, 0, 0, -1.012291F)
        );

        return LayerDefinition.create(mesh, 128, 128);
    }

    public final RenderType RENDER_TYPE;
    private final ModelPart blades;

    public ModelWindGenerator(EntityModelSet entityModelSet) {
        super(entityModelSet.bakeLayer(GENERATOR_LAYER), RenderTypes::entitySolid);
        RENDER_TYPE = renderType(GENERATOR_TEXTURE);
        blades = root().getChild("blades");
    }

    public Collection<Line> getWireFrame(WindGeneratorRotationRenderState state) {
        setupAnim(state);
        return ModelUtil.getPartsAsWireFrame(root());
    }

    @Override
    public void setupAnim(WindGeneratorRotationRenderState state) {
        super.setupAnim(state);
        blades.setRotation(0, 0, (state.angle % 360) * Mth.DEG_TO_RAD);
    }

    public static class WindGeneratorRotationRenderState {

        public WindGeneratorRotationRenderState(float angle) {
            this.angle = angle;
        }

        public float angle;
    }
}