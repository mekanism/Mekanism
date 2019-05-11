package mekanism.common.item;

import io.netty.buffer.ByteBuf;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.base.IItemNetwork;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemElectricBow extends ItemEnergized implements IItemNetwork {

    public ItemElectricBow() {
        super(120000);
        setFull3D();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        super.addInformation(itemstack, world, list, flag);

        list.add(EnumColor.PINK + LangUtils.localizeWithFormat("mekanism.tooltip.fireMode", LangUtils
              .transOnOff(getFireState(itemstack))));
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityLivingBase entityLiving,
          int itemUseCount) {
        if (entityLiving instanceof EntityPlayer && getEnergy(itemstack) > 0) {
            EntityPlayer player = (EntityPlayer) entityLiving;
            boolean flag = player.capabilities.isCreativeMode
                           || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, itemstack) > 0;
            ItemStack ammo = findAmmo(player);

            int maxItemUse = getMaxItemUseDuration(itemstack) - itemUseCount;
            maxItemUse = ForgeEventFactory
                  .onArrowLoose(itemstack, world, player, maxItemUse, !itemstack.isEmpty() || flag);
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

                boolean noConsume = flag && itemstack.getItem() instanceof ItemArrow;

                if (!world.isRemote) {
                    ItemArrow itemarrow = (ItemArrow) (ammo.getItem() instanceof ItemArrow ? ammo.getItem()
                                                                                           : Items.ARROW);
                    EntityArrow entityarrow = itemarrow.createArrow(world, itemstack, player);
                    entityarrow.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, f * 3.0F, 1.0F);

                    if (f == 1.0F) {
                        entityarrow.setIsCritical(true);
                    }

                    if (!player.capabilities.isCreativeMode) {
                        setEnergy(itemstack, getEnergy(itemstack) - (getFireState(itemstack) ? 1200 : 120));
                    }

                    if (noConsume) {
                        entityarrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
                    }

                    entityarrow.setFire(getFireState(itemstack) ? 100 : 0);

                    world.spawnEntity(entityarrow);
                }

                world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT,
                      SoundCategory.NEUTRAL, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

                if (!noConsume) {
                    ammo.shrink(1);

                    if (ammo.getCount() == 0) {
                        player.inventory.deleteStack(ammo);
                    }
                }

                player.addStat(StatList.getObjectUseStats(this));
            }
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack itemstack) {
        return 72000;
    }

    @Nonnull
    @Override
    public EnumAction getItemUseAction(ItemStack itemstack) {
        return EnumAction.BOW;
    }

    private ItemStack findAmmo(EntityPlayer player) {
        if (isArrow(player.getHeldItem(EnumHand.OFF_HAND))) {
            return player.getHeldItem(EnumHand.OFF_HAND);
        } else if (isArrow(player.getHeldItem(EnumHand.MAIN_HAND))) {
            return player.getHeldItem(EnumHand.MAIN_HAND);
        } else {
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                ItemStack itemstack = player.inventory.getStackInSlot(i);

                if (isArrow(itemstack)) {
                    return itemstack;
                }
            }

            return ItemStack.EMPTY;
        }
    }

    protected boolean isArrow(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemArrow;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand hand) {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);
        boolean flag = !findAmmo(playerIn).isEmpty();

        ActionResult<ItemStack> ret = ForgeEventFactory.onArrowNock(itemStackIn, worldIn, playerIn, hand, flag);
        if (ret != null) {
            return ret;
        }

        if (!playerIn.capabilities.isCreativeMode && !flag) {
            return new ActionResult<>(EnumActionResult.FAIL, itemStackIn);
        } else {
            playerIn.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
        }
    }

    public void setFireState(ItemStack itemstack, boolean state) {
        ItemDataUtils.setBoolean(itemstack, "fireState", state);
    }

    public boolean getFireState(ItemStack itemstack) {
        return ItemDataUtils.getBoolean(itemstack, "fireState");
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Override
    public void handlePacketData(ItemStack stack, ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            boolean state = dataStream.readBoolean();
            setFireState(stack, state);
        }
    }
}
