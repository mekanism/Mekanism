package mekanism.common.content.gear;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.ModuleConfigItem.BooleanData;
import mekanism.common.content.gear.Modules.ModuleData;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public abstract class Module {

    protected List<ModuleConfigItem<?>> configItems = new ArrayList<>();

    private ModuleData<?> data;
    private ItemStack container;

    private ModuleConfigItem<Boolean> enabled;
    private int installed = 1;

    public void init(ModuleData<?> data, ItemStack container) {
        this.data = data;
        this.container = container;

        init();
    }

    public void init() {
        enabled = addConfigItem(new ModuleConfigItem<>(this, "enabled", MekanismLang.MODULE_ENABLED, new BooleanData()));
    }

    protected <T> ModuleConfigItem<T> addConfigItem(ModuleConfigItem<T> item) {
        configItems.add(item);
        return item;
    }

    public void tick(PlayerEntity player) {
        if (isEnabled()) {
            if (!player.world.isRemote()) {
                tickServer(player);
            }
        }
    }

    protected void tickServer(PlayerEntity player) {}

    public final void read(CompoundNBT nbt) {
        installed = nbt.getInt(NBTConstants.AMOUNT);
        for (ModuleConfigItem<?> item : configItems) {
            item.read(nbt);
        }
    }

    /**
     * Save this module on the container ItemStack. Will create proper NBT structure if it does not yet exist.
     * @param callback - will run after the NBT data is saved
     */
    public final void save(Runnable callback) {
        CompoundNBT modulesTag = ItemDataUtils.getCompound(container, NBTConstants.MODULES);
        CompoundNBT nbt = modulesTag.getCompound(data.getName());

        nbt.putInt(NBTConstants.AMOUNT, installed);
        for (ModuleConfigItem<?> item : configItems) {
            item.write(nbt);
        }

        modulesTag.put(data.getName(), nbt);
        ItemDataUtils.setCompound(container, NBTConstants.MODULES, modulesTag);

        if (callback != null) {
            callback.run();
        }
    }

    public String getName() {
        return data.getName();
    }

    public ModuleData<?> getData() {
        return data;
    }

    public int getInstalledCount() {
        return installed;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    protected ItemStack getContainer() {
        return container;
    }

    public List<ModuleConfigItem<?>> getConfigItems() {
        return configItems;
    }
}
