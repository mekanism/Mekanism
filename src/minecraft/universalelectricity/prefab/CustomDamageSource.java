package universalelectricity.prefab;

import net.minecraft.util.DamageSource;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class CustomDamageSource extends DamageSource
{
	/**
	 * Use this damage source for all types of electrical attacks.
	 */
	public static final CustomDamageSource electrocution = ((CustomDamageSource) new CustomDamageSource("electrocution").setDamageBypassesArmor()).setDeathMessage("%1$s got electrocuted!");

	public CustomDamageSource(String damageType)
	{
		super(damageType);
	}

	public CustomDamageSource setDeathMessage(String deathMessage)
	{
		LanguageRegistry.instance().addStringLocalization("death.attack." + this.damageType, deathMessage);
		return this;
	}

	@Override
	public DamageSource setDamageBypassesArmor()
	{
		return super.setDamageBypassesArmor();
	}

	@Override
	public DamageSource setDamageAllowedInCreativeMode()
	{
		return super.setDamageAllowedInCreativeMode();
	}

	@Override
	public DamageSource setFireDamage()
	{
		return super.setFireDamage();
	}
}
