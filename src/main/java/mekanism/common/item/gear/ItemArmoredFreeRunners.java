package mekanism.common.item.gear;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;

public class ItemArmoredFreeRunners extends ItemFreeRunners implements IAttributeRefresher {

    private static final ArmoredFreeRunnerMaterial ARMORED_FREE_RUNNER_MATERIAL = new ArmoredFreeRunnerMaterial();

    private final AttributeCache attributeCache;

    public ItemArmoredFreeRunners(Properties properties) {
        super(ARMORED_FREE_RUNNER_MATERIAL, properties);
        this.attributeCache = new AttributeCache(this, MekanismConfig.gear.armoredFreeRunnerArmor, MekanismConfig.gear.armoredFreeRunnerToughness,
              MekanismConfig.gear.armoredFreeRunnerKnockbackResistance);
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
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

    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlot slot, @Nonnull ItemStack stack) {
        return slot == getSlot() ? attributeCache.getAttributes() : ImmutableMultimap.of();
    }

    @Override
    public void addToBuilder(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        UUID modifier = ARMOR_MODIFIER_UUID_PER_SLOT[getSlot().getIndex()];
        builder.put(Attributes.ARMOR, new AttributeModifier(modifier, "Armor modifier", getDefense(), Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifier, "Armor toughness", getToughness(), Operation.ADDITION));
        builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(modifier, "Armor knockback resistance", getMaterial().getKnockbackResistance(),
              Operation.ADDITION));
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    private static class ArmoredFreeRunnerMaterial extends FreeRunnerMaterial {
        @Override
        public int getDefenseForSlot(EquipmentSlot slotType) {
            return slotType == EquipmentSlot.FEET ? MekanismConfig.gear.armoredFreeRunnerArmor.get() : 0;
        }

        @Override
        public String getName() {
            return Mekanism.MODID + ":free_runners_armored";
        }

        @Override
        public float getToughness() {
            return MekanismConfig.gear.armoredFreeRunnerToughness.get();
        }

        @Override
        public float getKnockbackResistance() {
            return MekanismConfig.gear.armoredFreeRunnerKnockbackResistance.get();
        }
    }
}
