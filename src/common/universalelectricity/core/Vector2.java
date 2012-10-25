package universalelectricity.core;

import net.minecraft.src.MathHelper;

/**
 * Vector2 Class is used for defining objects in a 2D space. Vector2 makes it
 * easier to handle the coordinates of objects. Instead of fumbling with x and y
 * variables, all x and y variables are stored in one class. Vector3.x,
 * Vector3.y.
 * 
 * @author Calclavia
 */

public class Vector2 implements Cloneable
{
	public double x;
	public double y;

	public Vector2()
	{
		this(0, 0);
	}

	public Vector2(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public Vector2(double x, double y)
	{
		this.x = x;
		this.y = y;
	}

	// Returns the values as an int
	public int intX()
	{
		return (int) Math.floor(this.x);
	}

	public int intY()
	{
		return (int) Math.floor(this.y);
	}

	/**
	 * Makes a new copy of this Vector. Prevents variable referencing problems.
	 */
	@Override
	public Vector2 clone()
	{
		return new Vector2(this.x, this.y);
	}

	public static boolean isPointInRegion(Vector2 point, Vector2 minPoint, Vector2 maxPoint)
	{
		return (point.x > minPoint.x && point.x < maxPoint.x) && (point.y > minPoint.y && point.y < maxPoint.y);
	}

	public static double distance(Vector2 par1, Vector2 par2)
	{
		double var2 = par1.x - par2.x;
		double var4 = par1.y - par2.y;
		return MathHelper.sqrt_double(var2 * var2 + var4 * var4);
	}

	public static double slope(Vector2 par1, Vector2 par2)
	{
		double var2 = par1.x - par2.x;
		double var4 = par1.y - par2.y;
		return var4 / var2;
	}

	public void add(Vector2 par1)
	{
		this.x += par1.x;
		this.y += par1.y;
	}

	public void add(double par1)
	{
		this.x += par1;
		this.y += par1;
	}

	public Vector2 round()
	{
		return new Vector2(Math.round(this.x), Math.round(this.y));
	}

	public Vector2 floor()
	{
		return new Vector2(Math.floor(this.x), Math.floor(this.y));
	}

	public String output()
	{
		return "Vector2: " + this.x + "," + this.y;
	}

	public void printVector()
	{
		System.out.println(output());
	}
}