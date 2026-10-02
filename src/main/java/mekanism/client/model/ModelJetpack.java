package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.LightCoordsUtil;
import org.jetbrains.annotations.UnknownNullability;

public class ModelJetpack extends MekanismJavaModel.NoState {

    public static final ModelLayerLocation JETPACK_LAYER = new ModelLayerLocation(Mekanism.rl("jetpack"), "main");
    public static final ModelLayerLocation JETPACK_BABY_LAYER = new ModelLayerLocation(Mekanism.rl("jetpack_baby"), "main");
    private static final Identifier JETPACK_TEXTURE = MekanismUtils.getRenderResource("jetpack.png");

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        createDefaultMesh(root, -3);

        return LayerDefinition.create(mesh, 128, 64);
    }

    protected static void createDefaultMesh(PartDefinition root, float fuelZ) {
        PartDefinition mainParts = root.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.ZERO);

        mainParts.addOrReplaceChild("pack_top", CubeListBuilder.create()
                    .texOffs(92, 28)
                    .addBox(-4, 0, 4, 8, 4, 1)
                    //Rear
                    .texOffs(106, 28)
                    .addBox(-4, 1, 1, 8, 3, 3)
                    //Doodads
                    .texOffs(116, 0)
                    .addBox(1, 0.5F, 4.2F, 2, 1, 1)
                    .addBox(1, 2, 4.2F, 2, 1, 1),
              PartPose.rotation(0.2094395F, 0, 0)
        );
        mainParts.addOrReplaceChild("pack_bottom", CubeListBuilder.create()
                    .texOffs(92, 42)
                    .addBox(-4, 4.1F, 1.5F, 8, 4, 4),
              PartPose.rotation(-0.0872665F, 0, 0)
        );
        mainParts.addOrReplaceChild("pack_mid", CubeListBuilder.create()
                    .texOffs(92, 34)
                    .addBox(-4, 3.3F, 1.5F, 8, 1, 4),
              PartPose.ZERO
        );

        mainParts.addOrReplaceChild("left_wing_support", CubeListBuilder.create()
                    .texOffs(71, 55)
                    .addBox(3, -1, 2.2F, 7, 2, 2)
                    //Extendo Support
                    .texOffs(94, 16)
                    .addBox(8, -0.2F, 2.5F, 9, 1, 1),
              PartPose.rotation(0, 0, 0.2792527F)
        );
        mainParts.addOrReplaceChild("right_wing_support", CubeListBuilder.create()
                    .texOffs(71, 55)
                    .addBox(-10, -1, 2.2F, 7, 2, 2)
                    //Extendo Support
                    .texOffs(94, 16)
                    .addBox(-17, -0.2F, 2.5F, 9, 1, 1),
              PartPose.rotation(0, 0, -0.2792527F)
        );

        mainParts.addOrReplaceChild("bottom_thruster", CubeListBuilder.create()
                    .texOffs(68, 26)
                    .addBox(-3, 8, 2.333333F, 6, 1, 2),
              PartPose.ZERO
        );
        mainParts.addOrReplaceChild("left_thruster", CubeListBuilder.create()
                    .texOffs(69, 30)
                    .addBox(7.8F, 1.5F, fuelZ - 0.5F, 3, 3, 3)
                    //Fuel Tube
                    .texOffs(92, 23)
                    .addBox(3.2F, 2, fuelZ, 8, 2, 2),
              PartPose.rotation(0.7853982F, -0.715585F, 0.3490659F)
        );
        mainParts.addOrReplaceChild("right_thruster", CubeListBuilder.create()
                    .texOffs(69, 30)
                    .addBox(-10.8F, 1.5F, fuelZ - 0.5F, 3, 3, 3)
                    //Fuel Tube
                    .texOffs(92, 23)
                    .addBox(-11.2F, 2, fuelZ, 8, 2, 2),
              PartPose.rotation(0.7853982F, 0.715585F, -0.3490659F)
        );

        PartDefinition litParts = root.addOrReplaceChild("lit", CubeListBuilder.create(), PartPose.ZERO);

        litParts.addOrReplaceChild("pack_core", CubeListBuilder.create()
                    .texOffs(69, 2)
                    .addBox(-3.5F, 3, 2, 7, 1, 3),
              PartPose.ZERO
        );
        litParts.addOrReplaceChild("lights", CubeListBuilder.create()
                    .texOffs(55, 2)
                    .addBox(2, 6.55F, 4, 1, 1, 1)
                    .addBox(0, 6.55F, 4, 1, 1, 1)
                    .addBox(-3, 6.55F, 4, 1, 1, 1),
              PartPose.ZERO
        );

        PartDefinition wingParts = root.addOrReplaceChild("wing", CubeListBuilder.create(), PartPose.ZERO);
        wingParts.addOrReplaceChild("left_blade", CubeListBuilder.create()
                    .texOffs(62, 5)
                    .addBox(3.3F, 1.1F, 3, 14, 2, 0),
              PartPose.rotation(0, 0, 0.2094395F)
        );
        wingParts.addOrReplaceChild("right_blade", CubeListBuilder.create()
                    .texOffs(62, 5)
                    .addBox(-17.3F, 1.1F, 3, 14, 2, 0),
              PartPose.rotation(0, 0, -0.2094395F)
        );
    }

    private final RenderType frameRenderType = RenderTypes.entitySolid(JETPACK_TEXTURE);
    private final RenderType wingRenderType;
    private final ModelPart parts;
    private final ModelPart litParts;
    private final ModelPart wingParts;

    public ModelJetpack(EntityModelSet entityModelSet) {
        this(entityModelSet.bakeLayer(JETPACK_LAYER));
    }

    public ModelJetpack(ModelPart root) {
        super(root);
        this.wingRenderType = MekanismRenderType.JETPACK_GLASS.apply(JETPACK_TEXTURE);
        parts = root.getChild("main");
        litParts = root.getChild("lit");
        wingParts = root.getChild("wing");
    }

    @Override
    public void collect(PoseStack poseStack, SubmitNodeCollector collector, int light, int overlayLight, @UnknownNullability FoilRendering foil, int outlineColor) {
        setupAnim();
        int nextOrder = collectParts(parts, poseStack, frameRenderType, collector, light, overlayLight, CommonColors.WHITE, null, foil, outlineColor, 1);
        nextOrder = collectParts(litParts, poseStack, frameRenderType, collector, LightCoordsUtil.FULL_BRIGHT, overlayLight, CommonColors.WHITE, null, foil, outlineColor, nextOrder);
        collectParts(wingParts, poseStack, wingRenderType, collector, LightCoordsUtil.FULL_BRIGHT, overlayLight, 0x33FFFFFF, null, foil, outlineColor, nextOrder);
    }
}