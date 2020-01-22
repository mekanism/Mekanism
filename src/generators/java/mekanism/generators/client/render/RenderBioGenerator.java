package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.generators.client.model.ModelBioGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

public class RenderBioGenerator extends TileEntityRenderer<TileEntityBioGenerator> {

    private static final Map<Direction, Map<Integer, Model3D>> energyDisplays = new EnumMap<>(Direction.class);
    private static final int stages = 40;
    private ModelBioGenerator model = new ModelBioGenerator();

    public static void resetCachedModels() {
        energyDisplays.clear();
    }

    public RenderBioGenerator(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void render(@Nonnull TileEntityBioGenerator tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
        if (tile.bioFuelSlot.fluidStored > 0) {
            matrix.push();
            GlowInfo glowInfo = MekanismRenderer.enableGlow();
            //TODO: FIXME, you can see through the back. Might have to make the main "model" into json and then just render the fluid as a TER
            MekanismRenderer.renderObject(getModel(tile.getDirection(), tile.getScaledFuelLevel(stages - 1)), matrix, renderer,
                  MekanismRenderType.renderFluidState(AtlasTexture.LOCATION_BLOCKS_TEXTURE), -1);
            MekanismRenderer.disableGlow(glowInfo);
            matrix.pop();
        }
        matrix.push();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 180, 0, 270, 90);
        matrix.rotate(Vector3f.field_229183_f_.func_229187_a_(180));
        model.render(matrix, renderer, light, overlayLight);
        matrix.pop();
    }

    @SuppressWarnings("incomplete-switch")
    private Model3D getModel(Direction side, int stage) {
        if (energyDisplays.containsKey(side) && energyDisplays.get(side).containsKey(stage)) {
            return energyDisplays.get(side).get(stage);
        }
        Model3D model = new Model3D();
        model.baseBlock = Blocks.WATER;
        model.setTexture(MekanismRenderer.energyIcon);
        switch (side) {
            case NORTH:
                model.minZ = 0.5;
                model.maxZ = 0.875;

                model.minX = 0.1875;
                model.maxX = 0.8215;
                break;
            case SOUTH:
                model.minZ = 0.125;
                model.maxZ = 0.5;

                model.minX = 0.1875;
                model.maxX = 0.8215;
                break;
            case WEST:
                model.minX = 0.5;
                model.maxX = 0.875;

                model.minZ = 0.1875;
                model.maxZ = 0.8215;
                break;
            case EAST:
                model.minX = 0.125;
                model.maxX = 0.5;

                model.minZ = 0.1875;
                model.maxZ = 0.8215;
                break;
        }
        model.minY = 0.4375 + 0.001;  //prevent z fighting at low fuel levels
        model.maxY = 0.4375 + ((float) stage / stages) * 0.4375 + 0.001;
        if (energyDisplays.containsKey(side)) {
            energyDisplays.get(side).put(stage, model);
        } else {
            Map<Integer, Model3D> map = new Int2ObjectOpenHashMap<>();
            map.put(stage, model);
            energyDisplays.put(side, map);
        }
        return model;
    }
}