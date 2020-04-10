package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registration.impl.EntityTypeDeferredRegister;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;

//TODO: Check the different settings. For example should robit be immune to fire
public class MekanismEntityTypes {

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(Mekanism.MODID);

    public static final EntityTypeRegistryObject<EntityFlame> FLAME = ENTITY_TYPES.register("flame", EntityType.Builder.<EntityFlame>create(EntityFlame::new, EntityClassification.MISC).size(0.5F, 0.5F).immuneToFire());
    public static final EntityTypeRegistryObject<EntityRobit> ROBIT = ENTITY_TYPES.register("robit", EntityType.Builder.<EntityRobit>create(EntityRobit::new, EntityClassification.MISC).size(0.5F, 0.5F));
}