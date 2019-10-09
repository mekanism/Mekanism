package mekanism.common.capabilities.basic;

import mekanism.api.IConfigurable;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * Created by ben on 19/05/16.
 */
public class DefaultConfigurable implements IConfigurable {

    public static void register() {
        CapabilityManager.INSTANCE.register(IConfigurable.class, new NullStorage<>(), DefaultConfigurable::new);
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }
}