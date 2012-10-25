package universalelectricity.implement;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.DamageSource;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class UEDamageSource extends DamageSource
{
	public static final List<UEDamageSource> damageSources = new ArrayList<UEDamageSource>();

	/**
	 * Use this damage source for all types of electrical attacks.
	 */
	public static final UEDamageSource electrocution = (UEDamageSource) new UEDamageSource("electrocution", "%1$s got electrocuted!").setDamageBypassesArmor();

	public String deathMessage;

	public UEDamageSource(String damageType)
	{
		super(damageType);
		damageSources.add(this);
	}

	public UEDamageSource(String damageType, String deathMessage)
	{
		this(damageType);
		this.setDeathMessage(deathMessage);
	}

	public UEDamageSource setDeathMessage(String deathMessage)
	{
		this.deathMessage = deathMessage;
		return this;
	}

	public DamageSource setDamageBypassesArmor()
	{
		return super.setDamageBypassesArmor();
	}

	public DamageSource setDamageAllowedInCreativeMode()
	{
		return super.setDamageAllowedInCreativeMode();
	}

	public DamageSource setFireDamage()
	{
		return super.setFireDamage();
	}

	public void registerDeathMessage()
	{
		LanguageRegistry.instance().addStringLocalization("death." + this.damageType, this.deathMessage);
	}
}
