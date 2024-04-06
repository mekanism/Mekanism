package mekanism.common.item.gear;

import java.util.List;
import java.util.Map;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;

public class ItemElectricBow extends BowItem implements IItemHUDProvider, ICustomCreativeTabContents, IAttachmentBasedModeItem<Boolean> {

    public ItemElectricBow(Properties properties) {
        super(properties.rarity(Rarity.RARE).setNoRepair().stacksTo(1));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        StorageUtils.addStoredEnergy(stack, tooltip, true);
        tooltip.add(MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(getMode(stack))));
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player) {
            //Vanilla diff - Get the energy container and validate we have enough energy, because if something went wrong, then we can exit early
            IEnergyContainer energyContainer = null;
            FloatingLong energyNeeded = FloatingLong.ZERO;
            if (!player.isCreative()) {
                energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                energyNeeded = getMode(stack) ? MekanismConfig.gear.electricBowEnergyUsageFire.get() : MekanismConfig.gear.electricBowEnergyUsage.get();
                if (energyContainer == null || energyContainer.extract(energyNeeded, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyNeeded)) {
                    return;
                }
            }
            boolean infinity = player.isCreative() || stack.getEnchantmentLevel(Enchantments.INFINITY_ARROWS) > 0;
            ItemStack ammo = player.getProjectile(stack);
            int charge = EventHooks.onArrowLoose(stack, world, player, getUseDuration(stack) - timeLeft, !ammo.isEmpty() || infinity);
            if (charge < 0) {
                return;
            }
            if (!ammo.isEmpty() || infinity) {
                float velocity = getPowerForTime(charge);
                if (velocity < 0.1) {
                    return;
                }
                if (ammo.isEmpty()) {
                    ammo = new ItemStack(Items.ARROW);
                }
                boolean noConsume = player.isCreative() || (ammo.getItem() instanceof ArrowItem arrow && arrow.isInfinite(ammo, stack, player));
                if (!world.isClientSide) {
                    ArrowItem arrowitem = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
                    AbstractArrow arrowEntity = arrowitem.createArrow(world, ammo, player);
                    arrowEntity = customArrow(arrowEntity, ammo);
                    arrowEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 3 * velocity, 1);
                    if (velocity == 1) {
                        arrowEntity.setCritArrow(true);
                    }
                    int power = stack.getEnchantmentLevel(Enchantments.POWER_ARROWS);
                    if (power > 0) {
                        arrowEntity.setBaseDamage(arrowEntity.getBaseDamage() + 0.5 * power + 0.5);
                    }
                    int punch = stack.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
                    if (punch > 0) {
                        arrowEntity.setKnockback(punch);
                    }
                    if (stack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0) {
                        arrowEntity.setSecondsOnFire(5 * SharedConstants.TICKS_PER_SECOND);
                    }
                    //Vanilla diff - Instead of damaging the item we remove energy from it
                    if (energyContainer != null) {
                        energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
                    }
                    if (noConsume || player.isCreative() && (ammo.getItem() == Items.SPECTRAL_ARROW || ammo.getItem() == Items.TIPPED_ARROW)) {
                        arrowEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    }
                    world.addFreshEntity(arrowEntity);
                }
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1,
                      1.0F / (world.random.nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
                if (!noConsume && !player.isCreative()) {
                    ammo.shrink(1);
                    if (ammo.isEmpty()) {
                        player.getInventory().removeItem(ammo);
                    }
                }
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        //Note: This stops application of it via enchanted books while in survival. We don't override isBookEnchantable as we don't care
        // if someone enchants it in creative and would rather not stop players from enchanting with books that have flame and power on them
        return enchantment != Enchantments.FLAMING_ARROWS && super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
        if (stack.isEmpty()) {
            return 0;
        } else if (enchantment == Enchantments.FLAMING_ARROWS && getMode(stack)) {
            return Math.max(1, super.getEnchantmentLevel(stack, enchantment));
        }
        return super.getEnchantmentLevel(stack, enchantment);
    }

    @Override
    public Map<Enchantment, Integer> getAllEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = super.getAllEnchantments(stack);
        if (getMode(stack)) {
            enchantments.merge(Enchantments.FLAMING_ARROWS, 1, Math::max);
        }
        return enchantments;
    }

    @Override
    public AttachmentType<Boolean> getModeAttachment() {
        return MekanismAttachmentTypes.ELECTRIC_BOW_MODE.get();
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        list.add(MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(getMode(stack))));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
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
    public void addItems(CreativeModeTab.Output tabOutput) {
        tabOutput.accept(StorageUtils.getFilledEnergyVariant(new ItemStack(this)));
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