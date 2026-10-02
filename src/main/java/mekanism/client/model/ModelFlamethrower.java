package mekanism.client.model;

import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

public class ModelFlamethrower extends Model<Unit> {

    public static final ModelLayerLocation FLAMETHROWER_LAYER = new ModelLayerLocation(Mekanism.rl("flamethrower"), "main");
    public static final Identifier FLAMETHROWER_TEXTURE = MekanismUtils.getRenderResource("flamethrower.png");

    //TODO - 26.3: Fix this rendering inside the ground
    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition axle = root.addOrReplaceChild("axle", CubeListBuilder.create()
                    .texOffs(32, 12)
                    .addBox(-2.5F, 15, -6.5F, 4, 4, 7),
              PartPose.ZERO
        );
        axle.addOrReplaceChild("bottom_left", CubeListBuilder.create()
                    .texOffs(0, 25)
                    .addBox(-0.5F, -0.5F, 0, 1, 1, 8),
              PartPose.offsetAndRotation(-2, 19, -7, 0, 0, 0.2094395F)
        );
        axle.addOrReplaceChild("bottom_right", CubeListBuilder.create()
                    .texOffs(0, 25)
                    .addBox(-0.5F, -0.5F, 0, 1, 1, 8),
              PartPose.offsetAndRotation(1, 19, -7, 0.0174533F, 0, -0.2094395F)
        );
        axle.addOrReplaceChild("top_right", CubeListBuilder.create()
                    .texOffs(0, 25)
                    .addBox(-0.5F, -0.5F, 0, 1, 1, 8),
              PartPose.offsetAndRotation(1, 15, -7, 0, 0, 0.2094395F)
        );
        axle.addOrReplaceChild("top_left", CubeListBuilder.create()
                    .texOffs(0, 25)
                    .addBox(-0.5F, -0.5F, 0, 1, 1, 8),
              PartPose.offsetAndRotation(-2, 15, -7, 0, 0, -0.2094395F)
        );

        PartDefinition barrel = root.addOrReplaceChild("barrel", CubeListBuilder.create()
                    .texOffs(19, 30)
                    .addBox(-1.5F, 16.5F, 11, 2, 2, 8)
                    //Rings
                    .texOffs(30, 25)
                    .addBox(-2, 16, 13, 3, 3, 1)
                    .addBox(-2, 16, 17, 3, 3, 1)
                    //Large Barrel
                    .texOffs(19, 48)
                    .addBox(-1.5F, 16F, 4F, 2, 3, 7)
                    //Decor2
                    .texOffs(17, 41)
                    .addBox(-2.5F, 16, 4, 4, 2, 4),
              PartPose.ZERO
        );
        barrel.addOrReplaceChild("large_decor", CubeListBuilder.create()
                    .texOffs(0, 48)
                    .addBox(0, 0, 0, 3, 3, 6),
              PartPose.offsetAndRotation(-2, 15, 4, -0.1115358F, 0, 0)
        );

        root.addOrReplaceChild("Ring", CubeListBuilder.create()
                    .texOffs(0, 14)
                    .addBox(-3, 14, 1, 5, 6, 4)
                    .texOffs(19, 14)
                    //Top
                    .addBox(-2, 13.5F, 1.466667F, 3, 1, 3)
                    //Bottom
                    .addBox(-2, 19.5F, 1.5F, 3, 1, 3),
              PartPose.ZERO
        );

        root.addOrReplaceChild("Grasp", CubeListBuilder.create()
                    .texOffs(24, 19)
                    .addBox(0, 0, 0, 2, 1, 1),
              PartPose.offsetAndRotation(-1.5F, 13, -1.1F, 0.7807508F, 0, 0)
        );
        root.addOrReplaceChild("GraspRod", CubeListBuilder.create()
                    .texOffs(19, 19)
                    .addBox(0, 0, 0, 1, 3, 1),
              PartPose.offsetAndRotation(-1, 13, -1, 0.2230717F, 0, 0)
        );
        root.addOrReplaceChild("SupportCenter", CubeListBuilder.create()
                    .texOffs(0, 40)
                    .addBox(0, 0, 0, 2, 1, 6),
              PartPose.offsetAndRotation(-1.5F, 12.4F, 6.6F, -0.1115358F, 0, 0)
        );
        root.addOrReplaceChild("SupportFront", CubeListBuilder.create()
                    .texOffs(19, 24)
                    .addBox(0, 0, 0, 1, 1, 4),
              PartPose.offsetAndRotation(-1, 13.1F, 12.5F, -1.226894F, 0, 0)
        );
        root.addOrReplaceChild("SupportRear", CubeListBuilder.create()
                    .texOffs(0, 35)
                    .addBox(0, 0, 0, 3, 1, 3),
              PartPose.offsetAndRotation(-2, 14, 4, 0.5424979F, 0, 0)
        );
        root.addOrReplaceChild("Flame", CubeListBuilder.create()
                    .texOffs(38, 0)
                    .addBox(0, 0, 0, 1, 1, 2),
              PartPose.offsetAndRotation(-1, 19.5F, 19, 0.7063936F, 0, 0)
        );
        root.addOrReplaceChild("FlameStrut", CubeListBuilder.create()
                    .texOffs(27, 0)
                    .addBox(0, 0, 0, 2, 1, 3),
              PartPose.offsetAndRotation(-1.466667F, 18.5F, 17, -0.2602503F, 0, 0)
        );
        root.addOrReplaceChild("HydrogenDecor", CubeListBuilder.create()
                    .texOffs(27, 5)
                    .addBox(0, 0, 0, 3, 1, 5),
              PartPose.offsetAndRotation(1.5F, 15.66667F, -4.933333F, 0, 0, 0.4438713F)
        );
        root.addOrReplaceChild("Hydrogen", CubeListBuilder.create()
                    .addBox(0, 0, 0, 3, 3, 10),
              PartPose.offsetAndRotation(1.5F, 16, -5.5F, 0, 0, 0.4438713F)
        );
        return LayerDefinition.create(mesh, 64, 64);
    }

    public final RenderType RENDER_TYPE;

    public ModelFlamethrower(EntityModelSet entityModelSet) {
        super(entityModelSet.bakeLayer(FLAMETHROWER_LAYER), RenderTypes::entitySolid);
        RENDER_TYPE = renderType(FLAMETHROWER_TEXTURE);
    }
}