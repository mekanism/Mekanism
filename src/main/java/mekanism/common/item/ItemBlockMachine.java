package mekanism.common.item;

import cofh.redstoneflux.api.IEnergyContainerItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.api.energy.IEnergizedItem;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismClient;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.Upgrade;
import mekanism.common.base.FluidItemWrapper;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFluidItemWrapper;
import mekanism.common.base.IItemNetwork;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.ITierItem;
import mekanism.common.base.IUpgradeTile;
import mekanism.api.TileNetworkList;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.frequency.Frequency;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.forgeenergy.ForgeEnergyItemWrapper;
import mekanism.common.integration.ic2.IC2ItemManager;
import mekanism.common.integration.tesla.TeslaItemWrapper;
import mekanism.common.inventory.InventoryPersonalChest;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.tile.prefab.TileEntityElectricBlock;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Item class for handling multiple machine block IDs. 0:0: Enrichment Chamber 0:1: Osmium Compressor 0:2: Combiner 0:3:
 * Crusher 0:4: Digital Miner 0:5: Basic Factory 0:6: Advanced Factory 0:7: Elite Factory 0:8: Metallurgic Infuser 0:9:
 * Purification Chamber 0:10: Energized Smelter 0:11: Teleporter 0:12: Electric Pump 0:13: Personal Chest 0:14:
 * Chargepad 0:15: Logistical Sorter 1:0: Rotary Condensentrator 1:1: Chemical Oxidizer 1:2: Chemical Infuser 1:3:
 * Chemical Injection Chamber 1:4: Electrolytic Separator 1:5: Precision Sawmill 1:6: Chemical Dissolution Chamber 1:7:
 * Chemical Washer 1:8: Chemical Crystallizer 1:9: Seismic Vibrator 1:10: Pressurized Reaction Chamber 1:11: Portable
 * Tank 1:12: Fluidic Plenisher 1:13: Laser 1:14: Laser Amplifier 1:15: Laser Tractor Beam 2:0: Entangled Block 2:1:
 * Solar Neutron Activator 2:2: Ambient Accumulator 2:3: Oredictionificator 2:4: Resistive Heater 2:5: Formulaic
 * Assemblicator 2:6: Fuelwood Heater
 *
 * @author AidanBrady
 */
@InterfaceList({
      @Interface(iface = "cofh.redstoneflux.api.IEnergyContainerItem", modid = MekanismHooks.REDSTONEFLUX_MOD_ID),
      @Interface(iface = "ic2.api.item.ISpecialElectricItem", modid = MekanismHooks.IC2_MOD_ID)
})
public class ItemBlockMachine extends ItemBlock implements IEnergizedItem, ISpecialElectricItem, IFactory,
      ISustainedInventory, ISustainedTank, IEnergyContainerItem, IFluidItemWrapper, ITierItem, ISecurityItem,
      IItemNetwork {

    public Block metaBlock;

    public ItemBlockMachine(Block block) {
        super(block);
        metaBlock = block;
        setHasSubtypes(true);
        setNoRepair();
        setMaxStackSize(1);
    }

    @Override
    public int getMetadata(int i) {
        return i;
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack itemstack) {
        if (MachineType.get(itemstack) != null) {
            return getTranslationKey() + "." + MachineType.get(itemstack).blockName;
        }

        return "null";
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack itemstack) {
        MachineType type = MachineType.get(itemstack);

        if (type == MachineType.BASIC_FACTORY || type == MachineType.ADVANCED_FACTORY
              || type == MachineType.ELITE_FACTORY) {
            BaseTier tier = type == MachineType.BASIC_FACTORY ? BaseTier.BASIC
                  : (type == MachineType.ADVANCED_FACTORY ? BaseTier.ADVANCED : BaseTier.ELITE);

            if (I18n.canTranslate(
                  "tile." + tier.getSimpleName() + RecipeType.values()[getRecipeType(itemstack)].getTranslationKey()
                        + "Factory")) {
                return LangUtils.localize(
                      "tile." + tier.getSimpleName() + RecipeType.values()[getRecipeType(itemstack)].getTranslationKey()
                            + "Factory");
            }

            return tier.getLocalizedName() + " " + RecipeType.values()[getRecipeType(itemstack)].getLocalizedName()
                  + " " + super.getItemStackDisplayName(itemstack);
        } else if (type == MachineType.FLUID_TANK) {
            return LangUtils.localize("tile.FluidTank" + getBaseTier(itemstack).getSimpleName() + ".name");
        }

        return super.getItemStackDisplayName(itemstack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list,
          @Nonnull ITooltipFlag flag) {
        MachineType type = MachineType.get(itemstack);

        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            if (type == MachineType.FLUID_TANK) {
                FluidStack fluidStack = getFluidStack(itemstack);

                if (fluidStack != null) {
                    int amount = getFluidStack(itemstack).amount;
                    String amountStr = amount == Integer.MAX_VALUE ? LangUtils.localize("gui.infinite") : amount + "mB";
                    list.add(EnumColor.AQUA + LangUtils.localizeFluidStack(fluidStack) + ": " + EnumColor.GREY
                          + amountStr);
                } else {
                    list.add(EnumColor.DARK_RED + LangUtils.localize("gui.empty") + ".");
                }

                int cap = FluidTankTier.values()[getBaseTier(itemstack).ordinal()].storage;
                list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + (
                      cap == Integer.MAX_VALUE ? LangUtils.localize("gui.infinite") : cap + " mB"));
            }

            if (type == MachineType.QUANTUM_ENTANGLOPORTER) {
                Frequency.Identity freq = Frequency.Identity
                      .load(ItemDataUtils.getCompound(itemstack, "entangleporter_frequency"));

                if (freq != null) {
                    list.add(
                          EnumColor.INDIGO + LangUtils.localize("gui.frequency") + ": " + EnumColor.GREY + freq.name);
                    list.add(EnumColor.INDIGO + LangUtils.localize("gui.mode") + ": " + EnumColor.GREY + LangUtils
                          .localize("gui." + (!freq.publicFreq ? "private" : "public")));
                }
            }

            list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.INDIGO + GameSettings
                  .getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + LangUtils
                  .localize("tooltip.forDetails") + ".");
            list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings
                  .getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + LangUtils
                  .localize("tooltip.and") + " " + EnumColor.AQUA + GameSettings
                  .getKeyDisplayString(MekanismKeyHandler.modeSwitchKey.getKeyCode()) + EnumColor.GREY + " " + LangUtils
                  .localize("tooltip.forDesc") + ".");
        } else if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.modeSwitchKey)) {
            if (hasSecurity(itemstack)) {
                list.add(SecurityUtils.getOwnerDisplay(Minecraft.getMinecraft().player,
                      MekanismClient.clientUUIDMap.get(getOwnerUUID(itemstack))));
                list.add(EnumColor.GREY + LangUtils.localize("gui.security") + ": " + SecurityUtils
                      .getSecurityDisplay(itemstack, Side.CLIENT));

                if (SecurityUtils.isOverridden(itemstack, Side.CLIENT)) {
                    list.add(EnumColor.RED + "(" + LangUtils.localize("gui.overridden") + ")");
                }
            }

            if (type == MachineType.BASIC_FACTORY || type == MachineType.ADVANCED_FACTORY
                  || type == MachineType.ELITE_FACTORY) {
                list.add(
                      EnumColor.INDIGO + LangUtils.localize("tooltip.recipeType") + ": " + EnumColor.GREY + RecipeType
                            .values()[getRecipeType(itemstack)].getLocalizedName());
            }

            if (type == MachineType.FLUID_TANK) {
                list.add(
                      EnumColor.INDIGO + LangUtils.localize("tooltip.portableTank.bucketMode") + ": " + EnumColor.GREY
                            + LangUtils.transYesNo(getBucketMode(itemstack)));
            }

            if (type.isElectric) {
                list.add(EnumColor.BRIGHT_GREEN + LangUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY
                      + MekanismUtils.getEnergyDisplay(getEnergy(itemstack), getMaxEnergy(itemstack)));
            }

            if (hasTank(itemstack) && type != MachineType.FLUID_TANK) {
                FluidStack fluidStack = getFluidStack(itemstack);

                if (fluidStack != null) {
                    list.add(EnumColor.PINK + LangUtils.localizeFluidStack(fluidStack) + ": " + EnumColor.GREY
                          + getFluidStack(itemstack).amount + "mB");
                }
            }

            if (type != MachineType.CHARGEPAD && type != MachineType.LOGISTICAL_SORTER) {
                list.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY + LangUtils
                      .transYesNo(getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
            }

            if (type.supportsUpgrades && ItemDataUtils.hasData(itemstack, "upgrades")) {
                Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getDataMap(itemstack));

                for (Map.Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
                    list.add(
                          entry.getKey().getColor() + "- " + entry.getKey().getName() + (entry.getKey().canMultiply() ?
                                ": " + EnumColor.GREY + "x" + entry.getValue() : ""));
                }
            }
        } else {
            list.addAll(MekanismUtils.splitTooltip(type.getDescription(), itemstack));
        }
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, @Nonnull BlockPos pos, @Nonnull EnumHand hand,
          @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        MachineType type = MachineType.get(stack);

        if (type == MachineType.FLUID_TANK && getBucketMode(stack)) {
            return EnumActionResult.PASS;
        }

        return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world,
          @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState state) {
        boolean place = true;

        MachineType type = MachineType.get(stack);

        if (type == MachineType.DIGITAL_MINER) {
            BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();
            for (int xPos = -1; xPos <= +1; xPos++) {
                for (int yPos = 0; yPos <= +1; yPos++) {
                    for (int zPos = -1; zPos <= +1; zPos++) {
                        testPos.setPos(pos.getX() + xPos, pos.getY() + yPos, pos.getZ() + zPos);
                        Block b = world.getBlockState(testPos).getBlock();

                        if (!world.isValid(testPos) || !world.isBlockLoaded(testPos, false) || !b
                              .isReplaceable(world, testPos)) {
                            place = false;
                        }
                    }
                }
            }
        } else if (type == MachineType.SOLAR_NEUTRON_ACTIVATOR || type == MachineType.SEISMIC_VIBRATOR) {
            BlockPos abovePos = pos.up();
            if (!world.isValid(abovePos) || !world.getBlockState(abovePos).getBlock().isReplaceable(world, abovePos)) {
                place = false;
            }
        }

        if (place && super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state)) {
            TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);

            if (tileEntity instanceof TileEntityFluidTank) {
                TileEntityFluidTank tile = (TileEntityFluidTank) tileEntity;
                tile.tier = FluidTankTier.values()[getBaseTier(stack).ordinal()];
                tile.fluidTank.setCapacity(tile.tier.storage);
            }

            if (tileEntity instanceof ISecurityTile) {
                ISecurityTile security = (ISecurityTile) tileEntity;
                security.getSecurity().setOwnerUUID(getOwnerUUID(stack));

                if (hasSecurity(stack)) {
                    security.getSecurity().setMode(getSecurity(stack));
                }

                if (getOwnerUUID(stack) == null) {
                    security.getSecurity().setOwnerUUID(player.getUniqueID());
                }
            }

            if (tileEntity instanceof IUpgradeTile) {
                if (ItemDataUtils.hasData(stack, "upgrades")) {
                    ((IUpgradeTile) tileEntity).getComponent().read(ItemDataUtils.getDataMap(stack));
                }
            }

            if (tileEntity instanceof ISideConfiguration) {
                ISideConfiguration config = (ISideConfiguration) tileEntity;

                if (ItemDataUtils.hasData(stack, "sideDataStored")) {
                    config.getConfig().read(ItemDataUtils.getDataMap(stack));
                    config.getEjector().read(ItemDataUtils.getDataMap(stack));
                }
            }

            if (tileEntity instanceof ISustainedData) {
                if (stack.getTagCompound() != null) {
                    ((ISustainedData) tileEntity).readSustainedData(stack);
                }
            }

            if (tileEntity instanceof IRedstoneControl) {
                if (ItemDataUtils.hasData(stack, "controlType")) {
                    ((IRedstoneControl) tileEntity)
                          .setControlType(RedstoneControl.values()[ItemDataUtils.getInt(stack, "controlType")]);
                }
            }

            if (tileEntity instanceof TileEntityFactory) {
                TileEntityFactory factory = (TileEntityFactory) tileEntity;
                RecipeType recipeType = RecipeType.values()[getRecipeType(stack)];
                factory.setRecipeType(recipeType);
                world.notifyNeighborsOfStateChange(pos, tileEntity.getBlockType(), true);

                Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity),
                      tileEntity.getNetworkedData(new TileNetworkList())), new Range4D(Coord4D.get(tileEntity)));
            }

            if (tileEntity instanceof ISustainedTank) {
                if (hasTank(stack) && getFluidStack(stack) != null) {
                    ((ISustainedTank) tileEntity).setFluidStack(getFluidStack(stack));
                }
            }

            if (tileEntity instanceof ISustainedInventory) {
                ((ISustainedInventory) tileEntity).setInventory(getInventory(stack));
            }

            if (tileEntity instanceof TileEntityElectricBlock) {
                ((TileEntityElectricBlock) tileEntity).electricityStored = getEnergy(stack);
            }

            if (!world.isRemote && tileEntity instanceof TileEntityQuantumEntangloporter && ItemDataUtils
                  .hasData(stack, "entangleporter_frequency")) {
                Frequency.Identity freq = Frequency.Identity
                      .load(ItemDataUtils.getCompound(stack, "entangleporter_frequency"));

                if (freq != null) {
                    ((TileEntityQuantumEntangloporter) tileEntity).setFrequency(freq.name, freq.publicFreq);
                }
            }

            return true;
        }

        return false;
    }

    public boolean tryPlaceContainedLiquid(World world, ItemStack itemstack, BlockPos pos) {
        if (getFluidStack(itemstack) == null || !getFluidStack(itemstack).getFluid().canBePlacedInWorld()) {
            return false;
        } else {
            Material material = world.getBlockState(pos).getMaterial();
            boolean flag = !material.isSolid();

            if (!world.isAirBlock(pos) && !flag) {
                return false;
            } else {
                if (world.provider.doesWaterVaporize() && getFluidStack(itemstack).getFluid() == FluidRegistry.WATER) {
                    world.playSound(null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                          SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
                          2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

                    for (int l = 0; l < 8; l++) {
                        world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, pos.getX() + Math.random(),
                              pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
                    }
                } else {
                    if (!world.isRemote && flag && !material.isLiquid()) {
                        world.destroyBlock(pos, true);
                    }

                    world.setBlockState(pos,
                          MekanismUtils.getFlowingBlock(getFluidStack(itemstack).getFluid()).getDefaultState(), 3);
                }

                return true;
            }
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entityplayer, @Nonnull EnumHand hand) {
        ItemStack itemstack = entityplayer.getHeldItem(hand);
        MachineType type = MachineType.get(itemstack);

        if (MachineType.get(itemstack) == MachineType.PERSONAL_CHEST) {
            if (!world.isRemote) {
                if (getOwnerUUID(itemstack) == null) {
                    setOwnerUUID(itemstack, entityplayer.getUniqueID());
                }

                if (SecurityUtils.canAccess(entityplayer, itemstack)) {
                    InventoryPersonalChest inventory = new InventoryPersonalChest(entityplayer, hand);
                    MekanismUtils.openPersonalChestGui((EntityPlayerMP) entityplayer, null, inventory, false);
                } else {
                    SecurityUtils.displayNoAccess(entityplayer);
                }
            }
        } else if (type == MachineType.FLUID_TANK && getBucketMode(itemstack)) {
            if (SecurityUtils.canAccess(entityplayer, itemstack)) {
                RayTraceResult pos = rayTrace(world, entityplayer, !entityplayer.isSneaking());
                //It can be null if there is nothing in range
                if (pos != null && pos.typeOfHit == RayTraceResult.Type.BLOCK) {
                    Coord4D coord = new Coord4D(pos.getBlockPos(), world);

                    if (!world.provider.canMineBlock(entityplayer, coord.getPos())) {
                        return new ActionResult<>(EnumActionResult.FAIL, itemstack);
                    }

                    if (!entityplayer.isSneaking()) {
                        if (!entityplayer.canPlayerEdit(coord.getPos(), pos.sideHit, itemstack)) {
                            return new ActionResult<>(EnumActionResult.FAIL, itemstack);
                        }

                        FluidStack fluid = MekanismUtils.getFluid(world, coord, false);

                        if (fluid != null && (getFluidStack(itemstack) == null || getFluidStack(itemstack)
                              .isFluidEqual(fluid))) {
                            int needed = getCapacity(itemstack) - (getFluidStack(itemstack) != null ? getFluidStack(
                                  itemstack).amount : 0);

                            if (fluid.amount > needed) {
                                return new ActionResult<>(EnumActionResult.FAIL, itemstack);
                            }

                            if (getFluidStack(itemstack) == null) {
                                setFluidStack(fluid, itemstack);
                            } else {
                                FluidStack newStack = getFluidStack(itemstack);
                                newStack.amount += fluid.amount;
                                setFluidStack(newStack, itemstack);
                            }

                            world.setBlockToAir(coord.getPos());
                        }
                    } else {
                        FluidStack stored = getFluidStack(itemstack);

                        if (stored == null || stored.amount < Fluid.BUCKET_VOLUME) {
                            return new ActionResult<>(EnumActionResult.FAIL, itemstack);
                        }

                        Coord4D trans = coord.offset(pos.sideHit);

                        if (!entityplayer.canPlayerEdit(trans.getPos(), pos.sideHit, itemstack)) {
                            return new ActionResult<>(EnumActionResult.FAIL, itemstack);
                        }

                        if (tryPlaceContainedLiquid(world, itemstack, trans.getPos())
                              && !entityplayer.capabilities.isCreativeMode) {
                            FluidStack newStack = stored.copy();
                            newStack.amount -= Fluid.BUCKET_VOLUME;

                            setFluidStack(newStack.amount > 0 ? newStack : null, itemstack);
                        }
                    }
                }

                return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
            } else {
                SecurityUtils.displayNoAccess(entityplayer);
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, itemstack);
    }

    @Override
    public int getRecipeType(ItemStack itemStack) {
        if (itemStack.getTagCompound() == null) {
            return 0;
        }

        return itemStack.getTagCompound().getInteger("recipeType");
    }

    @Override
    public void setRecipeType(int type, ItemStack itemStack) {
        if (itemStack.getTagCompound() == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        itemStack.getTagCompound().setInteger("recipeType", type);
    }

    @Override
    public void setInventory(NBTTagList nbtTags, Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemDataUtils.setList((ItemStack) data[0], "Items", nbtTags);
        }
    }

    @Override
    public NBTTagList getInventory(Object... data) {
        if (data[0] instanceof ItemStack) {
            return ItemDataUtils.getList((ItemStack) data[0], "Items");
        }

        return null;
    }

    @Override
    public void setFluidStack(FluidStack fluidStack, Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) data[0];

            if (fluidStack == null || fluidStack.amount == 0 || fluidStack.getFluid() == null) {
                ItemDataUtils.removeData(itemStack, "fluidTank");
            } else {
                ItemDataUtils.setCompound(itemStack, "fluidTank", fluidStack.writeToNBT(new NBTTagCompound()));
            }
        }
    }

    @Override
    public FluidStack getFluidStack(Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) data[0];

            if (!ItemDataUtils.hasData(itemStack, "fluidTank")) {
                return null;
            }

            return FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "fluidTank"));
        }

        return null;
    }

    @Override
    public boolean hasTank(Object... data) {
        if (!(data[0] instanceof ItemStack) || !(((ItemStack) data[0]).getItem() instanceof ISustainedTank)) {
            return false;
        }

        MachineType type = MachineType.get((ItemStack) data[0]);

        return type == MachineType.ELECTRIC_PUMP || type == MachineType.FLUID_TANK
              || type == MachineType.FLUIDIC_PLENISHER;
    }

    public void setBucketMode(ItemStack itemStack, boolean bucketMode) {
        ItemDataUtils.setBoolean(itemStack, "bucketMode", bucketMode);
    }

    public boolean getBucketMode(ItemStack itemStack) {
        return ItemDataUtils.getBoolean(itemStack, "bucketMode");
    }

    @Override
    public double getEnergy(ItemStack itemStack) {
        if (!MachineType.get(itemStack).isElectric) {
            return 0;
        }

        return ItemDataUtils.getDouble(itemStack, "energyStored");
    }

    @Override
    public void setEnergy(ItemStack itemStack, double amount) {
        if (!MachineType.get(itemStack).isElectric) {
            return;
        }

        ItemDataUtils.setDouble(itemStack, "energyStored", Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0));
    }

    @Override
    public double getMaxEnergy(ItemStack itemStack) {
        MachineType machineType = MachineType
              .get(Block.getBlockFromItem(itemStack.getItem()), itemStack.getItemDamage());

        if (machineType.isFactory()) {
            // 200*tier.processes*recipeType.getEnergyUsage(); // From TileEntityFactory
            RecipeType recipeType = RecipeType.values()[getRecipeType(itemStack)];
            int tierProcess = machineType.factoryTier.processes;
            double baseMaxEnergy = 200 * tierProcess * recipeType.getEnergyUsage();

            return MekanismUtils.getMaxEnergy(itemStack, baseMaxEnergy);
        }

        return MekanismUtils.getMaxEnergy(itemStack, machineType.baseEnergy);
    }

    @Override
    public double getMaxTransfer(ItemStack itemStack) {
        return getMaxEnergy(itemStack) * 0.005;
    }

    @Override
    public boolean canReceive(ItemStack itemStack) {
        return MachineType.get(itemStack).isElectric;
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int receiveEnergy(ItemStack theItem, int energy, boolean simulate) {
        if (canReceive(theItem)) {
            double energyNeeded = getMaxEnergy(theItem) - getEnergy(theItem);
            double toReceive = Math.min(energy * general.FROM_RF, energyNeeded);

            if (!simulate) {
                setEnergy(theItem, getEnergy(theItem) + toReceive);
            }

            return (int) Math.round(toReceive * general.TO_RF);
        }

        return 0;
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int extractEnergy(ItemStack theItem, int energy, boolean simulate) {
        if (canSend(theItem)) {
            double energyRemaining = getEnergy(theItem);
            double toSend = Math.min((energy * general.FROM_RF), energyRemaining);

            if (!simulate) {
                setEnergy(theItem, getEnergy(theItem) - toSend);
            }

            return (int) Math.round(toSend * general.TO_RF);
        }

        return 0;
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int getEnergyStored(ItemStack theItem) {
        return (int) (getEnergy(theItem) * general.TO_RF);
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int getMaxEnergyStored(ItemStack theItem) {
        return (int) (getMaxEnergy(theItem) * general.TO_RF);
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public IElectricItemManager getManager(ItemStack itemStack) {
        return IC2ItemManager.getManager(this);
    }

    @Override
    public FluidStack getFluid(ItemStack container) {
        return getFluidStack(container);
    }

    @Override
    public int getCapacity(ItemStack container) {
        return FluidTankTier.values()[getBaseTier(container).ordinal()].storage;
    }

    @Override
    public int fill(ItemStack container, FluidStack resource, boolean doFill) {
        if (MachineType.get(container) == MachineType.FLUID_TANK && resource != null) {
            if (getBaseTier(container) == BaseTier.CREATIVE) {
                setFluidStack(PipeUtils.copy(resource, Integer.MAX_VALUE), container);
                return resource.amount;
            }

            FluidStack stored = getFluidStack(container);
            int toFill;

            if (stored != null && stored.getFluid() != resource.getFluid()) {
                return 0;
            }

            if (stored == null) {
                toFill = Math.min(resource.amount, getCapacity(container));
            } else {
                toFill = Math.min(resource.amount, getCapacity(container) - stored.amount);
            }

            if (doFill) {
                int fillAmount = toFill + (stored == null ? 0 : stored.amount);
                setFluidStack(PipeUtils.copy(resource, (stored != null ? stored.amount : 0) + toFill), container);
            }

            return toFill;
        }

        return 0;
    }

    @Override
    public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) {
        if (MachineType.get(container) == MachineType.FLUID_TANK) {
            FluidStack stored = getFluidStack(container);

            if (stored != null) {
                FluidStack toDrain = PipeUtils.copy(stored, Math.min(stored.amount, maxDrain));

                if (doDrain && getBaseTier(container) != BaseTier.CREATIVE) {
                    stored.amount -= toDrain.amount;
                    setFluidStack(stored.amount > 0 ? stored : null, container);
                }

                return toDrain;
            }
        }

        return null;
    }

    @Override
    public BaseTier getBaseTier(ItemStack itemstack) {
        if (!itemstack.hasTagCompound()) {
            return BaseTier.BASIC;
        }

        return BaseTier.values()[itemstack.getTagCompound().getInteger("tier")];
    }

    @Override
    public void setBaseTier(ItemStack itemstack, BaseTier tier) {
        if (!itemstack.hasTagCompound()) {
            itemstack.setTagCompound(new NBTTagCompound());
        }

        itemstack.getTagCompound().setInteger("tier", tier.ordinal());
    }

    @Override
    public UUID getOwnerUUID(ItemStack stack) {
        if (ItemDataUtils.hasData(stack, "ownerUUID")) {
            return UUID.fromString(ItemDataUtils.getString(stack, "ownerUUID"));
        }

        return null;
    }

    @Override
    public void setOwnerUUID(ItemStack stack, UUID owner) {
        if (owner == null) {
            ItemDataUtils.removeData(stack, "ownerUUID");
            return;
        }

        ItemDataUtils.setString(stack, "ownerUUID", owner.toString());
    }

    @Override
    public SecurityMode getSecurity(ItemStack stack) {
        if (!general.allowProtection) {
            return SecurityMode.PUBLIC;
        }

        return SecurityMode.values()[ItemDataUtils.getInt(stack, "security")];
    }

    @Override
    public void setSecurity(ItemStack stack, SecurityMode mode) {
        ItemDataUtils.setInt(stack, "security", mode.ordinal());
    }

    @Override
    public boolean hasSecurity(ItemStack stack) {
        MachineType type = MachineType.get(stack);

        return type != MachineType.LASER && type != MachineType.CHARGEPAD && type != MachineType.TELEPORTER
              && type != MachineType.QUANTUM_ENTANGLOPORTER;

    }

    @Override
    public boolean hasOwner(ItemStack stack) {
        return hasSecurity(stack);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new ItemCapabilityWrapper(stack, new TeslaItemWrapper(), new ForgeEnergyItemWrapper(),
              new FluidItemWrapper()) {
            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
                if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
                    return MachineType.get(itemStack) == MachineType.FLUID_TANK;
                }

                return super.hasCapability(capability, facing);
            }
        };
    }

    @Override
    public void handlePacketData(ItemStack stack, ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            if (MachineType.get(stack) == MachineType.FLUID_TANK) {
                boolean state = dataStream.readBoolean();
                setBucketMode(stack, state);
            }
        }
    }
}
