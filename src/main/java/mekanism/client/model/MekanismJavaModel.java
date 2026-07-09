package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.client.render.MekanismRenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

//TODO - 26.2: review if any of these can be converted back to regular java models - needs rendertype without texture & only single rendertype/light coords
public abstract class MekanismJavaModel<STATE> /*extends Model<STATE>*/ {

    protected final ModelPart root;
    protected final List<ModelPart> allParts;

    public MekanismJavaModel(ModelPart root) {
        this.root = root;
        this.allParts = root.getAllParts();
    }

    public abstract void collect(STATE state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlayLight, @UnknownNullability FoilRendering foil, int outlineColor);

    public void setupAnim(STATE state) {
        this.resetPose();
    }

    public final void resetPose() {
        for (ModelPart part : this.allParts) {
            part.resetPose();
        }
    }

    public ModelPart root() {
        return root;
    }

    protected static int collectParts(List<ModelPart> parts, PoseStack poseStack, RenderType renderType, SubmitNodeCollector collector, int light, int overlayLight,
          int argb, @Nullable TextureAtlasSprite sprite, @UnknownNullability FoilRendering foil, int outlineColor, int nextOrder) {
        for (ModelPart part : parts) {
            collector.order(nextOrder++).submitModelPart(part, poseStack, renderType, light, overlayLight, sprite, argb, null, outlineColor);
            if (foil != FoilRendering.NONE) {
                collector.order(nextOrder++).submitModelPart(part, poseStack, foil.renderType(), light, overlayLight, sprite, argb, null, outlineColor);
            }
        }
        return nextOrder;
    }

    protected static List<ModelPart> getRenderableParts(ModelPart root, ModelPartData... modelPartData) {
        List<ModelPart> parts = new ArrayList<>(modelPartData.length);
        for (ModelPartData partData : modelPartData) {
            parts.add(partData.getFromRoot(root));
        }
        return parts;
    }

    public static LayerDefinition createLayerDefinition(int textureWidth, int textureHeight, ModelPartData... parts) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partDefinition = mesh.getRoot();
        for (ModelPartData part : parts) {
            part.addToDefinition(partDefinition);
        }
        return LayerDefinition.create(mesh, textureWidth, textureHeight);
    }

    public abstract static class NoState extends MekanismJavaModel<Unit> {

        public NoState(ModelPart root) {
            super(root);
        }

        public void setupAnim() {
            setupAnim(Unit.INSTANCE);
        }

        @Override
        public final void collect(Unit unused, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlayLight, @UnknownNullability FoilRendering foil, int outlineColor) {
            collect(poseStack, submitNodeCollector, light, overlayLight, foil, outlineColor);
        }

        public abstract void collect(PoseStack poseStack, SubmitNodeCollector collector, int light, int overlayLight, @UnknownNullability FoilRendering foil, int outlineColor);
    }

    public enum FoilRendering {
        NONE,
        ITEM,
        ARMOR;

        public RenderType renderType() {
            return switch (this) {
                case ITEM -> RenderTypes.entityGlint();
                case ARMOR -> MekanismRenderType.ARMOR_GLINT;
                default -> throw new IllegalStateException("No glint render type");
            };
        }

        public FoilRendering foil(boolean hasFoil) {
            return hasFoil ? this : NONE;
        }
    }
}