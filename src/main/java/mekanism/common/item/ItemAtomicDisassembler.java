package mekanism.common.item;

import com.google.common.collect.Multimap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDirt.DirtType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.WorldEvents;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemAtomicDisassembler extends ItemEnergized {

    public ItemAtomicDisassembler() {
        super(1000000);
    }

    @Override
    public boolean canHarvestBlock(@Nonnull IBlockState state, ItemStack stack) {
        return state.getBlock() != Blocks.BEDROCK;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        super.addInformation(itemstack, world, list, flag);
        Mode mode = getMode(itemstack);
        list.add(LangUtils.localize("tooltip.mode") + ": " + EnumColor.INDIGO + mode.getModeName());
        list.add(LangUtils.localize("tooltip.efficiency") + ": " + EnumColor.INDIGO + mode.getEfficiency());
    }

    @Override
    public boolean hitEntity(ItemStack itemstack, EntityLivingBase target, EntityLivingBase attacker) {
        double energy = getEnergy(itemstack);
        //TODO: Damage should really be scaled if we do not have the full power needed
        float damage = energy > 0 ? 20 : 4;
        if (attacker instanceof EntityPlayer) {
            target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), damage);
        } else {
            target.attackEntityFrom(DamageSource.causeMobDamage(attacker), damage);
        }
        if (energy > 0) {
            setEnergy(itemstack, energy - 2000);
        }
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, IBlockState state) {
        return getEnergy(itemstack) != 0 ? getMode(itemstack).getEfficiency() : 1F;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack itemstack, World world, IBlockState state, BlockPos pos, EntityLivingBase entityliving) {
        if (state.getBlockHardness(world, pos) == 0.0D) {
            setEnergy(itemstack, getEnergy(itemstack) - getDestroyEnergy(itemstack) / 2F);
        } else {
            setEnergy(itemstack, getEnergy(itemstack) - getDestroyEnergy(itemstack));
        }
        return true;
    }

    private RayTraceResult doRayTrace(IBlockState state, BlockPos pos, EntityPlayer player) {
        Vec3d positionEyes = player.getPositionEyes(1.0F);
        Vec3d playerLook = player.getLook(1.0F);
        double blockReachDistance = player.getAttributeMap().getAttributeInstance(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        Vec3d maxReach = positionEyes.add(playerLook.x * blockReachDistance, playerLook.y * blockReachDistance, playerLook.z * blockReachDistance);
        RayTraceResult res = state.collisionRayTrace(player.world, pos, playerLook, maxReach);
        //noinspection ConstantConditions - idea thinks it's nonnull due to package level annotations, but it's not
        return res != null ? res : new RayTraceResult(RayTraceResult.Type.MISS, Vec3d.ZERO, EnumFacing.UP, pos);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        super.onBlockStartBreak(itemstack, pos, player);
        if (!player.world.isRemote&& !player.capabilities.isCreativeMode && getMode(itemstack) == Mode.VEIN) {
            IBlockState state = player.world.getBlockState(pos);
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
            if (isOre) {
                Coord4D orig = new Coord4D(pos, player.world);
                Set<Coord4D> found = new Finder(player, stack, orig, raytrace).calc();
                int destroyEnergy = getDestroyEnergy(itemstack);
                for (Coord4D coord : found) {
                    if (coord.equals(orig) || getEnergy(itemstack) < destroyEnergy) {
                        continue;
                    }
                    Block block2 = coord.getBlock(player.world);
                    block2.onBlockHarvested(player.world, coord.getPos(), state, player);
                    player.world.playEvent(WorldEvents.BREAK_BLOCK_EFFECTS, coord.getPos(), Block.getStateId(state));
                    player.world.setBlockToAir(coord.getPos());
                    block2.breakBlock(player.world, coord.getPos(), state);
                    block2.dropBlockAsItem(player.world, coord.getPos(), state, 0);
                    setEnergy(itemstack, getEnergy(itemstack) - getDestroyEnergy(itemstack));
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
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entityplayer, @Nonnull EnumHand hand) {
        ItemStack itemstack = entityplayer.getHeldItem(hand);
        if (entityplayer.isSneaking()) {
            if (!world.isRemote) {
                toggleMode(itemstack);
                Mode mode = getMode(itemstack);
                entityplayer.sendMessage(new TextComponentString(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + " " + EnumColor.GREY + LangUtils.localize("tooltip.modeToggle")
                                                                 + " " + EnumColor.INDIGO + mode.getModeName() + EnumColor.AQUA + " (" + mode.getEfficiency() + ")"));
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        }
        return new ActionResult<>(EnumActionResult.PASS, itemstack);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) {
            Block block = world.getBlockState(pos).getBlock();
            if (block == Blocks.DIRT || block == Blocks.GRASS_PATH) {
                return useItemAs(player, world, pos, hand, side, this::useHoe);
            } else if (block == Blocks.GRASS) {
                return useItemAs(player, world, pos, hand, side, this::useShovel);
            }
        }
        return EnumActionResult.PASS;
    }

    private EnumActionResult useItemAs(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, ItemUseConsumer consumer) {
        ItemStack stack = player.getHeldItem(hand);
        if (consumer.use(stack, player, world, pos, side) == EnumActionResult.FAIL) {
            return EnumActionResult.FAIL;
        }
        int efficiency = getMode(stack).getEfficiency();
        //TODO: Let radius eventually be defined in the enum
        int radius = efficiency == 20 ? 1 : efficiency == 128 ? 2 : 0;
        if (radius > 0) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x != 0 || z != 0) {
                        //Don't attempt to use it on the source location as it was already done above
                        consumer.use(stack, player, world, pos.add(x, 0, z), side);
                    }
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }

    private EnumActionResult useHoe(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing facing) {
        if (!player.canPlayerEdit(pos.offset(facing), facing, stack)) {
            return EnumActionResult.FAIL;
        }
        int hook = ForgeEventFactory.onHoeUse(stack, player, world, pos);
        if (hook != 0) {
            return hook > 0 ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
        }
        if (facing != EnumFacing.DOWN && world.isAirBlock(pos.up())) {
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (block == Blocks.GRASS || block == Blocks.GRASS_PATH) {
                return setBlock(stack, player, world, pos, Blocks.FARMLAND.getDefaultState());
            } else if (block == Blocks.DIRT) {
                DirtType type = state.getValue(BlockDirt.VARIANT);
                if (type == DirtType.DIRT) {
                    return setBlock(stack, player, world, pos, Blocks.FARMLAND.getDefaultState());
                } else if (type == DirtType.COARSE_DIRT) {
                    return setBlock(stack, player, world, pos, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, DirtType.DIRT));
                }
            }
        }
        return EnumActionResult.PASS;
    }

    private EnumActionResult useShovel(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing facing) {
        if (!player.canPlayerEdit(pos.offset(facing), facing, stack)) {
            return EnumActionResult.FAIL;
        } else if (facing != EnumFacing.DOWN && world.isAirBlock(pos.up())) {
            Block block = world.getBlockState(pos).getBlock();
            if (block == Blocks.GRASS) {
                return setBlock(stack, player, world, pos, Blocks.GRASS_PATH.getDefaultState());
            }
        }
        return EnumActionResult.PASS;
    }

    private EnumActionResult setBlock(ItemStack stack, EntityPlayer player, World worldIn, BlockPos pos, IBlockState state) {
        worldIn.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (!worldIn.isRemote) {
            worldIn.setBlockState(pos, state, 11);
            stack.damageItem(1, player);
        }
        return EnumActionResult.SUCCESS;
    }

    private int getHoeEnergy() {
        return 10 * MekanismConfig.current().general.DISASSEMBLER_USAGE.val();
    }

    private int getDestroyEnergy(ItemStack itemStack) {
        return MekanismConfig.current().general.DISASSEMBLER_USAGE.val() * getMode(itemStack).getEfficiency();
    }

    public Mode getMode(ItemStack itemStack) {
        Mode[] values = Mode.values();
        //If it is out of bounds just shift it as if it had gone around that many times
        int mode = ItemDataUtils.getInt(itemStack, "mode") % values.length;
        return values[mode];
    }

    public void toggleMode(ItemStack itemStack) {
        Mode mode = getMode(itemStack);
        ItemDataUtils.setInt(itemStack, "mode", mode.ordinal() + 1 < Mode.values().length ? mode.ordinal() + 1 : 0);
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multiMap = super.getItemAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multiMap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
        }
        return multiMap;
    }

    public enum Mode {
        NORMAL("normal", 20),
        SLOW("slow", 8),
        FAST("fast", 128),
        VEIN("vein", 20),
        OFF("off", 0);

        private final String mode;
        private final int efficiency;

        Mode(String mode, int efficiency) {
            this.mode = mode;
            this.efficiency = efficiency;
        }

        public String getModeName() {
            return LangUtils.localize("tooltip.disassembler." + mode);
        }

        public int getEfficiency() {
            return efficiency;
        }
    }

    public static class Finder {

        public static Map<Block, List<Block>> ignoreBlocks = new HashMap<>();

        static {
            ignoreBlocks.put(Blocks.REDSTONE_ORE, Arrays.asList(Blocks.REDSTONE_ORE, Blocks.LIT_REDSTONE_ORE));
            ignoreBlocks.put(Blocks.LIT_REDSTONE_ORE, Arrays.asList(Blocks.REDSTONE_ORE, Blocks.LIT_REDSTONE_ORE));
        }

        private final EntityPlayer player;
        public final World world;
        public final ItemStack stack;
        public final Coord4D location;
        public final Set<Coord4D> found = new HashSet<>();
        private final RayTraceResult rayTraceResult;
        private final Block startBlock;
        private final boolean isWood;

        public Finder(EntityPlayer p, ItemStack s, Coord4D loc, RayTraceResult traceResult) {
            player = p;
            world = p.world;
            stack = s;
            location = loc;
            startBlock = loc.getBlock(world);
            rayTraceResult = traceResult;
            isWood = OreDictCache.getOreDictName(stack).contains("logWood");
        }

        public void loop(Coord4D pointer) {
            if (found.contains(pointer) || found.size() > 128) {
                return;
            }
            found.add(pointer);
            for (EnumFacing side : EnumFacing.VALUES) {
                Coord4D coord = pointer.offset(side);
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
        EnumActionResult use(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing facing);
    }
}