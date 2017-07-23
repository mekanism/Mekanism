package ic2.api.crops;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * CropCard example
 * @author estebes
 */
public class ExampleCropCard extends CropCard {
	@Override
	public String getId() {
		return "example";
	}

	@Override
	public String getOwner() {
		return "myaddon";
	}

	/**
	 * See {@link CropProperties} for more info.
	 */
	@Override
	public CropProperties getProperties() {
		return new CropProperties(1, 0, 4, 0, 0, 2);
	}

	@Override
	public int getMaxSize() {
		return 5;
	}

	@Override
	public ItemStack getGain(ICropTile crop) {
		return new ItemStack(Items.DIAMOND, 1);
	}

	@Override
	public List<ResourceLocation> getTexturesLocation() {
		List<ResourceLocation> ret = new ArrayList<ResourceLocation>(getMaxSize());

		for (int size = 1; size <= getMaxSize(); size++) {
			ret.add(new ResourceLocation("myaddon", "blocks/crop/" + getId() + "_" + size));
		}

		return ret;
	}
}
