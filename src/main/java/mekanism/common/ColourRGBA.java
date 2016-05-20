package mekanism.common;

/**
 * Created by ben on 30/04/16.
 */
public class ColourRGBA
{
	public byte r;
	public byte g;
	public byte b;
	public byte a;

    public ColourRGBA(double d1, double d2, double d3, double d4) 
    {
    	r = (byte)d1;
    	g = (byte)d2;
        b = (byte)d3;
        a = (byte)d4;
    }
    
    public int rgba() 
    {
        return (r & 0xFF) << 24 | (g & 0xFF) << 16 | (b & 0xFF) << 8 | (a & 0xFF);
    }
}
