package mekanism.common.integration.projecte;

import com.google.gson.JsonParseException;
import mekanism.common.integration.MekanismHooks;
import moze_intel.projecte.api.imc.IMCMethods;
import moze_intel.projecte.api.imc.NSSCreatorInfo;
import moze_intel.projecte.api.nss.NSSCreator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.fml.InterModComms;

//TODO: Convert various things ingredient representation to being able to also do it via tags?
//TODO: Factor energy into the equation of how much EMC something costs?
public class NSSHelper {

    private static final NSSCreator gasCreator = gasName -> {
        if (gasName.startsWith("#")) {
            return NSSGas.createTag(getResourceLocation(gasName.substring(1), "gas tag"));
        }
        return NSSGas.createGas(getResourceLocation(gasName, "gas"));
    };

    private static final NSSCreator infuseTypeCreator = infuseTypeName -> {
        if (infuseTypeName.startsWith("#")) {
            return NSSInfuseType.createTag(getResourceLocation(infuseTypeName.substring(1), "infuse type tag"));
        }
        return NSSInfuseType.createInfuseType(getResourceLocation(infuseTypeName, "infuse type"));
    };

    private static final NSSCreator pigmentCreator = pigmentName -> {
        if (pigmentName.startsWith("#")) {
            return NSSPigment.createTag(getResourceLocation(pigmentName.substring(1), "pigment tag"));
        }
        return NSSPigment.createPigment(getResourceLocation(pigmentName, "pigment"));
    };

    private static final NSSCreator slurryCreator = slurryName -> {
        if (slurryName.startsWith("#")) {
            return NSSSlurry.createTag(getResourceLocation(slurryName.substring(1), "slurry tag"));
        }
        return NSSSlurry.createSlurry(getResourceLocation(slurryName, "slurry"));
    };

    public static void init() {
        register("GAS", gasCreator);
        register("INFUSE_TYPE", infuseTypeCreator);
        register("PIGMENT", pigmentCreator);
        register("SLURRY", slurryCreator);
    }

    private static void register(String key, NSSCreator creator) {
        InterModComms.sendTo(MekanismHooks.PROJECTE_MOD_ID, IMCMethods.REGISTER_NSS_SERIALIZER, () -> new NSSCreatorInfo(key, creator));
    }

    private static ResourceLocation getResourceLocation(String s, String type) throws JsonParseException {
        try {
            return new ResourceLocation(s);
        } catch (ResourceLocationException e) {
            throw new JsonParseException("Malformed " + type + " ID", e);
        }
    }
}