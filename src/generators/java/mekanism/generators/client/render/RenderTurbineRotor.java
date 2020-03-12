package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.Mekanism;
import mekanism.generators.client.model.ModelTurbine;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;

@ParametersAreNonnullByDefault
public class RenderTurbineRotor extends MekanismTileEntityRenderer<TileEntityTurbineRotor> {

    private static final float BASE_SPEED = 512F;
    public static boolean internalRender = false;
    private ModelTurbine model = new ModelTurbine();

    public RenderTurbineRotor(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityTurbineRotor tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (tile.getMultiblock() != null && !internalRender) {
            return;
        }

        matrix.push();

        int baseIndex = tile.getPosition() * 2;
        float rotateSpeed = 0.0F;

        if (tile.getMultiblock() != null && SynchronizedTurbineData.clientRotationMap.containsKey(tile.getMultiblock())) {
            rotateSpeed = SynchronizedTurbineData.clientRotationMap.getFloat(tile.getMultiblock());
        }

        if (!Mekanism.proxy.isPaused()) {
            tile.rotationLower = (tile.rotationLower + rotateSpeed * BASE_SPEED * (1F / (float) (baseIndex + 1))) % 360;
            tile.rotationUpper = (tile.rotationUpper + rotateSpeed * BASE_SPEED * (1F / (float) (baseIndex + 2))) % 360;
        }

        if (tile.getHousedBlades() > 0) {
            matrix.push();
            matrix.translate(0.5, -1, 0.5);
            matrix.rotate(Vector3f.YP.rotationDegrees(tile.rotationLower));
            model.render(matrix, renderer, light, overlayLight, baseIndex);
            matrix.pop();
        }

        if (tile.getHousedBlades() == 2) {
            matrix.push();
            matrix.translate(0.5, -0.5, 0.5);
            matrix.rotate(Vector3f.YP.rotationDegrees(tile.rotationUpper));
            model.render(matrix, renderer, light, overlayLight, baseIndex + 1);
            matrix.pop();
        }
        matrix.pop();
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.TURBINE_ROTOR;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTurbineRotor tile) {
        return tile.getMultiblock() != null && !internalRender;
    }
}