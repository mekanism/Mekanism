package mekanism.common.item.gear;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemArmoredFreeRunners extends ItemFreeRunners implements IAttributeRefresher {

    private static final ArmoredFreeRunnerMaterial ARMORED_FREE_RUNNER_MATERIAL = new ArmoredFreeRunnerMaterial();

    private final AttributeCache attributeCache;

    public ItemArmoredFreeRunners(Properties properties) {
        super(ARMORED_FREE_RUNNER_MATERIAL, properties);
        this.attributeCache = new AttributeCache(this, MekanismConfig.gear.armoredFreeRunnerArmor, MekanismConfig.gear.armoredFreeRunnerToughness,
              MekanismConfig.gear.armoredFreeRunnerKnockbackResistance);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.armoredFreeRunners());
    }

    @Override
    public int getDefense() {
        return getMaterial().getDefenseForSlot(getSlot());
    }

    @Override
    public float getToughness() {
        return getMaterial().getToughness();
    }

    @NotNull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
        return slot == getSlot() ? attributeCache.get() : ImmutableMultimap.of();
    }

    @Override
    public void addToBuilder(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        UUID modifier = ARMOR_MODIFIER_UUID_PER_SLOT[getSlot().getIndex()];
        builder.put(Attributes.ARMOR, new AttributeModifier(modifier, "Armor modifier", getDefense(), Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifier, "Armor toughness", getToughness(), Operation.ADDITION));
        builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(modifier, "Armor knockback resistance", getMaterial().getKnockbackResistance(),
              Operation.ADDITION));
    }

    @NothingNullByDefault
    private static class ArmoredFreeRunnerMaterial extends FreeRunnerMaterial {

        @Override
        public int getDefenseForSlot(EquipmentSlot slotType) {
            return slotType == EquipmentSlot.FEET ? MekanismConfig.gear.armoredFreeRunnerArmor.getOrDefault() : 0;
        }

        @Override
        public String getName() {
            return Mekanism.MODID + ":free_runners_armored";
        }

        @Override
        public float getToughness() {
            return MekanismConfig.gear.armoredFreeRunnerToughness.getOrDefault();
        }

        @Override
        public float getKnockbackResistance() {
            return MekanismConfig.gear.armoredFreeRunnerKnockbackResistance.getOrDefault();
        }
    }
}
