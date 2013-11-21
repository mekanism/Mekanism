package mekanism.induction.client;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glShadeModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mekanism.induction.common.MekanismInduction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Electric shock Fxs.
 * 
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class FXElectricBolt extends EntityFX
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(MekanismInduction.DOMAIN, MekanismInduction.MODEL_TEXTURE_DIRECTORY + "fadedSphere.png");
	public static final ResourceLocation PARTICLE_RESOURCE = new ResourceLocation("textures/particle/particles.png");

	/** The width of the electrical bolt. */
	private float boltWidth;
	/** The maximum length of the bolt */
	public double boltLength;
	/** Electric Bolt's start and end positions; */
	private BoltPoint start;
	private BoltPoint end;
	/** An array of the segments of the bolt. */
	private List<BoltSegment> segments = new ArrayList<BoltSegment>();
	private final Map<Integer, Integer> parentIDMap = new HashMap<Integer, Integer>();
	/** Determines how complex the bolt is. */
	public float complexity;
	public int segmentCount;
	private int maxSplitID;
	private Random rand;

	public FXElectricBolt(World world, Vector3 startVec, Vector3 targetVec, boolean doSplits)
	{
		super(world, startVec.x, startVec.y, startVec.z);

		rand = new Random();
		start = new BoltPoint(startVec);
		end = new BoltPoint(targetVec);

		if (end.y == Double.POSITIVE_INFINITY)
		{
			end.y = Minecraft.getMinecraft().thePlayer.posY + 30;
		}

		/** By default, we do an electrical color */
		segmentCount = 1;
		particleMaxAge = (3 + rand.nextInt(3) - 1);
		complexity = 2f;
		boltWidth = 0.05f;
		boltLength = start.distance(end);
		setUp(doSplits);
	}

	public FXElectricBolt(World world, Vector3 startVec, Vector3 targetVec)
	{
		this(world, startVec, targetVec, true);
	}

	/**
	 * Calculate all required segments of the entire bolt.
	 */
	private void setUp(boolean doSplits)
	{
		segments.add(new BoltSegment(start, end));
		recalculate();

		if (doSplits)
		{
			double offsetRatio = boltLength * complexity;
			split(2, offsetRatio / 10, 0.7f, 0.1f, 20 / 2);
			split(2, offsetRatio / 15, 0.5f, 0.1f, 25 / 2);
			split(2, offsetRatio / 25, 0.5f, 0.1f, 28 / 2);
			split(2, offsetRatio / 38, 0.5f, 0.1f, 30 / 2);
			split(2, offsetRatio / 55, 0, 0, 0);
			split(2, offsetRatio / 70, 0, 0, 0);
			recalculate();

			Collections.sort(segments, new Comparator()
			{
				public int compare(BoltSegment bolt1, BoltSegment bolt2)
				{
					return Float.compare(bolt2.alpha, bolt1.alpha);
				}

				@Override
				public int compare(Object obj1, Object obj2)
				{
					return compare((BoltSegment) obj1, (BoltSegment) obj2);
				}
			});
		}
	}

	public FXElectricBolt setColor(float r, float g, float b)
	{
		particleRed = r + (rand.nextFloat() * 0.1f) - 0.1f;
		particleGreen = g + (rand.nextFloat() * 0.1f) - 0.1f;
		particleBlue = b + (rand.nextFloat() * 0.1f) - 0.1f;
		return this;
	}

	/**
	 * Slits a large segment into multiple smaller ones.
	 * 
	 * @param splitAmount - The amount of splits
	 * @param offset - The multiplier scale for the offset.
	 * @param splitChance - The chance of creating a split.
	 * @param splitLength - The length of each split.
	 * @param splitAngle - The angle of the split.
	 */
	public void split(int splitAmount, double offset, float splitChance, float splitLength, float splitAngle)
	{
		/** Temporarily store old segments in a new array */
		List<BoltSegment> oldSegments = segments;
		segments = new ArrayList();
		/** Previous segment */
		BoltSegment prev = null;

		for (BoltSegment segment : oldSegments)
		{
			prev = segment.prev;
			/** Length of each subsegment */
			Vector3 subSegment = segment.difference.clone().scale(1.0F / splitAmount);

			/**
			 * Creates an array of new bolt points. The first and last points of the bolts are the
			 * respected start and end points of the current segment.
			 */
			BoltPoint[] newPoints = new BoltPoint[splitAmount + 1];
			Vector3 startPoint = segment.start;
			newPoints[0] = segment.start;
			newPoints[splitAmount] = segment.end;

			/**
			 * Create bolt points.
			 */
			for (int i = 1; i < splitAmount; i++)
			{
				Vector3 newOffset = segment.difference.getPerpendicular().rotate(rand.nextFloat() * 360, segment.difference).scale((rand.nextFloat() - 0.5F) * offset);
				Vector3 basePoint = startPoint.clone().translate(subSegment.clone().scale(i));

				newPoints[i] = new BoltPoint(basePoint, newOffset);
			}

			for (int i = 0; i < splitAmount; i++)
			{
				BoltSegment next = new BoltSegment(newPoints[i], newPoints[(i + 1)], segment.alpha, segment.id * splitAmount + i, segment.splitID);
				next.prev = prev;

				if (prev != null)
				{
					prev.next = next;
				}

				if ((i != 0) && (rand.nextFloat() < splitChance))
				{
					Vector3 splitrot = next.difference.xCrossProduct().rotate(rand.nextFloat() * 360, next.difference);
					Vector3 diff = next.difference.clone().rotate((rand.nextFloat() * 0.66F + 0.33F) * splitAngle, splitrot).scale(splitLength);
					maxSplitID += 1;
					parentIDMap.put(maxSplitID, next.splitID);
					BoltSegment split = new BoltSegment(newPoints[i], new BoltPoint(newPoints[(i + 1)].base, newPoints[(i + 1)].offset.clone().translate(diff)), segment.alpha / 2f, next.id, maxSplitID);
					split.prev = prev;
					segments.add(split);
				}

				prev = next;
				segments.add(next);
			}

			if (segment.next != null)
			{
				segment.next.prev = prev;
			}
		}

		segmentCount *= splitAmount;

	}

	private void recalculate()
	{
		HashMap<Integer, Integer> lastActiveSegment = new HashMap<Integer, Integer>();

		Collections.sort(segments, new Comparator()
		{
			public int compare(BoltSegment o1, BoltSegment o2)
			{
				int comp = Integer.valueOf(o1.splitID).compareTo(Integer.valueOf(o2.splitID));

				if (comp == 0)
				{
					return Integer.valueOf(o1.id).compareTo(Integer.valueOf(o2.id));
				}

				return comp;
			}

			@Override
			public int compare(Object obj, Object obj1)
			{
				return compare((BoltSegment) obj, (BoltSegment) obj1);
			}
		});

		int lastSplitCalc = 0;
		int lastActiveSeg = 0;

		for (BoltSegment segment : segments)
		{
			if (segment != null)
			{
				if (segment.splitID > lastSplitCalc)
				{
					lastActiveSegment.put(lastSplitCalc, lastActiveSeg);
					lastSplitCalc = segment.splitID;
					
					if(lastActiveSegment.get(parentIDMap.get(segment.splitID)) != null)
					{
						lastActiveSeg = lastActiveSegment.get(parentIDMap.get(segment.splitID)).intValue();
					}
					else {
						lastActiveSeg = 0;
					}
				}

				lastActiveSeg = segment.id;
			}
		}

		lastActiveSegment.put(lastSplitCalc, lastActiveSeg);
		lastSplitCalc = 0;
		lastActiveSeg = lastActiveSegment.get(0).intValue();
		BoltSegment segment;

		for (Iterator<BoltSegment> iterator = segments.iterator(); iterator.hasNext(); segment.recalculate())
		{
			segment = iterator.next();

			if (lastSplitCalc != segment.splitID)
			{
				lastSplitCalc = segment.splitID;
				lastActiveSeg = lastActiveSegment.get(segment.splitID);
			}

			if (segment.id > lastActiveSeg)
			{
				iterator.remove();
			}
		}
	}

	@Override
	public void onUpdate()
	{
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if (particleAge++ >= particleMaxAge)
		{
			setDead();
		}
	}

	@Override
	public void renderParticle(Tessellator tessellator, float partialframe, float cosYaw, float cosPitch, float sinYaw, float sinSinPitch, float cosSinPitch)
	{
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;

		tessellator.draw();
		GL11.glPushMatrix();

		GL11.glDepthMask(false);
		GL11.glEnable(3042);

		glShadeModel(GL_SMOOTH);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
		/**
		 * Render the actual bolts.
		 */
		tessellator.startDrawingQuads();
		tessellator.setBrightness(15728880);
		Vector3 playerVector = new Vector3(sinYaw * -cosPitch, -cosSinPitch / cosYaw, cosYaw * cosPitch);

		int renderlength = (int) ((particleAge + partialframe + (int) (boltLength * 3.0F)) / (int) (boltLength * 3.0F) * segmentCount);

		for (BoltSegment segment : segments)
		{
			if (segment != null && segment.id <= renderlength)
			{
				double renderWidth = boltWidth * ((new Vector3(player).distance(segment.start) / 5f + 1f) * (1 + segment.alpha) * 0.5f);
				renderWidth = Math.min(boltWidth, Math.max(renderWidth, 0));

				if (segment.difference.getMagnitude() > 0 && segment.difference.getMagnitude() != Double.NaN && segment.difference.getMagnitude() != Double.POSITIVE_INFINITY && renderWidth > 0 && renderWidth != Double.NaN && renderWidth != Double.POSITIVE_INFINITY)
				{
					Vector3 diffPrev = playerVector.crossProduct(segment.prevDiff).scale(renderWidth / segment.sinPrev);
					Vector3 diffNext = playerVector.crossProduct(segment.nextDiff).scale(renderWidth / segment.sinNext);
					Vector3 startVec = segment.start;
					Vector3 endVec = segment.end;
					float rx1 = (float) (startVec.x - interpPosX);
					float ry1 = (float) (startVec.y - interpPosY);
					float rz1 = (float) (startVec.z - interpPosZ);
					float rx2 = (float) (endVec.x - interpPosX);
					float ry2 = (float) (endVec.y - interpPosY);
					float rz2 = (float) (endVec.z - interpPosZ);

					tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, (1.0F - (particleAge >= 0 ? ((float) particleAge / (float) particleMaxAge) : 0.0F) * 0.6f) * segment.alpha);
					tessellator.addVertexWithUV(rx2 - diffNext.x, ry2 - diffNext.y, rz2 - diffNext.z, 0.5D, 0.0D);
					tessellator.addVertexWithUV(rx1 - diffPrev.x, ry1 - diffPrev.y, rz1 - diffPrev.z, 0.5D, 0.0D);
					tessellator.addVertexWithUV(rx1 + diffPrev.x, ry1 + diffPrev.y, rz1 + diffPrev.z, 0.5D, 1.0D);
					tessellator.addVertexWithUV(rx2 + diffNext.x, ry2 + diffNext.y, rz2 + diffNext.z, 0.5D, 1.0D);

					/**
					 * Render the bolts balls.
					 */

					if (segment.next == null)
					{
						Vector3 roundEnd = segment.end.clone().translate(segment.difference.clone().normalize().scale(renderWidth));
						float rx3 = (float) (roundEnd.x - interpPosX);
						float ry3 = (float) (roundEnd.y - interpPosY);
						float rz3 = (float) (roundEnd.z - interpPosZ);
						tessellator.addVertexWithUV(rx3 - diffNext.x, ry3 - diffNext.y, rz3 - diffNext.z, 0.0D, 0.0D);
						tessellator.addVertexWithUV(rx2 - diffNext.x, ry2 - diffNext.y, rz2 - diffNext.z, 0.5D, 0.0D);
						tessellator.addVertexWithUV(rx2 + diffNext.x, ry2 + diffNext.y, rz2 + diffNext.z, 0.5D, 1.0D);
						tessellator.addVertexWithUV(rx3 + diffNext.x, ry3 + diffNext.y, rz3 + diffNext.z, 0.0D, 1.0D);
					}

					if (segment.prev == null)
					{
						Vector3 roundEnd = segment.start.clone().difference(segment.difference.clone().normalize().scale(renderWidth));
						float rx3 = (float) (roundEnd.x - interpPosX);
						float ry3 = (float) (roundEnd.y - interpPosY);
						float rz3 = (float) (roundEnd.z - interpPosZ);
						tessellator.addVertexWithUV(rx1 - diffPrev.x, ry1 - diffPrev.y, rz1 - diffPrev.z, 0.5D, 0.0D);
						tessellator.addVertexWithUV(rx3 - diffPrev.x, ry3 - diffPrev.y, rz3 - diffPrev.z, 0.0D, 0.0D);
						tessellator.addVertexWithUV(rx3 + diffPrev.x, ry3 + diffPrev.y, rz3 + diffPrev.z, 0.0D, 1.0D);
						tessellator.addVertexWithUV(rx1 + diffPrev.x, ry1 + diffPrev.y, rz1 + diffPrev.z, 0.5D, 1.0D);
					}
				}
			}
		}

		tessellator.draw();

		GL11.glDisable(3042);
		GL11.glDepthMask(true);
		GL11.glPopMatrix();

		FMLClientHandler.instance().getClient().renderEngine.bindTexture(PARTICLE_RESOURCE);

		tessellator.startDrawingQuads();
	}

	private class BoltPoint extends Vector3
	{
		public Vector3 base;
		public Vector3 offset;

		public BoltPoint(Vector3 b, Vector3 o)
		{
			super(b.clone().translate(o));
			base = b;
			offset = o;
		}

		public BoltPoint(Vector3 base)
		{
			this(base, new Vector3());
		}
	}

	private class BoltSegment
	{
		public BoltPoint start;
		public BoltPoint end;
		public BoltSegment prev;
		public BoltSegment next;
		public float alpha;
		public int id;
		public int splitID;

		/**
		 * All differences are cached.
		 */
		public Vector3 difference;
		public Vector3 prevDiff;
		public Vector3 nextDiff;
		public double sinPrev;
		public double sinNext;

		public BoltSegment(BoltPoint start, BoltPoint end)
		{
			this(start, end, 1, 0, 0);
		}

		public BoltSegment(BoltPoint s, BoltPoint e, float a, int i, int id)
		{
			start = s;
			end = e;
			alpha = a;
			id = i;
			splitID = id;
			difference = end.clone().difference(start);
		}

		public void recalculate()
		{
			if (prev != null)
			{
				Vector3 prevDiffNorm = prev.difference.clone().normalize();
				Vector3 diffNorm = difference.clone().normalize();
				prevDiff = diffNorm.clone().translate(prevDiffNorm).normalize();
				sinPrev = Math.sin(diffNorm.anglePreNorm(prevDiffNorm.clone().scale(-1)) / 2);
			}
			else
			{
				prevDiff = difference.clone().normalize();
				sinPrev = 1;
			}

			if (next != null)
			{
				Vector3 nextDiffNorm = next.difference.clone().normalize();
				Vector3 diffNorm = difference.clone().normalize();
				nextDiff = diffNorm.clone().translate(nextDiffNorm).normalize();
				sinNext = Math.sin(diffNorm.anglePreNorm(nextDiffNorm.clone().scale(-1)) / 2);
			}
			else
			{
				nextDiff = difference.clone().normalize();
				sinNext = 1;
			}
		}
	}
}