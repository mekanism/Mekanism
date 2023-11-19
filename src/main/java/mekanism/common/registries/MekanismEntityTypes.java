package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.EntityTypeDeferredRegister;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class MekanismEntityTypes {

    private MekanismEntityTypes() {
    }

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(Mekanism.MODID);

    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityFlame>> FLAME = ENTITY_TYPES.register("flame", EntityType.Builder.of(EntityFlame::new, MobCategory.MISC).sized(0.5F, 0.5F).fireImmune());
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityRobit>> ROBIT = ENTITY_TYPES.register("robit", EntityType.Builder.of(EntityRobit::new, MobCategory.MISC).sized(0.6F, 0.65F).fireImmune().noSummon(), EntityRobit::getDefaultAttributes);
}
