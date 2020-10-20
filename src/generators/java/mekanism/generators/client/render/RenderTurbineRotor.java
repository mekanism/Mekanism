package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.ModelTurbine;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.vector.Vector3f;

@ParametersAreNonnullByDefault
public class RenderTurbineRotor extends MekanismTileEntityRenderer<TileEntityTurbineRotor> {

    public static RenderTurbineRotor INSTANCE;
    private static final float BASE_SPEED = 512F;
    public final ModelTurbine model = new ModelTurbine();

    public RenderTurbineRotor(TileEntityRendererDispatcher renderer) {
        super(renderer);
        INSTANCE = this;
    }

    @Override
    protected void render(TileEntityTurbineRotor tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (tile.getMultiblock() == null) {
            render(tile, matrix, model.getBuffer(renderer), light, overlayLight);
        }
    }

    public void render(TileEntityTurbineRotor tile, MatrixStack matrix, IVertexBuilder buffer, int light, int overlayLight) {
        int housedBlades = tile.getHousedBlades();
        if (housedBlades == 0) {
            return;
        }
        int baseIndex = tile.getPosition() * 2;
        if (!Minecraft.getInstance().isGamePaused()) {
            if (tile.getMultiblock() != null && TurbineMultiblockData.clientRotationMap.containsKey(tile.getMultiblock())) {
                float rotateSpeed = TurbineMultiblockData.clientRotationMap.getFloat(tile.getMultiblock()) * BASE_SPEED;
                tile.rotationLower = (tile.rotationLower + rotateSpeed * (1F / (baseIndex + 1))) % 360;
                tile.rotationUpper = (tile.rotationUpper + rotateSpeed * (1F / (baseIndex + 2))) % 360;
            } else {
                tile.rotationLower = tile.rotationLower % 360;
                tile.rotationUpper = tile.rotationUpper % 360;
            }
        }
        //Bottom blade
        matrix.push();
        matrix.translate(0.5, -1, 0.5);
        matrix.rotate(Vector3f.YP.rotationDegrees(tile.rotationLower));
        model.render(matrix, buffer, light, overlayLight, baseIndex);
        matrix.pop();
        //Top blade
        if (housedBlades == 2) {
            matrix.push();
            matrix.translate(0.5, -0.5, 0.5);
            matrix.rotate(Vector3f.YP.rotationDegrees(tile.rotationUpper));
            model.render(matrix, buffer, light, overlayLight, baseIndex + 1);
            matrix.pop();
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.TURBINE_ROTOR;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTurbineRotor tile) {
        return tile.getMultiblock() == null && tile.getHousedBlades() > 0;
    }
}