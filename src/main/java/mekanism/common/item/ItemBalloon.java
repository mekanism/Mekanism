package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Pos3D;
import mekanism.common.base.IMetaItem;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.util.LangUtils;
import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBalloon extends ItemMekanism implements IMetaItem {

    public ItemBalloon() {
        super();
        setHasSubtypes(true);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, new DispenserBehavior());
    }

    @Override
    public String getTexture(int meta) {
        return "Balloon";
    }

    @Override
    public int getVariants() {
        return EnumColor.DYES.length;
    }

    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        EnumColor dye = getColor(stack);
        return (int) (dye.getColor(0) * 255) << 16 | (int) (dye.getColor(1) * 255) << 8 | (int) (dye.getColor(2) * 255);
    }

    public EnumColor getColor(ItemStack stack) {
        return EnumColor.DYES[stack.getItemDamage()];
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tabs, @Nonnull NonNullList<ItemStack> list) {
        if (!isInCreativeTab(tabs)) {
            return;
        }
        for (int i = 0; i < EnumColor.DYES.length; i++) {
            EnumColor color = EnumColor.DYES[i];
            if (color != null) {
                ItemStack stack = new ItemStack(this);
                stack.setItemDamage(i);
                list.add(stack);
            }
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entityplayer, @Nonnull EnumHand hand) {
        ItemStack itemstack = entityplayer.getHeldItem(hand);
        if (!world.isRemote) {
            Pos3D pos = new Pos3D(hand == EnumHand.MAIN_HAND ? -0.4 : 0.4, 0, 0.3).rotateYaw(entityplayer.renderYawOffset).translate(new Pos3D(entityplayer));
            world.spawnEntity(new EntityBalloon(world, pos.x - 0.5, pos.y - 0.25, pos.z - 0.5, getColor(itemstack)));
        }
        if (!entityplayer.capabilities.isCreativeMode) {
            itemstack.shrink(1);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        EnumColor color = getColor(stack);
        String dyeName = color.getDyedName();
        if (I18n.canTranslate(getTranslationKey(stack) + "." + color.dyeName)) {
            return LangUtils.localize(getTranslationKey(stack) + "." + color.dyeName);
        }
        if (getColor(stack) == EnumColor.BLACK) {
            dyeName = EnumColor.DARK_GREY + color.getDyeName();
        }
        return dyeName + " " + LangUtils.localize("tooltip.balloon");
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            AxisAlignedBB bound = new AxisAlignedBB(pos, pos.add(1, 3, 1));
            List<EntityBalloon> balloonsNear = player.world.getEntitiesWithinAABB(EntityBalloon.class, bound);
            if (balloonsNear.size() > 0) {
                return EnumActionResult.FAIL;
            }
            if (world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
                pos = pos.down();
            }
            if (!world.isSideSolid(pos, EnumFacing.UP)) {
                return EnumActionResult.FAIL;
            }
            if (canReplace(world, pos.up()) && canReplace(world, pos.up(2))) {
                world.setBlockToAir(pos.up());
                world.setBlockToAir(pos.up(2));
                if (!world.isRemote) {
                    world.spawnEntity(new EntityBalloon(world, new Coord4D(pos, world), getColor(stack)));
                    stack.shrink(1);
                }
                return EnumActionResult.SUCCESS;
            }
            return EnumActionResult.FAIL;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
        if (player.isSneaking()) {
            if (!player.world.isRemote) {
                AxisAlignedBB bound = new AxisAlignedBB(entity.posX - 0.2, entity.posY - 0.5, entity.posZ - 0.2, entity.posX + 0.2,
                      entity.posY + entity.height + 4, entity.posZ + 0.2);
                List<EntityBalloon> balloonsNear = player.world.getEntitiesWithinAABB(EntityBalloon.class, bound);
                for (EntityBalloon balloon : balloonsNear) {
                    if (balloon.latchedEntity == entity) {
                        return true;
                    }
                }
                player.world.spawnEntity(new EntityBalloon(entity, getColor(stack)));
                stack.shrink(1);
            }
            return true;
        }
        return false;
    }

    private boolean canReplace(World world, BlockPos pos) {
        return world.isAirBlock(pos) || world.getBlockState(pos).getBlock().isReplaceable(world, pos);
    }

    public class DispenserBehavior extends BehaviorDefaultDispenseItem {

        @Nonnull
        @Override
        public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            Coord4D coord = new Coord4D(source.getX(), source.getY(), source.getZ(), source.getWorld().provider.getDimension());
            EnumFacing side = source.getBlockState().getValue(BlockDispenser.FACING);

            List<EntityLivingBase> entities = source.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, coord.offset(side).getBoundingBox());
            boolean latched = false;

            for (EntityLivingBase entity : entities) {
                AxisAlignedBB bound = new AxisAlignedBB(entity.posX - 0.2, entity.posY - 0.5, entity.posZ - 0.2, entity.posX + 0.2,
                      entity.posY + entity.height + 4, entity.posZ + 0.2);
                List<EntityBalloon> balloonsNear = source.getWorld().getEntitiesWithinAABB(EntityBalloon.class, bound);
                boolean hasBalloon = false;
                for (EntityBalloon balloon : balloonsNear) {
                    if (balloon.latchedEntity == entity) {
                        hasBalloon = true;
                    }
                }
                if (!hasBalloon) {
                    source.getWorld().spawnEntity(new EntityBalloon(entity, getColor(stack)));
                    latched = true;
                }
            }
            if (!latched) {
                Pos3D pos = new Pos3D(coord).translate(0, -0.5, 0);
                switch (side) {
                    case DOWN:
                        pos = pos.translate(0, -2.5, 0);
                        break;
                    case UP:
                        pos = pos.translate(0, 0, 0);
                        break;
                    case NORTH:
                        pos = pos.translate(0, -1, -0.5);
                        break;
                    case SOUTH:
                        pos = pos.translate(0, -1, 0.5);
                        break;
                    case WEST:
                        pos = pos.translate(-0.5, -1, 0);
                        break;
                    case EAST:
                        pos = pos.translate(0.5, -1, 0);
                        break;
                    default:
                        break;
                }
                if (!source.getWorld().isRemote) {
                    source.getWorld().spawnEntity(new EntityBalloon(source.getWorld(), pos.x, pos.y, pos.z, getColor(stack)));
                }
            }
            stack.shrink(1);
            return stack;
        }
    }
}