package ic2.api.crops;

import java.util.Collection;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.BiomeDictionary.Type;

/**
 * General management of the crop system.
 */
public abstract class Crops {
	public static Crops instance;

	public static CropCard weed; // weed has special properties, thus it's exposed here

	/**
	 * Adds a crop nutrient biome bonus.
	 *
	 * +10/-10  0 indicates no bonus and negative values indicate a penalty.
	 *
	 * @param type Forge biome type to apply the bonus in
	 * @param nutrientsBonus Nutrient stat bonus
	 */
	public abstract void addBiomenutrientsBonus(Type type, int nutrientsBonus);

	/**
	 * Adds a crop humidity biome bonus.
	 *
	 * +10/-10 0 indicates no bonus and negative values indicate a penalty.
	 *
	 * @param type Forge biome type to apply the bonus in
	 * @param humidityBonus Humidity stat bonus
	 */
	public abstract void addBiomehumidityBonus(Type type, int humidityBonus);


	/**
	 * Gets the humidity bonus for a biome.
	 *
	 * @param biome Biome to check
	 * @return Humidity bonus or 0 if none
	 */
	public abstract int getHumidityBiomeBonus(BiomeGenBase biome);

	/**
	 * Gets the nutrient bonus for a biome.
	 *
	 * @param biome Biome to check
	 * @return Nutrient bonus or 0 if none
	 */
	public abstract int getNutrientBiomeBonus(BiomeGenBase biome);

	/**
	 * Get the crop card for the specified owner and name.
	 *
	 * @param owner CropCard owner mod id.
	 * @param name CropCard name.
	 * @return Matching CropCard.
	 */
	public abstract CropCard getCropCard(String owner, String name);

	/**
	 * Get the crop card for the specified seed item stack.
	 *
	 * @param stack ItemStack containing seeds for the crop.
	 * @return Matching CropCard.
	 */
	public abstract CropCard getCropCard(ItemStack stack);

	/**
	 * Returns a list of all crops.
	 *
	 * @return All registered crops.
	 */
	public abstract Collection<CropCard> getCrops();

	/**
	 * Returns the list of registered crops.
	 *
	 * @return Registered crops by ID
	 */
	@Deprecated
	public abstract CropCard[] getCropList();

	/**
	 * Register a plant.
	 *
	 * @param crop Plant to register.
	 * @return Autoassigned id for legacy compatibility, TODO: change to void.
	 */
	public abstract short registerCrop(CropCard crop);

	/**
	 * Register a plant and provide a legacy id for migration.
	 *
	 * @param crop Plant to register.
	 * @param legacyId ID previously used for this crop.
	 * @return true, TODO: change to void.
	 */
	public abstract boolean registerCrop(CropCard crop, int legacyId);

	/**
	 * @deprecated use the CropCard version.
	 */
	@Deprecated
	public abstract boolean registerBaseSeed(ItemStack stack, int id, int size, int growth, int gain, int resistance);

	/**
	 * Registers a base seed, an item used to plant a crop.
	 *
	 * @param stack item
	 * @param id plant ID
	 * @param size initial size
	 * @param growth initial growth stat
	 * @param gain initial gain stat
	 * @param resistance initial resistance stat
	 * @return True if successful
	 */
	public abstract boolean registerBaseSeed(ItemStack stack, CropCard crop, int size, int growth, int gain, int resistance);

	/**
	 * Finds a base seed from the given item.
	 *
	 * @return Base seed or null if none found
	 */
	public abstract BaseSeed getBaseSeed(ItemStack stack);

	/**
	 * Execute registerSprites for all registered crop cards.
	 *
	 * This method will get called by IC2, don't call it yourself.
	 */
	@SideOnly(Side.CLIENT)
	public abstract void startSpriteRegistration(IIconRegister iconRegister);

	/**
	 * Returns the ID for the given crop.
	 *
	 * @param crop Crop to look up
	 * @return ID, or -1 if not found
	 * @deprecated IDs aren't used anymore.
	 */
	@Deprecated
	public abstract int getIdFor(CropCard crop);
}
