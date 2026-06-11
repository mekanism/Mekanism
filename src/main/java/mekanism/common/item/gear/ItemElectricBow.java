package mekanism.common.item.gear;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.proxy.AutomatedEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ItemElectricBow extends BowItem implements IItemHUDProvider, ICustomCreativeTabContents, IAttachmentBasedModeItem<Boolean> {

    public ItemElectricBow(Properties properties) {
        super(properties.rarity(Rarity.RARE).setNoCombineRepair().stacksTo(1).component(MekanismDataComponents.ELECTRIC_BOW_MODE, false));
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        StorageUtils.addStoredEnergy(ItemAccessUtils.sideEffectFreeAccess(stack), tooltipAdder, true);
        tooltipAdder.accept(MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(getMode(stack))));
    }

    @Override
    public boolean releaseUsing(ItemStack bow, Level world, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player) || player.isCreative()) {
            return super.releaseUsing(bow, world, entity, timeLeft);
        }
        EnergyHandler energyHandler = AutomatedEnergyHandler.manual(Capabilities.ENERGY.getCapability(ItemAccess.forStack(bow)));
        if (energyHandler == null) {
            return false;
        }
        //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
        try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
            int energyNeeded = getMode(bow) ? MekanismConfig.gear.electricBowEnergyUsageFire.get() : MekanismConfig.gear.electricBowEnergyUsage.get();
            if (energyHandler.extract(energyNeeded, transaction) == energyNeeded && super.releaseUsing(bow, world, entity, timeLeft)) {
                //If we could use the energy, and we actually had a projectile to fire
                // commit the transaction and return that we successfully released
                transaction.commit();
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        //Note: This stops application of it via enchanted books while in survival. We don't override isBookEnchantable as we don't care
        // if someone enchants it in creative and would rather not stop players from enchanting with books that have flame and power on them
        return !enchantment.is(Enchantments.FLAME) && super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public int getEnchantmentLevel(ItemInstance instance, Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.FLAME) && getMode(instance)) {
            return Math.max(1, super.getEnchantmentLevel(instance, enchantment));
        }
        return super.getEnchantmentLevel(instance, enchantment);
    }

    @Override
    public ItemEnchantments getAllEnchantments(ItemStack stack, RegistryLookup<Enchantment> lookup) {
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
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDStrings(List<Component> list, Player player, ITEM instance, EquipmentSlot slotType) {
        list.add(MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(getMode(instance))));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return StorageUtils.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    public void addItems(Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(ContainerType.ENERGY.getFilledVariant(item, null));
    }

    @Override
    public void changeMode(Player player, ItemAccess itemAccess, int shift, DisplayChange displayChange, TransactionContext transaction) {
        if (Math.abs(shift) % 2 == 1) {
            //We are changing by an odd amount, so toggle the mode
            boolean newState = !getMode(itemAccess);
            if (setMode(itemAccess, player, newState, transaction)) {
                displayChange.sendMessage(player, newState, s -> MekanismLang.FIRE_MODE.translate(OnOff.of(s, true)));
            }
        }
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> Component getScrollTextComponent(ITEM instance) {
        return MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(getMode(instance), true));
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