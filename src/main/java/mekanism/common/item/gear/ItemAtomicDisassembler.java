package mekanism.common.item.gear;

import com.google.common.collect.Multimap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.ItemEnergized;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDirt.DirtType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.WorldEvents;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemAtomicDisassembler extends ItemEnergized {

    public ItemAtomicDisassembler() {
        super("atomic_disassembler", MekanismConfig.current().general.disassemblerBatteryCapacity.val());
    }

    @Override
    public boolean canHarvestBlock(@Nonnull BlockState state, ItemStack stack) {
        return state.getBlock() != Blocks.BEDROCK;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        Mode mode = getMode(stack);
        tooltip.add(LangUtils.localize("tooltip.mode") + ": " + EnumColor.INDIGO + mode.getModeName());
        tooltip.add(LangUtils.localize("tooltip.efficiency") + ": " + EnumColor.INDIGO + mode.getEfficiency());
    }

    @Override
    public boolean hitEntity(ItemStack itemstack, LivingEntity target, LivingEntity attacker) {
        double energy = getEnergy(itemstack);
        int energyCost = MekanismConfig.current().general.disassemblerEnergyUsageWeapon.val();
        int minDamage = MekanismConfig.current().general.disassemblerDamageMin.val();
        int damageDifference = MekanismConfig.current().general.disassemblerDamageMax.val() - minDamage;
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
            setEnergy(itemstack, energy - energyCost);
        }
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return getEnergy(itemstack) != 0 ? getMode(itemstack).getEfficiency() : 1F;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack itemstack, World world, BlockState state, BlockPos pos, LivingEntity entityliving) {
        setEnergy(itemstack, getEnergy(itemstack) - getDestroyEnergy(itemstack, state.getBlockHardness(world, pos)));
        return true;
    }

    private RayTraceResult doRayTrace(BlockState state, BlockPos pos, PlayerEntity player) {
        Vec3d positionEyes = player.getPositionEyes(1.0F);
        Vec3d playerLook = player.getLook(1.0F);
        double blockReachDistance = player.getAttributeMap().getAttributeInstance(PlayerEntity.REACH_DISTANCE).getAttributeValue();
        Vec3d maxReach = positionEyes.add(playerLook.x * blockReachDistance, playerLook.y * blockReachDistance, playerLook.z * blockReachDistance);
        RayTraceResult res = state.collisionRayTrace(player.world, pos, playerLook, maxReach);
        //noinspection ConstantConditions - idea thinks it's nonnull due to package level annotations, but it's not
        return res != null ? res : new RayTraceResult(Type.MISS, Vec3d.ZERO, Direction.UP, pos);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, PlayerEntity player) {
        super.onBlockStartBreak(itemstack, pos, player);
        if (!player.world.isRemote && !player.isCreative()) {
            Mode mode = getMode(itemstack);
            boolean extended = mode == Mode.EXTENDED_VEIN;
            if (extended || mode == Mode.VEIN) {
                BlockState state = player.world.getBlockState(pos);
                Block block = state.getBlock();
                if (block == Blocks.LIT_REDSTONE_ORE) {
                    block = Blocks.REDSTONE_ORE;
                }
                RayTraceResult raytrace = doRayTrace(state, pos, player);
                ItemStack stack = block.getPickBlock(state, raytrace, player.world, pos, player);
                List<String> names = OreDictCache.getOreDictName(stack);
                boolean isOre = false;
                for (String s : names) {
                    if (s.startsWith("ore") || s.equals("logWood")) {
                        isOre = true;
                        break;
                    }
                }
                if (isOre || extended) {
                    Coord4D orig = new Coord4D(pos, player.world);
                    Set<Coord4D> found = new Finder(player, stack, orig, raytrace, extended ? MekanismConfig.current().general.disassemblerMiningRange.val() : -1).calc();
                    for (Coord4D coord : found) {
                        if (coord.equals(orig)) {
                            continue;
                        }
                        int destroyEnergy = getDestroyEnergy(itemstack, coord.getBlockState(player.world).getBlockHardness(player.world, coord.getPos()));
                        if (getEnergy(itemstack) < destroyEnergy) {
                            continue;
                        }
                        Block block2 = coord.getBlock(player.world);
                        block2.onBlockHarvested(player.world, coord.getPos(), state, player);
                        player.world.playEvent(WorldEvents.BREAK_BLOCK_EFFECTS, coord.getPos(), Block.getStateId(state));
                        player.world.removeBlock(coord.getPos(), false);
                        block2.breakBlock(player.world, coord.getPos(), state);
                        block2.dropBlockAsItem(player.world, coord.getPos(), state, 0);
                        setEnergy(itemstack, getEnergy(itemstack) - destroyEnergy);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isFull3D() {
        return true;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entityplayer, @Nonnull Hand hand) {
        ItemStack itemstack = entityplayer.getHeldItem(hand);
        if (entityplayer.isSneaking()) {
            if (!world.isRemote) {
                toggleMode(itemstack);
                Mode mode = getMode(itemstack);
                entityplayer.sendMessage(new StringTextComponent(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + " " + EnumColor.GREY + LangUtils.localize("tooltip.modeToggle")
                                                                 + " " + EnumColor.INDIGO + mode.getModeName() + EnumColor.AQUA + " (" + mode.getEfficiency() + ")"));
            }
            return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
        }
        return new ActionResult<>(ActionResultType.PASS, itemstack);
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) {
            ItemStack stack = player.getHeldItem(hand);
            int diameter = getMode(stack).getDiameter();
            if (diameter > 0) {
                Block block = world.getBlockState(pos).getBlock();
                if (block == Blocks.DIRT || block == Blocks.GRASS_PATH) {
                    return useItemAs(player, world, pos, side, stack, diameter, this::useHoe);
                } else if (block == Blocks.GRASS_BLOCK) {
                    return useItemAs(player, world, pos, side, stack, diameter, this::useShovel);
                }
            }
        }
        return ActionResultType.PASS;
    }

    private ActionResultType useItemAs(PlayerEntity player, World world, BlockPos pos, Direction side, ItemStack stack, int diameter, ItemUseConsumer consumer) {
        double energy = getEnergy(stack);
        int hoeUsage = MekanismConfig.current().general.disassemblerEnergyUsageHoe.val();
        if (energy < hoeUsage || consumer.use(stack, player, world, pos, side) == ActionResultType.FAIL) {
            //Fail if we don't have enough energy or using the item failed
            return ActionResultType.FAIL;
        }
        double energyUsed = hoeUsage;
        int radius = (diameter - 1) / 2;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (energyUsed + hoeUsage > energy) {
                    break;
                } else if ((x != 0 || z != 0) && consumer.use(stack, player, world, pos.add(x, 0, z), side) == ActionResultType.SUCCESS) {
                    //Don't attempt to use it on the source location as it was already done above
                    // If we successfully used it in a spot increment how much energy we used
                    energyUsed += hoeUsage;
                }
            }
        }
        setEnergy(stack, energy - energyUsed);
        return ActionResultType.SUCCESS;
    }

    private ActionResultType useHoe(ItemStack stack, PlayerEntity player, World world, BlockPos pos, Direction facing) {
        if (!player.canPlayerEdit(pos.offset(facing), facing, stack)) {
            return ActionResultType.FAIL;
        }
        int hook = ForgeEventFactory.onHoeUse(stack, player, world, pos);
        if (hook != 0) {
            return hook > 0 ? ActionResultType.SUCCESS : ActionResultType.FAIL;
        }
        if (facing != Direction.DOWN && world.isAirBlock(pos.up())) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            BlockState newState = null;
            if (block == Blocks.GRASS_BLOCK || block == Blocks.GRASS_PATH) {
                newState = Blocks.FARMLAND.getDefaultState();
            } else if (block == Blocks.DIRT) {
                DirtType type = state.get(BlockDirt.VARIANT);
                if (type == DirtType.DIRT) {
                    newState = Blocks.FARMLAND.getDefaultState();
                } else if (type == DirtType.COARSE_DIRT) {
                    newState = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, DirtType.DIRT);
                }
            }
            if (newState != null) {
                setBlock(stack, player, world, pos, newState);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    private ActionResultType useShovel(ItemStack stack, PlayerEntity player, World world, BlockPos pos, Direction facing) {
        if (!player.canPlayerEdit(pos.offset(facing), facing, stack)) {
            return ActionResultType.FAIL;
        } else if (facing != Direction.DOWN && world.isAirBlock(pos.up())) {
            Block block = world.getBlockState(pos).getBlock();
            if (block == Blocks.GRASS_BLOCK) {
                setBlock(stack, player, world, pos, Blocks.GRASS_PATH.getDefaultState());
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    private void setBlock(ItemStack stack, PlayerEntity player, World worldIn, BlockPos pos, BlockState state) {
        worldIn.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (!worldIn.isRemote) {
            worldIn.setBlockState(pos, state, 11);
            stack.damageItem(1, player);
        }
    }

    private int getDestroyEnergy(ItemStack itemStack, float hardness) {
        int destroyEnergy = MekanismConfig.current().general.disassemblerEnergyUsage.val() * getMode(itemStack).getEfficiency();
        return hardness == 0 ? destroyEnergy / 2 : destroyEnergy;
    }

    public Mode getMode(ItemStack itemStack) {
        return Mode.getFromInt(ItemDataUtils.getInt(itemStack, "mode"));
    }

    public void toggleMode(ItemStack itemStack) {
        ItemDataUtils.setInt(itemStack, "mode", Mode.getNextEnabledAsInt(getMode(itemStack)));
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EquipmentSlotType equipmentSlot) {
        Multimap<String, AttributeModifier> multiMap = super.getItemAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EquipmentSlotType.MAINHAND) {
            multiMap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
        }
        return multiMap;
    }

    public enum Mode {
        NORMAL("normal", 20, 3, () -> true),
        SLOW("slow", 8, 1, () -> MekanismConfig.current().general.disassemblerSlowMode.val()),
        FAST("fast", 128, 5, () -> MekanismConfig.current().general.disassemblerFastMode.val()),
        VEIN("vein", 20, 3, () -> MekanismConfig.current().general.disassemblerVeinMining.val()),
        EXTENDED_VEIN("extended_vein", 20, 3, () -> MekanismConfig.current().general.disassemblerExtendedMining.val()),
        OFF("off", 0, 0, () -> true);

        private final Supplier<Boolean> checkEnabled;
        private final String mode;
        private final int efficiency;
        //Must be odd, or zero
        private final int diameter;

        Mode(String mode, int efficiency, int diameter, Supplier<Boolean> checkEnabled) {
            this.mode = mode;
            this.efficiency = efficiency;
            this.diameter = diameter;
            this.checkEnabled = checkEnabled;
        }

        /**
         * Gets a Mode from its ordinal. NOTE: if this mode is not enabled then it will reset to NORMAL
         */
        public static Mode getFromInt(int ordinal) {
            Mode[] values = values();
            //If it is out of bounds just shift it as if it had gone around that many times
            Mode mode = values[ordinal % values.length];
            return mode.isEnabled() ? mode : NORMAL;
        }

        public static int getNextEnabledAsInt(Mode mode) {
            //Get the next mode
            Mode next = mode.getNext();
            //keep going until we find one that is enabled (we know at the very least NORMAL and OFF are enabled
            while (!next.isEnabled()) {
                next = next.getNext();
            }
            return next.ordinal();
        }

        private Mode getNext() {
            Mode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        public String getModeName() {
            return LangUtils.localize("mekanism.tooltip.disassembler." + mode);
        }

        public int getEfficiency() {
            return efficiency;
        }

        public int getDiameter() {
            return diameter;
        }

        public boolean isEnabled() {
            return checkEnabled.get();
        }
    }

    public static class Finder {

        public static Map<Block, List<Block>> ignoreBlocks = new HashMap<>();

        static {
            ignoreBlocks.put(Blocks.REDSTONE_ORE, Arrays.asList(Blocks.REDSTONE_ORE, Blocks.LIT_REDSTONE_ORE));
            ignoreBlocks.put(Blocks.LIT_REDSTONE_ORE, Arrays.asList(Blocks.REDSTONE_ORE, Blocks.LIT_REDSTONE_ORE));
        }

        private final PlayerEntity player;
        public final World world;
        public final ItemStack stack;
        public final Coord4D location;
        public final Set<Coord4D> found = new HashSet<>();
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
            isWood = OreDictCache.getOreDictName(stack).contains("logWood");
            maxRange = range;
            maxCount = MekanismConfig.current().general.disassemblerMiningCount.val() - 1;
        }

        public void loop(Coord4D pointer) {
            if (found.contains(pointer) || found.size() > maxCount) {
                return;
            }
            found.add(pointer);
            for (Direction side : Direction.values()) {
                Coord4D coord = pointer.offset(side);
                if (maxRange > 0 && location.distanceTo(coord) > maxRange) {
                    continue;
                }
                if (coord.exists(world)) {
                    Block block = coord.getBlock(world);
                    if (checkID(block)) {
                        ItemStack blockStack = block.getPickBlock(coord.getBlockState(world), rayTraceResult, world, coord.getPos(), player);
                        if (ItemHandlerHelper.canItemStacksStack(stack, blockStack) || (block == startBlock && isWood && coord.getBlockMeta(world) % 4 == stack.getItemDamage() % 4)) {
                            loop(coord);
                        }
                    }
                }
            }
        }

        public Set<Coord4D> calc() {
            loop(location);
            return found;
        }

        public boolean checkID(Block b) {
            Block origBlock = location.getBlock(world);
            List<Block> ignored = ignoreBlocks.get(origBlock);
            return ignored == null ? b == origBlock : ignored.contains(b);
        }
    }

    @FunctionalInterface
    interface ItemUseConsumer {

        //Used to reference useHoe and useShovel via lambda references
        ActionResultType use(ItemStack stack, PlayerEntity player, World world, BlockPos pos, Direction facing);
    }
}