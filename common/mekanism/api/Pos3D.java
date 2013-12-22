package mekanism.api;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class Pos3D
{
	public double xPos;
	public double yPos;
	public double zPos;
	
	public Pos3D()
	{
		this(0, 0, 0);
	}
	
	public Pos3D(double x, double y, double z)
	{
		xPos = x;
		yPos = y;
		zPos = z;
	}
	
	public Pos3D(Entity entity)
	{
		this(entity.posX, entity.posY, entity.posZ);
	}
	
	public Pos3D diff(Pos3D pos)
	{
		return new Pos3D(xPos-pos.xPos, yPos-pos.yPos, zPos-pos.zPos);
	}
	
	public static Pos3D fromMotion(Entity entity)
	{
		return new Pos3D(entity.motionX, entity.motionY, entity.motionZ);
	}
	
	public Pos3D translate(double x, double y, double z)
	{
		xPos += x;
		yPos += y;
		zPos += z;
		
		return this;
	}
	
	public Pos3D translate(Pos3D pos)
	{
		return translate(pos.xPos, pos.yPos, pos.zPos);
	}
	
	public double distance(Pos3D pos)
	{
	    double subX = xPos - pos.xPos;
	    double subY = yPos - pos.yPos;
	    double subZ = zPos - pos.zPos;
	    return MathHelper.sqrt_double(subX * subX + subY * subY + subZ * subZ);
	}
	
	public Pos3D rotateYaw(double yaw)
	{
		double yawRadians = Math.toRadians(yaw);

		double x = xPos;
		double z = zPos;

		if(yaw != 0)
		{
			xPos = x * Math.cos(yawRadians) - z * Math.sin(yawRadians);
			zPos = x * Math.sin(yawRadians) + z * Math.cos(yawRadians);
		}
		
		return this;
	}
	
	public Pos3D scale(double x, double y, double z)
	{
		xPos *= x;
		yPos *= y;
		zPos *= z;
		
		return this;
	}
	
	public Pos3D scale(double scale)
	{
		return scale(scale, scale, scale);
	}
	
	@Override
	public Pos3D clone()
	{
		return new Pos3D(xPos, yPos, zPos);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Pos3D && 
				((Pos3D)obj).xPos == xPos && 
				((Pos3D)obj).yPos == yPos && 
				((Pos3D)obj).zPos == zPos;
	}
	
	@Override
	public int hashCode() 
	{
		int code = 1;
		code = 31 * code + new Double(xPos).hashCode();
		code = 31 * code + new Double(yPos).hashCode();
		code = 31 * code + new Double(zPos).hashCode();
		return code;
	}
}
