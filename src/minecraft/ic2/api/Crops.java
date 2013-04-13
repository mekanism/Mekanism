package ic2.api;

import java.util.HashMap;

import net.minecraft.world.biome.BiomeGenBase;

/**
 * General management of the crop system. All crop management methods will be moved here in the 2.00 update.
 */
public class Crops {
	private static final HashMap<BiomeGenBase,Integer> humidityBiomeBonus = new HashMap<BiomeGenBase,Integer>();
	private static final HashMap<BiomeGenBase,Integer> nutrientBiomeBonus = new HashMap<BiomeGenBase,Integer>();
	
	/**
	 * Add a crop humidity and nutrient biome bonus.
	 * 
	 * 0 indicates no bonus and negative values indicate a penalty.
	 * 
	 * @param biome Biome to apply the bonus in
	 * @param humidityBonus Humidity stat bonus
	 * @param nutrientsBonus Nutrient stat bonus
	 */
	public static void addBiomeBonus(BiomeGenBase biome, int humidityBonus, int nutrientsBonus) {
		humidityBiomeBonus.put(biome, humidityBonus);
		nutrientBiomeBonus.put(biome, nutrientsBonus);
	}
	
	/**
	 * Get the humidity bonus for a biome. 
	 * 
	 * @param biome biome to check
	 * @return Humidity bonus or 0 if none
	 */
	public static int getHumidityBiomeBonus(BiomeGenBase biome) {
		return humidityBiomeBonus.containsKey(biome) ? humidityBiomeBonus.get(biome) : 0;
	}
	
	/**
	 * Get the nutrient bonus for a biome. 
	 * 
	 * @param biome biome to check
	 * @return Nutrient bonus or 0 if none
	 */
	public static int getNutrientBiomeBonus(BiomeGenBase biome) {
		return nutrientBiomeBonus.containsKey(biome) ? nutrientBiomeBonus.get(biome) : 0;
	}
}
