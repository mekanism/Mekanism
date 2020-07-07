package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

@ParametersAreNonnullByDefault
public class RenderPersonalChest extends MekanismTileEntityRenderer<TileEntityPersonalChest> {

    private static final ResourceLocation texture = MekanismUtils.getResource(ResourceType.TEXTURE_BLOCKS, "models/personal_chest.png");

    private final ModelRenderer lid;
    private final ModelRenderer base;
    private final ModelRenderer latch;

    public RenderPersonalChest(TileEntityRendererDispatcher renderer) {
        super(renderer);
        this.base = new ModelRenderer(64, 64, 0, 19);
        this.base.addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F, 0.0F);
        this.lid = new ModelRenderer(64, 64, 0, 0);
        this.lid.addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F, 0.0F);
        this.lid.rotationPointY = 9.0F;
        this.lid.rotationPointZ = 1.0F;
        this.latch = new ModelRenderer(64, 64, 0, 0);
        this.latch.addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
        this.latch.rotationPointY = 8.0F;
    }

    @Override
    protected void render(TileEntityPersonalChest tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        matrix.push();
        if (!tile.isRemoved()) {
            matrix.translate(0.5D, 0.5D, 0.5D);
            matrix.rotate(Vector3f.YP.rotationDegrees(-tile.getDirection().getHorizontalAngle()));
            matrix.translate(-0.5D, -0.5D, -0.5D);
        }
        float lidAngle = tile.prevLidAngle + (tile.lidAngle - tile.prevLidAngle) * partialTick;
        lidAngle = 1.0F - lidAngle;
        lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;
        IVertexBuilder builder = renderer.getBuffer(RenderType.getEntityCutout(texture));
        lid.rotateAngleX = -(lidAngle * ((float) Math.PI / 2F));
        latch.rotateAngleX = lid.rotateAngleX;
        lid.render(matrix, builder, light, overlayLight);
        latch.render(matrix, builder, light, overlayLight);
        base.render(matrix, builder, light, overlayLight);
        matrix.pop();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.PERSONAL_CHEST;
    }
}