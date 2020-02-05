package mekanism.common.item.gear;

import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.api.IDisableableEnum;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.render.item.gear.RenderAtomicDisassembler;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.ItemEnergized;
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
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants.WorldEvents;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    private BlockRayTraceResult doRayTrace(BlockState state, BlockPos pos, PlayerEntity player) {
        Vec3d positionEyes = player.getEyePosition(1.0F);
        Vec3d playerLook = player.getLook(1.0F);
        double blockReachDistance = player.getAttribute(PlayerEntity.REACH_DISTANCE).getValue();
        Vec3d maxReach = positionEyes.add(playerLook.x * blockReachDistance, playerLook.y * blockReachDistance, playerLook.z * blockReachDistance);
        //TODO: Fix this
        BlockRayTraceResult res = player.world.rayTraceBlocks(new RayTraceContext(positionEyes, playerLook, BlockMode.COLLIDER, FluidMode.NONE, player));
        //RayTraceResult res = state.collisionRayTrace(player.world, pos, playerLook, maxReach);
        //TODO: Should the miss have a different vector
        return res != null ? res : BlockRayTraceResult.createMiss(Vec3d.ZERO, Direction.UP, pos);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemStack, BlockPos pos, PlayerEntity player) {
        super.onBlockStartBreak(itemStack, pos, player);
        if (!player.world.isRemote && !player.isCreative()) {
            Mode mode = getMode(itemStack);
            boolean extended = mode == Mode.EXTENDED_VEIN;
            if (extended || mode == Mode.VEIN) {
                BlockState state = player.world.getBlockState(pos);
                Block block = state.getBlock();
                BlockRayTraceResult raytrace = doRayTrace(state, pos, player);
                ItemStack itemStackBlock = block.getPickBlock(state, raytrace, player.world, pos, player);
                
                if (matchTag(itemStackBlock, Tags.Items.ORES, ItemTags.LOGS) || extended) {
                    Coord4D orig = new Coord4D(pos, player.world);
                    Set<Coord4D> found = new Finder(player, itemStackBlock, orig, raytrace, extended ? MekanismConfig.general.disassemblerMiningRange.get() : -1).calc();
                    for (Coord4D coord : found) {
                        if (coord.equals(orig)) {
                            continue;
                        }
                        BlockPos coordPos = coord.getPos();
                        BlockState coordState = player.world.getBlockState(coordPos);
                        int destroyEnergy = getDestroyEnergy(itemStack, coordState.getBlockHardness(player.world, coordPos));
                        if (getEnergy(itemStack) < destroyEnergy) {
                            continue;
                        }
                        Block block2 = coordState.getBlock();
                        //TODO: Should these be using coordState instead of state??
                        block2.onBlockHarvested(player.world, coordPos, state, player);
                        player.world.playEvent(WorldEvents.BREAK_BLOCK_EFFECTS, coordPos, Block.getStateId(state));
                        player.world.removeBlock(coordPos, false);
                        //TODO: Check this
                        block2.onReplaced(state, player.world, coordPos, Blocks.AIR.getDefaultState(), false);
                        Block.spawnDrops(state, player.world, coordPos, MekanismUtils.getTileEntity(player.world, coordPos));
                        setEnergy(itemStack, getEnergy(itemStack) - destroyEnergy);
                    }
                }
            }
        }
        return false;
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

    @SafeVarargs
    private final boolean matchTag(ItemStack itemStack, Tag<Item>... tags) {
        // Find collapse tags a list of paths.
        List<String> tagPaths = Arrays.stream(tags)
                .map(Tag::getId)
                .map(ResourceLocation::getPath)
                .collect(Collectors.toList());

        // Get list of tags matching item collapsed to paths.
        List<String> itemPaths = ItemTags.getCollection().getOwningTags(itemStack.getItem()).stream().map(ResourceLocation::getPath).collect(Collectors.toList());

        return itemPaths.stream().anyMatch(tagPaths::contains);
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

        public static Map<Block, List<Block>> ignoreBlocks = new Object2ObjectOpenHashMap<>();

        static {
            ignoreBlocks.put(Blocks.REDSTONE_ORE, Collections.singletonList(Blocks.REDSTONE_ORE));
        }

        private final PlayerEntity player;
        public final World world;
        public final ItemStack stack;
        public final Coord4D location;
        public final Set<Coord4D> found = new ObjectOpenHashSet<>();
        private final RayTraceResult rayTraceResult;
        private final Block startBlock;
        private final boolean isWood;
        private final int maxRange;
        private final int maxCount;

        public Finder(PlayerEntity p, ItemStack s, Coord4D loc, RayTraceResult traceResult, int range) {
            player = p;
            world = p.world;
            stack = s;
            location = loc;
            startBlock = loc.getBlock(world);
            rayTraceResult = traceResult;
            isWood = stack.getItem().isIn(ItemTags.LOGS);
            maxRange = range;
            maxCount = MekanismConfig.general.disassemblerMiningCount.get() - 1;
        }

        public void loop(Coord4D pointer) {
            if (found.contains(pointer) || found.size() > maxCount) {
                return;
            }
            found.add(pointer);
            for (Direction side : EnumUtils.DIRECTIONS) {
                Coord4D coord = pointer.offset(side);
                if (maxRange > 0 && location.distanceTo(coord) > maxRange) {
                    continue;
                }
                if (world.isBlockLoaded(coord.getPos())) {
                    Block block = coord.getBlock(world);
                    //TODO: Verify this works as a replacement for the below commented code
                    if (block == startBlock) {
                        loop(coord);
                    }
                    /*if (checkID(block)) {
                        ItemStack blockStack = block.getPickBlock(coord.getBlockState(world), rayTraceResult, world, coord.getPos(), player);
                        if (ItemHandlerHelper.canItemStacksStack(stack, blockStack) || (block == startBlock && isWood && coord.getBlockMeta(world) % 4 == stack.getDamage() % 4)) {
                            loop(coord);
                        }
                    }*/
                }
            }
        }

        public Set<Coord4D> calc() {
            loop(location);
            return found;
        }

        public boolean checkID(Block b) {
            Block origBlock = location.getBlock(world);
            //TODO: Is there a point in ignored at all anyways
            List<Block> ignored = ignoreBlocks.get(origBlock);
            return ignored == null ? b == origBlock : ignored.contains(b);
        }
    }

    @FunctionalInterface
    interface ItemUseConsumer {

        //Used to reference useHoe and useShovel via lambda references
        ActionResultType use(ItemStack stack, PlayerEntity player, ItemUseContext context);
    }
}