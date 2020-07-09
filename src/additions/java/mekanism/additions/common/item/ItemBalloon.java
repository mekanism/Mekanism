package mekanism.additions.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.additions.common.entity.EntityBalloon;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemBalloon extends Item {

    private final EnumColor color;

    public ItemBalloon(EnumColor color) {
        super(ItemDeferredRegister.getMekBaseProperties());
        this.color = color;
        DispenserBlock.registerDispenseBehavior(this, new DispenserBehavior());
    }

    public EnumColor getColor() {
        return color;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, @Nonnull PlayerEntity player, @Nonnull Hand hand) {
        if (!world.isRemote) {
            Pos3D pos = new Pos3D(hand == Hand.MAIN_HAND ? -0.4 : 0.4, 0, 0.3).rotateYaw(player.renderYawOffset).translate(new Pos3D(player));
            world.addEntity(new EntityBalloon(world, pos.x - 0.5, pos.y - 1.25, pos.z - 0.5, color));
        }
        ItemStack stack = player.getHeldItem(hand);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBalloon) {
            return TextComponentUtil.build(((ItemBalloon) item).getColor(), super.getDisplayName(stack));
        }
        return super.getDisplayName(stack);
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        ItemStack stack = player.getHeldItem(context.getHand());
        if (player.isSneaking()) {
            BlockPos pos = context.getPos();
            AxisAlignedBB bound = new AxisAlignedBB(pos, pos.add(1, 3, 1));
            List<EntityBalloon> balloonsNear = player.world.getEntitiesWithinAABB(EntityBalloon.class, bound);
            if (!balloonsNear.isEmpty()) {
                return ActionResultType.FAIL;
            }
            World world = context.getWorld();
            if (MekanismUtils.isValidReplaceableBlock(world, pos)) {
                pos = pos.down();
            }
            if (!Block.hasEnoughSolidSide(world, pos, Direction.UP)) {
                return ActionResultType.FAIL;
            }
            if (MekanismUtils.isValidReplaceableBlock(world, pos.up()) && MekanismUtils.isValidReplaceableBlock(world, pos.up(2))) {
                world.removeBlock(pos.up(), false);
                world.removeBlock(pos.up(2), false);
                if (!world.isRemote) {
                    world.addEntity(new EntityBalloon(world, pos, color));
                    stack.shrink(1);
                }
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.FAIL;
        }
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public ActionResultType itemInteractionForEntity(@Nonnull ItemStack stack, PlayerEntity player, @Nonnull LivingEntity entity, @Nonnull Hand hand) {
        if (player.isSneaking()) {
            if (!player.world.isRemote) {
                AxisAlignedBB bound = new AxisAlignedBB(entity.getPosX() - 0.2, entity.getPosY() - 0.5, entity.getPosZ() - 0.2,
                      entity.getPosX() + 0.2, entity.getPosY() + entity.getSize(entity.getPose()).height + 4, entity.getPosZ() + 0.2);
                List<EntityBalloon> balloonsNear = player.world.getEntitiesWithinAABB(EntityBalloon.class, bound);
                for (EntityBalloon balloon : balloonsNear) {
                    if (balloon.latchedEntity == entity) {
                        return ActionResultType.SUCCESS;
                    }
                }
                player.world.addEntity(new EntityBalloon(entity, color));
                stack.shrink(1);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    public class DispenserBehavior extends DefaultDispenseItemBehavior {

        @Nonnull
        @Override
        public ItemStack dispenseStack(IBlockSource source, @Nonnull ItemStack stack) {
            Direction side = source.getBlockState().get(DispenserBlock.FACING);
            BlockPos sourcePos = source.getBlockPos();
            BlockPos offsetPos = sourcePos.offset(side);
            List<LivingEntity> entities = source.getWorld().getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(offsetPos, offsetPos.add(1, 1, 1)));
            boolean latched = false;

            for (LivingEntity entity : entities) {
                AxisAlignedBB bound = new AxisAlignedBB(entity.getPosX() - 0.2, entity.getPosY() - 0.5, entity.getPosZ() - 0.2,
                      entity.getPosX() + 0.2, entity.getPosY() + entity.getSize(entity.getPose()).height + 4, entity.getPosZ() + 0.2);
                List<EntityBalloon> balloonsNear = source.getWorld().getEntitiesWithinAABB(EntityBalloon.class, bound);
                boolean hasBalloon = false;
                for (EntityBalloon balloon : balloonsNear) {
                    if (balloon.latchedEntity == entity) {
                        hasBalloon = true;
                        break;
                    }
                }
                if (!hasBalloon) {
                    source.getWorld().addEntity(new EntityBalloon(entity, color));
                    latched = true;
                }
            }
            if (!latched) {
                Pos3D pos = Pos3D.create(sourcePos).translate(0, -0.5, 0);
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
                    source.getWorld().addEntity(new EntityBalloon(source.getWorld(), pos.x, pos.y, pos.z, color));
                }
            }
            stack.shrink(1);
            return stack;
        }
    }
}