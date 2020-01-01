package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.generators.client.model.ModelBioGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

public class RenderBioGenerator extends TileEntityRenderer<TileEntityBioGenerator> {

    private static final int stages = 40;
    private ModelBioGenerator model = new ModelBioGenerator();
    private Map<Direction, DisplayInteger[]> energyDisplays = new EnumMap<>(Direction.class);

    public RenderBioGenerator(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void func_225616_a_(@Nonnull TileEntityBioGenerator tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
        if (tile.bioFuelSlot.fluidStored > 0) {
            //TODO: 1.15
            /*matrix.func_227860_a_();
            RenderSystem.enableCull();
            RenderSystem.enableBlend();
            RenderSystem.disableLighting();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlowInfo glowInfo = MekanismRenderer.enableGlow();
            RenderSystem.translatef((float) x, (float) y, (float) z);
            field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);
            getDisplayList(tile.getDirection())[tile.getScaledFuelLevel(stages - 1)].render();
            MekanismRenderer.disableGlow(glowInfo);
            RenderSystem.enableLighting();
            RenderSystem.disableBlend();
            RenderSystem.disableCull();
            matrix.func_227865_b_();*/
        }

        matrix.func_227860_a_();
        matrix.func_227861_a_(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 180, 0, 270, 90);
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(180));
        model.render(matrix, renderer, light, overlayLight);
        matrix.func_227865_b_();
    }

    //TODO: 1.15
    /*@SuppressWarnings("incomplete-switch")
    private DisplayInteger[] getDisplayList(Direction side) {
        if (energyDisplays.containsKey(side)) {
            return energyDisplays.get(side);
        }

        DisplayInteger[] displays = new DisplayInteger[stages];

        Model3D model3D = new Model3D();
        model3D.baseBlock = Blocks.WATER;
        model3D.setTexture(MekanismRenderer.energyIcon);

        for (int i = 0; i < stages; i++) {
            displays[i] = DisplayInteger.createAndStart();

            switch (side) {
                case NORTH:
                    model3D.minZ = 0.5;
                    model3D.maxZ = 0.875;

                    model3D.minX = 0.1875;
                    model3D.maxX = 0.8215;
                    break;
                case SOUTH:
                    model3D.minZ = 0.125;
                    model3D.maxZ = 0.5;

                    model3D.minX = 0.1875;
                    model3D.maxX = 0.8215;
                    break;
                case WEST:
                    model3D.minX = 0.5;
                    model3D.maxX = 0.875;

                    model3D.minZ = 0.1875;
                    model3D.maxZ = 0.8215;
                    break;
                case EAST:
                    model3D.minX = 0.125;
                    model3D.maxX = 0.5;

                    model3D.minZ = 0.1875;
                    model3D.maxZ = 0.8215;
                    break;
            }

            model3D.minY = 0.4375 + 0.001;  //prevent z fighting at low fuel levels
            model3D.maxY = 0.4375 + ((float) i / stages) * 0.4375 + 0.001;

            MekanismRenderer.renderObject(model3D);
            GlStateManager.endList();
        }

        energyDisplays.put(side, displays);
        return displays;
    }*/
}