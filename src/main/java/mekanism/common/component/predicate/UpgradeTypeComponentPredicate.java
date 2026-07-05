package mekanism.common.component.predicate;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismRegistries;
import mekanism.api.upgrade.IUpgradeHelper;
import mekanism.api.upgrade.Upgrade;
import net.minecraft.advancements.predicates.SingleComponentItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;

public record UpgradeTypeComponentPredicate(ResourceKey<Upgrade> upgradeType) implements SingleComponentItemPredicate<Holder<Upgrade>> {

    public static final Codec<UpgradeTypeComponentPredicate> CODEC = ResourceKey.codec(MekanismRegistries.Keys.UPGRADES).xmap(UpgradeTypeComponentPredicate::new, UpgradeTypeComponentPredicate::upgradeType);

    @Override
    public DataComponentType<Holder<Upgrade>> componentType() {
        return IUpgradeHelper.INSTANCE.dataComponent();
    }

    @Override
    public boolean matches(Holder<Upgrade> value) {
        return value.is(upgradeType);
    }
}