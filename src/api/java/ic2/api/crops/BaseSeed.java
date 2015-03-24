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
	 * @param statGrowth1 plant growth stat
	 * @param statGain1 plant gain stat
	 * @param statResistance1 plant resistance stat
	 * @param stackSize1 for internal usage only
	 */
	@SuppressWarnings("deprecation")
	public BaseSeed(CropCard crop, int size, int statGrowth, int statGain, int statResistance, int stackSize) {
		super();
		this.crop = crop;
		this.id = Crops.instance.getIdFor(crop);
		this.size = size;
		this.statGrowth = statGrowth;
		this.statGain = statGain;
		this.statResistance = statResistance;
		this.stackSize = stackSize;
	}

	/**
	 * @deprecated Use the CropCard version.
	 */
	@Deprecated
	public BaseSeed(int id, int size, int statGrowth, int statGain, int statResistance, int stackSize) {
		this(getCropFromId(id), size, statGrowth, statGain, statResistance, stackSize);
	}

	@SuppressWarnings("deprecation")
	private static CropCard getCropFromId(int id) {
		CropCard[] crops = Crops.instance.getCropList();

		if (id < 0 || id >= crops.length) return null;

		return crops[id];
	}


	/**
	 * Plant.
	 */
	public final CropCard crop;

	/**
	 * @deprecated IDs aren't used anymore.
	 */
	@Deprecated
	public int id;

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

	/**
	 * For internal usage only.
	 */
	public int stackSize;
}
