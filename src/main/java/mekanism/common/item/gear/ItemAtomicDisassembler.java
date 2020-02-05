package mekanism.common.item.gear;

import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IDisableableEnum;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.render.item.gear.RenderAtomicDisassembler;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockBounding;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.ItemEnergized;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;

//TODO: Use HoeItem#HOE_LOOKUP for figuring out what to do with different block types?
// And ShovelItem#SHOVEL_LOOKUP for figuring out the grass -> grass path
public class ItemAtomicDisassembler extends ItemEnergized {

    //TODO: Set some tool types?
    public ItemAtomicDisassembler(Properties properties) {
        super(MekanismConfig.general.disassemblerBatteryCapacity.get(), properties.setNoRepair().setISTER(() -> getISTER()));
    }

    @OnlyIn(Dist.CLIENT)
    private static Callable<ItemStackTileEntityRenderer> getISTER() {
        //NOTE: This extra method is needed to avoid classloading issues on servers
        return RenderAtomicDisassembler::new;
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
                    Set<BlockPos> found = new Finder(state, pos, world, extended ? MekanismConfig.general.disassemblerMiningRange.get() : -1).calc();
                    for (BlockPos foundPos : found) {
                        if (pos.equals(foundPos)) {
                            //TODO: Make it so this just doesn't get added instead of specifically checking for it
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
        PlayerEntity player = context.getPlayer();
        if (player != null && !player.isShiftKeyDown()) {
            Hand hand = context.getHand();
            ItemStack stack = player.getHeldItem(hand);
            int diameter = getMode(stack).getDiameter();
            if (diameter > 0) {
                World world = context.getWorld();
                BlockPos pos = context.getPos();
                Block block = world.getBlockState(pos).getBlock();
                if (block == Blocks.DIRT || block == Blocks.GRASS_PATH) {
                    return useItemAs(player, context, stack, diameter, this::useHoe);
                } else if (block == Blocks.GRASS_BLOCK) {
                    return useItemAs(player, context, stack, diameter, this::useShovel);
                }
            }
        }
        return ActionResultType.PASS;
    }

    private ActionResultType useItemAs(PlayerEntity player, ItemUseContext context, ItemStack stack, int diameter, ItemUseConsumer consumer) {
        double energy = getEnergy(stack);
        int hoeUsage = MekanismConfig.general.disassemblerEnergyUsageHoe.get();
        if (energy < hoeUsage || consumer.use(stack, player, context) == ActionResultType.FAIL) {
            //Fail if we don't have enough energy or using the item failed
            return ActionResultType.FAIL;
        }
        double energyUsed = hoeUsage;
        int radius = (diameter - 1) / 2;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (energyUsed + hoeUsage > energy) {
                    break;
                }
                //TODO: Fix AOE usage
                /*if ((x != 0 || z != 0) && consumer.use(stack, player, hand, world, pos.add(x, 0, z), side) == ActionResultType.SUCCESS) {
                    //Don't attempt to use it on the source location as it was already done above
                    // If we successfully used it in a spot increment how much energy we used
                    energyUsed += hoeUsage;
                }*/
            }
        }
        setEnergy(stack, energy - energyUsed);
        return ActionResultType.SUCCESS;
    }

    private ActionResultType useHoe(ItemStack stack, PlayerEntity player, ItemUseContext context) {
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();
        if (!player.canPlayerEdit(pos.offset(facing), facing, stack)) {
            return ActionResultType.FAIL;
        }
        int hook = ForgeEventFactory.onHoeUse(context);
        if (hook != 0) {
            return hook > 0 ? ActionResultType.SUCCESS : ActionResultType.FAIL;
        }
        World world = context.getWorld();
        if (facing != Direction.DOWN && world.isAirBlock(pos.up())) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            BlockState newState = null;
            if (block == Blocks.GRASS_BLOCK || block == Blocks.GRASS_PATH) {
                newState = Blocks.FARMLAND.getDefaultState();
            } else if (block == Blocks.DIRT) {
                newState = Blocks.FARMLAND.getDefaultState();
            } else if (block == Blocks.COARSE_DIRT) {
                newState = Blocks.DIRT.getDefaultState();
            }
            if (newState != null) {
                setBlock(stack, player, context.getHand(), world, pos, newState);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    private ActionResultType useShovel(ItemStack stack, PlayerEntity player, ItemUseContext context) {
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();
        if (!player.canPlayerEdit(pos.offset(facing), facing, stack)) {
            return ActionResultType.FAIL;
        }
        World world = context.getWorld();
        if (facing != Direction.DOWN && world.isAirBlock(pos.up())) {
            Block block = world.getBlockState(pos).getBlock();
            if (block == Blocks.GRASS_BLOCK) {
                setBlock(stack, player, context.getHand(), world, pos, Blocks.GRASS_PATH.getDefaultState());
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    private void setBlock(ItemStack stack, PlayerEntity player, Hand hand, World world, BlockPos pos, BlockState state) {
        world.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (!world.isRemote) {
            world.setBlockState(pos, state, 11);
            stack.damageItem(1, player, entity -> entity.sendBreakAnimation(hand));
        }
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

    public static class Finder {

        private final Set<BlockPos> found = new ObjectOpenHashSet<>();
        private final BlockPos location;
        private final Block startBlock;
        private final IWorld world;
        private final int maxRange;
        private final int maxCount;

        public Finder(BlockState state, BlockPos loc, IWorld world, int range) {
            this.world = world;
            location = loc;
            startBlock = state.getBlock();
            maxRange = range;
            maxCount = MekanismConfig.general.disassemblerMiningCount.get() - 1;
        }

        public void loop(BlockPos pointer) {
            if (found.size() > maxCount || found.contains(pointer)) {
                return;
            }
            found.add(pointer);
            for (Direction side : EnumUtils.DIRECTIONS) {
                BlockPos pos = pointer.offset(side);
                if (maxRange == -1 || Math.sqrt(location.distanceSq(pos)) <= maxRange) {
                    if (world.isBlockLoaded(pos) && startBlock == world.getBlockState(pos).getBlock()) {
                        //TODO: This here given we don't finish checking directions before adding the ones that
                        // we want to loop is why https://github.com/mekanism/Mekanism/issues/5767 is a thing
                        // as we are doing things recursively. Might be cleaner to just make this not use recursion
                        // or do so to a lesser extent
                        loop(pos);
                    }
                }
            }
        }

        public Set<BlockPos> calc() {
            loop(location);
            return found;
        }
    }

    @FunctionalInterface
    interface ItemUseConsumer {

        //Used to reference useHoe and useShovel via lambda references
        ActionResultType use(ItemStack stack, PlayerEntity player, ItemUseContext context);
    }
}