package mekanism.common.tile.laser;

import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.SupportsXpContainment;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.WorldUtils.DropsWithXp;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class TileEntityLaserTractorBeam extends TileEntityLaserReceptor implements SupportsXpContainment {

    @Nullable
    private Holder<Upgrade> xpContainmentUpgrade;
    private boolean clientHasContainmentUpgrade;
    private int storedExperience;

    public TileEntityLaserTractorBeam(BlockPos pos, BlockState state) {
        super(MekanismBlocks.LASER_TRACTOR_BEAM, pos, state);
    }

    @Override
    protected ISingleContainerHolder<IEnergyContainer> getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = VariableCapacityEnergyContainer.create(MachineEnergyContainer.validateBlock(this)::getStorage, BasicEnergyContainer.notExternal, BasicEnergyContainer.internalOnly, listener);
        return _ -> energyContainer;
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        for (int slotX = 0; slotX < 9; slotX++) {
            for (int slotY = 0; slotY < 3; slotY++) {
                OutputInventorySlot slot = OutputInventorySlot.at(listener, 8 + slotX * 18, 16 + slotY * 18);
                builder.addContainer(slot);
                slot.setSlotType(ContainerSlotType.NORMAL);
            }
        }
        return builder.build();
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (upgradesRegistry == null) {
            xpContainmentUpgrade = null;
        } else {
            //Note: We don't have to reset this on tag reload, as datapack registries do not support being reloaded without the server restarting
            xpContainmentUpgrade = upgradesRegistry.get(UpgradeIds.XP_CONTAINMENT).orElse(null);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level instanceof ServerLevel serverLevel && storedExperience > 0) {
            ExperienceOrb.award(serverLevel, Vec3.atCenterOf(pos), storedExperience);
        }
    }

    @Override
    public boolean hasXpContainment() {
        if (level != null && level.isClientSide()) {
            return clientHasContainmentUpgrade;
        }
        //If the xp containment upgrade has been removed as an upgrade, or if there is at least one installed
        return xpContainmentUpgrade == null || getUpgrades(xpContainmentUpgrade) > 0;
    }

    @Override
    public void popExperience(ServerPlayer player) {
        ExperienceOrb.award(player.level(), player.position(), storedExperience);
        storedExperience = 0;
    }

    @Override
    protected void handleBreakBlock(BlockState state, ServerLevel level, BlockPos hitPos, Player player, ItemStack tool, TransactionContext transaction) {
        DropsWithXp<ItemStack> dropsWithXp = WorldUtils.getDrops(state, level, hitPos, WorldUtils.getTileEntity(level, hitPos), player, tool);
        List<ItemStack> drops = dropsWithXp.items();
        int xp = dropsWithXp.experience();
        if (xp > 0 && hasXpContainment()) {
            int roomFor = Integer.MAX_VALUE - storedExperience;
            if (xp <= roomFor) {
                storedExperience += xp;
            } else {
                storedExperience = Integer.MAX_VALUE;
            }
            markForSave();
            xp = 0;
        }
        //Collect any extra drops that might have happened due to say breaking the top part of a door or flower and try to add them
        //Note: Technically we should just always return true rather than relying on the return result of the add method,
        // but as array lists always will return true as they are modified we don't have to worry about that
        Mekanism.worldTickHandler.fallbackItemCollector = drops::add;
        breakBlock(state, level, hitPos, tool, xp);
        Mekanism.worldTickHandler.fallbackItemCollector = null;
        if (!drops.isEmpty()) {
            BlockPos dropPos = null;
            Direction opposite = null;
            IMekanismResourceHandler<ItemResource, IInventorySlot> inventoryHandler = this::getInventorySlots;
            for (ItemStack drop : drops) {
                if (drop.isEmpty()) {//Not sure if this can ever be the case, but handle it just in case
                    continue;
                }
                int toInsert = drop.count();
                //Try inserting it first where it can stack and then into empty slots
                int inserted = inventoryHandler.insert(ItemResource.of(drop), toInsert, transaction, AutomationType.INTERNAL);
                if (inserted < toInsert) {
                    //If we have some drop left over that we couldn't fit, then spawn it into the world
                    // Note: We use an adjusted position and an opposite direction to provide the item with momentum towards the tractor beam
                    // so that even though we couldn't fit the items into our inventory we can still have them appear to be "pulled" to the tractor beam
                    if (dropPos == null) {
                        Direction direction = getDirection();
                        dropPos = worldPosition.relative(direction, 2);
                        opposite = direction.getOpposite();
                    }
                    Block.popResourceFromFace(level, dropPos, opposite, drop.copyWithCount(toInsert - inserted));
                }
            }
        }
    }

    @Override
    protected boolean handleHitItem(ItemEntity entity, TransactionContext transaction) {
        ItemStack stack = entity.getItem();
        //Try inserting it first where it can stack and then into empty slots
        IMekanismResourceHandler<ItemResource, IInventorySlot> inventoryHandler = this::getInventorySlots;
        int inserted = inventoryHandler.insert(ItemResource.of(stack), stack.count(), transaction, AutomationType.INTERNAL);
        if (inserted == stack.count()) {
            //If we have finished grabbing it all then remove the entity
            entity.discard();
            return true;
        }
        //If we couldn't fit it all, shrink how much of the item the entity is representing and let it continue processing
        stack.shrink(inserted);
        return super.handleHitItem(entity, transaction);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::hasXpContainment, value -> clientHasContainmentUpgrade = value));
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(SerializationConstants.EXPERIENCE, storedExperience);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedExperience = input.getIntOr(SerializationConstants.EXPERIENCE, 0);
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    int getSlotCount() {
        return getInventorySlots().size();
    }

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getItemInSlot", docPlaceholder = "amplifier slot")
    IInventorySlot getSlot(int slot) throws ComputerException {
        List<IInventorySlot> slots = getInventorySlots();
        if (slot < 0 || slot >= slots.size()) {
            throw new ComputerException("Slot: '%d' is out of bounds, as this laser amplifier only has '%d' slots (zero indexed).", slot, slots.size());
        }
        return slots.get(slot);
    }
    //End methods IComputerTile
}