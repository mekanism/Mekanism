package ic2.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * A bunch of Events to handle the power of the Mining Laser.
 */
@Cancelable
public class LaserEvent extends WorldEvent {
	// the Laser Entity
	public final Entity lasershot;

	// the following variables can be changed and the Laser will adjust to them

	// the Player firing the Laser. If the Laser gets "reflected" the Player could change.
	public EntityLivingBase owner;
	// Range of the Laser Shot. Determine the amount of broken blocks, as well, as each broken block will subtract ~1F from range.
	public float range, power;
	public int blockBreaks;
	// Determines whether the laser will explode upon hitting something
	public boolean explosive, smelt;

	public LaserEvent(World world, Entity lasershot, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt) {
		super(world);

		this.lasershot = lasershot;
		this.owner = owner;
		this.range = range;
		this.power = power;
		this.blockBreaks = blockBreaks;
		this.explosive = explosive;
		this.smelt = smelt;
	}

	/**
	 * Event when the Laser is getting shot by a Player.
	 *
	 * The Item is the Laser Gun which the "Player" has shot
	 */
	public static class LaserShootEvent extends LaserEvent {
		ItemStack laserItem;

		public LaserShootEvent(World world, Entity lasershot, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt, ItemStack laseritem) {
			super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);

			this.laserItem = laseritem;
		}
	}

	/**
	 * Event when the Laser is exploding for some Reason.
	 *
	 * The Laser will no longer exist after this Event is posted as it either Explodes or despawns after the Event is fired.
	 */
	public static class LaserExplodesEvent extends LaserEvent {
		// explosion strength, even that can be changed!
		public float explosionPower, explosionDropRate, explosionEntityDamage;

		public LaserExplodesEvent(World world, Entity lasershot, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt, float explosionpower1, float explosiondroprate1, float explosionentitydamage1) {
			super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);

			this.explosionPower = explosionpower1;
			this.explosionDropRate = explosiondroprate1;
			this.explosionEntityDamage = explosionentitydamage1;
		}
	}

	/**
	 * Event when the Laser is hitting a Block
	 * x, y and z are the Coords of the Block.
	 *
	 * Canceling this Event stops the Laser from attempting to break the Block, what is very useful for Glass.
	 * Use lasershot.setDead() to remove the Shot entirely.
	 */
	public static class LaserHitsBlockEvent extends LaserEvent {
		// targeted block, even that can be changed!
		public BlockPos pos;
		public EnumFacing side;
		// removeBlock determines if the Block will be removed. dropBlock determines if the Block should drop something.
		public boolean removeBlock, dropBlock;
		public float dropChance;

		public LaserHitsBlockEvent(World world, Entity lasershot, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive1, boolean smelt1, BlockPos pos, EnumFacing side, float dropChance, boolean removeBlock, boolean dropBlock) {
			super(world, lasershot, owner, range, power, blockBreaks, explosive1, smelt1);

			this.pos = pos;
			this.side = side;
			this.removeBlock = removeBlock;
			this.dropBlock = dropBlock;
			this.dropChance = dropChance;
		}
	}

	/**
	 * Event when the Laser is getting at a Living Entity
	 *
	 * Canceling this Event ignores the Entity
	 * Use lasershot.setDead() to remove the Shot entirely.
	 */
	public static class LaserHitsEntityEvent extends LaserEvent {
		// the Entity which the Laser has shot at, even the target can be changed!
		public Entity hitEntity;

		public LaserHitsEntityEvent(World world, Entity lasershot, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt, Entity hitentity) {
			super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);

			this.hitEntity = hitentity;
		}
	}
}
