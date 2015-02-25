package mekanism.client.render.entity;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.entity.EntityFlame;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderFlame extends Render
{
    public void doRender(EntityFlame entity, double x, double y, double z, float f, float partialTick)
    {
    	float alpha = (float)(entity.ticksExisted+partialTick)/(float)EntityFlame.LIFESPAN;
    	float size = (float)Math.pow(2*alpha, 2);
    	
        GL11.glPushMatrix();
        MekanismRenderer.glowOn();
        MekanismRenderer.blendOn();
        GL11.glColor4f(1, 1, 1, 1-alpha);
        
        bindTexture(getEntityTexture(entity));
        
        GL11.glTranslatef((float)x, (float)y, (float)z);
        GL11.glRotatef((entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTick) - 90F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick, 0.0F, 0.0F, 1.0F);
        
        Tessellator tessellator = Tessellator.instance;
        
        int i = 0;
        float f2 = 0.0F;
        float f3 = 0.5F;
        float f4 = (float)(0 + i * 10) / 32F;
        float f5 = (float)(5 + i * 10) / 32F;
        float scale = 0.05625F*(0.8F+size);
        
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glRotatef(45F, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(scale, scale, scale);
        GL11.glTranslatef(-4F, 0.0F, 0.0F);

        for(int j = 0; j < 4; j++)
        {
            GL11.glRotatef(90F, 1.0F, 0.0F, 0.0F);
            GL11.glNormal3f(0.0F, 0.0F, scale);
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(-8D, -2D, 0.0D, f2, f4);
            tessellator.addVertexWithUV(8D, -2D, 0.0D, f3, f4);
            tessellator.addVertexWithUV(8D, 2D, 0.0D, f3, f5);
            tessellator.addVertexWithUV(-8D, 2D, 0.0D, f2, f5);
            tessellator.draw();
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        MekanismRenderer.glowOff();
        MekanismRenderer.blendOff();
        GL11.glPopMatrix();
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float f, float partialTick)
    {
        doRender((EntityFlame)entity, x, y, z, f, partialTick);
    }
    
    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return new ResourceLocation("mekanism:render/Flame.png");
    }
}