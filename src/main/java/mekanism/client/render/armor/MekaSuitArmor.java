package mekanism.client.render.armor;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mekanism.client.render.obj.OBJModelCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.geometry.IModelGeometryPart;

public class MekaSuitArmor extends CustomArmor {

    public static MekaSuitArmor HELMET = new MekaSuitArmor(0.5F, EquipmentSlotType.HEAD);
    public static MekaSuitArmor BODYARMOR = new MekaSuitArmor(0.5F, EquipmentSlotType.CHEST);
    public static MekaSuitArmor PANTS = new MekaSuitArmor(0.5F, EquipmentSlotType.LEGS);
    public static MekaSuitArmor BOOTS = new MekaSuitArmor(0.5F, EquipmentSlotType.FEET);

    private static IModelConfiguration HELMET_CONFIG = new MekaSuitModelConfiguration(s -> s.startsWith("helmet"));
    private static IModelConfiguration CHEST_CONFIG = new MekaSuitModelConfiguration(s -> s.startsWith("chest") || s.startsWith("shoulder") || s.startsWith("back"));
    private static IModelConfiguration LEFT_ARM_CONFIG = new MekaSuitModelConfiguration(s -> s.startsWith("left_arm"));
    private static IModelConfiguration RIGHT_ARM_CONFIG = new MekaSuitModelConfiguration(s -> s.startsWith("right_arm"));

    private EquipmentSlotType type;

    private MekaSuitArmor(float size, EquipmentSlotType type) {
        super(size);
        this.type = type;
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect, ItemStack stack) {
        if (type == EquipmentSlotType.HEAD) {
            matrix.push();
            IBakedModel model = OBJModelCache.MEKASUIT.getBakedModel(HELMET_CONFIG);
            render(renderer, matrix, light, overlayLight, model);
            bipedHead.translateRotate(matrix);
            matrix.pop();
        } else if (type == EquipmentSlotType.CHEST) {
            matrix.push();
            IBakedModel model = OBJModelCache.MEKASUIT.getBakedModel(CHEST_CONFIG);
            render(renderer, matrix, light, overlayLight, model);
            bipedBody.translateRotate(matrix);
            matrix.pop();
        } else if (type == EquipmentSlotType.LEGS) {
            matrix.push();
            bipedLeftLeg.translateRotate(matrix);
            matrix.pop();
            matrix.push();
            bipedRightLeg.translateRotate(matrix);
            matrix.pop();
        } else if (type == EquipmentSlotType.FEET) {
            matrix.push();
            bipedLeftLeg.translateRotate(matrix);
            matrix.pop();
            matrix.push();
            bipedRightLeg.translateRotate(matrix);
            matrix.pop();
        }
    }

    private void render(IRenderTypeBuffer renderer, MatrixStack matrix, int light, int overlayLight, IBakedModel model) {
        IVertexBuilder builder = renderer.getBuffer(RenderType.getCutout());
        MatrixStack.Entry last = matrix.getLast();
        for (BakedQuad quad : model.getQuads(null, null, Minecraft.getInstance().world.getRandom(), EmptyModelData.INSTANCE)) {
            builder.addVertexData(last, quad, 1, 1, 1, 1, light, overlayLight);
        }
    }

    private static class MekaSuitModelConfiguration implements IModelConfiguration {

        private Predicate<String> canShow;

        public MekaSuitModelConfiguration(Predicate<String> canShow) {
            this.canShow = canShow;
        }

        @Nullable
        @Override
        public IUnbakedModel getOwnerModel() {
            return null;
        }

        @Nonnull
        @Override
        public String getModelName() {
            return "transmitter_contents";
        }

        @Override
        public boolean isTexturePresent(@Nonnull String name) {
            System.out.println("IS PRESENT " + name);
            return false;
        }

        @Nonnull
        @Override
        public Material resolveTexture(@Nonnull String name) {
            System.out.println("MATERIAL " + name);
            return ModelLoaderRegistry.blockMaterial(name);
        }

        @Override
        public boolean isShadedInGui() {
            return false;
        }

        @Override
        public boolean isSideLit() {
            return false;
        }

        @Override
        public boolean useSmoothLighting() {
            return false;
        }

        @Nonnull
        @Override
        @Deprecated
        public ItemCameraTransforms getCameraTransforms() {
            return ItemCameraTransforms.DEFAULT;
        }

        @Nonnull
        @Override
        public IModelTransform getCombinedTransform() {
            return ModelRotation.X0_Y0;
        }

        @Override
        public boolean getPartVisibility(IModelGeometryPart part, boolean fallback) {
            //Ignore fallback as we always have a true or false answer
            return getPartVisibility(part);
        }

        @Override
        public boolean getPartVisibility(@Nonnull IModelGeometryPart part) {
            return canShow.test(part.name());
        }
    }
}
