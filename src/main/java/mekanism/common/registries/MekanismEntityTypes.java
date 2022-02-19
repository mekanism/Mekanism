package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registration.impl.EntityTypeDeferredRegister;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;

public class MekanismEntityTypes {

    private MekanismEntityTypes() {
    }

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(Mekanism.MODID);

    public static final EntityTypeRegistryObject<EntityFlame> FLAME = ENTITY_TYPES.register("flame", EntityType.Builder.of(EntityFlame::new, EntityClassification.MISC).sized(0.5F, 0.5F).fireImmune());
    public static final EntityTypeRegistryObject<EntityRobit> ROBIT = ENTITY_TYPES.register("robit", EntityType.Builder.of(EntityRobit::new, EntityClassification.MISC).sized(0.6F, 0.65F).fireImmune().noSummon(), EntityRobit::getDefaultAttributes);
}
