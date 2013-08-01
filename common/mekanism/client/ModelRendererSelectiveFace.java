package mekanism.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;

import org.bouncycastle.util.Arrays;
import org.lwjgl.opengl.GL11;

public class ModelRendererSelectiveFace
{
    public float textureWidth;
    public float textureHeight;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    public boolean[] connectedFaces;
    public boolean mirror;
    public boolean showModel;
    public boolean isHidden;
    public List cubeList;
    private int textureOffsetX;
    private int textureOffsetY;
    private int displayList;
    private boolean compiled;
    private ModelBase baseModel;
    

    public ModelRendererSelectiveFace(ModelBase par1ModelBase)
    {
        textureWidth = 64.0F;
        textureHeight = 32.0F;
        showModel = true;
        cubeList = new ArrayList();
        baseModel = par1ModelBase;
        setTextureSize(par1ModelBase.textureWidth, par1ModelBase.textureHeight);
    }

    public ModelRendererSelectiveFace(ModelBase par1ModelBase, int par2, int par3)
    {
        this(par1ModelBase);
        setTextureOffset(par2, par3);
    }

    public ModelRendererSelectiveFace setTextureOffset(int par1, int par2)
    {
        textureOffsetX = par1;
        textureOffsetY = par2;
        return this;
    }

    public ModelRendererSelectiveFace addBox(float minX, float minY, float minZ, int sizeX, int sizeY, int sizeZ)
    {
        cubeList.add(new ModelBoxSelectiveFace(this, textureOffsetX, textureOffsetY, minX, minY, minZ, sizeX, sizeY, sizeZ, 0.0F));
        return this;
    }

    public void setRotationPoint(float pointX, float pointY, float pointZ)
    {
        rotationPointX = pointX;
        rotationPointY = pointY;
        rotationPointZ = pointZ;
    }
    
    @SideOnly(Side.CLIENT)
    public void render(boolean[] connected, float par1)
    {
        if (!isHidden)
        {
            if (showModel)
            {
                if (!(compiled && Arrays.areEqual(connected, connectedFaces)))
                {
                	connectedFaces = connected;
                    compileDisplayList(par1);
                }

                GL11.glTranslatef(offsetX, offsetY, offsetZ);
                int i;

                if (rotateAngleX == 0.0F && rotateAngleY == 0.0F && rotateAngleZ == 0.0F)
                {
                    if (rotationPointX == 0.0F && rotationPointY == 0.0F && rotationPointZ == 0.0F)
                    {
                        GL11.glCallList(displayList);
                    }
                    else
                    {
                        GL11.glTranslatef(rotationPointX * par1, rotationPointY * par1, rotationPointZ * par1);
                        GL11.glCallList(displayList);
                        GL11.glTranslatef(-rotationPointX * par1, -rotationPointY * par1, -rotationPointZ * par1);
                    }
                }
                else
                {
                    GL11.glPushMatrix();
                    GL11.glTranslatef(rotationPointX * par1, rotationPointY * par1, rotationPointZ * par1);

                    if (rotateAngleZ != 0.0F)
                    {
                        GL11.glRotatef(rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
                    }

                    if (rotateAngleY != 0.0F)
                    {
                        GL11.glRotatef(rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
                    }

                    if (rotateAngleX != 0.0F)
                    {
                        GL11.glRotatef(rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
                    }

                    GL11.glCallList(displayList);
                    GL11.glPopMatrix();
                }

                GL11.glTranslatef(-offsetX, -offsetY, -offsetZ);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void compileDisplayList(float par1)
    {
        displayList = GLAllocation.generateDisplayLists(1);
        GL11.glNewList(displayList, GL11.GL_COMPILE);
        Tessellator tessellator = Tessellator.instance;

        for (int i = 0; i < cubeList.size(); ++i)
        {
            ((ModelBoxSelectiveFace)cubeList.get(i)).render(tessellator, connectedFaces, par1);
        }

        GL11.glEndList();
        compiled = true;
    }

    public ModelRendererSelectiveFace setTextureSize(int par1, int par2)
    {
        textureWidth = (float)par1;
        textureHeight = (float)par2;
        return this;
    }
}
