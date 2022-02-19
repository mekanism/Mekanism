package mekanism.common.registration.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityTypeDeferredRegister extends WrappedDeferredRegister<EntityType<?>> {

    private Map<EntityTypeRegistryObject<? extends LivingEntity>, Supplier<MutableAttribute>> livingEntityAttributes = new HashMap<>();

    public EntityTypeDeferredRegister(String modid) {
        super(modid, ForgeRegistries.ENTITIES);
    }

    public <ENTITY extends LivingEntity> EntityTypeRegistryObject<ENTITY> register(String name, EntityType.Builder<ENTITY> builder, Supplier<MutableAttribute> attributes) {
        EntityTypeRegistryObject<ENTITY> entityTypeRO = register(name, builder);
        livingEntityAttributes.put(entityTypeRO, attributes);
        return entityTypeRO;
    }

    public <ENTITY extends Entity> EntityTypeRegistryObject<ENTITY> register(String name, EntityType.Builder<ENTITY> builder) {
        return register(name, () -> builder.build(name), EntityTypeRegistryObject::new);
    }

    @Override
    public void register(IEventBus bus) {
        super.register(bus);
        bus.addListener(this::registerEntityAttributes);
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        if (livingEntityAttributes == null) {
            Mekanism.logger.error("GlobalEntityTypeAttributes have already been set. This should not happen.");
        } else {
            //Register our living entity attributes
            for (Map.Entry<EntityTypeRegistryObject<? extends LivingEntity>, Supplier<MutableAttribute>> entry : livingEntityAttributes.entrySet()) {
                event.put(entry.getKey().get(), entry.getValue().get().build());
            }
            //And set the map to null to allow it to be garbage collected
            livingEntityAttributes = null;
        }
    }
}