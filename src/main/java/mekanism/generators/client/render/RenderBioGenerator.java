package mekanism.generators.client.render;

import java.util.EnumMap;
import java.util.Map;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelBioGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderBioGenerator extends TileEntitySpecialRenderer<TileEntityBioGenerator> {

    private static final int stages = 40;
    private ModelBioGenerator model = new ModelBioGenerator();
    private Map<EnumFacing, DisplayInteger[]> energyDisplays = new EnumMap<>(EnumFacing.class);

    @Override
    public void render(TileEntityBioGenerator tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        if (tileEntity.bioFuelSlot.fluidStored > 0) {
            push();

            MekanismRenderer.glowOn();
            GlStateManager.translate((float) x, (float) y, (float) z);
            bindTexture(MekanismRenderer.getBlocksTexture());
            getDisplayList(tileEntity.facing)[tileEntity.getScaledFuelLevel(stages - 1)].render();
            MekanismRenderer.glowOff();

            pop();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "BioGenerator.png"));

        switch (tileEntity.facing.ordinal()) {
            case 2:
                GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F);
                break;
            case 3:
                GlStateManager.rotate(0, 0.0F, 1.0F, 0.0F);
                break;
            case 4:
                GlStateManager.rotate(270, 0.0F, 1.0F, 0.0F);
                break;
            case 5:
                GlStateManager.rotate(90, 0.0F, 1.0F, 0.0F);
                break;
        }

        GlStateManager.rotate(180, 0F, 0F, 1F);
        model.render(0.0625F);
        GlStateManager.popMatrix();
    }

    @SuppressWarnings("incomplete-switch")
    private DisplayInteger[] getDisplayList(EnumFacing side) {
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
                case NORTH: {
                    model3D.minZ = 0.5;
                    model3D.maxZ = 0.875;

                    model3D.minX = 0.1875;
                    model3D.maxX = 0.8215;
                    break;
                }
                case SOUTH: {
                    model3D.minZ = 0.125;
                    model3D.maxZ = 0.5;

                    model3D.minX = 0.1875;
                    model3D.maxX = 0.8215;
                    break;
                }
                case WEST: {
                    model3D.minX = 0.5;
                    model3D.maxX = 0.875;

                    model3D.minZ = 0.1875;
                    model3D.maxZ = 0.8215;
                    break;
                }
                case EAST: {
                    model3D.minX = 0.125;
                    model3D.maxX = 0.5;

                    model3D.minZ = 0.1875;
                    model3D.maxZ = 0.8215;
                    break;
                }
            }

            model3D.minY = 0.4375 + 0.001;  //prevent z fighting at low fuel levels
            model3D.maxY = 0.4375 + ((float) i / stages) * 0.4375 + 0.001;

            MekanismRenderer.renderObject(model3D);
            DisplayInteger.endList();
        }

        energyDisplays.put(side, displays);

        return displays;
    }

    private void pop() {
        GL11.glPopAttrib();
        GlStateManager.popMatrix();
    }

    private void push() {
        GlStateManager.pushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
}