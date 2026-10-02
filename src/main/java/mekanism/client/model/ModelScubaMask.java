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

public class ModelScubaMask extends MekanismJavaModel.NoState {

    public static final ModelLayerLocation MASK_LAYER = new ModelLayerLocation(Mekanism.rl("scuba_mask"), "main");
    public static final ModelLayerLocation MASK_BABY_LAYER = new ModelLayerLocation(Mekanism.rl("scuba_mask_baby"), "main");
    private static final Identifier MASK_TEXTURE = MekanismUtils.getRenderResource("scuba_set.png");

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition mainParts = root.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.ZERO);
        mainParts.addOrReplaceChild("helmet_feed", CubeListBuilder.create()
                    .texOffs(88, 43)
                    .addBox(-2F, -2F, 2F, 4, 3, 4),
              PartPose.ZERO
        );
        mainParts.addOrReplaceChild("mouth_intake", CubeListBuilder.create()
                    .texOffs(118, 42)
                    .addBox(-1.5F, -0.7F, -6F, 3, 2, 3),
              PartPose.offsetAndRotation(0F, -2F, 0F, 0.2094395F, 0F, 0F)
        );
        mainParts.addOrReplaceChild("tube", CubeListBuilder.create()
                    .texOffs(106, 50)
                    //Front
                    .addBox(-4.5F, -1F, -5.5F, 9, 1, 1)
                    //Back
                    .addBox(-4.5F, -1F, 4.5F, 9, 1, 1)
                    //Sides
                    .texOffs(106, 54)
                    //Left
                    .addBox(4.5F, -1F, -4.5F, 1, 1, 9)
                    //Right
                    .addBox(-5.5F, -1F, -4.5F, 1, 1, 9),
              PartPose.ZERO
        );

        PartDefinition fin = mainParts.addOrReplaceChild("fin", CubeListBuilder.create()
                    .texOffs(72, 34)
                    //Left
                    .addBox(5.5F, -6F, -1F, 2, 2, 5)
                    //Right
                    .addBox(-7.5F, -6F, -1F, 2, 2, 5)
                    //Back
                    .texOffs(80, 0)
                    .addBox(-1F, -9.6F, 2.5F, 2, 10, 3),
              PartPose.ZERO
        );
        fin.addOrReplaceChild("upper", CubeListBuilder.create()
                    .texOffs(78, 50)
                    //Left
                    .addBox(5F, -7.5F, -3.3F, 1, 2, 12)
                    //Right
                    .addBox(-6F, -7.5F, -3.3F, 1, 2, 12),
              PartPose.rotation(0.0698132F, 0F, 0F)
        );

        mainParts.addOrReplaceChild("plate_top", CubeListBuilder.create()
                    .texOffs(104, 34)
                    .addBox(-3F, -10F, -2F, 6, 2, 6),
              PartPose.rotation(0.1396263F, 0F, 0F)
        );
        mainParts.addOrReplaceChild("left_filter", CubeListBuilder.create()
                    .texOffs(108, 42)
                    .addBox(3.4F, -1.8F, -5F, 2, 3, 3),
              PartPose.rotation(0F, 0.3839724F, 0.5061455F)
        );
        mainParts.addOrReplaceChild("right_filter", CubeListBuilder.create()
                    .texOffs(108, 42)
                    .addBox(-5.4F, -1.8F, -5F, 2, 3, 3),
              PartPose.rotation(0F, -0.3839724F, -0.5061455F)
        );
        mainParts.addOrReplaceChild("filter_pipe", CubeListBuilder.create()
                    //Lower
                    .texOffs(92, 41)
                    .addBox(-3F, 1F, -5F, 5, 1, 1)
                    //Front Left Corner
                    .texOffs(109, 50)
                    .addBox(3.5F, -1F, -4.5F, 1, 1, 1)
                    //Front Right Corner
                    .texOffs(109, 50)
                    .addBox(-4.5F, -1F, -4.5F, 1, 1, 1)
                    //Upper
                    .texOffs(104, 42)
                    .addBox(-0.5F, 0F, -5F, 1, 1, 1)
                    //Back Left Corner
                    .texOffs(109, 50)
                    .addBox(3.5F, -1F, 3.5F, 1, 1, 1)
                    //Back Right Corner
                    .texOffs(109, 50)
                    .addBox(-4.5F, -1F, 3.5F, 1, 1, 1),
              PartPose.ZERO
        );

        //Special (lights and glass)

        root.addOrReplaceChild("lights", CubeListBuilder.create()
                    .texOffs(89, 37)
                    //Left
                    .addBox(5.5F, -6F, -2F, 2, 2, 1)
                    //Right
                    .addBox(-7.5F, -6F, -2F, 2, 2, 1),
              PartPose.ZERO
        );

        root.addOrReplaceChild("glass", CubeListBuilder.create()
                    //Top
                    .addBox(-4F, -9F, -4F, 8, 1, 8)
                    //Front
                    .addBox(-4F, -8F, -5F, 8, 7, 1)
                    //Right
                    .addBox(-5F, -8F, -4F, 1, 7, 8)
                    //Left
                    .addBox(4F, -8F, -4F, 1, 7, 8)
                    //Back Right
                    .addBox(-4F, -8F, 4F, 3, 7, 1)
                    //Back Left
                    .addBox(1F, -8F, 4F, 3, 7, 1),
              PartPose.ZERO
        );

        return LayerDefinition.create(mesh, 128, 64);
    }

    private final RenderType GLASS_RENDER_TYPE = MekanismRenderType.STANDARD.apply(MASK_TEXTURE);
    private final RenderType RENDER_TYPE = RenderTypes.entitySolid(MASK_TEXTURE);
    private final ModelPart parts;
    private final ModelPart litParts;
    private final ModelPart glass;

    public ModelScubaMask(EntityModelSet entityModelSet) {
        this(entityModelSet.bakeLayer(MASK_LAYER));
    }

    public ModelScubaMask(ModelPart root) {
        super(root);
        parts = root.getChild("main");
        litParts = root.getChild("lights");
        glass = root.getChild("glass");
    }

    @Override
    public void collect(PoseStack matrix, SubmitNodeCollector collector, int light, int overlayLight, @UnknownNullability FoilRendering foil, int outlineColor) {
        setupAnim();
        int nextOrder = collectParts(parts, matrix, RENDER_TYPE, collector, light, overlayLight, CommonColors.WHITE, null, foil, outlineColor, 1);
        nextOrder = collectParts(litParts, matrix, RENDER_TYPE, collector, LightCoordsUtil.FULL_BRIGHT, overlayLight, CommonColors.WHITE, null, foil, outlineColor, nextOrder);
        collectParts(glass, matrix, GLASS_RENDER_TYPE, collector, LightCoordsUtil.FULL_BRIGHT, overlayLight, 0x4CFFFFFF, null, foil, outlineColor, nextOrder);
    }
}