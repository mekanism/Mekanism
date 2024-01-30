package mekanism.common.registries;

import com.mojang.serialization.Codec;
import java.util.Objects;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.chemical.item.ChemicalTankContentsHandler;
import mekanism.common.capabilities.merged.GaugeDropperContentsHandler;
import mekanism.common.content.gear.ModuleContainer;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.gear.ItemAtomicDisassembler.DisassemblerMode;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.item.gear.ItemFreeRunners.FreeRunnerMode;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.AttachmentTypeDeferredRegister;
import net.neoforged.neoforge.attachment.AttachmentType;

public class MekanismAttachmentTypes {

    private MekanismAttachmentTypes() {
    }

    public static final AttachmentTypeDeferredRegister ATTACHMENT_TYPES = new AttachmentTypeDeferredRegister(Mekanism.MODID);

    //Note: We do not specify copy on death as we want radiation to reset to baseline on death
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Double>> RADIATION = ATTACHMENT_TYPES.register("radiation",
          () -> AttachmentType.builder(() -> RadiationManager.BASELINE)
                .serialize(Codec.doubleRange(RadiationManager.BASELINE, Double.MAX_VALUE))
                //Note: Technically this comparator is not needed as by default neo only checks for attachment compatability for item stacks,
                // but we set it regardless just so that if anyone is checking it for entities then they can bypass the serialization for it
                .comparator(Objects::equals)
                .build()
    );

    //Note: As we only attach this to items we don't need to make it copy on death
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<ModuleContainer>> MODULE_CONTAINER = ATTACHMENT_TYPES.register("module_container",
          () -> AttachmentType.serializable(ModuleContainer::create)
                .comparator(ModuleContainer::isCompatible)
                .build());

    //Non-serializable attachments for use in persisting a backing object between mutliple capabilities
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<ChemicalTankContentsHandler>> CHEMICAL_TANK_CONTENTS_HANDLER = ATTACHMENT_TYPES.register("chemical_tank_contents_handler",
          () -> AttachmentType.builder(ChemicalTankContentsHandler::createDummy).build());
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<GaugeDropperContentsHandler>> GAUGE_DROPPER_CONTENTS_HANDLER = ATTACHMENT_TYPES.register("gauge_dropper_contents_handler",
          () -> AttachmentType.builder(GaugeDropperContentsHandler::createDummy).build());

    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<DisassemblerMode>> DISASSEMBLER_MODE = ATTACHMENT_TYPES.register("disassembler_mode", DisassemblerMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<ConfiguratorMode>> CONFIGURATOR_MODE = ATTACHMENT_TYPES.register("configurator_mode", ConfiguratorMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<FlamethrowerMode>> FLAMETHROWER_MODE = ATTACHMENT_TYPES.register("flamethrower_mode", FlamethrowerMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<FreeRunnerMode>> FREE_RUNNER_MODE = ATTACHMENT_TYPES.register("free_runner_mode", FreeRunnerMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<JetpackMode>> JETPACK_MODE = ATTACHMENT_TYPES.register("jetpack_mode", JetpackMode.class);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> SCUBA_TANK_MODE = ATTACHMENT_TYPES.registerBoolean("scuba_tank_mode", false);
    public static final MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> ELECTRIC_BOW_MODE = ATTACHMENT_TYPES.registerBoolean("electric_bow_mode", false);
}