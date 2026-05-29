package mekanism.common.item.gear;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.EnergyUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public class ItemElectricBow extends BowItem implements IItemHUDProvider, ICustomCreativeTabContents, IAttachmentBasedModeItem<Boolean> {

    public ItemElectricBow(Properties properties) {
        super(properties.rarity(Rarity.RARE).setNoCombineRepair().stacksTo(1).component(MekanismDataComponents.ELECTRIC_BOW_MODE, false));
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        StorageUtils.addStoredEnergy(ItemAccess.forStack(stack), tooltipAdder, true);
        tooltipAdder.accept(MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(getMode(stack))));
    }

    @Override
    public boolean releaseUsing(@NotNull ItemStack bow, @NotNull Level world, @NotNull LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player && !player.isCreative()) {
            EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(ItemAccess.forStack(bow));
            if (energyHandler == null) {
                return false;
            }
            //TODO - 26.1: is there a risk that this is in a transactional context? Such as if an auto clicker is using energy,
            // and wraps the entire hitting the entity within their transaction?
            // Either way should we maybe just compare against the stored energy instead of having to open a transaction
            try (Transaction simulation = Transaction.openRoot()) {
                int energyNeeded = getMode(bow) ? MekanismConfig.gear.electricBowEnergyUsageFire.get() : MekanismConfig.gear.electricBowEnergyUsage.get();
                if (EnergyUtils.extractManual(energyHandler, energyNeeded, simulation) < energyNeeded) {
                    return false;
                }
            }
        }
        return super.releaseUsing(bow, world, entity, timeLeft);
    }

    @Override
    protected void shoot(@NotNull ServerLevel world, @NotNull LivingEntity entity, @NotNull InteractionHand hand, @NotNull ItemStack bow,
          @NotNull List<ItemStack> potentialAmmo, float velocity, float inaccuracy, boolean critical, @Nullable LivingEntity target) {
        super.shoot(world, entity, hand, bow, potentialAmmo, velocity, inaccuracy, critical, target);
        if (entity instanceof Player player && !player.isCreative() && !potentialAmmo.isEmpty()) {
            EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(ItemAccess.forStack(bow));
            if (energyHandler != null) {
                //TODO - 26.1: is there a risk that this is in a transactional context? Such as if an auto clicker is using energy,
                // and wraps the entire hitting the entity within their transaction?
                try (Transaction transaction = Transaction.openRoot()) {
                    //Use energy
                    int energyNeeded = getMode(bow) ? MekanismConfig.gear.electricBowEnergyUsageFire.get() : MekanismConfig.gear.electricBowEnergyUsage.get();
                    EnergyUtils.extractManual(energyHandler, energyNeeded, transaction);
                    transaction.commit();
                }
            }
        }
    }

    @Override
    public boolean isPrimaryItemFor(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        //Note: This stops application of it via enchanted books while in survival. We don't override isBookEnchantable as we don't care
        // if someone enchants it in creative and would rather not stop players from enchanting with books that have flame and power on them
        return !enchantment.is(Enchantments.FLAME) && super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public int getEnchantmentLevel(@NotNull ItemInstance stack, @NotNull Holder<Enchantment> enchantment) {
        if (stack instanceof ItemStack itemStack && itemStack.isEmpty()) {
            return 0;
        } else if (enchantment.is(Enchantments.FLAME) && getMode(stack)) {
            return Math.max(1, super.getEnchantmentLevel(stack, enchantment));
        }
        return super.getEnchantmentLevel(stack, enchantment);
    }

    @NotNull
    @Override
    public ItemEnchantments getAllEnchantments(@NotNull ItemStack stack, @NotNull RegistryLookup<Enchantment> lookup) {
        ItemEnchantments enchantments = super.getAllEnchantments(stack, lookup);
        if (getMode(stack)) {
            Optional<Reference<Enchantment>> enchantment = lookup.get(Enchantments.FLAME);
            if (enchantment.isPresent()) {
                Holder<Enchantment> flame = enchantment.get();
                if (enchantments.getLevel(flame) == 0) {
                    ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchantments);
                    mutable.set(flame, 1);
                    return mutable.toImmutable();
                }
            }
        }
        return enchantments;
    }

    @Override
    public DataComponentType<Boolean> getModeDataType() {
        return MekanismDataComponents.ELECTRIC_BOW_MODE.get();
    }

    @Override
    public Boolean getDefaultMode() {
        return Boolean.FALSE;
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        list.add(MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(getMode(stack))));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return StorageUtils.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    public void addItems(Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(StorageUtils.getFilledEnergyVariant(item));
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        if (Math.abs(shift) % 2 == 1) {
            //We are changing by an odd amount, so toggle the mode
            boolean newState = !getMode(stack);
            setMode(stack, player, newState);
            displayChange.sendMessage(player, newState, s -> MekanismLang.FIRE_MODE.translate(OnOff.of(s, true)));
        }
    }

    @NotNull
    @Override
    public Component getScrollTextComponent(@NotNull ItemStack stack) {
        return MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(getMode(stack), true));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        //Ignore NBT for energized items causing re-equip animations
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        //Ignore NBT for energized items causing block break reset
        return oldStack.getItem() != newStack.getItem();
    }
}