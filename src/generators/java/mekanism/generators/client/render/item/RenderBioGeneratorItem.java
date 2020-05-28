package mekanism.generators.client.render.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.generators.client.model.ModelBioGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;

public class RenderBioGeneratorItem extends ItemStackTileEntityRenderer {

    private static final ModelBioGenerator bioGenerator = new ModelBioGenerator();

    @Override
    public void render(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
        matrix.push();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        matrix.translate(0, -1, 0);
        bioGenerator.render(matrix, renderer, light, overlayLight);
        matrix.pop();
    }
}