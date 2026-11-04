package mekanism.client.integration.nerb;

import it.hurts.shatterbyte.nerb.common.config.NERBClientConfig.ButtonMode;
import it.hurts.shatterbyte.nerb.common.init.ConfigRegistry;
import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Mekanism.MODID, dist = Dist.CLIENT, depends = MekanismHooks.NERB_MOD_ID)
public class MekanismNerb {

    public MekanismNerb() {
        Mekanism.hooks.recipeBookDisabledChecks.add(() -> ConfigRegistry.CLIENT.getButtonMode() != ButtonMode.RECIPE_BOOK);
    }
}