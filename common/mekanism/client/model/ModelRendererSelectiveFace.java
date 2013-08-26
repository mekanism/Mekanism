package mekanism.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.BooleanArray;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.Tessellator;

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
    
    public boolean mirror;
    public boolean showModel;
    public boolean isHidden;
    
    public List<ModelBoxSelectiveFace> cubeList = new ArrayList<ModelBoxSelectiveFace>();
    
    private int textureOffsetX;
    private int textureOffsetY;
    
    private ModelBase baseModel;
    
    private Map<BooleanArray, DisplayInteger> displayLists = new HashMap<BooleanArray, DisplayInteger>();
    

    public ModelRendererSelectiveFace(ModelBase modelBase)
    {
        textureWidth = 64.0F;
        textureHeight = 32.0F;
        showModel = true;
        baseModel = modelBase;
        
        setTextureSize(modelBase.textureWidth, modelBase.textureHeight);
    }

    public ModelRendererSelectiveFace(ModelBase modelBase, int offsetX, int offsetY)
    {
        this(modelBase);
        setTextureOffset(offsetX, offsetY);
    }

    public ModelRendererSelectiveFace setTextureOffset(int offsetX, int offsetY)
    {
        textureOffsetX = offsetX;
        textureOffsetY = offsetY;
        
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
    public void render(boolean[] dontRender, float scaleFactor)
    {
        if(!isHidden)
        {
            if(showModel)
            {
            	DisplayInteger currentDisplayList = displayLists.get(new BooleanArray(dontRender));
            	
                if(currentDisplayList == null)
                {
                    currentDisplayList = compileDisplayList(dontRender, scaleFactor);
                }

                GL11.glTranslatef(offsetX, offsetY, offsetZ);
                
                int i;

                if(rotateAngleX == 0.0F && rotateAngleY == 0.0F && rotateAngleZ == 0.0F)
                {
                    if(rotationPointX == 0.0F && rotationPointY == 0.0F && rotationPointZ == 0.0F)
                    {
                        currentDisplayList.render();
                    }
                    else {
                        GL11.glTranslatef(rotationPointX * scaleFactor, rotationPointY * scaleFactor, rotationPointZ * scaleFactor);
                        currentDisplayList.render();
                        GL11.glTranslatef(-rotationPointX * scaleFactor, -rotationPointY * scaleFactor, -rotationPointZ * scaleFactor);
                    }
                }
                else
                {
                    GL11.glPushMatrix();
                    GL11.glTranslatef(rotationPointX * scaleFactor, rotationPointY * scaleFactor, rotationPointZ * scaleFactor);

                    if(rotateAngleZ != 0.0F)
                    {
                        GL11.glRotatef(rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
                    }

                    if(rotateAngleY != 0.0F)
                    {
                        GL11.glRotatef(rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
                    }

                    if(rotateAngleX != 0.0F)
                    {
                        GL11.glRotatef(rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
                    }

                    currentDisplayList.render();
                    GL11.glPopMatrix();
                }

                GL11.glTranslatef(-offsetX, -offsetY, -offsetZ);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private DisplayInteger compileDisplayList(boolean[] dontRender, float scaleFactor)
    {
        DisplayInteger displayList = DisplayInteger.createAndStart();
        Tessellator tessellator = Tessellator.instance;

        for(int i = 0; i < cubeList.size(); ++i)
        {
            cubeList.get(i).render(tessellator, dontRender, scaleFactor);
        }

        displayList.endList();
        displayLists.put(new BooleanArray(dontRender), displayList);
        
        return displayList;
    }

    public ModelRendererSelectiveFace setTextureSize(int sizeX, int sizeY)
    {
        textureWidth = sizeX;
        textureHeight = sizeY;
        
        return this;
    }
}
