package net.darkhax.tesla.lib;

import net.darkhax.tesla.api.ITeslaHolder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PowerBar {
    
    /**
     * Constant reference to the included gui element sheet.
     */
    private static final ResourceLocation TEXTURE_SHEET = new ResourceLocation("tesla", "textures/gui/elements.png");
    
    /**
     * The x position of the power bar on the screen.
     */
    private final int x;
    
    /**
     * The y position of the power bar on the screen.
     */
    private final int y;
    
    /**
     * The screen to draw the power bar on.
     */
    private final GuiScreen screen;
    
    /**
     * The type of background the use when drawing the power bar.
     */
    private final BackgroundType background;
    
    /**
     * Object that can be used to draw a power bar on the screen.
     *
     * @param screen The screen to draw the power bar on.
     * @param x The x position of the power bar on the screen.
     * @param y The y position of the power bar on the screen.
     * @param type The background type to use for the power bar.
     */
    public PowerBar(GuiScreen screen, int x, int y, BackgroundType type) {
        
        this.background = type;
        this.screen = screen;
        this.x = x;
        this.y = y;
    }
    
    /**
     * Draws a power bar that represents the power held within an ITeslaHolder.
     * 
     * @param holder The holder to represent.
     */
    public void draw (ITeslaHolder holder) {
        
        draw(holder.getStoredPower(), holder.getCapacity());
    }
    
    /**
     * Draws a power bard that represents the based power information.
     * 
     * @param power The amount of power to represent.
     * @param capacity The capacity to represent.
     */
    public void draw (long power, long capacity) {
        
        screen.mc.getTextureManager().bindTexture(TEXTURE_SHEET);
        
        if (this.background == BackgroundType.LIGHT)
            screen.drawTexturedModalRect(x, y, 3, 1, 14, 50);
            
        else if (this.background == BackgroundType.DARK)
            screen.drawTexturedModalRect(x, y, 3, 53, 14, 50);
            
        long powerOffset = (power * 51) / capacity;
        screen.drawTexturedModalRect(x + 1, (int) (y + 50 - powerOffset), 18, (int) (51 - powerOffset), 14, (int) (powerOffset + 2));
    }
    
    /**
     * Describes a background image to be rendered for a power bar.
     */
    public enum BackgroundType {
        
        /**
         * No background texture.
         */
        NONE,
        
        /**
         * The lighter background texture.
         */
        LIGHT,
        
        /**
         * The darker background texture.
         */
        DARK,
    }
}