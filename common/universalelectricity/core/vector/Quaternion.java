package universalelectricity.core.vector;

/**
 * Quaternion class designed to be used for the rotation of objects.
 * 
 * Do not use in MC 1.6.4, subject to change!
 * 
 * @author DarkGuardsman, Calclavia
 */
public class Quaternion implements Cloneable
{
	public static final float TOLERANCE = 0.00001f;
	public double x, y, z, w;

	public Quaternion()
	{
		this(0, 0, 0, 1);
	}

	public Quaternion(Quaternion copy)
	{
		this(copy.x, copy.y, copy.z, copy.w);
	}

	public Quaternion(double x, double y, double z, double w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	/**
	 * Convert from Euler Angles. Basically we create 3 Quaternions, one for pitch, one for yaw, one
	 * for roll and multiply those together. the calculation below does the same, just shorter
	 */
	public Quaternion(float pitch, float yaw, float roll)
	{
		float p = (float) (pitch * (Math.PI / 180) / 2.0);
		float y = (float) (yaw * (Math.PI / 180) / 2.0);
		float r = (float) (roll * (Math.PI / 180) / 2.0);

		float sinp = (float) Math.sin(p);
		float siny = (float) Math.sin(y);
		float sinr = (float) Math.sin(r);
		float cosp = (float) Math.cos(p);
		float cosy = (float) Math.cos(y);
		float cosr = (float) Math.cos(r);

		this.x = sinr * cosp * cosy - cosr * sinp * siny;
		this.y = cosr * sinp * cosy + sinr * cosp * siny;
		this.z = cosr * cosp * siny - sinr * sinp * cosy;
		this.w = cosr * cosp * cosy + sinr * sinp * siny;

		this.normalize();
	}

	public Quaternion(Vector3 vector, double w)
	{
		this(vector.x, vector.y, vector.z, w);
	}

	public static Quaternion IDENTITY()
	{
		return new Quaternion();
	}

	public Quaternion set(Quaternion quaternion)
	{
		this.w = quaternion.w;
		this.x = quaternion.x;
		this.y = quaternion.y;
		this.z = quaternion.z;
		return this;
	}

	public Quaternion set(double x, double y, double z, double w)
	{
		return this.set(new Quaternion(x, y, z, w));
	}

	public Quaternion normalize()
	{
		double magnitude = this.magnitude();
		this.x /= magnitude;
		this.y /= magnitude;
		this.z /= magnitude;
		this.w /= magnitude;
		return this;
	}

	public double magnitude()
	{
		return Math.sqrt(this.magnitudeSquared());
	}

	public double magnitudeSquared()
	{
		return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
	}

	public Quaternion inverse()
	{
		double d = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
		return new Quaternion(this.x / d, -this.y / d, -this.z / d, -this.w / d);
	}

	/**
	 * Gets the conjugate of this Quaternion
	 */
	public Quaternion getConjugate()
	{
		return this.clone().conjugate();
	}

	public Quaternion conjugate()
	{
		this.y = -this.y;
		this.z = -this.z;
		this.w = -this.w;
		return this;
	}

	/**
	 * Let the current quaternion be "a". Multiplying the a with b applies the rotation a to b.
	 */
	public Quaternion getMultiply(Quaternion b)
	{
		return this.clone().multiply(b);
	}

	public Quaternion multiply(Quaternion b)
	{
		Quaternion a = this;
		double newX = a.x * b.x - a.y * b.y - a.z * b.z - a.w * b.w;
		double newY = a.x * b.y + a.y * b.x + a.z * b.w - a.w * b.z;
		double newZ = a.x * b.z - a.y * b.w + a.z * b.x + a.w * b.y;
		double newW = a.x * b.w + a.y * b.z - a.z * b.y + a.w * b.x;
		this.set(newX, newY, newZ, newW);
		return this;
	}

	public Quaternion divide(Quaternion b)
	{
		Quaternion a = this;
		return a.inverse().multiply(b);
	}

	/** Multi a vector against this in other words applying rotation */
	public Vector3 multi(Vector3 vec)
	{
		Vector3 vn = vec.clone();

		Quaternion vecQuat = new Quaternion(0, 0, 0, 1), resQuat;
		vecQuat.x = (float) vn.x;
		vecQuat.y = (float) vn.y;
		vecQuat.z = (float) vn.z;
		vecQuat.w = 0.0f;

		resQuat = vecQuat.multiply(this.getConjugate());
		resQuat = this.multiply(resQuat);

		return new Vector3(resQuat.x, resQuat.y, resQuat.z);
	}

	public static Quaternion fromAxis(Vector3 vector, double angle)
	{
		angle *= 0.5f;
		Vector3 vn = vector.clone().normalize();
		float sinAngle = (float) Math.sin(angle);
		return new Quaternion(vn.x * sinAngle, vn.y * sinAngle, vn.z * sinAngle, Math.cos(angle));
	}

	/*
	 * Convert to Matrix public Matrix4 getMatrix() { float x2 = (float) (x * x); float y2 = (float)
	 * (y * y); float z2 = (float) (z * z); float xy = (float) (x * y); float xz = (float) (x * z);
	 * float yz = (float) (y * z); float wx = (float) (w * x); float wy = (float) (w * y); float wz
	 * = (float) (w * z);
	 * 
	 * // This calculation would be a lot more complicated for non-unit length quaternions // Note:
	 * The constructor of Matrix4 expects the Matrix in column-major format like expected // by //
	 * OpenGL return new Matrix4(1.0f - 2.0f * (y2 + z2), 2.0f * (xy - wz), 2.0f * (xz + wy), 0.0f,
	 * 2.0f * (xy + wz), 1.0f - 2.0f * (x2 + z2), 2.0f * (yz - wx), 0.0f, 2.0f * (xz - wy), 2.0f *
	 * (yz + wx), 1.0f - 2.0f * (x2 + y2), 0.0f, 0.0f, 0.0f, 0.0f, 1.0f); }
	 */

	/**
	 * Convert to Axis/Angles
	 * 
	 * @param axis - The axis of rotation
	 * @param angle - The angle of rotation
	 */
	public void getAxisAngle(Vector3 axis, float angle)
	{
		float scale = (float) axis.getMagnitude();
		this.x = this.x / scale;
		this.y = this.y / scale;
		this.z = this.z / scale;
		angle = (float) (Math.acos(this.w) * 2.0f);
	}

	@Override
	public Quaternion clone()
	{
		return new Quaternion(this);
	}

	@Override
	public String toString()
	{
		return "Quaternion [" + x + ", " + y + ", " + z + ", " + w + "]";
	}
}
