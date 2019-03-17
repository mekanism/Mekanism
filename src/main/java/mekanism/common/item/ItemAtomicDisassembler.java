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
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemAtomicDisassembler extends ItemEnergized {

    public double HOE_USAGE = 10 * general.DISASSEMBLER_USAGE;

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

        list.add(LangUtils.localize("tooltip.mode") + ": " + EnumColor.INDIGO + getModeName(itemstack));
        list.add(LangUtils.localize("tooltip.efficiency") + ": " + EnumColor.INDIGO + getEfficiency(itemstack));
    }

    @Override
    public boolean hitEntity(ItemStack itemstack, EntityLivingBase target, EntityLivingBase attacker) {
        if (getEnergy(itemstack) > 0) {
            if (attacker instanceof EntityPlayer) {
                target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), 20);
            } else {
                target.attackEntityFrom(DamageSource.causeMobDamage(attacker), 20);
            }

            setEnergy(itemstack, getEnergy(itemstack) - 2000);
        } else {
            if (attacker instanceof EntityPlayer) {
                target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), 4);
            } else {
                target.attackEntityFrom(DamageSource.causeMobDamage(attacker), 4);
            }

        }

        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, IBlockState state) {
        return getEnergy(itemstack) != 0 ? getEfficiency(itemstack) : 1F;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack itemstack, World world, IBlockState state, BlockPos pos,
          EntityLivingBase entityliving) {
        if (state.getBlockHardness(world, pos) != 0.0D) {
            setEnergy(itemstack, getEnergy(itemstack) - (general.DISASSEMBLER_USAGE * getEfficiency(itemstack)));
        } else {
            setEnergy(itemstack, getEnergy(itemstack) - (general.DISASSEMBLER_USAGE * (getEfficiency(itemstack)) / 2));
        }

        return true;
    }

    private RayTraceResult doRayTrace(IBlockState state, BlockPos pos, EntityPlayer player) {
        Vec3d positionEyes = player.getPositionEyes(1.0F);
        Vec3d playerLook = player.getLook(1.0F);
        double blockReachDistance = player.getAttributeMap().getAttributeInstance(EntityPlayer.REACH_DISTANCE)
              .getAttributeValue();
        Vec3d maxReach = positionEyes.add(playerLook.x * blockReachDistance, playerLook.y * blockReachDistance,
              playerLook.z * blockReachDistance);
        RayTraceResult res = state.collisionRayTrace(player.world, pos, playerLook, maxReach);
        //noinspection ConstantConditions - idea thinks it's nonnull due to package level annotations, but it's not
        return res != null ? res : new RayTraceResult(RayTraceResult.Type.MISS, Vec3d.ZERO, EnumFacing.UP, pos);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        super.onBlockStartBreak(itemstack, pos, player);

        if (!player.world.isRemote) {
            IBlockState state = player.world.getBlockState(pos);
            Block block = state.getBlock();
            int meta = block.getMetaFromState(state);

            if (block == Blocks.LIT_REDSTONE_ORE) {
                block = Blocks.REDSTONE_ORE;
            }

            RayTraceResult raytrace = doRayTrace(state, pos, player);
            ItemStack stack = block.getPickBlock(state, raytrace, player.world, pos, player);
            Coord4D orig = new Coord4D(pos, player.world);

            List<String> names = MekanismUtils.getOreDictName(stack);

            boolean isOre = false;

            for (String s : names) {
                if (s.startsWith("ore") || s.equals("logWood")) {
                    isOre = true;
                }
            }

            if (getMode(itemstack) == 3 && isOre && !player.capabilities.isCreativeMode) {
                Set<Coord4D> found = new Finder(player, stack, new Coord4D(pos, player.world), raytrace).calc();

                for (Coord4D coord : found) {
                    if (coord.equals(orig) || getEnergy(itemstack) < (general.DISASSEMBLER_USAGE * getEfficiency(
                          itemstack))) {
                        continue;
                    }

                    Block block2 = coord.getBlock(player.world);

                    block2.onBlockHarvested(player.world, coord.getPos(), state, player);
                    player.world.playEvent(null, 2001, coord.getPos(), Block.getStateId(state));
                    player.world.setBlockToAir(coord.getPos());
                    block2.breakBlock(player.world, coord.getPos(), state);
                    block2.dropBlockAsItem(player.world, coord.getPos(), state, 0);

                    setEnergy(itemstack,
                          getEnergy(itemstack) - (general.DISASSEMBLER_USAGE * getEfficiency(itemstack)));
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
                entityplayer.sendMessage(new TextComponentString(
                      EnumColor.DARK_BLUE + Mekanism.LOG_TAG + " " + EnumColor.GREY + LangUtils
                            .localize("tooltip.modeToggle")
                            + " " + EnumColor.INDIGO + getModeName(itemstack) + EnumColor.AQUA + " (" + getEfficiency(
                            itemstack) + ")"));
            }

            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        }

        return new ActionResult<>(EnumActionResult.PASS, itemstack);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side,
          float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        Block block = world.getBlockState(pos).getBlock();

        if (!player.isSneaking() && (block == Blocks.DIRT || block == Blocks.GRASS || block == Blocks.GRASS_PATH)) {
            if (useHoe(stack, player, world, pos, side) == EnumActionResult.FAIL) {
                return EnumActionResult.FAIL;
            }

            switch (getEfficiency(stack)) {
                case 20:
                    for (int x1 = -1; x1 <= +1; x1++) {
                        for (int z1 = -1; z1 <= +1; z1++) {
                            useHoe(stack, player, world, pos.add(x1, 0, z1), side);
                        }
                    }

                    break;
                case 128:
                    for (int x1 = -2; x1 <= +2; x1++) {
                        for (int z1 = -2; z1 <= +2; z1++) {
                            useHoe(stack, player, world, pos.add(x1, 0, z1), side);
                        }
                    }

                    break;
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @SuppressWarnings("incomplete-switch")
    private EnumActionResult useHoe(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos,
          EnumFacing facing) {
        if (!playerIn.canPlayerEdit(pos.offset(facing), facing, stack)) {
            return EnumActionResult.FAIL;
        } else {
            int hook = net.minecraftforge.event.ForgeEventFactory.onHoeUse(stack, playerIn, worldIn, pos);
            if (hook != 0) {
                return hook > 0 ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
            }

            IBlockState iblockstate = worldIn.getBlockState(pos);
            Block block = iblockstate.getBlock();

            if (facing != EnumFacing.DOWN && worldIn.isAirBlock(pos.up())) {
                if (block == Blocks.GRASS || block == Blocks.GRASS_PATH) {
                    setBlock(stack, playerIn, worldIn, pos, Blocks.FARMLAND.getDefaultState());
                    return EnumActionResult.SUCCESS;
                }

                if (block == Blocks.DIRT) {
                    switch (iblockstate.getValue(BlockDirt.VARIANT)) {
                        case DIRT:
                            setBlock(stack, playerIn, worldIn, pos, Blocks.FARMLAND.getDefaultState());
                            return EnumActionResult.SUCCESS;
                        case COARSE_DIRT:
                            setBlock(stack, playerIn, worldIn, pos, Blocks.DIRT.getDefaultState()
                                  .withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT));
                            return EnumActionResult.SUCCESS;
                    }
                }
            }

            return EnumActionResult.PASS;
        }
    }

    protected void setBlock(ItemStack stack, EntityPlayer player, World worldIn, BlockPos pos, IBlockState state) {
        worldIn.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);

        if (!worldIn.isRemote) {
            worldIn.setBlockState(pos, state, 11);
            stack.damageItem(1, player);
        }
    }

    public int getEfficiency(ItemStack itemStack) {
        switch (getMode(itemStack)) {
            case 0:
                return 20;
            case 1:
                return 8;
            case 2:
                return 128;
            case 3:
                return 20;
            case 4:
                return 0;
        }

        return 0;
    }

    public int getMode(ItemStack itemStack) {
        return ItemDataUtils.getInt(itemStack, "mode");
    }

    public String getModeName(ItemStack itemStack) {
        int mode = getMode(itemStack);

        switch (mode) {
            case 0:
                return LangUtils.localize("tooltip.disassembler.normal");
            case 1:
                return LangUtils.localize("tooltip.disassembler.slow");
            case 2:
                return LangUtils.localize("tooltip.disassembler.fast");
            case 3:
                return LangUtils.localize("tooltip.disassembler.vein");
            case 4:
                return LangUtils.localize("tooltip.disassembler.off");
        }

        return null;
    }

    public void toggleMode(ItemStack itemStack) {
        ItemDataUtils.setInt(itemStack, "mode", getMode(itemStack) < 4 ? getMode(itemStack) + 1 : 0);
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                  new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
        }

        return multimap;
    }

    public static class Finder {

        public static Map<Block, List<Block>> ignoreBlocks = new HashMap<>();

        static {
            ignoreBlocks.put(Blocks.REDSTONE_ORE, Arrays.asList(Blocks.REDSTONE_ORE, Blocks.LIT_REDSTONE_ORE));
            ignoreBlocks.put(Blocks.LIT_REDSTONE_ORE, Arrays.asList(Blocks.REDSTONE_ORE, Blocks.LIT_REDSTONE_ORE));
        }

        private final EntityPlayer player;
        public World world;
        public ItemStack stack;
        public Coord4D location;
        public Set<Coord4D> found = new HashSet<>();
        RayTraceResult rayTraceResult;
        private Block startBlock;

        public Finder(EntityPlayer p, ItemStack s, Coord4D loc, RayTraceResult traceResult) {
            player = p;
            world = p.world;
            stack = s;
            location = loc;
            startBlock = loc.getBlock(world);
            rayTraceResult = traceResult;
        }

        public void loop(Coord4D pointer) {
            if (found.contains(pointer) || found.size() > 128) {
                return;
            }

            found.add(pointer);

            for (EnumFacing side : EnumFacing.VALUES) {
                Coord4D coord = pointer.offset(side);

                ItemStack blockStack = coord.getBlock(world)
                      .getPickBlock(coord.getBlockState(world), rayTraceResult, world, coord.getPos(), player);

                if (coord.exists(world) && checkID(coord.getBlock(world)) && (stack.isItemEqual(blockStack) || (
                      coord.getBlock(world) == startBlock && MekanismUtils.getOreDictName(stack).contains("logWood")
                            && coord.getBlockMeta(world) % 4 == stack.getItemDamage() % 4))) {
                    loop(coord);
                }
            }
        }

        public Set<Coord4D> calc() {
            loop(location);

            return found;
        }

        public boolean checkID(Block b) {
            Block origBlock = location.getBlock(world);
            return (ignoreBlocks.get(origBlock) == null && b == origBlock) || (ignoreBlocks.get(origBlock) != null
                  && ignoreBlocks.get(origBlock).contains(b));
        }
    }
}
