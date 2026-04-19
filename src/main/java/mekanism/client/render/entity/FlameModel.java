package mekanism.client.render.entity;

import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.entity.RenderFlame.FlameRenderState;
import mekanism.common.Mekanism;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class FlameModel extends EntityModel<FlameRenderState> {

    public static final ModelLayerLocation FLAME_LAYER = new ModelLayerLocation(Mekanism.rl("flame"), "main");

    protected FlameModel(ModelPart root) {
        super(root, MekanismRenderType.FLAME);
    }

    /**
     * @implNote Based off vanilla's {@link net.minecraft.client.model.object.projectile.ArrowModel#createBodyLayer()}
     */
    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        //TODO - 26.1: Is this the correct part of the arrow model we are copying
        CubeListBuilder cross = CubeListBuilder.create()
              .texOffs(0, 0)
              //TODO - 26.1: Figure out the tex scale's we want to be using as our flame particle goes further than vanilla's arrow
              // but we might also just not be using it all
              .addBox(-12.0F, -2.0F, 0.0F, 16.0F, 4.0F, 0.0F, CubeDeformation.NONE, 1.0F, 0.8F);
        root.addOrReplaceChild("cross_1", cross, PartPose.rotation((float) (Math.PI / 4), 0.0F, 0.0F));
        root.addOrReplaceChild("cross_2", cross, PartPose.rotation((float) (Math.PI * 3.0 / 4.0), 0.0F, 0.0F));
        //TODO - 26.1: I believe the normal might be different than it used to be?
        //builder.setNormal(matrix.last(), 0, 0, scale);
        //TODO - 26.1: What does this scale represent?
        return LayerDefinition.create(mesh.transformed(pose -> pose.scaled(0.9F)), 32, 32);
    }
}