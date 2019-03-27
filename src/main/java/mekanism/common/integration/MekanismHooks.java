package mekanism.common.integration;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import dan200.computercraft.api.ComputerCraftAPI;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.Recipes;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import li.cil.oc.api.Driver;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.OreDictCache;
import mekanism.common.Resource;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType;
import mekanism.common.integration.computer.CCPeripheral;
import mekanism.common.integration.computer.OCDriver;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.recipe.RecipeHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.event.FMLInterModComms;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 *
 * @author AidanBrady
 */
public final class MekanismHooks {

    public static final String COFH_API_MOD_ID = "cofhapi";
    public static final String IC2_MOD_ID = "ic2";
    public static final String COMPUTERCRAFT_MOD_ID = "computercraft";
    public static final String APPLIED_ENERGISTICS_2_MOD_ID = "appliedenergistics2";
    public static final String TESLA_MOD_ID = "tesla";
    public static final String MCMULTIPART_MOD_ID = "mcmultipart";
    public static final String REDSTONEFLUX_MOD_ID = "redstoneflux";
    public static final String OPENCOMPUTERS_MOD_ID = "opencomputers";
    public static final String GALACTICRAFT_MOD_ID = "Galacticraft API";
    public static final String WAILA_MOD_ID = "Waila";
    public static final String BUILDCRAFT_MOD_ID = "BuildCraft";

    public boolean IC2Loaded = false;
    public boolean CCLoaded = false;
    public boolean AE2Loaded = false;
    public boolean TeslaLoaded = false;
    public boolean MCMPLoaded = false;
    public boolean RFLoaded = false;

    public void hook() {
        if (Loader.isModLoaded(IC2_MOD_ID)) {
            IC2Loaded = true;
        }
        if (Loader.isModLoaded(COMPUTERCRAFT_MOD_ID)) {
            CCLoaded = true;
        }
        if (Loader.isModLoaded(APPLIED_ENERGISTICS_2_MOD_ID)) {
            AE2Loaded = true;
        }
        if (Loader.isModLoaded(TESLA_MOD_ID)) {
            TeslaLoaded = true;
        }
        if (Loader.isModLoaded(MCMULTIPART_MOD_ID)) {
            MCMPLoaded = true;
        }
        if (Loader.isModLoaded(REDSTONEFLUX_MOD_ID)) {
            RFLoaded = true;
        }

        if (IC2Loaded) {
            hookIC2Recipes();
            Mekanism.logger.info("Hooked into IC2 successfully.");
        }

        if (CCLoaded) {
            loadCCPeripheralProviders();
        }

        if (AE2Loaded) {
            registerAE2Recipes();
        }

        if (Loader.isModLoaded("crafttweaker")) {
            CrafttweakerIntegration.registerCommands();
            CrafttweakerIntegration.applyRecipeChanges();
        }

        Wrenches.initialise();
    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void hookIC2Recipes() {
        for (MachineRecipe<IRecipeInput, Collection<ItemStack>> entry : Recipes.macerator.getRecipes()) {
            if (!entry.getInput().getInputs().isEmpty()) {
                if (!RecipeHandler.Recipe.CRUSHER.containsRecipe(entry.getInput().getInputs().get(0))) {
                    List<String> names = OreDictCache.getOreDictName(entry.getInput().getInputs().get(0));

                    for (String name : names) {
                        if (name.startsWith("ingot") || name.startsWith("crystal")) {
                            RecipeHandler.addCrusherRecipe(entry.getInput().getInputs().get(0),
                                  entry.getOutput().iterator().next());
                            break;
                        }
                    }
                }
            }
        }

        try {
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("oreOsmium"), null, false,
                  new ItemStack(MekanismItems.Dust, 2, Resource.OSMIUM.ordinal()));
        } catch (Exception ignored) {
        }

        try {
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotOsmium"), null, false,
                  new ItemStack(MekanismItems.Dust, 1, Resource.OSMIUM.ordinal()));
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotRefinedObsidian"), null, false,
                  new ItemStack(MekanismItems.OtherDust, 1, 5));
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotRefinedGlowstone"), null, false,
                  new ItemStack(Items.GLOWSTONE_DUST));
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotSteel"), null, false,
                  new ItemStack(MekanismItems.OtherDust, 1, 1));
        } catch (Exception ignored) {
        }

        try {
            for (Resource resource : Resource.values()) {
                Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("clump" + resource.getName()), null, false,
                      new ItemStack(MekanismItems.DirtyDust, 1, resource.ordinal()));
            }
        } catch (Exception ignored) {
        }
    }

    @Method(modid = COMPUTERCRAFT_MOD_ID)
    public void loadCCPeripheralProviders() {
        try {
            ComputerCraftAPI.registerPeripheralProvider(new CCPeripheral.CCPeripheralProvider());
        } catch (Exception ignored) {
        }
    }

    @Method(modid = OPENCOMPUTERS_MOD_ID)
    public void loadOCDrivers() {
        try {
            Driver.add(new OCDriver());
        } catch (Exception ignored) {
        }
    }

    public void addPulverizerRecipe(ItemStack input, ItemStack output, int energy) {
        NBTTagCompound nbtTags = new NBTTagCompound();

        nbtTags.setInteger("energy", energy);
        nbtTags.setTag("input", input.writeToNBT(new NBTTagCompound()));
        nbtTags.setTag("primaryOutput", output.writeToNBT(new NBTTagCompound()));

        FMLInterModComms.sendMessage("mekanism", "PulverizerRecipe", nbtTags);
    }

    public void registerAE2P2P() {
        for (TransmitterType type : TransmitterType.values()) {
            if (type.getTransmission().equals(TransmissionType.ITEM)) {
                FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-item",
                      new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
            } else if (type.getTransmission().equals(TransmissionType.FLUID)) {
                FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-fluid",
                      new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
            } else if (type.getTransmission().equals(TransmissionType.ENERGY)) {
                FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-fe-power",
                      new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
            }
        }
    }

    public void registerAE2Recipes() {
        try {
            IItems itemApi = AEApi.instance().definitions().items();
            IMaterials materialsApi = AEApi.instance().definitions().materials();
            IBlocks blocksApi = AEApi.instance().definitions().blocks();

            Optional<ItemStack> certusCrystal = materialsApi.certusQuartzCrystal().maybeStack(1);
            Optional<ItemStack> certusDust = materialsApi.certusQuartzDust().maybeStack(1);
            Optional<ItemStack> pureCertus = materialsApi.purifiedCertusQuartzCrystal().maybeStack(1);
            Optional<ItemStack> chargedCrystal = materialsApi.certusQuartzCrystalCharged().maybeStack(1);
            Optional<ItemStack> fluixCrystal = materialsApi.fluixCrystal().maybeStack(1);
            Optional<ItemStack> pureFluix = materialsApi.purifiedFluixCrystal().maybeStack(1);
            Optional<ItemStack> fluixDust = materialsApi.fluixDust().maybeStack(1);
            Optional<ItemStack> certusOre = blocksApi.quartzOre().maybeStack(1);
            Optional<ItemStack> chargedOre = blocksApi.quartzOreCharged().maybeStack(1);
            Optional<Item> crystalSeed = itemApi.crystalSeed().maybeItem();
            Optional<ItemStack> pureNether = materialsApi.purifiedNetherQuartzCrystal().maybeStack(1);

            if (certusCrystal.isPresent() && certusDust.isPresent()) {
                RecipeHandler.addCrusherRecipe(certusCrystal.get().copy(), certusDust.get().copy());
            }

            if (chargedCrystal.isPresent() && certusDust.isPresent()) {
                RecipeHandler.addCrusherRecipe(chargedCrystal.get().copy(), certusDust.get().copy());
            }

            if (fluixCrystal.isPresent() && fluixDust.isPresent()) {
                RecipeHandler.addCrusherRecipe(fluixCrystal.get().copy(), fluixDust.get().copy());
            }

            if (certusOre.isPresent() && certusCrystal.isPresent()) {
                ItemStack crystalOut = certusCrystal.get().copy();
                crystalOut.setCount(4);
                RecipeHandler.addEnrichmentChamberRecipe(certusOre.get().copy(), crystalOut);
            }

            if (chargedOre.isPresent() && chargedCrystal.isPresent()) {
                ItemStack crystalOut = chargedCrystal.get().copy();
                crystalOut.setCount(4);
                RecipeHandler.addEnrichmentChamberRecipe(chargedOre.get().copy(), crystalOut);
            }

            if (certusDust.isPresent() && pureCertus.isPresent()) {
                ItemStack crystalOut = pureCertus.get().copy();
                RecipeHandler.addEnrichmentChamberRecipe(certusDust.get().copy(), crystalOut);
            }

            if (fluixDust.isPresent() && pureFluix.isPresent()) {
                ItemStack crystalOut = pureFluix.get().copy();
                RecipeHandler.addEnrichmentChamberRecipe(fluixDust.get().copy(), crystalOut);
            }

            if (fluixCrystal.isPresent() && pureFluix.isPresent()) {
                RecipeHandler.addEnrichmentChamberRecipe(fluixCrystal.get().copy(), pureFluix.get().copy());
            }

            if (certusCrystal.isPresent() && pureCertus.isPresent()) {
                RecipeHandler.addEnrichmentChamberRecipe(certusCrystal.get().copy(), pureCertus.get().copy());
            }

            if (crystalSeed.isPresent()) {
                NonNullList<ItemStack> seeds = NonNullList.create();
                crystalSeed.get().getSubItems(CreativeTabs.SEARCH,
                      seeds);//there appears to be no way to get this via api, so fall back to unloc names
                for (ItemStack stack : seeds) {
                    String unloc = crystalSeed.get().getTranslationKey(stack);
                    if (unloc.endsWith("certus") && pureCertus.isPresent()) {
                        RecipeHandler.addEnrichmentChamberRecipe(stack, pureCertus.get().copy());
                    } else if (unloc.endsWith("nether") && pureNether.isPresent()) {
                        RecipeHandler.addEnrichmentChamberRecipe(stack, pureNether.get().copy());
                    } else if (unloc.endsWith("fluix") && pureFluix.isPresent()) {
                        RecipeHandler.addEnrichmentChamberRecipe(stack, pureFluix.get().copy());
                    }
                }
            }

        } catch (Exception e) {
            Mekanism.logger.error("Something went wrong with ae2 integration", e);
        } catch (IncompatibleClassChangeError e) {
            Mekanism.logger.error("AE2 api has changed unexpectedly", e);
        }
    }
}
