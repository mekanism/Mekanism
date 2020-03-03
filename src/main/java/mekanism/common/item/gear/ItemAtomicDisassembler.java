package mekanism.common.item.gear;

import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IDisableableEnum;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.render.item.ISTERProvider;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockBounding;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.IItemHUDProvider;
import mekanism.common.item.ItemEnergized;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;

public class ItemAtomicDisassembler extends ItemEnergized implements IItemHUDProvider {

    public ItemAtomicDisassembler(Properties properties) {
        //TODO: Set some tool types?
        super(MekanismConfig.general.disassemblerBatteryCapacity.get(), properties.setNoRepair().setISTER(ISTERProvider::disassembler));
    }

    @Override
    public boolean canHarvestBlock(@Nonnull BlockState state) {
        return state.getBlock() != Blocks.BEDROCK;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        Mode mode = getMode(stack);
        tooltip.add(MekanismLang.MODE.translate(EnumColor.INDIGO, mode));
        tooltip.add(MekanismLang.DISASSEMBLER_EFFICIENCY.translate(EnumColor.INDIGO, mode.getEfficiency()));
    }

    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        double energy = getEnergy(stack);
        int energyCost = MekanismConfig.general.disassemblerEnergyUsageWeapon.get();
        int minDamage = MekanismConfig.general.disassemblerDamageMin.get();
        int damageDifference = MekanismConfig.general.disassemblerDamageMax.get() - minDamage;
        //If we don't have enough power use it at a reduced power level
        double percent = 1;
        if (energy < energyCost && energyCost != 0) {
            percent = energy / energyCost;
        }
        float damage = (float) (minDamage + damageDifference * percent);
        if (attacker instanceof PlayerEntity) {
            target.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) attacker), damage);
        } else {
            target.attackEntityFrom(DamageSource.causeMobDamage(attacker), damage);
        }
        if (energy > 0) {
            setEnergy(stack, energy - energyCost);
        }
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return getEnergy(stack) != 0 ? getMode(stack).getEfficiency() : 1F;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity entityliving) {
        setEnergy(stack, getEnergy(stack) - getDestroyEnergy(stack, state.getBlockHardness(world, pos)));
        return true;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemStack, BlockPos pos, PlayerEntity player) {
        World world = player.world;
        if (!world.isRemote && !player.isCreative()) {
            Mode mode = getMode(itemStack);
            boolean extended = mode == Mode.EXTENDED_VEIN;
            if (extended || mode == Mode.VEIN) {
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof BlockBounding) {
                    //Even though we now handle breaking bounding blocks properly, don't allow vein mining
                    // them as an added safety measure
                    return super.onBlockStartBreak(itemStack, pos, player);
                }
                //If it is extended or should be treated as an ore
                if (extended || state.isIn(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE)) {
                    ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
                    List<BlockPos> found = findPositions(state, pos, world, extended ? MekanismConfig.general.disassemblerMiningRange.get() : -1);
                    for (BlockPos foundPos : found) {
                        if (pos.equals(foundPos)) {
                            continue;
                        }
                        BlockState foundState = world.getBlockState(foundPos);
                        int destroyEnergy = getDestroyEnergy(itemStack, foundState.getBlockHardness(world, foundPos));
                        double energy = getEnergy(itemStack);
                        if (energy < destroyEnergy) {
                            //If we don't have energy to break the block continue
                            //Note: We do not break as given the energy scales with hardness, so it is possible we still have energy to break another block
                            // Given we validate the blocks are the same but their block states may be different thus making them have different
                            // block hardness values in a modded context
                            continue;
                        }
                        int exp = ForgeHooks.onBlockBreakEvent(world, serverPlayerEntity.interactionManager.getGameType(), serverPlayerEntity, foundPos);
                        if (exp == -1) {
                            //If we can't actually break the block continue (this allows mods to stop us from vein mining into protected land)
                            continue;
                        }
                        //Otherwise break the block
                        Block block = foundState.getBlock();
                        //Get the tile now so that we have it for when we try to harvest the block
                        TileEntity tileEntity = MekanismUtils.getTileEntity(world, foundPos);
                        //Remove the block
                        boolean removed = foundState.removedByPlayer(world, foundPos, player, true, world.getFluidState(foundPos));
                        if (removed) {
                            block.onPlayerDestroy(world, foundPos, foundState);
                            //Harvest the block allowing it to handle block drops, incrementing block mined count, and adding exhaustion
                            block.harvestBlock(world, player, foundPos, foundState, tileEntity, itemStack);
                            player.addStat(Stats.ITEM_USED.get(this));
                            if (exp > 0) {
                                //If we have xp drop it
                                block.dropXpOnBlockBreak(world, foundPos, exp);
                            }
                            //Use energy
                            setEnergy(itemStack, energy - destroyEnergy);
                        }
                    }
                }
            }
        }
        return super.onBlockStartBreak(itemStack, pos, player);
    }

    private static List<BlockPos> findPositions(BlockState state, BlockPos location, IWorld world, int maxRange) {
        List<BlockPos> found = new ArrayList<>();
        Set<BlockPos> checked = new ObjectOpenHashSet<>();
        found.add(location);
        Block startBlock = state.getBlock();
        int maxCount = MekanismConfig.general.disassemblerMiningCount.get() - 1;
        for (int i = 0; i < found.size(); i++) {
            BlockPos blockPos = found.get(i);
            checked.add(blockPos);
            for (BlockPos pos : BlockPos.getAllInBoxMutable(blockPos.add(-1, -1, -1), blockPos.add(1, 1, 1))) {
                //We can check contains as mutable
                if (!checked.contains(pos)) {
                    if (maxRange == -1 || Math.sqrt(location.distanceSq(pos)) <= maxRange) {
                        if (world.isBlockLoaded(pos) && startBlock == world.getBlockState(pos).getBlock()) {
                            //Make sure to add it as immutable
                            found.add(pos.toImmutable());
                            if (found.size() > maxCount) {
                                return found;
                            }
                        }
                    }
                }
            }
        }
        return found;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isShiftKeyDown()) {
            if (!world.isRemote) {
                toggleMode(stack);
                Mode mode = getMode(stack);
                player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                      MekanismLang.DISASSEMBLER_MODE_TOGGLE.translateColored(EnumColor.GRAY, EnumColor.INDIGO, mode,
                            MekanismLang.GENERIC_PARENTHESIS.translateColored(EnumColor.AQUA, mode.getEfficiency()))));
            }
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        return MekanismUtils.performActions(
              //First try to use the disassembler as an axe
              stripLogsAOE(context),
              //Then as a shovel
              //Fire a generic use event, if we are allowed to use the tool return zero otherwise return -1
              // This is to mirror how onHoeUse returns of 0 if allowed, -1 if not allowed, and 1 if processing happened in the event
              () -> tillAOE(context, ShovelItem.SHOVEL_LOOKUP, ctx -> onToolUse(ctx.getPlayer(), ctx.getHand(), ctx.getPos(), ctx.getFace()) ? 0 : -1,
                    SoundEvents.ITEM_SHOVEL_FLATTEN, MekanismConfig.general.disassemblerEnergyUsageShovel.get()),
              //Finally as a hoe
              () -> tillAOE(context, HoeItem.HOE_LOOKUP, ForgeEventFactory::onHoeUse, SoundEvents.ITEM_HOE_TILL, MekanismConfig.general.disassemblerEnergyUsageHoe.get())
        );
    }

    private ActionResultType tillAOE(ItemUseContext context, Map<Block, BlockState> lookup, ToIntFunction<ItemUseContext> onItemUse, SoundEvent sound, int energyUsage) {
        PlayerEntity player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            //Skip if we don't have a player or they are sneaking
            return ActionResultType.PASS;
        }
        Direction sideHit = context.getFace();
        if (sideHit == Direction.DOWN) {
            //Don't allow tilling a block from underneath
            return ActionResultType.PASS;
        }
        Hand hand = context.getHand();
        ItemStack stack = player.getHeldItem(hand);
        int diameter = getMode(stack).getDiameter();
        if (diameter == 0) {
            //If we don't have any blocks we are going to want to do, then skip it
            return ActionResultType.PASS;
        }
        double energy = getEnergy(stack);
        if (energy < energyUsage) {
            //Fail if we don't have enough energy or using the item failed
            return ActionResultType.FAIL;
        }
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        BlockState tilledState = lookup.get(world.getBlockState(pos).getBlock());
        if (tilledState == null) {
            //Skip tilling the blocks if the one we clicked cannot be tilled
            return ActionResultType.PASS;
        }
        BlockPos abovePos = pos.up();
        BlockState aboveState = world.getBlockState(abovePos);
        //Check to make sure the block above is not opaque
        if (aboveState.isOpaqueCube(world, abovePos)) {
            //If the block above our source is opaque, just skip tiling in general
            return ActionResultType.PASS;
        }
        int useResult = onItemUse.applyAsInt(context);
        if (useResult < 0) {
            return ActionResultType.PASS;
        } else if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }
        if (useResult == 0) {
            //Processing did not happen in the hook so we need to process it
            world.setBlockState(pos, tilledState, 11);
            Material aboveMaterial = aboveState.getMaterial();
            if (aboveMaterial == Material.PLANTS || aboveMaterial == Material.TALL_PLANTS) {
                world.destroyBlock(abovePos, true);
            }
            world.playSound(player, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        double energyUsed = energyUsage;
        int radius = (diameter - 1) / 2;
        for (BlockPos newPos : BlockPos.getAllInBoxMutable(pos.add(-radius, 0, -radius), pos.add(radius, 0, radius))) {
            if (pos.equals(newPos)) {
                //Skip the source position as it is free and we manually handled it before the loop
                continue;
            }
            if (energyUsed + energyUsage > energy) {
                break;
            }
            BlockState stateAbove = world.getBlockState(newPos.up());
            //Check to make sure the block above is not opaque and that the result we would get from tilling the other block is
            // the same as the one we got on the initial block we interacted with
            if (!stateAbove.isOpaqueCube(world, newPos.up()) && tilledState == lookup.get(world.getBlockState(newPos).getBlock())) {
                //Some of the below methods don't behave properly when the BlockPos is mutable, so now that we are onto ones where it may actually
                // matter we make sure to get an immutable instance of newPos
                newPos = newPos.toImmutable();
                useResult = onItemUse.applyAsInt(new ItemUseContext(player, hand, new BlockRayTraceResult(Vec3d.ZERO, Direction.UP, newPos, false)));
                if (useResult < 0) {
                    //We were denied from using the item so continue to the next block
                    continue;
                }
                //Add energy cost
                energyUsed += energyUsage;
                if (useResult > 0) {
                    //Processing happened in the hook so we use our desired fuel amount
                    continue;
                } //else we are allowed to use the item
                //Replace the block. Note it just directly sets it (in the same way that HoeItem/ShovelItem do)
                world.setBlockState(newPos, tilledState, 11);
                Material aboveMaterial = stateAbove.getMaterial();
                if (aboveMaterial == Material.PLANTS || aboveMaterial == Material.TALL_PLANTS) {
                    //If the block above the one we tilled is a plant, then we try to remove it
                    world.destroyBlock(newPos.up(), true);
                }
                world.playSound(player, newPos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }
        setEnergy(stack, energy - energyUsed);
        return ActionResultType.SUCCESS;
    }

    /**
     * Strips logs in an AOE using a shovel (ex: grass to grass path). Charge affects the AOE. Optional per-block EMC cost.
     */
    private ActionResultType stripLogsAOE(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            //Skip if we don't have a player or they are sneaking
            return ActionResultType.PASS;
        }
        Hand hand = context.getHand();
        ItemStack stack = player.getHeldItem(hand);
        int diameter = getMode(stack).getDiameter();
        if (diameter == 0) {
            //If we don't have any blocks we are going to want to do, then skip it
            return ActionResultType.PASS;
        }
        double energy = getEnergy(stack);
        int energyUsage = MekanismConfig.general.disassemblerEnergyUsageAxe.get();
        if (energy < energyUsage) {
            //Fail if we don't have enough energy or using the item failed
            return ActionResultType.FAIL;
        }
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Map<Block, Block> lookup = AxeItem.BLOCK_STRIPPING_MAP;
        BlockState clickedState = world.getBlockState(pos);
        Block strippedBlock = lookup.get(clickedState.getBlock());
        if (strippedBlock == null) {
            //Skip stripping the blocks if the one we clicked cannot be stripped
            return ActionResultType.PASS;
        }
        //Note: We don't need to fire a check for the tool being used here as we never would have had our current method get called
        // if a generic interact was not allowed
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }
        Axis axis = clickedState.get(RotatedPillarBlock.AXIS);
        BlockState strippedState = strippedBlock.getDefaultState().with(RotatedPillarBlock.AXIS, axis);
        //Process the block we interacted with initially and play the sound
        world.setBlockState(pos, strippedState, 11);
        world.playSound(player, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
        Direction side = context.getFace();
        double energyUsed = energyUsage;
        for (BlockPos newPos : getStrippingArea(pos, side, (diameter - 1) / 2)) {
            if (pos.equals(newPos)) {
                //Skip the source position as it is free and we manually handled it before the loop
                continue;
            }
            if (energyUsed + energyUsage > energy) {
                break;
            }
            //Check to make that the result we would get from stripping the other block is the same as the one we got on the initial block we interacted with
            // Also make sure that it is on the same axis as the block we initially clicked
            BlockState state = world.getBlockState(newPos);
            if (strippedBlock == lookup.get(state.getBlock()) && axis == state.get(RotatedPillarBlock.AXIS)) {
                //Some of the below methods don't behave properly when the BlockPos is mutable, so now that we are onto ones where it may actually
                // matter we make sure to get an immutable instance of newPos
                newPos = newPos.toImmutable();
                if (!onToolUse(player, hand, newPos, side)) {
                    //We were denied from using the item so continue to the next block
                    continue;
                } //else we are allowed to use the item
                //Add energy cost
                energyUsed += energyUsage;
                //Replace the block. Note it just directly sets it (in the same way that AxeItem does).
                world.setBlockState(newPos, strippedState, 11);
                world.playSound(player, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }
        setEnergy(stack, energy - energyUsed);
        return ActionResultType.SUCCESS;
    }

    private static Iterable<BlockPos> getStrippingArea(BlockPos pos, Direction direction, int radius) {
        AxisAlignedBB box;
        switch (direction) {
            case EAST:
            case WEST:
                box = new AxisAlignedBB(pos.getX(), pos.getY() - radius, pos.getZ() - radius, pos.getX(), pos.getY() + radius, pos.getZ() + radius);
                break;
            case UP:
            case DOWN:
                box = new AxisAlignedBB(pos.getX() - radius, pos.getY(), pos.getZ() - radius, pos.getX() + radius, pos.getY(), pos.getZ() + radius);
                break;
            case SOUTH:
            case NORTH:
                box = new AxisAlignedBB(pos.getX() - radius, pos.getY() - radius, pos.getZ(), pos.getX() + radius, pos.getY() + radius, pos.getZ());
                break;
            default:
                return BlockPos.getAllInBoxMutable(BlockPos.ZERO, BlockPos.ZERO);
        }
        return BlockPos.getAllInBoxMutable(new BlockPos(box.minX, box.minY, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ));
    }

    /**
     * Tries to use a tool as a player
     *
     * @return True if the player is allowed to use the tool, false otherwise
     */
    private static boolean onToolUse(PlayerEntity player, Hand hand, BlockPos pos, Direction face) {
        RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, pos, face);
        //Nothing happens if it is cancelled or we are not allowed to use the item so return false
        return !event.isCanceled() && event.getUseItem() != Result.DENY;
    }

    private int getDestroyEnergy(ItemStack itemStack, float hardness) {
        int destroyEnergy = MekanismConfig.general.disassemblerEnergyUsage.get() * getMode(itemStack).getEfficiency();
        return hardness == 0 ? destroyEnergy / 2 : destroyEnergy;
    }

    public Mode getMode(ItemStack itemStack) {
        return Mode.getFromInt(ItemDataUtils.getInt(itemStack, "mode"));
    }

    public void toggleMode(ItemStack itemStack) {
        ItemDataUtils.setInt(itemStack, "mode", getMode(itemStack).getNext().ordinal());
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
        Multimap<String, AttributeModifier> multiMap = super.getAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EquipmentSlotType.MAINHAND) {
            multiMap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, Operation.ADDITION));
        }
        return multiMap;
    }

    public enum Mode implements IDisableableEnum<Mode>, IHasTranslationKey {
        NORMAL(MekanismLang.DISASSEMBLER_NORMAL, 20, 3, () -> true),
        SLOW(MekanismLang.DISASSEMBLER_SLOW, 8, 1, MekanismConfig.general.disassemblerSlowMode::get),
        FAST(MekanismLang.DISASSEMBLER_FAST, 128, 5, MekanismConfig.general.disassemblerFastMode::get),
        VEIN(MekanismLang.DISASSEMBLER_VEIN, 20, 3, MekanismConfig.general.disassemblerVeinMining::get),
        EXTENDED_VEIN(MekanismLang.DISASSEMBLER_EXTENDED_VEIN, 20, 3, MekanismConfig.general.disassemblerExtendedMining::get),
        OFF(MekanismLang.DISASSEMBLER_OFF, 0, 0, () -> true);

        private static Mode[] VALUES = values();
        private final Supplier<Boolean> checkEnabled;
        private final ILangEntry langEntry;
        private final int efficiency;
        //Must be odd, or zero
        private final int diameter;

        Mode(ILangEntry langEntry, int efficiency, int diameter, Supplier<Boolean> checkEnabled) {
            this.langEntry = langEntry;
            this.efficiency = efficiency;
            this.diameter = diameter;
            this.checkEnabled = checkEnabled;
        }

        /**
         * Gets a Mode from its ordinal. NOTE: if this mode is not enabled then it will reset to NORMAL
         */
        public static Mode getFromInt(int index) {
            Mode mode = VALUES[Math.floorMod(index, VALUES.length)];
            return mode.isEnabled() ? mode : NORMAL;
        }

        @Nonnull
        @Override
        public Mode byIndex(int index) {
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            return VALUES[Math.floorMod(index, VALUES.length)];
        }

        @Override
        public String getTranslationKey() {
            return langEntry.getTranslationKey();
        }

        public int getEfficiency() {
            return efficiency;
        }

        public int getDiameter() {
            return diameter;
        }

        @Override
        public boolean isEnabled() {
            return checkEnabled.get();
        }
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list, ItemStack stack) {
        Mode mode = getMode(stack);
        list.add(MekanismLang.MODE.translate(EnumColor.INDIGO, mode));
        list.add(MekanismLang.DISASSEMBLER_EFFICIENCY.translate(EnumColor.INDIGO, mode.getEfficiency()));
    }
}