package mekanism.common.component.predicate;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismRegistries;
import mekanism.api.upgrade.Upgrade;
import mekanism.common.component.UpgradeType;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.advancements.predicates.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;

public record UpgradeTypeComponentPredicate(ResourceKey<Upgrade> upgradeType) implements SingleComponentItemPredicate<UpgradeType> {

    public static final Codec<UpgradeTypeComponentPredicate> CODEC = ResourceKey.codec(MekanismRegistries.Keys.UPGRADES).xmap(UpgradeTypeComponentPredicate::new, UpgradeTypeComponentPredicate::upgradeType);

    @Override
    public DataComponentType<UpgradeType> componentType() {
        return MekanismDataComponents.UPGRADE_TYPE.get();
    }

    @Override
    public boolean matches(UpgradeType value) {
        return value.type().is(upgradeType);
    }
}