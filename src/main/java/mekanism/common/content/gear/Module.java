package mekanism.common.content.gear;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.ModuleConfigItem.BooleanData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public abstract class Module {

    protected List<ModuleConfigItem<?>> configItems = new ArrayList<>();

    private String name;
    private ItemStack container;

    private ModuleConfigItem<Boolean> enabled;

    public void init(String name, ItemStack container) {
        this.name = name;
        this.container = container;

        init();
    }

    public void init() {
        enabled = addConfigItem(new ModuleConfigItem<>("enabled", MekanismLang.MODULE_ENABLED, new BooleanData()));
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
        for (ModuleConfigItem<?> item : configItems) {
            item.read(nbt);
        }
    }

    public final void write(CompoundNBT nbt) {
        for (ModuleConfigItem<?> item : configItems) {
            item.write(nbt);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setContainer(ItemStack container) {
        this.container = container;
    }

    protected ItemStack getContainer() {
        return container;
    }
}
