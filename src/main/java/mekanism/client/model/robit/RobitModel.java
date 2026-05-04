package mekanism.client.model.robit;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import mekanism.common.Mekanism;
import mekanism.client.render.entity.RenderRobit.RobitRenderState;

public class RobitModel extends EntityModel<RobitRenderState> {

    public static final ModelLayerLocation ROBIT_LAYER = new ModelLayerLocation(Mekanism.rl("robit"), "main");

    public RobitModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        return LayerDefinition.create(mesh, 32, 32);
    }
}
