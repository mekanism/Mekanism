package universalelectricity.prefab.potion;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class CustomPotionEffect extends PotionEffect
{
	public CustomPotionEffect(int potionID, int duration, int amplifier)
	{
		super(potionID, duration, amplifier);
	}

	public CustomPotionEffect(Potion potion, int duration, int amplifier)
	{
		this(potion.getId(), duration, amplifier);
	}

	/**
	 * Creates a potion effect with custom curable items.
	 * 
	 * @param curativeItems - ItemStacks that can cure this potion effect
	 */
	public CustomPotionEffect(int potionID, int duration, int amplifier, List<ItemStack> curativeItems)
	{
		super(potionID, duration, amplifier);

		if (curativeItems == null)
		{
			this.setCurativeItems(new ArrayList<ItemStack>());
		}
		else
		{
			this.setCurativeItems(curativeItems);
		}
	}
}
