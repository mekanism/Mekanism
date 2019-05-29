package mekanism.generators.client.render;

import mekanism.client.render.MekanismRenderHelper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelGasGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGasGenerator extends TileEntitySpecialRenderer<TileEntityGasGenerator> {

    private ModelGasGenerator model = new ModelGasGenerator();

    @Override
    public void render(TileEntityGasGenerator tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).translate(x + 0.5, y + 1.5, z + 0.5);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "GasGenerator.png"));

        switch (tileEntity.facing.ordinal()) {
            case 2://NORTH
                renderHelper.rotateY(90, 1);
                break;
            case 3://SOUTH
                renderHelper.rotateY(270, 1);
                break;
            case 4://WEST
                renderHelper.rotateY(180, 1);
                break;
            case 5://EAST
                renderHelper.rotateY(0, 1);
                break;
        }

        renderHelper.rotateYZ(180, 1, 1).rotateY(90, -1).rotateY(90, 1);
        model.render(0.0625F);
        renderHelper.cleanup();
    }
}