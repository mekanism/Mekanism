package ic2.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class ExplosionEvent extends WorldEvent {
	public ExplosionEvent(World world, Entity entity,
			Vec3d pos,
			double power,
			EntityLivingBase igniter,
			int radiationRange, double rangeLimit) {
		super(world);

		this.entity = entity;
		this.pos = pos;;
		this.power = power;
		this.igniter = igniter;
		this.radiationRange = radiationRange;
		this.rangeLimit = rangeLimit;
	}

	/**
	 * Entity representing the explosive, may be null.
	 */
	public final Entity entity;
	public final Vec3d pos;
	public final double power;
	/**
	 * Entity causing the explosion, may be null.
	 */
	public final EntityLivingBase igniter;
	public final int radiationRange;
	public final double rangeLimit;
}
