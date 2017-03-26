package ic2.api.crops;

/**
 * Base agriculture seed. Used to determine the state of a plant once it is planted from an item.
 */
public class BaseSeed {
	/**
	 * Create a BaseSeed object.
	 *
	 * @param crop plant
	 * @param size plant size
	 * @param statGrowth plant growth stat
	 * @param statGain plant gain stat
	 * @param statResistance plant resistance stat
	 * @param stackSize for internal usage only
	 */
	public BaseSeed(CropCard crop, int size, int statGrowth, int statGain, int statResistance) {
		this.crop = crop;
		this.size = size;
		this.statGrowth = statGrowth;
		this.statGain = statGain;
		this.statResistance = statResistance;
	}

	/**
	 * @deprecated use the other constructor instead
	 */
	@Deprecated
	public BaseSeed(CropCard crop, int size, int statGrowth, int statGain, int statResistance, int stackSize) {
		this(crop, size, statGrowth, statGain, statResistance);
	}

	/**
	 * Plant.
	 */
	public final CropCard crop;

	/**
	 * Plant size.
	 */
	public int size;

	/**
	 * Plant growth stat.
	 */
	public int statGrowth;

	/**
	 * Plant gain stat.
	 */
	public int statGain;

	/**
	 * Plant resistance stat.
	 */
	public int statResistance;
}
