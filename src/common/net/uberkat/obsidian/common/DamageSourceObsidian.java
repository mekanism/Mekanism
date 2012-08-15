package net.uberkat.obsidian.common;

import net.minecraft.src.*;

public class DamageSourceObsidian extends EntityDamageSourceIndirect
{
    private Entity damageSourceProjectile;
    private Entity damageSourceEntity;

    public DamageSourceObsidian(String s, Entity entity, Entity entity1)
    {
        super(s, entity, entity1);
        damageSourceProjectile = entity;
        damageSourceEntity = entity1;
    }

    public Entity getProjectile()
    {
        return damageSourceProjectile;
    }

    public Entity getEntity()
    {
        return damageSourceEntity;
    }

    public static DamageSource causeWeaponDamage(Entity entity, Entity entity1)
    {
        return (new DamageSourceObsidian("weapon", entity, entity1)).setProjectile();
    }
    
    public static DamageSource causeObsidianArrowDamage(EntityObsidianArrow arrow, Entity entity)
    {
    	return (new EntityDamageSourceIndirect("arrow", arrow, entity)).setProjectile();
    }
}
