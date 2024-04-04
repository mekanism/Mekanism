package mekanism.common.registration.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.jetbrains.annotations.NotNull;

public class EntityTypeDeferredRegister extends MekanismDeferredRegister<EntityType<?>> {

    private Map<Supplier<? extends EntityType<? extends LivingEntity>>, Supplier<Builder>> livingEntityAttributes = new HashMap<>();

    public EntityTypeDeferredRegister(String modid) {
        super(Registries.ENTITY_TYPE, modid);
    }

    public <ENTITY extends LivingEntity> MekanismDeferredHolder<EntityType<?>, EntityType<ENTITY>> register(String name, EntityType.Builder<ENTITY> builder, Supplier<Builder> attributes) {
        MekanismDeferredHolder<EntityType<?>, EntityType<ENTITY>> entityTypeRO = register(name, builder);
        livingEntityAttributes.put(entityTypeRO, attributes);
        return entityTypeRO;
    }

    public <ENTITY extends Entity> MekanismDeferredHolder<EntityType<?>, EntityType<ENTITY>> register(String name, EntityType.Builder<ENTITY> builder) {
        return register(name, () -> builder.build(name));
    }

    @Override
    public void register(@NotNull IEventBus bus) {
        super.register(bus);
        bus.addListener(this::registerEntityAttributes);
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        if (livingEntityAttributes == null) {
            Mekanism.logger.error("GlobalEntityTypeAttributes have already been set. This should not happen.");
        } else {
            //Register our living entity attributes
            for (Map.Entry<Supplier<? extends EntityType<? extends LivingEntity>>, Supplier<Builder>> entry : livingEntityAttributes.entrySet()) {
                event.put(entry.getKey().get(), entry.getValue().get().build());
            }
            //And set the map to null to allow it to be garbage collected
            livingEntityAttributes = null;
        }
    }
}