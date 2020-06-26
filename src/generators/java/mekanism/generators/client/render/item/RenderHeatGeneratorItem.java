package mekanism.generators.client.render.item;

import javax.annotation.Nonnull;
import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.common.util.StorageUtils;
import mekanism.generators.client.model.ModelHeatGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

public class RenderHeatGeneratorItem extends ItemStackTileEntityRenderer {

    private static final ModelHeatGenerator heatGenerator = new ModelHeatGenerator();

    @Override
    public void func_239207_a_(@Nonnull ItemStack stack, @Nonnull TransformType transformType, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
        matrix.push();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        matrix.translate(0, -1, 0);
        heatGenerator.render(matrix, renderer, light, overlayLight, !StorageUtils.getStoredEnergyFromNBT(stack).isZero(), stack.hasEffect());
        matrix.pop();
    }
}