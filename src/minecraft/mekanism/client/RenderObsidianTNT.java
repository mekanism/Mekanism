package mekanism.client;

import mekanism.common.EntityObsidianTNT;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

public class RenderObsidianTNT extends Render
{
    private RenderBlocks blockRenderer;

    public RenderObsidianTNT()
    {
        blockRenderer = new RenderBlocks();
        shadowSize = 0.5F;
    }
    
    @Override
    public void doRender(Entity entity, double x, double y, double z, float f, float f1)
    {
        renderObsidianTNT((EntityObsidianTNT)entity, x, y, z, f, f1);
    }

    public void renderObsidianTNT(EntityObsidianTNT entityobsidiantnt, double x, double y, double z, float f, float f1)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);
        GL11.glScalef(0.8F, 0.8F, 0.8F);

        if((entityobsidiantnt.fuse - f1) + 1.0F < 10F)
        {
            float scale = 1.0F - ((entityobsidiantnt.fuse - f1) + 1.0F) / 10F;

            if(scale < 0.0F)
            {
                scale = 0.0F;
            }

            if(scale > 1.0F)
            {
                scale = 1.0F;
            }

            scale *= scale;
            scale *= scale;
            float renderScale = 1.0F + scale * 0.3F;
            GL11.glScalef(renderScale, renderScale, renderScale);
        }

        float f3 = (1.0F - ((entityobsidiantnt.fuse - f1) + 1.0F) / 100F) * 0.8F;
        loadTexture("/terrain.png");
        blockRenderer.renderBlockAsItem(Mekanism.ObsidianTNT, 0, entityobsidiantnt.getBrightness(f1));

        if(entityobsidiantnt.fuse / 5 % 2 == 0)
        {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, f3);
            blockRenderer.renderBlockAsItem(Mekanism.ObsidianTNT, 0, 1.0F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        GL11.glPopMatrix();
    }
}
