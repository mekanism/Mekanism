package micdoodle8.mods.galacticraft.api.entity;

/**
 * Implement into entities that are living, but can breath without oxygen
 */
public interface IEntityBreathable
{
	/**
	 * Whether or not this entity can currently breathe without oxygen in it's
	 * vicinity
	 */
	public boolean canBreath();
}
