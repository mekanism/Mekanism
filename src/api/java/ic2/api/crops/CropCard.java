package ic2.api.crops;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * Base agriculture crop.
 *
 * Any crop extending this can be registered using registerCrop to be added into the game.
 */
public abstract class CropCard {
	public CropCard() {
	}

	/**
	 * Plant name for identifying this crop within your mod.
	 *
	 * The name has to be unique within the mod and is used for saving.
	 * By default this name will be also used to determine displayKey() and registerSprites().
	 *
	 * @note changing name or owner will cause existing crops in users' worlds to disappear.
	 *
	 * @return Plant name
	 */
	public abstract String getName();

	/**
	 * Determine the mod id owning this crop.
	 *
	 * The owner serves as a name space. With every mod using a different owner, a mod only has to
	 * make sure it doesn't have conflicts with name() in itself.
	 * It's recommended to hard code this to your mod id as specified in the @Mod annotation.
	 * Do not use IC2's mod id here.
	 *
	 * @note changing name or owner will cause existing crops in users' worlds to disappear.
	 */
	public abstract String getOwner();

	/**
	 * Translation key for display to the player.
	 *
	 * It's highly recommended to specify a valid key from your language file here, e.g. add
	 * "yourmod.crop.yourCropName = Your crop's name" to the language file, override name() to
	 * return "yourCropName" and override displayName() to return "yourmod.crop."+name().
	 *
	 * @return Unlocalized name.
	 */
	public String getDisplayName() {
		return getName(); // return the raw name for backwards compatibility
	}

	/**
	 * Your name here, will be shown in "Discovered by:" when analyzing seeds.
	 *
	 * @return Your name
	 */
	public String getDiscoveredBy() {
		return "unknown";
	}

	/**
	 * Description of your plant. Keep it short, a few characters per line for up to two lines.
	 * Default is showing attributes of your plant, 2 per line.
	 *
	 * @param i line to get, starting from 0
	 * @return The line
	 */
	public String desc(int i) {
		String[] att = getAttributes();

		if (att == null || att.length == 0) return "";

		if (i == 0) {
			String s = att[0];
			if (att.length >= 2) {
				s+=", "+att[1];
				if (att.length >= 3) s+=",";
			}
			return s;
		}
		if (att.length < 3) return "";
		String s = att[2];
		if (att.length >= 4) s+=", "+att[3];
		return s;
	}

	/**
	 * Crop roots length. Maximum roots length should be 5.
	 * @param cropTile reference to ICropTile
	 * @return roots length use in isBlockBelow
	 */
	public int getRootsLength(ICropTile cropTile) {
		return 1;
	}

	/**
	 * Object containing the crop properties info - See ic2.api.crops.CropProperties for more info.
	 */
	public abstract CropProperties getProperties();

	/**
	 * Additional attributes of the plant, also influencing breeding.
	 * Plants sharing stats and attributes will tend to cross-breed more often.
	 *
	 * @return Attributes as an array of strings
	 */
	public String[] getAttributes() {
		return new String[] {};
	}

	/**
	 * Determine the max crop size.
	 *
	 * Currently only used for texture allocation.
	 */
	public abstract int getMaxSize();

	/**
	 * Amount of growth points needed to increase the plant's size.
	 * Default is 200 * tier.
	 */
	public int getGrowthDuration(ICropTile cropTile) {
		return getProperties().getTier() * 200;
	}

	/**
	 * Check whether the plant can grow further.
	 *
	 * Consider:
	 * - Humidity, nutrients and air quality
	 * - Current size
	 * - Light level
	 * - Special biomes or conditions, accessible through crop.worldObj
	 *
	 * This method will be called upon empty upgraded crops to check whether a neighboring plant can cross onto it! Don't check if the size is greater than 0 and if the ID is real.
	 *
	 * @param cropTile reference to ICropTile
	 * @return Whether the crop can grow
	 */
	public boolean canGrow(ICropTile cropTile) {
		return cropTile.getCurrentSize() < getMaxSize();
	}

	/**
	 * Calculate the influence for the plant to grow based on humidity, nutrients and air.
	 * Normal behavior is rating the three stats "normal", with each of them ranging from 0-30.
	 * Basic rule: Assume everything returns 10. All together must equal 30. Add the factors to your likings, for example (humidity*0.7)+(nutrients*0.9)+(air*1.4)
	 *
	 * Default is humidity + nutrients + air (no factors).
	 *
	 * @param crop reference to ICropTile
	 * @param humidity ground humidity, influenced by hydration
	 * @param nutrients nutrient quality in ground, based on fertilizers
	 * @param air air quality, influences by open gardens and less crops surrounding this one
	 * @return 0-30
	 */
	public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air) {
		return humidity + nutrients + air;
	}

	/**
	 * Used to determine whether the plant can crossbreed with another crop.
	 * Default is allow crossbreeding if the size is greater or equal than 3.
	 *
	 * @param crop crop to crossbreed with
	 */
	public boolean canCross(ICropTile crop) {
		return crop.getCurrentSize() >= 3;
	}


	/**
	 * Called when the plant is rightclicked by a player.
	 * Default action is harvesting.
	 *
	 * Only called Serverside.
	 *
	 * @param cropTile reference to ICropTile
	 * @param player player rightclicking the crop
	 * @return Whether the plant has changed
	 */
	public boolean onRightClick(ICropTile cropTile, EntityPlayer player) {
		return cropTile.performManualHarvest();
	}

	/**
	 * Use in Crop Harvester with insert Cropnalyzer to get best Output.
	 *
	 * @param cropTile reference to ICropTile
	 * @return need crop size for best output.
	 */

	public int getOptimalHarvestSize(ICropTile cropTile) {
		return getMaxSize();
	}

	/**
	 * Check whether the crop can be harvested.
	 *
	 * @param cropTile reference to ICropTile
	 * @return Whether the crop can be harvested in its current state.
	 */
	public boolean canBeHarvested(ICropTile cropTile) {
		return cropTile.getCurrentSize() == getMaxSize();
	}

	/**
	 * Base chance for dropping the plant's gains, specify values greater than 1 for multiple drops.
	 * Default is 0.95^tier.
	 *
	 * @return Chance to drop the gains
	 */
	public double dropGainChance() {
		return Math.pow(0.95, getProperties().getTier());
	}

	/**
	 * Item obtained from harvesting the plant.
	 *
	 * @param crop reference to ICropTile
	 * @return Item obtained
	 */
	public abstract ItemStack getGain(ICropTile crop);

	/**
	 * Get the size of the plant after harvesting.
	 * Default is 1.
	 *
	 * @param cropTile reference to ICropTile
	 * @return Plant size after harvesting
	 */
	public int getSizeAfterHarvest(ICropTile cropTile) {
		return 1;
	}


	/**
	 * Called when the plant is left clicked by a player.
	 * Default action is picking the plant.
	 *
	 * Only called server side.
	 *
	 * @param cropTile reference to ICropTile
	 * @param player player left clicked the crop
	 * @return Whether the plant has changed
	 */
	public boolean onLeftClick(ICropTile cropTile, EntityPlayer player) {
		return cropTile.pick();
	}

	/**
	 * Base chance for dropping seeds when the plant is picked.
	 * Default is 0.5*0.8^tier with a bigger chance for sizes greater than 2 and absolutely no chance for size 0.
	 *
	 * @param crop reference to ICropTile
	 * @return Chance to drop the seeds
	 */
	public float dropSeedChance(ICropTile crop) {
		if (crop.getCurrentSize() == 1) return 0;
		float base = 0.5F;
		if (crop.getCurrentSize() == 2) base/=2F;
		for (int i = 0; i < getProperties().getTier(); i++) {
			base*=0.8;
		}
		return base;
	}

	/**
	 * Obtain seeds dropped when the plant is picked.
	 * Multiple drops of the returned ItemStack can occur.
	 * Default action is generating a seed from this crop.
	 *
	 * @param crop reference to ICropTile
	 * @return Seeds
	 */
	public ItemStack getSeeds(ICropTile crop) {
		return crop.generateSeeds(crop.getCrop(), crop.getStatGrowth(), crop.getStatGain(), crop.getStatResistance(), crop.getScanLevel());
	}

	/**
	 * Called when a neighbor block to the crop has changed.
	 *
	 * @param crop reference to ICropTile
	 */
	public void onNeighbourChange(ICropTile crop) {
		//
	}

	/**
	 * Check if the crop emits a redstone signal.
	 *
	 * @return Whether the crop emits a redstone signal.
	 */
	public boolean isRedstoneSignalEmitter(ICropTile cropTile) {
		return false;
	}

	/***
	 * Get the emitted redstone signal strength.
	 *
	 * @return The redstone signal strength.
	 */
	public int getEmittedRedstoneSignal(ICropTile cropTile) {
		return 0;
	}

	/**
	 * Called when the crop is destroyed.
	 *
	 * @param crop reference to ICropTile
	 */
	public void onBlockDestroyed(ICropTile crop) {
		//
	}

	/**
	 * Get the light value emitted by the plant.
	 *
	 * @param crop reference to ICropTile
	 * @return Light value emitted
	 */
	public int getEmittedLight(ICropTile crop) {
		return 0;
	}

	/**
	 * Default is true if the entity is an EntityLiving in jumping or sprinting state.
	 *
	 * @param crop reference to ICropTile
	 * @param entity entity colliding
	 * @return Whether trampling calculation should happen, return false if the plant is no longer valid.
	 */
	public boolean onEntityCollision(ICropTile crop, Entity entity) {
		return (entity instanceof EntityLivingBase && entity.isSprinting());
	}


	/**
	 * Called every time the crop ticks.
	 * Should be called every 256 ticks or around 13 seconds.
	 *
	 * @param cropTile reference to ICropTile
	 */
	public void tick(ICropTile cropTile) {
		// nothing by default
	}

	/**
	 * Check whether this plant spreads weed to surrounding tiles.
	 * Default is true if the plant has a high growth stat (or is weeds) and size greater or equal than 2.
	 *
	 * @param cropTile reference to ICropTile
	 * @return Whether the plant spreads weed
	 */
	public boolean isWeed(ICropTile cropTile) {
		return cropTile.getCurrentSize() >= 2 &&
				(cropTile.getCrop() == Crops.weed || cropTile.getStatGrowth() >= 24);
	}

	/**
	 * Retrieve the crop world.
	 * @param cropTile reference to ICropTile.
	 * @return The crop world object.
	 */
	public World getWorld(ICropTile cropTile) {
		return cropTile.getWorld();
	}

	/**
	 * Retrieve the crop's unlocalized name.
	 * @return Unlocalized name
	 */
	public String getUnlocalizedName() {
		return "crop." + getName() + ".name";
	}

	public List<ResourceLocation> getModelLocation() {
		List<ResourceLocation> ret = new ArrayList<ResourceLocation>();
		for (int i = 1; i <= getMaxSize(); i++) {
			ret.add(new ResourceLocation(getOwner().toLowerCase(Locale.ENGLISH), "blocks/crop/"+getName()+"_"+i));
		}
		return ret;
	}
}
