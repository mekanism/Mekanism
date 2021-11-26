package mekanism.additions.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.additions.common.entity.EntityBalloon;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.WorldUtils;
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
import net.minecraft.util.HandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemBalloon extends Item {

    private final EnumColor color;

    public ItemBalloon(EnumColor color) {
        super(ItemDeferredRegister.getMekBaseProperties());
        this.color = color;
        DispenserBlock.registerBehavior(this, new DispenserBehavior(this.color));
    }

    public EnumColor getColor() {
        return color;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> use(World world, @Nonnull PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            boolean rightHand = (player.getMainArm() == HandSide.RIGHT) == (hand == Hand.MAIN_HAND);
            Vector3d pos = new Pos3D(rightHand ? -0.4 : 0.4, 0, 0.3).yRot(player.yBodyRot).translate(new Pos3D(player));
            EntityBalloon balloon = EntityBalloon.create(world, pos.x - 0.5, pos.y - 1.25, pos.z - 0.5, color);
            if (balloon == null) {
                return new ActionResult<>(ActionResultType.FAIL, stack);
            }
            world.addFreshEntity(balloon);
        }
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Nonnull
    @Override
    public ITextComponent getName(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBalloon) {
            return TextComponentUtil.build(((ItemBalloon) item).getColor(), super.getName(stack));
        }
        return super.getName(stack);
    }

    @Nonnull
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        ItemStack stack = context.getItemInHand();
        if (player.isShiftKeyDown()) {
            BlockPos pos = context.getClickedPos();
            AxisAlignedBB bound = new AxisAlignedBB(pos, pos.offset(1, 3, 1));
            List<EntityBalloon> balloonsNear = player.level.getEntitiesOfClass(EntityBalloon.class, bound);
            if (!balloonsNear.isEmpty()) {
                return ActionResultType.FAIL;
            }
            World world = context.getLevel();
            if (WorldUtils.isValidReplaceableBlock(world, pos)) {
                pos = pos.below();
            }
            if (!Block.canSupportCenter(world, pos, Direction.UP)) {
                return ActionResultType.FAIL;
            }
            if (WorldUtils.isValidReplaceableBlock(world, pos.above()) && WorldUtils.isValidReplaceableBlock(world, pos.above(2))) {
                world.removeBlock(pos.above(), false);
                world.removeBlock(pos.above(2), false);
                if (!world.isClientSide) {
                    EntityBalloon balloon = EntityBalloon.create(world, pos, color);
                    if (balloon == null) {
                        return ActionResultType.FAIL;
                    }
                    world.addFreshEntity(balloon);
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
    public ActionResultType interactLivingEntity(@Nonnull ItemStack stack, PlayerEntity player, @Nonnull LivingEntity entity, @Nonnull Hand hand) {
        if (player.isShiftKeyDown()) {
            if (!player.level.isClientSide) {
                AxisAlignedBB bound = new AxisAlignedBB(entity.getX() - 0.2, entity.getY() - 0.5, entity.getZ() - 0.2,
                      entity.getX() + 0.2, entity.getY() + entity.getDimensions(entity.getPose()).height + 4, entity.getZ() + 0.2);
                List<EntityBalloon> balloonsNear = player.level.getEntitiesOfClass(EntityBalloon.class, bound);
                for (EntityBalloon balloon : balloonsNear) {
                    if (balloon.latchedEntity == entity) {
                        return ActionResultType.SUCCESS;
                    }
                }
                EntityBalloon balloon = EntityBalloon.create(entity, color);
                if (balloon == null) {
                    return ActionResultType.FAIL;
                }
                player.level.addFreshEntity(balloon);
                stack.shrink(1);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    private static class DispenserBehavior extends DefaultDispenseItemBehavior {

        private final EnumColor color;

        public DispenserBehavior(EnumColor color) {
            this.color = color;
        }

        @Nonnull
        @Override
        public ItemStack execute(IBlockSource source, @Nonnull ItemStack stack) {
            Direction side = source.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos sourcePos = source.getPos();
            BlockPos offsetPos = sourcePos.relative(side);
            List<LivingEntity> entities = source.getLevel().getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(offsetPos, offsetPos.offset(1, 1, 1)));
            boolean latched = false;

            for (LivingEntity entity : entities) {
                AxisAlignedBB bound = new AxisAlignedBB(entity.getX() - 0.2, entity.getY() - 0.5, entity.getZ() - 0.2,
                      entity.getX() + 0.2, entity.getY() + entity.getDimensions(entity.getPose()).height + 4, entity.getZ() + 0.2);
                List<EntityBalloon> balloonsNear = source.getLevel().getEntitiesOfClass(EntityBalloon.class, bound);
                boolean hasBalloon = false;
                for (EntityBalloon balloon : balloonsNear) {
                    if (balloon.latchedEntity == entity) {
                        hasBalloon = true;
                        break;
                    }
                }
                if (!hasBalloon) {
                    EntityBalloon balloon = EntityBalloon.create(entity, color);
                    if (balloon != null) {
                        source.getLevel().addFreshEntity(balloon);
                    }
                    latched = true;
                }
            }
            if (!latched) {
                Vector3d pos = Vector3d.atLowerCornerOf(sourcePos).add(0, -0.5, 0);
                switch (side) {
                    case DOWN:
                        pos = pos.add(0, -3.5, 0);
                        break;
                    case NORTH:
                        pos = pos.add(0, -1, -0.5);
                        break;
                    case SOUTH:
                        pos = pos.add(0, -1, 0.5);
                        break;
                    case WEST:
                        pos = pos.add(-0.5, -1, 0);
                        break;
                    case EAST:
                        pos = pos.add(0.5, -1, 0);
                        break;
                    case UP:
                    default:
                        break;
                }
                if (!source.getLevel().isClientSide) {
                    EntityBalloon balloon = EntityBalloon.create(source.getLevel(), pos.x, pos.y, pos.z, color);
                    if (balloon != null) {
                        source.getLevel().addFreshEntity(balloon);
                    }
                }
            }
            stack.shrink(1);
            return stack;
        }
    }
}