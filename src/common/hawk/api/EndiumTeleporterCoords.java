
package hawk.api;

/**
 * 
 * Used in order to store Endium Teleporter coordinates.
 * 
 * @author Elusivehawk
 */
public class EndiumTeleporterCoords
{
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private int dimensionID;
	private int symbolA;
	private int symbolB;
	private int symbolC;
	
	public EndiumTeleporterCoords(int x, int y, int z, int dimension, int symbol1, int symbol2, int symbol3)
	{
		if (symbol1 != 0 && symbol2 != 0 && symbol3 != 0)
		{
			this.xCoord = x;
			this.yCoord = y;
			this.zCoord = z;
			this.dimensionID = dimension;
			this.symbolA = symbol1;
			this.symbolB = symbol2;
			this.symbolC = symbol3;
			
		}
		else
		{
			throw new RuntimeException("Hawk's Machinery: Symbol cannot be 0!");
		}
		
	}
	
	public boolean isEqual(int symbol1, int symbol2, int symbol3)
	{
		return this.symbolA == symbol1 && this.symbolB == symbol2 && this.symbolC == symbol3;
	}
	
	public boolean isEqualXYZ(int x, int y, int z)
	{
		return this.xCoord == x && this.yCoord == y && this.zCoord == z;
	}
	
	public int x()
	{
		return this.xCoord;
	}
	
	public int y()
	{
		return this.yCoord;
	}
	
	public int z()
	{
		return this.zCoord;
	}
	
	public int symA()
	{
		return this.symbolA;
	}
	
	public int symB()
	{
		return this.symbolB;
	}
	
	public int symC()
	{
		return this.symbolC;
	}
	
	public int dimension()
	{
		return this.dimensionID;
	}
	
	public void setSymbolA(int symbol)
	{
		this.symbolA = symbol;
	}
	
	public void setSymbolB(int symbol)
	{
		this.symbolB = symbol;
	}
	
	public void setSymbolC(int symbol)
	{
		this.symbolC = symbol;
	}
	
	public void setDimension(int dimension)
	{
		this.dimensionID = dimension;
	}
	
}
