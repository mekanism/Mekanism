package mekanism.common.item.gear;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.base.IItemNetwork;
import mekanism.common.item.ItemEnergized;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;

//TODO: UPDATE to more closely match logic of BowItem. Maybe even extend BowItem and then implement IItemEnergized instead
public class ItemElectricBow extends ItemEnergized implements IItemNetwork {

    public ItemElectricBow(Properties properties) {
        //TODO: Config max energy, damage, etc
        super(120_000, properties.setNoRepair());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(getFireState(stack))));
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, LivingEntity entityLiving, int itemUseCount) {
        if (entityLiving instanceof PlayerEntity && getEnergy(stack) > 0) {
            PlayerEntity player = (PlayerEntity) entityLiving;
            boolean flag = player.isCreative() || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
            ItemStack ammo = findAmmo(player);

            int maxItemUse = getUseDuration(stack) - itemUseCount;
            maxItemUse = ForgeEventFactory.onArrowLoose(stack, world, player, maxItemUse, !stack.isEmpty() || flag);
            if (maxItemUse < 0) {
                return;
            }

            if (flag || !ammo.isEmpty()) {
                if (ammo.isEmpty()) {
                    ammo = new ItemStack(Items.ARROW);
                }
                float f = maxItemUse / 20F;
                f = (f * f + f * 2.0F) / 3F;
                if (f < 0.1D) {
                    return;
                }
                if (f > 1.0F) {
                    f = 1.0F;
                }
                boolean noConsume = flag && stack.getItem() instanceof ArrowItem;
                if (!world.isRemote) {
                    ArrowItem itemarrow = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
                    AbstractArrowEntity entityarrow = itemarrow.createArrow(world, stack, player);
                    entityarrow.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, f * 3.0F, 1.0F);
                    if (f == 1.0F) {
                        entityarrow.setIsCritical(true);
                    }
                    if (!player.isCreative()) {
                        setEnergy(stack, getEnergy(stack) - (getFireState(stack) ? 1200 : 120));
                    }
                    if (noConsume) {
                        entityarrow.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                    }
                    entityarrow.setFire(getFireState(stack) ? 100 : 0);
                    world.addEntity(entityarrow);
                }

                world.playSound(null, player.func_226277_ct_(), player.func_226278_cu_(), player.func_226281_cx_(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL,
                      1.0F, 1.0F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

                if (!noConsume) {
                    ammo.shrink(1);
                    if (ammo.getCount() == 0) {
                        player.inventory.deleteStack(ammo);
                    }
                }
                player.addStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Nonnull
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    private ItemStack findAmmo(PlayerEntity player) {
        if (isArrow(player.getHeldItem(Hand.OFF_HAND))) {
            return player.getHeldItem(Hand.OFF_HAND);
        } else if (isArrow(player.getHeldItem(Hand.MAIN_HAND))) {
            return player.getHeldItem(Hand.MAIN_HAND);
        }
        for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (isArrow(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    protected boolean isArrow(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ArrowItem;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        boolean flag = !findAmmo(player).isEmpty();
        ActionResult<ItemStack> ret = ForgeEventFactory.onArrowNock(stack, world, player, hand, flag);
        if (ret != null) {
            return ret;
        }
        if (!player.isCreative() && !flag) {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
        player.setActiveHand(hand);
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    public void setFireState(ItemStack stack, boolean state) {
        ItemDataUtils.setBoolean(stack, "fireState", state);
    }

    public boolean getFireState(ItemStack stack) {
        return ItemDataUtils.getBoolean(stack, "fireState");
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Override
    public void handlePacketData(IWorld world, ItemStack stack, PacketBuffer dataStream) {
        if (!world.isRemote()) {
            boolean state = dataStream.readBoolean();
            setFireState(stack, state);
        }
    }
}