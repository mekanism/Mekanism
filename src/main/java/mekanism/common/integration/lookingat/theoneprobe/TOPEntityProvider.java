package mekanism.common.integration.lookingat.theoneprobe;

import mcjty.theoneprobe.api.IProbeHitEntityData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoEntityProvider;
import mcjty.theoneprobe.api.ProbeMode;
import mekanism.common.Mekanism;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.integration.lookingat.theoneprobe.TOPProvider.TOPLookingAtHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TOPEntityProvider implements IProbeInfoEntityProvider {

    static final TOPEntityProvider INSTANCE = new TOPEntityProvider();

    @Override
    public String getID() {
        return Mekanism.rl("entity_data").toString();
    }

    @Override
    public void addProbeEntityInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, Entity entity, IProbeHitEntityData data) {
        LookingAtUtils.addInfo(new TOPLookingAtHelper(info), entity);
    }
}