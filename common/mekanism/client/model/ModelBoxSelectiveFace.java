package mekanism.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.Tessellator;

public class ModelBoxSelectiveFace
{
    private PositionTextureVertex[] vertexPositions;
    
    private TexturedQuad[] quadList;
    
    public final float posX1;
    public final float posY1;
    public final float posZ1;
    
    public final float posX2;
    public final float posY2;
    public final float posZ2;

    public ModelBoxSelectiveFace(ModelRendererSelectiveFace modelRenderer, int textureOffsetU, int textureOffsetV, float xMin, float yMin, float zMin, int xSize, int ySize, int zSize, float scaleFactor)
    {
        posX1 = xMin;
        posY1 = yMin;
        posZ1 = zMin;
        
        posX2 = xMin + xSize;
        posY2 = yMin + ySize;
        posZ2 = zMin + zSize;
        
        vertexPositions = new PositionTextureVertex[8];
        
        quadList = new TexturedQuad[6];
        
        float xMax = xMin + xSize;
        float yMax = yMin + ySize;
        float zMax = zMin + zSize;
        
        xMin -= scaleFactor;
        yMin -= scaleFactor;
        zMin -= scaleFactor;
        
        xMax += scaleFactor;
        yMax += scaleFactor;
        zMax += scaleFactor;

        if(modelRenderer.mirror)
        {
            float placeholder = xMax;
            xMax = xMin;
            xMin = placeholder;
        }

        PositionTextureVertex positiontexturevertex0 = new PositionTextureVertex(xMin, yMin, zMin, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(xMax, yMin, zMin, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(xMax, yMax, zMin, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(xMin, yMax, zMin, 8.0F, 0.0F);
        PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(xMin, yMin, zMax, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(xMax, yMin, zMax, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(xMax, yMax, zMax, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(xMin, yMax, zMax, 8.0F, 0.0F);
        
        vertexPositions[0] = positiontexturevertex0;
        vertexPositions[1] = positiontexturevertex1;
        vertexPositions[2] = positiontexturevertex2;
        vertexPositions[3] = positiontexturevertex3;
        vertexPositions[4] = positiontexturevertex4;
        vertexPositions[5] = positiontexturevertex5;
        vertexPositions[6] = positiontexturevertex6;
        vertexPositions[7] = positiontexturevertex7;
        
        quadList[0] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex2, positiontexturevertex3, positiontexturevertex7, positiontexturevertex6}, textureOffsetU + zSize + xSize, textureOffsetV + zSize, textureOffsetU + zSize + xSize + xSize, textureOffsetV, modelRenderer.textureWidth, modelRenderer.textureHeight);
        quadList[1] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex5, positiontexturevertex4, positiontexturevertex0, positiontexturevertex1}, textureOffsetU + zSize, textureOffsetV, textureOffsetU + zSize + xSize, textureOffsetV + zSize, modelRenderer.textureWidth, modelRenderer.textureHeight);
        quadList[2] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex5, positiontexturevertex6, positiontexturevertex7}, textureOffsetU + zSize + xSize + zSize, textureOffsetV + zSize, textureOffsetU + zSize + xSize + zSize + xSize, textureOffsetV + zSize + ySize, modelRenderer.textureWidth, modelRenderer.textureHeight);
        quadList[3] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex1, positiontexturevertex0, positiontexturevertex3, positiontexturevertex2}, textureOffsetU + zSize, textureOffsetV + zSize, textureOffsetU + zSize + xSize, textureOffsetV + zSize + ySize, modelRenderer.textureWidth, modelRenderer.textureHeight);
        quadList[4] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex0, positiontexturevertex4, positiontexturevertex7, positiontexturevertex3}, textureOffsetU, textureOffsetV + zSize, textureOffsetU + zSize, textureOffsetV + zSize + ySize, modelRenderer.textureWidth, modelRenderer.textureHeight);
        quadList[5] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex5, positiontexturevertex1, positiontexturevertex2, positiontexturevertex6}, textureOffsetU + zSize + xSize, textureOffsetV + zSize, textureOffsetU + zSize + xSize + zSize, textureOffsetV + zSize + ySize, modelRenderer.textureWidth, modelRenderer.textureHeight);

        if(modelRenderer.mirror)
        {
            for(int quad = 0; quad < quadList.length; quad++)
            {
                quadList[quad].flipFace();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void render(Tessellator tessellator, boolean[] skippedFaces, float scaleFactor)
    {
    	if(skippedFaces.length == quadList.length)
		{
	        for(int i = 0; i < skippedFaces.length; ++i)
	        {
	        	if(!skippedFaces[i])
	        	{
	        		quadList[i].draw(tessellator, scaleFactor);
	        	}
	        }
		}
    }
}