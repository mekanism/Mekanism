package mekanism.client;

import mekanism.common.EntityObsidianTNT;
import mekanism.common.Mekanism;
import net.minecraft.src.*;

import org.lwjgl.opengl.GL11;

public class RenderObsidianTNT extends Render
{
    private RenderBlocks blockRenderer;

    public RenderObsidianTNT()
    {
        blockRenderer = new RenderBlocks();
        shadowSize = 0.5F;
    }

    public void renderObsidianTNT(EntityObsidianTNT entityobsidiantnt, double d, double d1, double d2, float f, float f1)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)d, (float)d1, (float)d2);

        if (((float)entityobsidiantnt.fuse - f1) + 1.0F < 10F)
        {
            float f2 = 1.0F - (((float)entityobsidiantnt.fuse - f1) + 1.0F) / 10F;

            if (f2 < 0.0F)
            {
                f2 = 0.0F;
            }

            if (f2 > 1.0F)
            {
                f2 = 1.0F;
            }

            f2 *= f2;
            f2 *= f2;
            float f4 = 1.0F + f2 * 0.3F;
            GL11.glScalef(f4, f4, f4);
        }

        float f3 = (1.0F - (((float)entityobsidiantnt.fuse - f1) + 1.0F) / 100F) * 0.8F;
        loadTexture("/resources/mekanism/textures/terrain.png");
        blockRenderer.renderBlockAsItem(Mekanism.ObsidianTNT, 0, entityobsidiantnt.getBrightness(f1));

        if ((entityobsidiantnt.fuse / 5) % 2 == 0)
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

    @Override
    public void doRender(Entity entity, double d, double d1, double d2, float f, float f1)
    {
        renderObsidianTNT((EntityObsidianTNT)entity, d, d1, d2, f, f1);
    }
}
