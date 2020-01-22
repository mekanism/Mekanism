package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.block.basic.BlockTeleporterFrame;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderTeleporter extends TileEntityRenderer<TileEntityTeleporter> {

    private static Model3D EAST_WEST;
    private static Model3D NORTH_SOUTH;

    public static void resetCachedModels() {
        EAST_WEST = null;
        NORTH_SOUTH = null;
    }

    public RenderTeleporter(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void render(@Nonnull TileEntityTeleporter tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
        if (tile.shouldRender && tile.getWorld() != null) {
            matrix.push();
            GlowInfo glowInfo = MekanismRenderer.enableGlow();
            //TODO: Improve how it calculates which direction it is facing? In case there are multiple teleporters touching?
            Model3D overlayModel = getOverlayModel(tile.getWorld().getBlockState(tile.getPos().west()).getBlock() instanceof BlockTeleporterFrame);
            MekanismRenderer.renderObject(overlayModel, matrix, renderer, MekanismRenderType.configurableMachineState(AtlasTexture.LOCATION_BLOCKS_TEXTURE),
                  MekanismRenderer.getColorARGB(EnumColor.PURPLE, 0.75F));
            MekanismRenderer.disableGlow(glowInfo);
            matrix.pop();
        }
    }

    private Model3D getOverlayModel(boolean eastWest) {
        if (eastWest) {
            if (EAST_WEST == null) {
                EAST_WEST = new Model3D();
                EAST_WEST.baseBlock = Blocks.STONE;
                EAST_WEST.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getGas()));
                EAST_WEST.minY = 1;
                EAST_WEST.maxY = 3;
                EAST_WEST.minX = 0;
                EAST_WEST.minZ = 0.46;
                EAST_WEST.maxX = 1;
                EAST_WEST.maxZ = 0.54;
            }
            return EAST_WEST;
        }
        if (NORTH_SOUTH == null) {
            NORTH_SOUTH = new Model3D();
            NORTH_SOUTH.baseBlock = Blocks.STONE;
            NORTH_SOUTH.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getGas()));
            NORTH_SOUTH.minY = 1;
            NORTH_SOUTH.maxY = 3;
            NORTH_SOUTH.minX = 0.46;
            NORTH_SOUTH.minZ = 0;
            NORTH_SOUTH.maxX = 0.54;
            NORTH_SOUTH.maxZ = 1;
        }
        return NORTH_SOUTH;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTeleporter tile) {
        return tile.shouldRender && tile.getWorld() != null;
    }
}