package mekanism.client;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import net.minecraft.client.renderer.IImageBuffer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CapeBufferDownload implements IImageBuffer
{
    private int[] imageData;
    private int imageWidth;
    private int imageHeight;
    
    @Override
    public BufferedImage parseUserSkin(BufferedImage bufferedImage)
    {
        if(bufferedImage == null)
        {
            return null;
        }
        
        imageWidth = bufferedImage.getWidth(null);
        imageHeight = bufferedImage.getHeight(null);
        
        BufferedImage imageBuffer = new BufferedImage(imageWidth, imageHeight, 2);
        
        Graphics graphics = imageBuffer.getGraphics();
        graphics.drawImage(bufferedImage, 0, 0, null);
        graphics.dispose();
        
        imageData = ((DataBufferInt)imageBuffer.getRaster().getDataBuffer()).getData();
        
        boolean flag = false;
        
        int i;
        int j;
        int k;
        
        for(i = 32; i < 64; i++)
        {
            for(j = 0; j < 16; j++)
            {
                k = imageData[i + j * 64];
                
                if((k >> 24 & 0xFF) >= 128)
                {
                    continue;
                }
                
                flag = true;
            }
        }
        
        if(!flag)
        {
            for(i = 32; i < 64; i++)
            {
                for(j = 0; j < 16; j++)
                {
                    k = imageData[i + j * 64];
                    
                    if((k >> 24 & 0xFF) >= 128)
                    {
                        continue;
                    }
                    
                    flag = true;
                }
            }
        }
        
        return imageBuffer;
    }
}
