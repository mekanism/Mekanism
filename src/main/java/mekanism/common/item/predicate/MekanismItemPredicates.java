package mekanism.common.item.predicate;

import com.mojang.serialization.Codec;
import mekanism.common.Mekanism;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries.Keys;
import net.neoforged.neoforge.registries.RegistryObject;

public class MekanismItemPredicates {
    public static final DeferredRegister<Codec<? extends ICustomItemPredicate>> PREDICATES = DeferredRegister.create(Keys.ITEM_PREDICATE_SERIALIZERS, Mekanism.MODID);

    public static final RegistryObject<Codec<FullCanteenItemPredicate>> FULL_CANTEEN = PREDICATES.register("full_canteen", ()->Codec.unit(FullCanteenItemPredicate.INSTANCE));

    public static final RegistryObject<Codec<MaxedModuleContainerItemPredicate<?>>> MAXED_MODULE_CONTAINER_ITEM = PREDICATES.register("maxed_module_container", MaxedModuleContainerItemPredicate::makeCodec);
}
