package mekanism.client;

import com.mojang.datafixers.util.Either;
import java.util.function.Predicate;
import mekanism.api.gear.IClientModuleHelper;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.gear.ModuleData;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.common.content.gear.HUDElement;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;

public class ClientModuleHelper implements IClientModuleHelper {

    @Override
    public IHUDElement hudElementEnabled(Identifier icon, boolean enabled) {
        return hudElement(icon, OnOff.caps(enabled, false).getTextComponent(), enabled ? HUDColor.REGULAR : HUDColor.FADED);
    }

    @Override
    public IHUDElement hudElementPercent(Identifier icon, double ratio) {
        return hudElement(icon, TextUtils.getPercent(ratio), ratio > 0.2 ? HUDColor.REGULAR : (ratio > 0.1 ? HUDColor.WARNING : HUDColor.DANGER));
    }

    @Override
    public IHUDElement hudElement(Identifier icon, Component text, HUDColor color) {
        return HUDElement.of(icon, text, HUDElement.HUDColor.from(color));
    }

    @Override
    public synchronized void addMekaSuitModuleModels(Identifier location) {
        MekanismModelCache.INSTANCE.registerMekaSuitModuleModel(location);
    }

    @Override
    public synchronized <AVATAR extends Avatar & ClientAvatarEntity> void addMekaSuitModuleModelSpec(String name, Holder<ModuleData<?>> moduleData, EquipmentSlot slotType, Predicate<Either<HumanoidRenderState, AVATAR>> isActive) {
        MekaSuitArmor.registerModule(name, moduleData, slotType, isActive);
    }
}