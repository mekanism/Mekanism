package mekanism.common.item.predicate;

import com.mojang.serialization.Codec;
import mekanism.common.Mekanism;
import mekanism.common.registration.DeferredCodecHolder;
import mekanism.common.registration.DeferredCodecRegister;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MekanismItemPredicates {

    public static final DeferredCodecRegister<ICustomItemPredicate> PREDICATES = new DeferredCodecRegister<>(NeoForgeRegistries.Keys.ITEM_PREDICATE_SERIALIZERS, Mekanism.MODID);

    public static final DeferredCodecHolder<ICustomItemPredicate, FullCanteenItemPredicate> FULL_CANTEEN = PREDICATES.registerCodec("full_canteen", () -> Codec.unit(FullCanteenItemPredicate.INSTANCE));
    public static final DeferredCodecHolder<ICustomItemPredicate, MaxedModuleContainerItemPredicate<?>> MAXED_MODULE_CONTAINER_ITEM = PREDICATES.registerCodec("maxed_module_container", MaxedModuleContainerItemPredicate::makeCodec);
}
