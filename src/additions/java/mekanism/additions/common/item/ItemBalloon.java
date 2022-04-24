package mekanism.additions.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.additions.common.entity.EntityBalloon;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
    public InteractionResultHolder<ItemStack> use(Level world, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            boolean rightHand = (player.getMainArm() == HumanoidArm.RIGHT) == (hand == InteractionHand.MAIN_HAND);
            Vec3 pos = new Pos3D(rightHand ? -0.4 : 0.4, 0, 0.3).yRot(player.yBodyRot).translate(new Pos3D(player));
            EntityBalloon balloon = EntityBalloon.create(world, pos.x - 0.5, pos.y - 1.25, pos.z - 0.5, color);
            if (balloon == null) {
                return InteractionResultHolder.fail(stack);
            }
            world.addFreshEntity(balloon);
        }
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResultHolder.success(stack);
    }

    @Nonnull
    @Override
    public Component getName(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBalloon balloon) {
            return TextComponentUtil.build(balloon.getColor(), super.getName(stack));
        }
        return super.getName(stack);
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        if (player.isShiftKeyDown()) {
            BlockPos pos = context.getClickedPos();
            AABB bound = new AABB(pos, pos.offset(1, 3, 1));
            List<EntityBalloon> balloonsNear = player.level.getEntitiesOfClass(EntityBalloon.class, bound);
            if (!balloonsNear.isEmpty()) {
                return InteractionResult.FAIL;
            }
            Level world = context.getLevel();
            if (WorldUtils.isValidReplaceableBlock(world, pos)) {
                pos = pos.below();
            }
            if (!Block.canSupportCenter(world, pos, Direction.UP)) {
                return InteractionResult.FAIL;
            }
            if (WorldUtils.isValidReplaceableBlock(world, pos.above()) && WorldUtils.isValidReplaceableBlock(world, pos.above(2))) {
                world.removeBlock(pos.above(), false);
                world.removeBlock(pos.above(2), false);
                if (!world.isClientSide) {
                    EntityBalloon balloon = EntityBalloon.create(world, pos, color);
                    if (balloon == null) {
                        return InteractionResult.FAIL;
                    }
                    world.addFreshEntity(balloon);
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Nonnull
    @Override
    public InteractionResult interactLivingEntity(@Nonnull ItemStack stack, Player player, @Nonnull LivingEntity entity, @Nonnull InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (!player.level.isClientSide) {
                AABB bound = new AABB(entity.getX() - 0.2, entity.getY() - 0.5, entity.getZ() - 0.2,
                      entity.getX() + 0.2, entity.getY() + entity.getDimensions(entity.getPose()).height + 4, entity.getZ() + 0.2);
                List<EntityBalloon> balloonsNear = player.level.getEntitiesOfClass(EntityBalloon.class, bound);
                for (EntityBalloon balloon : balloonsNear) {
                    if (balloon.latchedEntity == entity) {
                        return InteractionResult.SUCCESS;
                    }
                }
                EntityBalloon balloon = EntityBalloon.create(entity, color);
                if (balloon == null) {
                    return InteractionResult.FAIL;
                }
                player.level.addFreshEntity(balloon);
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static class DispenserBehavior extends DefaultDispenseItemBehavior {

        private final EnumColor color;

        public DispenserBehavior(EnumColor color) {
            this.color = color;
        }

        @Nonnull
        @Override
        public ItemStack execute(BlockSource source, @Nonnull ItemStack stack) {
            Direction side = source.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos sourcePos = source.getPos();
            BlockPos offsetPos = sourcePos.relative(side);
            List<LivingEntity> entities = source.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(offsetPos, offsetPos.offset(1, 1, 1)));
            boolean latched = false;

            for (LivingEntity entity : entities) {
                AABB bound = new AABB(entity.getX() - 0.2, entity.getY() - 0.5, entity.getZ() - 0.2,
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
                Vec3 pos = Vec3.atLowerCornerOf(sourcePos).add(0, -0.5, 0);
                switch (side) {
                    case DOWN -> pos = pos.add(0, -3.5, 0);
                    case NORTH -> pos = pos.add(0, -1, -0.5);
                    case SOUTH -> pos = pos.add(0, -1, 0.5);
                    case WEST -> pos = pos.add(-0.5, -1, 0);
                    case EAST -> pos = pos.add(0.5, -1, 0);
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