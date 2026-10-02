package mekanism.client.model;

import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
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
import net.minecraft.util.Unit;

public class ModelScubaTank extends Model<Unit> {

    public static final ModelLayerLocation TANK_LAYER = new ModelLayerLocation(Mekanism.rl("scuba_tank"), "main");
    public static final ModelLayerLocation TANK_BABY_LAYER = new ModelLayerLocation(Mekanism.rl("scuba_tank_baby"), "main");
    private static final Identifier TANK_TEXTURE = MekanismUtils.getRenderResource("scuba_set.png");

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("left_tank", CubeListBuilder.create()
                    .texOffs(23, 54)
                    .addBox(-1F, 2F, 4F, 3, 7, 3)
                    //Cap
                    .texOffs(23, 51)
                    .addBox(-0.5F, 1F, 4.5F, 2, 1, 2),
              PartPose.rotation(-0.2443461F, 0.5235988F, 0F)
        );
        root.addOrReplaceChild("right_tank", CubeListBuilder.create()
                    .texOffs(23, 54)
                    .addBox(-2F, 2F, 4F, 3, 7, 3)
                    //Cap
                    .texOffs(23, 51)
                    .addBox(-1.5F, 1F, 4.5F, 2, 1, 2),
              PartPose.rotation(-0.2443461F, -0.5235988F, 0F)
        );
        root.addOrReplaceChild("tankDock", CubeListBuilder.create()
                    .texOffs(0, 55)
                    .addBox(-2F, 5F, 1F, 4, 4, 5),
              PartPose.ZERO
        );
        root.addOrReplaceChild("tankBridge", CubeListBuilder.create()
                    .texOffs(0, 47)
                    .addBox(-1F, 3F, -1.5F, 2, 5, 3),
              PartPose.rotation(0.5934119F, 0F, 0F)
        );
        root.addOrReplaceChild("tankPipeLower", CubeListBuilder.create()
                    .texOffs(0, 37)
                    .addBox(-0.5F, 2F, 3F, 1, 4, 1),
              PartPose.rotation(0.2094395F, 0F, 0F)
        );
        root.addOrReplaceChild("tankPipeUpper", CubeListBuilder.create()
                    .texOffs(4, 38)
                    .addBox(-0.5F, 1F, 1.5F, 1, 1, 3),
              PartPose.ZERO
        );
        root.addOrReplaceChild("tankBackBrace", CubeListBuilder.create()
                    .texOffs(0, 42)
                    .addBox(-3F, 2F, 0.5F, 6, 3, 2),
              PartPose.rotation(0.2443461F, 0F, 0F)
        );

        return LayerDefinition.create(mesh, 128, 64);
    }

    public final RenderType RENDER_TYPE = RenderTypes.entitySolid(TANK_TEXTURE);

    public ModelScubaTank(EntityModelSet entityModelSet) {
        this(entityModelSet.bakeLayer(TANK_LAYER));
    }

    public ModelScubaTank(ModelPart root) {
        super(root, RenderTypes::entitySolid);
    }

    public RenderType getRenderType() {
        return RENDER_TYPE;
    }

}