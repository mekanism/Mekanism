package mekanism.additions.common.registries;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.item.ItemBalloon;
import mekanism.additions.common.item.ItemWalkieTalkie;
import mekanism.api.text.EnumColorCollection;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.SpawnEggItem;

public class AdditionsItems {

    private AdditionsItems() {
    }

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekanismAdditions.MODID);

    public static final Map<BabyType, ItemRegistryObject<SpawnEggItem>> BABY_SPAWN_EGGS = Collections.unmodifiableMap(Util.make(new EnumMap<>(BabyType.class), spawnEggs -> {
        for (Map.Entry<BabyType, MekanismDeferredHolder<EntityType<?>, ? extends EntityType<? extends Monster>>> entry : AdditionsEntityTypes.BABIES.entrySet()) {
            spawnEggs.put(entry.getKey(), ITEMS.registerSpawnEgg(entry.getValue()));
        }
    }));

    public static final ItemRegistryObject<ItemWalkieTalkie> WALKIE_TALKIE = ITEMS.registerItem("walkie_talkie", ItemWalkieTalkie::new);

    public static final EnumColorCollection<ItemRegistryObject<ItemBalloon>> BALLOONS = EnumColorCollection.VALUES
          .map(color -> ITEMS.registerItem(color.getRegistryPrefix() + "_balloon", properties -> new ItemBalloon(properties, color)));
}