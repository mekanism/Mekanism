---
built_in_tables:
  mekanism.api.chemical.ChemicalStack:
    description: An amount of Gas/Fluid/Slurry/Pigment
    fields:
      amount:
        description: The amount in mB
        java_type: int
        type: Number (int)
      name:
        description: The Chemical's registered name
        java_type: net.minecraft.world.item.Item
        type: String (Item)
    human_name: Table (ChemicalStack)
  mekanism.common.content.filter.IFilter:
    description: |-
      Common Filter properties. Use the API Global to make constructing these a little easier.
      Filters are a combination of these base properties, an ItemStack or Mod Id or Tag component, and a device specific type.
      The exception to that is an Oredictionificator filter, which does not have an item/mod/tag component.
    fields:
      enabled:
        description: Whether the filter is enabled when added to a device
        java_type: boolean
        type: boolean
      type:
        description: The type of filter in this structure
        java_type: mekanism.common.content.filter.FilterType
        type: String (FilterType)
    human_name: Table (IFilter)
  mekanism.common.content.miner.MinerFilter:
    description: A Digital Miner filter
    extends: mekanism.common.content.filter.IFilter
    fields:
      replace_target:
        description: The name of the item block that will be used to replace a mined
          block
        java_type: net.minecraft.world.item.Item
        type: String (Item)
      requires_replacement:
        description: Whether the filter requires a replacement to be done before it
          will allow mining
        java_type: boolean
        type: boolean
    human_name: Table (MinerFilter)
  mekanism.common.content.miner.MinerItemStackFilter:
    description: Digital Miner filter with ItemStack filter properties
    extends: mekanism.common.content.miner.MinerFilter
    fields:
      components:
        description: The Component data of the filtered item, optional
        java_type: java.lang.String
        type: String
      item:
        description: The filtered item's registered name
        java_type: net.minecraft.world.item.Item
        type: String (Item)
    human_name: Table (MinerItemStackFilter)
  mekanism.common.content.miner.MinerModIDFilter:
    description: Digital Miner filter with Mod Id filter properties
    extends: mekanism.common.content.miner.MinerFilter
    fields:
      modid:
        description: The mod id to filter. e.g. mekanism
        java_type: java.lang.String
        type: String
    human_name: Table (MinerModIDFilter)
  mekanism.common.content.miner.MinerTagFilter:
    description: Digital Miner filter with Tag filter properties
    extends: mekanism.common.content.miner.MinerFilter
    fields:
      tag:
        description: The tag to filter. e.g. c:ores
        java_type: java.lang.String
        type: String
    human_name: Table (MinerTagFilter)
  mekanism.common.content.oredictionificator.OredictionificatorItemFilter:
    description: An Oredictionificator filter
    extends: mekanism.common.content.filter.IFilter
    fields:
      selected:
        description: The selected output item's registered name. Optional for adding
          a filter
        java_type: net.minecraft.world.item.Item
        type: String (Item)
      target:
        description: The target tag to match (input)
        java_type: java.lang.String
        type: String
    human_name: Table (OredictionificatorItemFilter)
  mekanism.common.content.qio.filter.QIOFilter:
    description: A Quantum Item Orchestration filter
    extends: mekanism.common.content.filter.IFilter
    human_name: Table (QIOFilter)
  mekanism.common.content.qio.filter.QIOItemStackFilter:
    description: QIO filter with ItemStack filter properties
    extends: mekanism.common.content.qio.filter.QIOFilter
    fields:
      components:
        description: The Component data of the filtered item, optional
        java_type: java.lang.String
        type: String
      fuzzy:
        description: Whether Fuzzy mode is enabled (checks only the item name/type)
        java_type: boolean
        type: boolean
      item:
        description: The filtered item's registered name
        java_type: net.minecraft.world.item.Item
        type: String (Item)
    human_name: Table (QIOItemStackFilter)
  mekanism.common.content.qio.filter.QIOModIDFilter:
    description: QIO filter with Mod Id filter properties
    extends: mekanism.common.content.qio.filter.QIOFilter
    fields:
      modid:
        description: The mod id to filter. e.g. mekanism
        java_type: java.lang.String
        type: String
    human_name: Table (QIOModIDFilter)
  mekanism.common.content.qio.filter.QIOTagFilter:
    description: QIO filter with Tag filter properties
    extends: mekanism.common.content.qio.filter.QIOFilter
    fields:
      tag:
        description: The tag to filter. e.g. c:ores
        java_type: java.lang.String
        type: String
    human_name: Table (QIOTagFilter)
  mekanism.common.content.transporter.SorterFilter:
    description: A Logistical Sorter filter
    extends: mekanism.common.content.filter.IFilter
    fields:
      allow_default:
        description: Allows the filtered item to travel to the default color destination
        java_type: boolean
        type: boolean
      color:
        description: The color configured, nil if none
        java_type: mekanism.api.text.EnumColor
        type: String (EnumColor)
      max:
        description: In Size Mode, the maximum to send
        java_type: int
        type: Number (int)
      min:
        description: In Size Mode, the minimum to send
        java_type: int
        type: Number (int)
      size:
        description: If Size Mode is enabled
        java_type: boolean
        type: boolean
    human_name: Table (SorterFilter)
  mekanism.common.content.transporter.SorterItemStackFilter:
    description: Logistical Sorter filter with ItemStack filter properties
    extends: mekanism.common.content.transporter.SorterFilter
    fields:
      components:
        description: The Component data of the filtered item, optional
        java_type: java.lang.String
        type: String
      fuzzy:
        description: Whether Fuzzy mode is enabled (checks only the item name/type)
        java_type: boolean
        type: boolean
      item:
        description: The filtered item's registered name
        java_type: net.minecraft.world.item.Item
        type: String (Item)
    human_name: Table (SorterItemStackFilter)
  mekanism.common.content.transporter.SorterModIDFilter:
    description: Logistical Sorter filter with Mod Id filter properties
    extends: mekanism.common.content.transporter.SorterFilter
    fields:
      modid:
        description: The mod id to filter. e.g. mekanism
        java_type: java.lang.String
        type: String
    human_name: Table (SorterModIDFilter)
  mekanism.common.content.transporter.SorterTagFilter:
    description: Logistical Sorter filter with Tag filter properties
    extends: mekanism.common.content.transporter.SorterFilter
    fields:
      tag:
        description: The tag to filter. e.g. c:ores
        java_type: java.lang.String
        type: String
    human_name: Table (SorterTagFilter)
  mekanism.common.lib.frequency.Frequency:
    description: A frequency's identity
    fields:
      key:
        description: Usually the name of the frequency entered in the GUI
        java_type: java.lang.String
        type: String
      owner:
        description: The UUID for the owner of the Frequency
        java_type: java.util.UUID
        type: String (UUID)
      security_mode:
        description: Whether the Frequency is public, trusted, or private
        java_type: mekanism.api.security.SecurityMode
        type: String (SecurityMode)
    human_name: Table (Frequency)
  net.minecraft.core.BlockPos:
    description: An xyz position
    fields:
      x:
        description: The x component
        java_type: int
        type: Number (int)
      y:
        description: The y component
        java_type: int
        type: Number (int)
      z:
        description: The z component
        java_type: int
        type: Number (int)
    human_name: Table (BlockPos)
  net.minecraft.core.GlobalPos:
    description: An xyz position with a dimension component
    fields:
      dimension:
        description: The dimension component
        java_type: net.minecraft.resources.ResourceLocation
        type: String (ResourceLocation)
      x:
        description: The x component
        java_type: int
        type: Number (int)
      y:
        description: The y component
        java_type: int
        type: Number (int)
      z:
        description: The z component
        java_type: int
        type: Number (int)
    human_name: Table (GlobalPos)
  net.minecraft.world.item.ItemStack:
    description: A stack of Item(s)
    fields:
      components:
        description: Any non default components of the item, in Command JSON format
        java_type: java.lang.String
        type: String
      count:
        description: The count of items in the stack
        java_type: int
        type: Number (int)
      name:
        description: The Item's registered name
        java_type: net.minecraft.world.item.Item
        type: String (Item)
    human_name: Table (ItemStack)
  net.minecraft.world.level.block.state.BlockState:
    description: A Block State
    fields:
      block:
        description: The Block's registered name, e.g. minecraft:sand
        java_type: java.lang.String
        type: String
      state:
        description: Any state parameters will be in Table format under this key.
          Not present if there are none
        java_type: java.util.Map
        type: Table
    human_name: Table (BlockState)
  net.neoforged.neoforge.fluids.FluidStack:
    description: An amount of fluid
    fields:
      amount:
        description: The amount in mB
        java_type: int
        type: Number (int)
      components:
        description: Any non default components of the fluid, in Command JSON format
        java_type: java.lang.String
        type: String
      name:
        description: The Fluid's registered name, e.g. minecraft:water
        java_type: net.minecraft.resources.ResourceLocation
        type: String (ResourceLocation)
    human_name: Table (FluidStack)
enums:
  mekanism.api.RelativeSide:
  - FRONT
  - LEFT
  - RIGHT
  - BACK
  - TOP
  - BOTTOM
  mekanism.api.Upgrade:
  - SPEED
  - ENERGY
  - FILTER
  - CHEMICAL
  - MUFFLING
  - ANCHOR
  - STONE_GENERATOR
  mekanism.api.security.SecurityMode:
  - PUBLIC
  - PRIVATE
  - TRUSTED
  mekanism.api.text.EnumColor:
  - BLACK
  - DARK_BLUE
  - DARK_GREEN
  - DARK_AQUA
  - DARK_RED
  - PURPLE
  - ORANGE
  - GRAY
  - DARK_GRAY
  - INDIGO
  - BRIGHT_GREEN
  - AQUA
  - RED
  - PINK
  - YELLOW
  - WHITE
  - BROWN
  - BRIGHT_PINK
  mekanism.common.block.attribute.AttributeStateBoilerValveMode$BoilerValveMode:
  - INPUT
  - OUTPUT_STEAM
  - OUTPUT_COOLANT
  mekanism.common.content.filter.FilterType:
  - MINER_ITEMSTACK_FILTER
  - MINER_MODID_FILTER
  - MINER_TAG_FILTER
  - SORTER_ITEMSTACK_FILTER
  - SORTER_MODID_FILTER
  - SORTER_TAG_FILTER
  - OREDICTIONIFICATOR_ITEM_FILTER
  - QIO_ITEMSTACK_FILTER
  - QIO_MODID_FILTER
  - QIO_TAG_FILTER
  mekanism.common.content.miner.ThreadMinerSearch$State:
  - IDLE
  - SEARCHING
  - PAUSED
  - FINISHED
  mekanism.common.content.network.transmitter.DiversionTransporter$DiversionControl:
  - DISABLED
  - HIGH
  - LOW
  mekanism.common.lib.transmitter.TransmissionType:
  - ENERGY
  - FLUID
  - CHEMICAL
  - ITEM
  - HEAT
  mekanism.common.tile.TileEntityChemicalTank$GasMode:
  - IDLE
  - DUMPING_EXCESS
  - DUMPING
  mekanism.common.tile.component.config.DataType:
  - NONE
  - INPUT
  - INPUT_1
  - INPUT_2
  - OUTPUT
  - OUTPUT_1
  - OUTPUT_2
  - INPUT_OUTPUT
  - ENERGY
  - EXTRA
  mekanism.common.tile.interfaces.IFluidContainerManager$ContainerEditMode:
  - BOTH
  - FILL
  - EMPTY
  mekanism.common.tile.interfaces.IRedstoneControl$RedstoneControl:
  - DISABLED
  - HIGH
  - LOW
  - PULSE
  mekanism.common.tile.laser.TileEntityLaserAmplifier$RedstoneOutput:
  - 'OFF'
  - ENTITY_DETECTION
  - ENERGY_CONTENTS
  mekanism.common.tile.qio.TileEntityQIODriveArray$DriveStatus:
  - NONE
  - OFFLINE
  - READY
  - NEAR_FULL
  - FULL
  mekanism.generators.common.block.attribute.AttributeStateFissionPortMode$FissionPortMode:
  - INPUT
  - OUTPUT_WASTE
  - OUTPUT_COOLANT
  mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter$FissionReactorLogic:
  - DISABLED
  - ACTIVATION
  - TEMPERATURE
  - CRITICAL_WASTE_LEVEL
  - DAMAGED
  - DEPLETED
  mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter$RedstoneStatus:
  - IDLE
  - OUTPUTTING
  - POWERED
  mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter$FusionReactorLogic:
  - DISABLED
  - READY
  - CAPACITY
  - DEPLETED
  net.minecraft.core.Direction:
  - DOWN
  - UP
  - NORTH
  - SOUTH
  - WEST
  - EAST
methods:
  'API Global: computerEnergyHelper':
  - description: Convert Forge Energy to Mekanism Joules
    methodName: feToJoules
    params:
    - java_type: long
      name: fe
      type: Number (long)
    returns:
      java_type: long
      type: Number (long)
  - description: Convert Mekanism Joules to Forge Energy
    methodName: joulesToFE
    params:
    - java_type: long
      name: joules
      type: Number (long)
    returns:
      java_type: long
      type: Number (long)
  'API Global: computerFilterHelper':
  - description: Create a Digital Miner Item Filter from an Item name
    methodName: createMinerItemFilter
    params:
    - java_type: net.minecraft.world.item.Item
      name: item
      type: String (Item)
    returns:
      java_type: mekanism.common.content.miner.MinerItemStackFilter
      type: Table (MinerItemStackFilter)
  - description: Create a Digital Miner Mod Id Filter from a mod id
    methodName: createMinerModIdFilter
    params:
    - java_type: java.lang.String
      name: modId
      type: String
    returns:
      java_type: mekanism.common.content.miner.MinerModIDFilter
      type: Table (MinerModIDFilter)
  - description: Create a Digital Miner Tag Filter from a Tag name
    methodName: createMinerTagFilter
    params:
    - java_type: java.lang.String
      name: tag
      type: String
    returns:
      java_type: mekanism.common.content.miner.MinerTagFilter
      type: Table (MinerTagFilter)
  - description: Create an Oredictionificator filter from a tag, without specifying
      an output item
    methodName: createOredictionificatorItemFilter
    params:
    - java_type: net.minecraft.resources.ResourceLocation
      name: filterTag
      type: String (ResourceLocation)
    returns:
      java_type: mekanism.common.content.oredictionificator.OredictionificatorItemFilter
      type: Table (OredictionificatorItemFilter)
  - description: Create an Oredictionificator filter from a tag and a selected output.
      The output is not validated.
    methodName: createOredictionificatorItemFilter
    params:
    - java_type: net.minecraft.resources.ResourceLocation
      name: filterTag
      type: String (ResourceLocation)
    - java_type: net.minecraft.world.item.Item
      name: selectedOutput
      type: String (Item)
    returns:
      java_type: mekanism.common.content.oredictionificator.OredictionificatorItemFilter
      type: Table (OredictionificatorItemFilter)
  - description: Create a QIO Item Filter structure from an Item name
    methodName: createQIOItemFilter
    params:
    - java_type: net.minecraft.world.item.Item
      name: item
      type: String (Item)
    returns:
      java_type: mekanism.common.content.qio.filter.QIOItemStackFilter
      type: Table (QIOItemStackFilter)
  - description: Create a QIO Mod Id Filter from a mod id
    methodName: createQIOModIdFilter
    params:
    - java_type: java.lang.String
      name: modId
      type: String
    returns:
      java_type: mekanism.common.content.qio.filter.QIOModIDFilter
      type: Table (QIOModIDFilter)
  - description: Create a QIO Tag Filter from a Tag name
    methodName: createQIOTagFilter
    params:
    - java_type: java.lang.String
      name: tag
      type: String
    returns:
      java_type: mekanism.common.content.qio.filter.QIOTagFilter
      type: Table (QIOTagFilter)
  - description: Create a Logistical Sorter Item Filter structure from an Item name
    methodName: createSorterItemFilter
    params:
    - java_type: net.minecraft.world.item.Item
      name: item
      type: String (Item)
    returns:
      java_type: mekanism.common.content.transporter.SorterItemStackFilter
      type: Table (SorterItemStackFilter)
  - description: Create a Logistical Sorter Mod Id Filter structure from a mod id
    methodName: createSorterModIdFilter
    params:
    - java_type: java.lang.String
      name: modId
      type: String
    returns:
      java_type: mekanism.common.content.transporter.SorterModIDFilter
      type: Table (SorterModIDFilter)
  - description: Create a Logistical Sorter Tag Filter from a tag
    methodName: createSorterTagFilter
    params:
    - java_type: java.lang.String
      name: tag
      type: String
    returns:
      java_type: mekanism.common.content.transporter.SorterTagFilter
      type: Table (SorterTagFilter)
  Antiprotonic Nucleosynthesizer:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the input gas tank.
    methodName: getInputChemical
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the input gas tank.
    methodName: getInputChemicalCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the input gas tank.
    methodName: getInputChemicalFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the input gas item slot.
    methodName: getInputChemicalItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the input gas tank.
    methodName: getInputChemicalNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the input item slot.
    methodName: getInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Bin:
  - description: Get the maximum number of items the bin can contain.
    methodName: getCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the type of item the Bin is locked to (or Air if not locked)
    methodName: getLock
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the bin.
    methodName: getStored
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: If true, the Bin is locked to a particular item type.
    methodName: isLocked
    returns:
      java_type: boolean
      type: boolean
  - description: Lock the Bin to the currently stored item type. The Bin must not
      be creative, empty, or already locked
    methodName: lock
  - description: Unlock the Bin's fixed item type. The Bin must not be creative, or
      already unlocked
    methodName: unlock
  Bio Generator:
  - description: Get the contents of the biofuel tank.
    methodName: getBioFuel
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the biofuel tank.
    methodName: getBioFuelCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the filled percentage of the biofuel tank.
    methodName: getBioFuelFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the biofuel tank.
    methodName: getBioFuelNeeded
    returns:
      java_type: int
      type: Number (int)
  - description: Get the contents of the energy item.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the fuel slot.
    methodName: getFuelItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Boiler Multiblock (formed):
  - description: Get the maximum possible boil rate for this Boiler, based on the
      number of Superheating Elements
    methodName: getBoilCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the rate of boiling (mB/t)
    methodName: getBoilRate
    returns:
      java_type: int
      type: Number (int)
  - description: Get the contents of the cooled coolant tank.
    methodName: getCooledCoolant
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the cooled coolant tank.
    methodName: getCooledCoolantCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the cooled coolant tank.
    methodName: getCooledCoolantFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the cooled coolant tank.
    methodName: getCooledCoolantNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the amount of heat lost to the environment in the last tick (Kelvin)
    methodName: getEnvironmentalLoss
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the heated coolant tank.
    methodName: getHeatedCoolant
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the heated coolant tank.
    methodName: getHeatedCoolantCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the heated coolant tank.
    methodName: getHeatedCoolantFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the heated coolant tank.
    methodName: getHeatedCoolantNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the maximum rate of boiling seen (mB/t)
    methodName: getMaxBoilRate
    returns:
      java_type: int
      type: Number (int)
  - description: Get the contents of the steam tank.
    methodName: getSteam
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the steam tank.
    methodName: getSteamCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the steam tank.
    methodName: getSteamFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the steam tank.
    methodName: getSteamNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: How many superheaters this Boiler has
    methodName: getSuperheaters
    returns:
      java_type: int
      type: Number (int)
  - description: Get the temperature of the boiler in Kelvin.
    methodName: getTemperature
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the water tank.
    methodName: getWater
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the water tank.
    methodName: getWaterCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the filled percentage of the water tank.
    methodName: getWaterFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the water tank.
    methodName: getWaterNeeded
    returns:
      java_type: int
      type: Number (int)
  Boiler Valve:
  - description: Toggle the current valve configuration to the previous option in
      the list
    methodName: decrementMode
  - description: Get the current configuration of this valve
    methodName: getMode
    returns:
      java_type: mekanism.common.block.attribute.AttributeStateBoilerValveMode$BoilerValveMode
      type: String (BoilerValveMode)
  - description: Toggle the current valve configuration to the next option in the
      list
    methodName: incrementMode
  - description: Change the configuration of this valve
    methodName: setMode
    params:
    - java_type: mekanism.common.block.attribute.AttributeStateBoilerValveMode$BoilerValveMode
      name: mode
      type: String (BoilerValveMode)
  Chemical Crystallizer:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the input tank.
    methodName: getInput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the input tank.
    methodName: getInputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the input tank.
    methodName: getInputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the input item slot.
    methodName: getInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the input tank.
    methodName: getInputNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the output slot.
    methodName: getOutput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Chemical Dissolution Chamber:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the gas input tank.
    methodName: getGasInput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the gas input tank.
    methodName: getGasInputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the gas input tank.
    methodName: getGasInputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the gas input tank.
    methodName: getGasInputNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the gas input item slot.
    methodName: getInputGasItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output tank.
    methodName: getOutput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output tank.
    methodName: getOutputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the output tank.
    methodName: getOutputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output tank.
    methodName: getOutputNeeded
    returns:
      java_type: long
      type: Number (long)
  Chemical Infuser:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the left input tank.
    methodName: getLeftInput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the left input tank.
    methodName: getLeftInputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the left input tank.
    methodName: getLeftInputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the left input item slot.
    methodName: getLeftInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the left input tank.
    methodName: getLeftInputNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the output (center) tank.
    methodName: getOutput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output (center) tank.
    methodName: getOutputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the output (center) tank.
    methodName: getOutputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the output item slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output (center) tank.
    methodName: getOutputNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the right input tank.
    methodName: getRightInput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the right input tank.
    methodName: getRightInputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the right input tank.
    methodName: getRightInputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the right input item slot.
    methodName: getRightInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the right input tank.
    methodName: getRightInputNeeded
    returns:
      java_type: long
      type: Number (long)
  Chemical Oxidizer:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the input slot.
    methodName: getInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output tank.
    methodName: getOutput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output tank.
    methodName: getOutputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the output tank.
    methodName: getOutputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the output item slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output tank.
    methodName: getOutputNeeded
    returns:
      java_type: long
      type: Number (long)
  Chemical Tank:
  - description: Descend the Dumping mode to the previous configuration in the list
    methodName: decrementDumpingMode
    requires_public_security: true
  - description: Get the capacity of the tank.
    methodName: getCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the drain slot.
    methodName: getDrainItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the current Dumping configuration
    methodName: getDumpingMode
    returns:
      java_type: mekanism.common.tile.TileEntityChemicalTank$GasMode
      type: String (GasMode)
  - description: Get the contents of the fill slot.
    methodName: getFillItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the filled percentage of the tank.
    methodName: getFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the tank.
    methodName: getNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the tank.
    methodName: getStored
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Advance the Dumping mode to the next configuration in the list
    methodName: incrementDumpingMode
    requires_public_security: true
  - description: Set the Dumping mode of the tank
    methodName: setDumpingMode
    params:
    - java_type: mekanism.common.tile.TileEntityChemicalTank$GasMode
      name: mode
      type: String (GasMode)
    requires_public_security: true
  Chemical Washer:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the fluid tank.
    methodName: getFluid
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the fluid tank.
    methodName: getFluidCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the filled percentage of the fluid tank.
    methodName: getFluidFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the fluid item input slot.
    methodName: getFluidItemInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the fluid item output slot.
    methodName: getFluidItemOutput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the fluid tank.
    methodName: getFluidNeeded
    returns:
      java_type: int
      type: Number (int)
  - description: Get the contents of the slurry item output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the input slurry tank.
    methodName: getSlurryInput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the input slurry tank.
    methodName: getSlurryInputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the input slurry tank.
    methodName: getSlurryInputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the input slurry tank.
    methodName: getSlurryInputNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the output slurry tank.
    methodName: getSlurryOutput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output slurry tank.
    methodName: getSlurryOutputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the output slurry tank.
    methodName: getSlurryOutputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the output slurry tank.
    methodName: getSlurryOutputNeeded
    returns:
      java_type: long
      type: Number (long)
  Combiner:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the main input slot.
    methodName: getMainInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the secondary input slot.
    methodName: getSecondaryInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Combining Factory:
  - description: Get the contents of the secondary input slot.
    methodName: getSecondaryInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Compressing/Infusing/Injecting/Purifying Factory:
  - description: Empty the contents of the chemical tank into the environment
    methodName: dumpChemical
    requires_public_security: true
  - description: Get the contents of the chemical tank.
    methodName: getChemical
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the chemical tank.
    methodName: getChemicalCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the chemical tank.
    methodName: getChemicalFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the chemical item (extra) slot.
    methodName: getChemicalItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the chemical tank.
    methodName: getChemicalNeeded
    returns:
      java_type: long
      type: Number (long)
  Compressing/Injecting/Purifying Machine:
  - description: Empty the contents of the gas tank into the environment
    methodName: dumpChemical
    requires_public_security: true
  - description: Get the contents of the chemical tank.
    methodName: getChemical
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the chemical tank.
    methodName: getChemicalCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the chemical tank.
    methodName: getChemicalFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the secondary input slot.
    methodName: getChemicalItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the chemical tank.
    methodName: getChemicalNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the input slot.
    methodName: getInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Digital Miner:
  - description: Add a new filter to the miner. Requires miner to be stopped/reset
      first
    methodName: addFilter
    params:
    - java_type: mekanism.common.content.miner.MinerFilter
      name: filter
      type: Table (MinerFilter)
    requires_public_security: true
    returns:
      java_type: boolean
      type: boolean
  - description: Remove the target for Replacement in Inverse Mode. Requires miner
      to be stopped/reset first
    methodName: clearInverseModeReplaceTarget
    requires_public_security: true
  - description: Whether Auto Eject is turned on
    methodName: getAutoEject
    returns:
      java_type: boolean
      type: boolean
  - description: Whether Auto Pull is turned on
    methodName: getAutoPull
    returns:
      java_type: boolean
      type: boolean
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the current list of Miner Filters
    methodName: getFilters
    returns:
      java_extra:
      - mekanism.common.content.miner.MinerFilter
      java_type: java.util.Collection
      type: List (Table (MinerFilter))
  - description: Whether Inverse Mode is enabled or not
    methodName: getInverseMode
    returns:
      java_type: boolean
      type: boolean
  - description: Get the configured Replacement target item
    methodName: getInverseModeReplaceTarget
    returns:
      java_type: net.minecraft.world.item.Item
      type: String (Item)
  - description: Whether Inverse Mode Require Replacement is turned on
    methodName: getInverseModeRequiresReplacement
    returns:
      java_type: boolean
      type: boolean
  - description: Get the contents of the internal inventory slot. 0 based.
    methodName: getItemInSlot
    params:
    - java_type: int
      name: slot
      type: Number (int)
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the maximum allowable Radius value, determined from the mod's
      config
    methodName: getMaxRadius
    returns:
      java_type: int
      type: Number (int)
  - description: Gets the configured maximum Y level for mining
    methodName: getMaxY
    returns:
      java_type: int
      type: Number (int)
  - description: Gets the configured minimum Y level for mining
    methodName: getMinY
    returns:
      java_type: int
      type: Number (int)
  - description: Get the current radius configured (blocks)
    methodName: getRadius
    returns:
      java_type: int
      type: Number (int)
  - description: Whether Silk Touch mode is enabled or not
    methodName: getSilkTouch
    returns:
      java_type: boolean
      type: boolean
  - description: Get the size of the Miner's internal inventory
    methodName: getSlotCount
    returns:
      java_type: int
      type: Number (int)
  - description: Get the state of the Miner's search
    methodName: getState
    returns:
      java_type: mekanism.common.content.miner.ThreadMinerSearch$State
      type: String (State)
  - description: Get the count of block found but not yet mined
    methodName: getToMine
    returns:
      java_type: int
      type: Number (int)
  - description: Whether the miner is currently running
    methodName: isRunning
    returns:
      java_type: boolean
      type: boolean
  - description: Removes the exactly matching filter from the miner. Requires miner
      to be stopped/reset first
    methodName: removeFilter
    params:
    - java_type: mekanism.common.content.miner.MinerFilter
      name: filter
      type: Table (MinerFilter)
    requires_public_security: true
    returns:
      java_type: boolean
      type: boolean
  - description: Stop the mining process and reset the Miner to be able to change
      settings
    methodName: reset
    requires_public_security: true
  - description: Update the Auto Eject setting
    methodName: setAutoEject
    params:
    - java_type: boolean
      name: eject
      type: boolean
    requires_public_security: true
  - description: Update the Auto Pull setting
    methodName: setAutoPull
    params:
    - java_type: boolean
      name: pull
      type: boolean
    requires_public_security: true
  - description: Update the Inverse Mode setting. Requires miner to be stopped/reset
      first
    methodName: setInverseMode
    params:
    - java_type: boolean
      name: enabled
      type: boolean
    requires_public_security: true
  - description: Update the target for Replacement in Inverse Mode. Requires miner
      to be stopped/reset first
    methodName: setInverseModeReplaceTarget
    params:
    - java_type: net.minecraft.world.item.Item
      name: target
      type: String (Item)
    requires_public_security: true
  - description: Update the Inverse Mode Requires Replacement setting. Requires miner
      to be stopped/reset first
    methodName: setInverseModeRequiresReplacement
    params:
    - java_type: boolean
      name: requiresReplacement
      type: boolean
    requires_public_security: true
  - description: Update the maximum Y level for mining. Requires miner to be stopped/reset
      first
    methodName: setMaxY
    params:
    - java_type: int
      name: maxY
      type: Number (int)
    requires_public_security: true
  - description: Update the minimum Y level for mining. Requires miner to be stopped/reset
      first
    methodName: setMinY
    params:
    - java_type: int
      name: minY
      type: Number (int)
    requires_public_security: true
  - description: Update the mining radius (blocks). Requires miner to be stopped/reset
      first
    methodName: setRadius
    params:
    - java_type: int
      name: radius
      type: Number (int)
    requires_public_security: true
  - description: Update the Silk Touch setting
    methodName: setSilkTouch
    params:
    - java_type: boolean
      name: silk
      type: boolean
    requires_public_security: true
  - description: Attempt to start the mining process
    methodName: start
    requires_public_security: true
  - description: Attempt to stop the mining process
    methodName: stop
    requires_public_security: true
  Dimensional Stabilizer:
  - description: 'Sets the chunks in the specified radius to not be kept loaded. The
      chunk the Stabilizer is in is always loaded. Range: [1, 2]'
    methodName: disableChunkLoadingFor
    params:
    - java_type: int
      name: radius
      type: Number (int)
    requires_public_security: true
  - description: 'Sets the chunks in the specified radius to be loaded. The chunk
      the Stabilizer is in is always loaded. Range: [1, 2]'
    methodName: enableChunkLoadingFor
    params:
    - java_type: int
      name: radius
      type: Number (int)
    requires_public_security: true
  - description: Get the number of chunks being loaded.
    methodName: getChunksLoaded
    returns:
      java_type: int
      type: Number (int)
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: 'Check if the Dimensional Stabilizer is configured to load a the
      specified relative chunk position at x,y (Stabilizer is at 0,0). Range: [-2,
      2]'
    methodName: isChunkLoadingAt
    params:
    - java_type: int
      name: x
      type: Number (int)
    - java_type: int
      name: z
      type: Number (int)
    returns:
      java_type: boolean
      type: boolean
  - description: 'Set if the Dimensional Stabilizer is configured to load a the specified
      relative position (Stabilizer is at 0,0). True = load the chunk, false = don''t
      load the chunk. Range: [-2, 2]'
    methodName: setChunkLoadingAt
    params:
    - java_type: int
      name: x
      type: Number (int)
    - java_type: int
      name: z
      type: Number (int)
    - java_type: boolean
      name: load
      type: boolean
    requires_public_security: true
  - description: 'Toggle loading the specified relative chunk at the relative x,y
      position (Stabilizer is at 0,0). Just like clicking the button in the GUI. Range:
      [-2, 2]'
    methodName: toggleChunkLoadingAt
    params:
    - java_type: int
      name: x
      type: Number (int)
    - java_type: int
      name: z
      type: Number (int)
    requires_public_security: true
  Diversion Transporter:
  - methodName: decrementMode
    params:
    - java_type: net.minecraft.core.Direction
      name: side
      type: String (Direction)
  - methodName: getMode
    params:
    - java_type: net.minecraft.core.Direction
      name: side
      type: String (Direction)
    returns:
      java_type: mekanism.common.content.network.transmitter.DiversionTransporter$DiversionControl
      type: String (DiversionControl)
  - methodName: incrementMode
    params:
    - java_type: net.minecraft.core.Direction
      name: side
      type: String (Direction)
  - methodName: setMode
    params:
    - java_type: net.minecraft.core.Direction
      name: side
      type: String (Direction)
    - java_type: mekanism.common.content.network.transmitter.DiversionTransporter$DiversionControl
      name: mode
      type: String (DiversionControl)
  Dynamic Tank Multiblock (formed):
  - methodName: decrementContainerEditMode
  - methodName: getChemicalTankCapacity
    returns:
      java_type: long
      type: Number (long)
  - methodName: getContainerEditMode
    returns:
      java_type: mekanism.common.tile.interfaces.IFluidContainerManager$ContainerEditMode
      type: String (ContainerEditMode)
  - methodName: getFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getStored
    returns:
      java_extra:
      - mekanism.api.chemical.ChemicalStack
      - net.neoforged.neoforge.fluids.FluidStack
      java_type: com.mojang.datafixers.util.Either
      type: Table (ChemicalStack) or Table (FluidStack)
  - methodName: getTankCapacity
    returns:
      java_type: int
      type: Number (int)
  - methodName: incrementContainerEditMode
  - methodName: setContainerEditMode
    params:
    - java_type: mekanism.common.tile.interfaces.IFluidContainerManager$ContainerEditMode
      name: mode
      type: String (ContainerEditMode)
  Electric Machine:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the input slot.
    methodName: getInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Electric Pump:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the buffer tank.
    methodName: getFluid
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the buffer tank.
    methodName: getFluidCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the filled percentage of the buffer tank.
    methodName: getFluidFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the buffer tank.
    methodName: getFluidNeeded
    returns:
      java_type: int
      type: Number (int)
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: reset
    requires_public_security: true
  Electrolytic Separator:
  - methodName: decrementLeftOutputDumpingMode
    requires_public_security: true
  - methodName: decrementRightOutputDumpingMode
    requires_public_security: true
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the input tank.
    methodName: getInput
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the input tank.
    methodName: getInputCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the filled percentage of the input tank.
    methodName: getInputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the input item slot.
    methodName: getInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the input tank.
    methodName: getInputNeeded
    returns:
      java_type: int
      type: Number (int)
  - description: Get the contents of the left output tank.
    methodName: getLeftOutput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the left output tank.
    methodName: getLeftOutputCapacity
    returns:
      java_type: long
      type: Number (long)
  - methodName: getLeftOutputDumpingMode
    returns:
      java_type: mekanism.common.tile.TileEntityChemicalTank$GasMode
      type: String (GasMode)
  - description: Get the filled percentage of the left output tank.
    methodName: getLeftOutputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the left output item slot.
    methodName: getLeftOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the left output tank.
    methodName: getLeftOutputNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the right output tank.
    methodName: getRightOutput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the right output tank.
    methodName: getRightOutputCapacity
    returns:
      java_type: long
      type: Number (long)
  - methodName: getRightOutputDumpingMode
    returns:
      java_type: mekanism.common.tile.TileEntityChemicalTank$GasMode
      type: String (GasMode)
  - description: Get the filled percentage of the right output tank.
    methodName: getRightOutputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the right output item slot.
    methodName: getRightOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the right output tank.
    methodName: getRightOutputNeeded
    returns:
      java_type: long
      type: Number (long)
  - methodName: incrementLeftOutputDumpingMode
    requires_public_security: true
  - methodName: incrementRightOutputDumpingMode
    requires_public_security: true
  - methodName: setLeftOutputDumpingMode
    params:
    - java_type: mekanism.common.tile.TileEntityChemicalTank$GasMode
      name: mode
      type: String (GasMode)
    requires_public_security: true
  - methodName: setRightOutputDumpingMode
    params:
    - java_type: mekanism.common.tile.TileEntityChemicalTank$GasMode
      name: mode
      type: String (GasMode)
    requires_public_security: true
  Energy Cube:
  - description: Get the contents of the charge slot.
    methodName: getChargeItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the discharge slot.
    methodName: getDischargeItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Factory Machine:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - methodName: getInput
    params:
    - java_type: int
      name: process
      type: Number (int)
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getOutput
    params:
    - java_type: int
      name: process
      type: Number (int)
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getRecipeProgress
    params:
    - java_type: int
      name: process
      type: Number (int)
    returns:
      java_type: int
      type: Number (int)
  - description: Total number of ticks it takes currently for the recipe to complete
    methodName: getTicksRequired
    returns:
      java_type: int
      type: Number (int)
  - methodName: isAutoSortEnabled
    returns:
      java_type: boolean
      type: boolean
  - methodName: setAutoSort
    params:
    - java_type: boolean
      name: enabled
      type: boolean
    requires_public_security: true
  Filter Wrapper:
  - methodName: getFilterType
    returns:
      java_type: mekanism.common.content.filter.FilterType
      type: String (FilterType)
  - methodName: isEnabled
    returns:
      java_type: boolean
      type: boolean
  - methodName: setEnabled
    params:
    - java_type: boolean
      name: enabled
      type: boolean
  Filter Wrapper (Digital Miner):
  - methodName: clone
    returns:
      java_type: mekanism.common.content.miner.MinerFilter
      type: Table (MinerFilter)
  - methodName: getReplaceTarget
    returns:
      java_type: net.minecraft.world.item.Item
      type: String (Item)
  - methodName: getRequiresReplacement
    returns:
      java_type: boolean
      type: boolean
  - methodName: hasBlacklistedElement
    returns:
      java_type: boolean
      type: boolean
  - methodName: setReplaceTarget
    params:
    - java_type: net.minecraft.world.item.Item
      name: value
      type: String (Item)
  - methodName: setRequiresReplacement
    params:
    - java_type: boolean
      name: value
      type: boolean
  Filter Wrapper (ItemStack):
  - methodName: getItemStack
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: setItem
    params:
    - java_type: net.minecraft.world.item.Item
      name: item
      type: String (Item)
  - methodName: setItemStack
    params:
    - java_type: net.minecraft.world.item.ItemStack
      name: stack
      type: Table (ItemStack)
  Filter Wrapper (Logistical Sorter):
  - methodName: clone
    returns:
      java_type: mekanism.common.content.transporter.SorterFilter
      type: Table (SorterFilter)
  - methodName: getAllowDefault
    returns:
      java_type: boolean
      type: boolean
  - methodName: getColor
    returns:
      java_type: mekanism.api.text.EnumColor
      type: String (EnumColor)
  - methodName: getMax
    returns:
      java_type: int
      type: Number (int)
  - methodName: getMin
    returns:
      java_type: int
      type: Number (int)
  - methodName: getSizeMode
    returns:
      java_type: boolean
      type: boolean
  - methodName: setAllowDefault
    params:
    - java_type: boolean
      name: value
      type: boolean
  - methodName: setColor
    params:
    - java_type: mekanism.api.text.EnumColor
      name: value
      type: String (EnumColor)
  - methodName: setMinMax
    params:
    - java_type: int
      name: min
      type: Number (int)
    - java_type: int
      name: max
      type: Number (int)
  - methodName: setSizeMode
    params:
    - java_type: boolean
      name: value
      type: boolean
  Filter Wrapper (Mod Id):
  - methodName: getModID
    returns:
      java_type: java.lang.String
      type: String
  - methodName: setModID
    params:
    - java_type: java.lang.String
      name: id
      type: String
  Filter Wrapper (Oredictionificator Item):
  - methodName: getSelectedOutput
    returns:
      java_type: net.minecraft.world.item.Item
      type: String (Item)
  - methodName: setSelectedOutput
    params:
    - java_type: net.minecraft.world.item.Item
      name: item
      type: String (Item)
  Filter Wrapper (Oredictionificator):
  - methodName: clone
    returns:
      java_type: mekanism.common.content.oredictionificator.OredictionificatorFilter
      type: Table (OredictionificatorFilter)
  - methodName: getFilter
    returns:
      java_type: java.lang.String
      type: String
  - methodName: setFilter
    params:
    - java_type: net.minecraft.resources.ResourceLocation
      name: tag
      type: String (ResourceLocation)
  Filter Wrapper (QIO):
  - methodName: clone
    returns:
      java_type: mekanism.common.content.qio.filter.QIOFilter
      type: Table (QIOFilter)
  Filter Wrapper (Tag):
  - methodName: getTagName
    returns:
      java_type: java.lang.String
      type: String
  - methodName: setTagName
    params:
    - java_type: java.lang.String
      name: name
      type: String
  Fission Reactor Logic Adapter:
  - methodName: getLogicMode
    returns:
      java_type: mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter$FissionReactorLogic
      type: String (FissionReactorLogic)
  - methodName: getRedstoneLogicStatus
    returns:
      java_type: mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter$RedstoneStatus
      type: String (RedstoneStatus)
  - methodName: setLogicMode
    params:
    - java_type: mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter$FissionReactorLogic
      name: logicType
      type: String (FissionReactorLogic)
  Fission Reactor Multiblock (formed):
  - description: Must be disabled, and if meltdowns are disabled must not have been
      force disabled
    methodName: activate
  - description: Actual burn rate as it may be lower if say there is not enough fuel
    methodName: getActualBurnRate
    returns:
      java_type: double
      type: Number (double)
  - methodName: getBoilEfficiency
    returns:
      java_type: double
      type: Number (double)
  - description: Configured burn rate
    methodName: getBurnRate
    returns:
      java_type: double
      type: Number (double)
  - methodName: getCoolant
    returns:
      java_extra:
      - mekanism.api.chemical.ChemicalStack
      - net.neoforged.neoforge.fluids.FluidStack
      java_type: com.mojang.datafixers.util.Either
      type: Table (ChemicalStack) or Table (FluidStack)
  - methodName: getCoolantCapacity
    returns:
      java_type: long
      type: Number (long)
  - methodName: getCoolantFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - methodName: getCoolantNeeded
    returns:
      java_type: long
      type: Number (long)
  - methodName: getDamagePercent
    returns:
      java_type: long
      type: Number (long)
  - methodName: getEnvironmentalLoss
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the fuel tank.
    methodName: getFuel
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - methodName: getFuelAssemblies
    returns:
      java_type: int
      type: Number (int)
  - description: Get the capacity of the fuel tank.
    methodName: getFuelCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the fuel tank.
    methodName: getFuelFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the fuel tank.
    methodName: getFuelNeeded
    returns:
      java_type: long
      type: Number (long)
  - methodName: getFuelSurfaceArea
    returns:
      java_type: int
      type: Number (int)
  - methodName: getHeatCapacity
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the heated coolant.
    methodName: getHeatedCoolant
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the heated coolant.
    methodName: getHeatedCoolantCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the heated coolant.
    methodName: getHeatedCoolantFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the heated coolant.
    methodName: getHeatedCoolantNeeded
    returns:
      java_type: long
      type: Number (long)
  - methodName: getHeatingRate
    returns:
      java_type: long
      type: Number (long)
  - methodName: getMaxBurnRate
    returns:
      java_type: long
      type: Number (long)
  - description: true -> active, false -> off
    methodName: getStatus
    returns:
      java_type: boolean
      type: boolean
  - description: Get the temperature of the reactor in Kelvin.
    methodName: getTemperature
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the waste tank.
    methodName: getWaste
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the waste tank.
    methodName: getWasteCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the waste tank.
    methodName: getWasteFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the waste tank.
    methodName: getWasteNeeded
    returns:
      java_type: long
      type: Number (long)
  - methodName: isForceDisabled
    returns:
      java_type: boolean
      type: boolean
  - description: Must be enabled
    methodName: scram
  - methodName: setBurnRate
    params:
    - java_type: double
      name: rate
      type: Number (double)
  Fission Reactor Port:
  - methodName: decrementMode
  - methodName: getMode
    returns:
      java_type: mekanism.generators.common.block.attribute.AttributeStateFissionPortMode$FissionPortMode
      type: String (FissionPortMode)
  - methodName: incrementMode
  - methodName: setMode
    params:
    - java_type: mekanism.generators.common.block.attribute.AttributeStateFissionPortMode$FissionPortMode
      name: mode
      type: String (FissionPortMode)
  Fluid Tank:
  - methodName: decrementContainerEditMode
    requires_public_security: true
  - description: Get the capacity of the tank.
    methodName: getCapacity
    returns:
      java_type: int
      type: Number (int)
  - methodName: getContainerEditMode
    returns:
      java_type: mekanism.common.tile.interfaces.IFluidContainerManager$ContainerEditMode
      type: String (ContainerEditMode)
  - description: Get the filled percentage of the tank.
    methodName: getFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the tank.
    methodName: getNeeded
    returns:
      java_type: int
      type: Number (int)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the tank.
    methodName: getStored
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - methodName: incrementContainerEditMode
    requires_public_security: true
  - methodName: setContainerEditMode
    params:
    - java_type: mekanism.common.tile.interfaces.IFluidContainerManager$ContainerEditMode
      name: mode
      type: String (ContainerEditMode)
    requires_public_security: true
  Fluidic Plenisher:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the buffer tank.
    methodName: getFluid
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the buffer tank.
    methodName: getFluidCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the filled percentage of the buffer tank.
    methodName: getFluidFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the buffer tank.
    methodName: getFluidNeeded
    returns:
      java_type: int
      type: Number (int)
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: reset
    requires_public_security: true
  Formulaic Assemblicator:
  - description: Requires recipe and auto mode to be disabled
    methodName: craftAvailableItems
    requires_public_security: true
  - description: Requires recipe and auto mode to be disabled
    methodName: craftSingleItem
    requires_public_security: true
  - description: Requires auto mode to be disabled
    methodName: emptyGrid
    requires_public_security: true
  - description: Requires an unencoded formula in the formula slot and a valid recipe
    methodName: encodeFormula
    requires_public_security: true
  - description: Requires auto mode to be disabled
    methodName: fillGrid
    requires_public_security: true
  - description: Requires valid encoded formula
    methodName: getAutoMode
    requires_public_security: true
    returns:
      java_type: boolean
      type: boolean
  - methodName: getCraftingInputSlot
    params:
    - java_type: int
      name: slot
      type: Number (int)
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getCraftingOutputSlot
    params:
    - java_type: int
      name: slot
      type: Number (int)
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getCraftingOutputSlots
    returns:
      java_type: int
      type: Number (int)
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getExcessRemainingItems
    returns:
      java_extra:
      - net.minecraft.world.item.ItemStack
      java_type: net.minecraft.core.NonNullList
      type: List (Table (ItemStack))
  - description: Get the contents of the formula slot.
    methodName: getFormulaItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getItemInSlot
    params:
    - java_type: int
      name: slot
      type: Number (int)
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getRecipeProgress
    returns:
      java_type: int
      type: Number (int)
  - methodName: getSlots
    returns:
      java_type: int
      type: Number (int)
  - description: Requires valid encoded formula
    methodName: getStockControl
    requires_public_security: true
    returns:
      java_type: boolean
      type: boolean
  - methodName: getTicksRequired
    returns:
      java_type: int
      type: Number (int)
  - methodName: hasRecipe
    returns:
      java_type: boolean
      type: boolean
  - methodName: hasValidFormula
    returns:
      java_type: boolean
      type: boolean
  - description: Requires valid encoded formula
    methodName: setAutoMode
    params:
    - java_type: boolean
      name: mode
      type: boolean
    requires_public_security: true
  - description: Requires valid encoded formula
    methodName: setStockControl
    params:
    - java_type: boolean
      name: mode
      type: boolean
    requires_public_security: true
  Fuelwood Heater:
  - methodName: getEnvironmentalLoss
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the fuel slot.
    methodName: getFuelItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the temperature of the heater in Kelvin.
    methodName: getTemperature
    returns:
      java_type: double
      type: Number (double)
  - methodName: getTransferLoss
    returns:
      java_type: double
      type: Number (double)
  Fusion Reactor Logic Adapter:
  - methodName: getLogicMode
    returns:
      java_type: mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter$FusionReactorLogic
      type: String (FusionReactorLogic)
  - methodName: isActiveCooledLogic
    returns:
      java_type: boolean
      type: boolean
  - methodName: setActiveCooledLogic
    params:
    - java_type: boolean
      name: active
      type: boolean
  - methodName: setLogicMode
    params:
    - java_type: mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter$FusionReactorLogic
      name: logicType
      type: String (FusionReactorLogic)
  Fusion Reactor Multiblock (formed):
  - methodName: getCaseTemperature
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the fuel tank.
    methodName: getDTFuel
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the fuel tank.
    methodName: getDTFuelCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the fuel tank.
    methodName: getDTFuelFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the fuel tank.
    methodName: getDTFuelNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the deuterium tank.
    methodName: getDeuterium
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the deuterium tank.
    methodName: getDeuteriumCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the deuterium tank.
    methodName: getDeuteriumFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the deuterium tank.
    methodName: getDeuteriumNeeded
    returns:
      java_type: long
      type: Number (long)
  - methodName: getEnvironmentalLoss
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the Hohlraum slot.
    methodName: getHohlraum
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: true -> water cooled, false -> air cooled
    methodName: getIgnitionTemperature
    params:
    - java_type: boolean
      name: active
      type: boolean
    returns:
      java_type: double
      type: Number (double)
  - methodName: getInjectionRate
    returns:
      java_type: int
      type: Number (int)
  - description: true -> water cooled, false -> air cooled
    methodName: getMaxCasingTemperature
    params:
    - java_type: boolean
      name: active
      type: boolean
    returns:
      java_type: double
      type: Number (double)
  - description: true -> water cooled, false -> air cooled
    methodName: getMaxPlasmaTemperature
    params:
    - java_type: boolean
      name: active
      type: boolean
    returns:
      java_type: double
      type: Number (double)
  - description: true -> water cooled, false -> air cooled
    methodName: getMinInjectionRate
    params:
    - java_type: boolean
      name: active
      type: boolean
    returns:
      java_type: int
      type: Number (int)
  - methodName: getPassiveGeneration
    params:
    - java_type: boolean
      name: active
      type: boolean
    returns:
      java_type: long
      type: Number (long)
  - methodName: getPlasmaTemperature
    returns:
      java_type: double
      type: Number (double)
  - methodName: getProductionRate
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the steam tank.
    methodName: getSteam
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the steam tank.
    methodName: getSteamCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the steam tank.
    methodName: getSteamFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the steam tank.
    methodName: getSteamNeeded
    returns:
      java_type: long
      type: Number (long)
  - methodName: getTransferLoss
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the tritium tank.
    methodName: getTritium
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the tritium tank.
    methodName: getTritiumCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the tritium tank.
    methodName: getTritiumFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the tritium tank.
    methodName: getTritiumNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the water tank.
    methodName: getWater
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the water tank.
    methodName: getWaterCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the filled percentage of the water tank.
    methodName: getWaterFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the water tank.
    methodName: getWaterNeeded
    returns:
      java_type: int
      type: Number (int)
  - description: Checks if a reaction is occurring.
    methodName: isIgnited
    returns:
      java_type: boolean
      type: boolean
  - methodName: setInjectionRate
    params:
    - java_type: int
      name: rate
      type: Number (int)
  Fusion Reactor Port:
  - description: true -> output, false -> input
    methodName: getMode
    returns:
      java_type: boolean
      type: boolean
  - description: true -> output, false -> input
    methodName: setMode
    params:
    - java_type: boolean
      name: output
      type: boolean
  Gas Generator:
  - methodName: getBurnRate
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the energy item slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the fuel tank.
    methodName: getFuel
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the fuel tank.
    methodName: getFuelCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the fuel tank.
    methodName: getFuelFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the fuel item slot.
    methodName: getFuelItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the fuel tank.
    methodName: getFuelNeeded
    returns:
      java_type: long
      type: Number (long)
  Generator:
  - methodName: getMaxOutput
    returns:
      java_type: long
      type: Number (long)
  - description: Get the amount of energy produced by this generator in the last tick.
    methodName: getProductionRate
    returns:
      java_type: long
      type: Number (long)
  Generic Mekanism Machine:
  - methodName: getComparatorLevel
    restriction: COMPARATOR
    returns:
      java_type: int
      type: Number (int)
  - methodName: getDirection
    restriction: DIRECTIONAL
    returns:
      java_type: net.minecraft.core.Direction
      type: String (Direction)
  - methodName: getEnergy
    restriction: ENERGY
    returns:
      java_type: long
      type: Number (long)
  - methodName: getEnergyFilledPercentage
    restriction: ENERGY
    returns:
      java_type: double
      type: Number (double)
  - methodName: getEnergyNeeded
    restriction: ENERGY
    returns:
      java_type: long
      type: Number (long)
  - methodName: getMaxEnergy
    restriction: ENERGY
    returns:
      java_type: long
      type: Number (long)
  - methodName: getRedstoneMode
    restriction: REDSTONE_CONTROL
    returns:
      java_type: mekanism.common.tile.interfaces.IRedstoneControl$RedstoneControl
      type: String (RedstoneControl)
  - methodName: setRedstoneMode
    params:
    - java_type: mekanism.common.tile.interfaces.IRedstoneControl$RedstoneControl
      name: type
      type: String (RedstoneControl)
    requires_public_security: true
    restriction: REDSTONE_CONTROL
  Heat Generator:
  - description: Get the contents of the energy item slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getEnvironmentalLoss
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the fuel item slot.
    methodName: getFuelItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the lava tank.
    methodName: getLava
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the lava tank.
    methodName: getLavaCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the filled percentage of the lava tank.
    methodName: getLavaFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the lava tank.
    methodName: getLavaNeeded
    returns:
      java_type: int
      type: Number (int)
  - description: Get the temperature of the generator in Kelvin.
    methodName: getTemperature
    returns:
      java_type: double
      type: Number (double)
  - methodName: getTransferLoss
    returns:
      java_type: double
      type: Number (double)
  Induction Matrix Multiblock (formed):
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getInstalledCells
    returns:
      java_type: int
      type: Number (int)
  - methodName: getInstalledProviders
    returns:
      java_type: int
      type: Number (int)
  - methodName: getLastInput
    returns:
      java_type: long
      type: Number (long)
  - methodName: getLastOutput
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getTransferCap
    returns:
      java_type: long
      type: Number (long)
  Induction Matrix Port:
  - description: true -> output, false -> input.
    methodName: getMode
    returns:
      java_type: boolean
      type: boolean
  - description: true -> output, false -> input
    methodName: setMode
    params:
    - java_type: boolean
      name: output
      type: boolean
  Industrial Turbine Multiblock (formed):
  - methodName: decrementDumpingMode
  - methodName: getBlades
    returns:
      java_type: int
      type: Number (int)
  - methodName: getCoils
    returns:
      java_type: int
      type: Number (int)
  - methodName: getCondensers
    returns:
      java_type: int
      type: Number (int)
  - methodName: getDispersers
    returns:
      java_type: int
      type: Number (int)
  - methodName: getDumpingMode
    returns:
      java_type: mekanism.common.tile.TileEntityChemicalTank$GasMode
      type: String (GasMode)
  - methodName: getFlowRate
    returns:
      java_type: long
      type: Number (long)
  - methodName: getLastSteamInputRate
    returns:
      java_type: long
      type: Number (long)
  - methodName: getMaxFlowRate
    returns:
      java_type: long
      type: Number (long)
  - methodName: getMaxProduction
    returns:
      java_type: long
      type: Number (long)
  - methodName: getMaxWaterOutput
    returns:
      java_type: long
      type: Number (long)
  - methodName: getProductionRate
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the steam tank.
    methodName: getSteam
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the steam tank.
    methodName: getSteamCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the steam tank.
    methodName: getSteamFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the steam tank.
    methodName: getSteamNeeded
    returns:
      java_type: long
      type: Number (long)
  - methodName: getVents
    returns:
      java_type: int
      type: Number (int)
  - methodName: incrementDumpingMode
  - methodName: setDumpingMode
    params:
    - java_type: mekanism.common.tile.TileEntityChemicalTank$GasMode
      name: mode
      type: String (GasMode)
  Isotopic Centrifuge:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the input tank.
    methodName: getInput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the input tank.
    methodName: getInputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the input tank.
    methodName: getInputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the input tank.
    methodName: getInputNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the output tank.
    methodName: getOutput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output tank.
    methodName: getOutputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the output tank.
    methodName: getOutputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output tank.
    methodName: getOutputNeeded
    returns:
      java_type: long
      type: Number (long)
  Laser:
  - methodName: getDiggingPos
    returns:
      java_type: net.minecraft.core.BlockPos
      type: Table (BlockPos)
  Laser Amplifier:
  - methodName: getDelay
    returns:
      java_type: int
      type: Number (int)
  - methodName: getMaxThreshold
    returns:
      java_type: long
      type: Number (long)
  - methodName: getMinThreshold
    returns:
      java_type: long
      type: Number (long)
  - methodName: getRedstoneOutputMode
    returns:
      java_type: mekanism.common.tile.laser.TileEntityLaserAmplifier$RedstoneOutput
      type: String (RedstoneOutput)
  - methodName: setDelay
    params:
    - java_type: int
      name: delay
      type: Number (int)
    requires_public_security: true
  - methodName: setMaxThreshold
    params:
    - java_type: long
      name: threshold
      type: Number (long)
    requires_public_security: true
  - methodName: setMinThreshold
    params:
    - java_type: long
      name: threshold
      type: Number (long)
    requires_public_security: true
  - methodName: setRedstoneOutputMode
    params:
    - java_type: mekanism.common.tile.laser.TileEntityLaserAmplifier$RedstoneOutput
      name: mode
      type: String (RedstoneOutput)
    requires_public_security: true
  Laser Tractor Beam:
  - methodName: getItemInSlot
    params:
    - java_type: int
      name: slot
      type: Number (int)
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getSlotCount
    returns:
      java_type: int
      type: Number (int)
  Logistical Sorter:
  - methodName: addFilter
    params:
    - java_type: mekanism.common.content.transporter.SorterFilter
      name: filter
      type: Table (SorterFilter)
    requires_public_security: true
    returns:
      java_type: boolean
      type: boolean
  - methodName: clearDefaultColor
    requires_public_security: true
  - methodName: decrementDefaultColor
    requires_public_security: true
  - methodName: getAutoMode
    returns:
      java_type: boolean
      type: boolean
  - methodName: getDefaultColor
    returns:
      java_type: mekanism.api.text.EnumColor
      type: String (EnumColor)
  - methodName: getFilters
    returns:
      java_extra:
      - mekanism.common.content.transporter.SorterFilter
      java_type: java.util.Collection
      type: List (Table (SorterFilter))
  - methodName: incrementDefaultColor
    requires_public_security: true
  - methodName: isRoundRobin
    returns:
      java_type: boolean
      type: boolean
  - methodName: isSingle
    returns:
      java_type: boolean
      type: boolean
  - methodName: removeFilter
    params:
    - java_type: mekanism.common.content.transporter.SorterFilter
      name: filter
      type: Table (SorterFilter)
    requires_public_security: true
    returns:
      java_type: boolean
      type: boolean
  - methodName: setAutoMode
    params:
    - java_type: boolean
      name: value
      type: boolean
    requires_public_security: true
  - methodName: setDefaultColor
    params:
    - java_type: mekanism.api.text.EnumColor
      name: color
      type: String (EnumColor)
    requires_public_security: true
  - methodName: setRoundRobin
    params:
    - java_type: boolean
      name: value
      type: boolean
    requires_public_security: true
  - methodName: setSingle
    params:
    - java_type: boolean
      name: value
      type: boolean
    requires_public_security: true
  Machine with Ejector Component:
  - methodName: clearInputColor
    params:
    - java_type: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    requires_public_security: true
  - methodName: clearOutputColor
    requires_public_security: true
  - methodName: decrementInputColor
    params:
    - java_type: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    requires_public_security: true
  - methodName: decrementOutputColor
    requires_public_security: true
  - methodName: getInputColor
    params:
    - java_type: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    returns:
      java_type: mekanism.api.text.EnumColor
      type: String (EnumColor)
  - methodName: getOutputColor
    returns:
      java_type: mekanism.api.text.EnumColor
      type: String (EnumColor)
  - methodName: hasStrictInput
    returns:
      java_type: boolean
      type: boolean
  - methodName: incrementInputColor
    params:
    - java_type: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    requires_public_security: true
  - methodName: incrementOutputColor
    requires_public_security: true
  - methodName: setInputColor
    params:
    - java_type: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    - java_type: mekanism.api.text.EnumColor
      name: color
      type: String (EnumColor)
    requires_public_security: true
  - methodName: setOutputColor
    params:
    - java_type: mekanism.api.text.EnumColor
      name: color
      type: String (EnumColor)
    requires_public_security: true
  - methodName: setStrictInput
    params:
    - java_type: boolean
      name: strict
      type: boolean
    requires_public_security: true
  Machine with Recipe Progress:
  - methodName: getRecipeProgress
    returns:
      java_type: int
      type: Number (int)
  - methodName: getTicksRequired
    returns:
      java_type: int
      type: Number (int)
  Machine with Security Component:
  - methodName: getOwnerName
    returns:
      java_type: java.lang.String
      type: String
  - methodName: getOwnerUUID
    returns:
      java_type: java.util.UUID
      type: String (UUID)
  - methodName: getSecurityMode
    returns:
      java_type: mekanism.api.security.SecurityMode
      type: String (SecurityMode)
  Machine with Side Configuration Component:
  - methodName: canEject
    params:
    - java_type: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    returns:
      java_type: boolean
      type: boolean
  - methodName: decrementMode
    params:
    - java_type: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    - java_type: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    requires_public_security: true
  - methodName: getConfigurableTypes
    returns:
      java_extra:
      - mekanism.common.lib.transmitter.TransmissionType
      java_type: java.util.List
      type: List (String (TransmissionType))
  - methodName: getMode
    params:
    - java_type: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    - java_type: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    requires_public_security: true
    returns:
      java_type: mekanism.common.tile.component.config.DataType
      type: String (DataType)
  - methodName: getSupportedModes
    params:
    - java_type: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    requires_public_security: true
    returns:
      java_extra:
      - mekanism.common.tile.component.config.DataType
      java_type: java.util.Set
      type: List (String (DataType))
  - methodName: incrementMode
    params:
    - java_type: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    - java_type: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    requires_public_security: true
  - methodName: isEjecting
    params:
    - java_type: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    returns:
      java_type: boolean
      type: boolean
  - methodName: setEjecting
    params:
    - java_type: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    - java_type: boolean
      name: ejecting
      type: boolean
    requires_public_security: true
  - methodName: setMode
    params:
    - java_type: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    - java_type: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    - java_type: mekanism.common.tile.component.config.DataType
      name: mode
      type: String (DataType)
    requires_public_security: true
  Machine with Upgrade Component:
  - methodName: getInstalledUpgrades
    returns:
      java_extra:
      - mekanism.api.Upgrade
      - java.lang.Integer
      java_type: java.util.Map
      type: Table (String (Upgrade) => Number (int))
  - methodName: getSupportedUpgrades
    returns:
      java_extra:
      - mekanism.api.Upgrade
      java_type: java.util.Set
      type: List (String (Upgrade))
  Mechanical Pipe:
  - methodName: getBuffer
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - methodName: getCapacity
    returns:
      java_type: long
      type: Number (long)
  - methodName: getFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - methodName: getNeeded
    returns:
      java_type: long
      type: Number (long)
  Metallurgic Infuser:
  - methodName: dumpInfuseType
    requires_public_security: true
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the infusion buffer.
    methodName: getInfuseType
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the infusion buffer.
    methodName: getInfuseTypeCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the infusion buffer.
    methodName: getInfuseTypeFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the infusion (extra) input slot.
    methodName: getInfuseTypeItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the infusion buffer.
    methodName: getInfuseTypeNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the input slot.
    methodName: getInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Modification Station:
  - description: Get the contents of the module holder slot (suit, tool, etc).
    methodName: getContainerItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the module slot.
    methodName: getModuleItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Multiblock:
  - methodName: isFormed
    restriction: MULTIBLOCK
    returns:
      java_type: boolean
      type: boolean
  Multiblock (formed):
  - methodName: getHeight
    returns:
      java_type: int
      type: Number (int)
  - methodName: getLength
    returns:
      java_type: int
      type: Number (int)
  - methodName: getMaxPos
    returns:
      java_type: net.minecraft.core.BlockPos
      type: Table (BlockPos)
  - methodName: getMinPos
    returns:
      java_type: net.minecraft.core.BlockPos
      type: Table (BlockPos)
  - methodName: getWidth
    returns:
      java_type: int
      type: Number (int)
  Nutritional Liquifier:
  - description: Get the contents of the fillable container slot.
    methodName: getContainerFillItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the filled container output slot.
    methodName: getContainerOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the input slot.
    methodName: getInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output tank.
    methodName: getOutput
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the output tank.
    methodName: getOutputCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the filled percentage of the output tank.
    methodName: getOutputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output tank.
    methodName: getOutputNeeded
    returns:
      java_type: int
      type: Number (int)
  Oredictionificator:
  - methodName: addFilter
    params:
    - java_type: mekanism.common.content.oredictionificator.OredictionificatorItemFilter
      name: filter
      type: Table (OredictionificatorItemFilter)
    requires_public_security: true
    returns:
      java_type: boolean
      type: boolean
  - methodName: getFilters
    returns:
      java_extra:
      - mekanism.common.content.oredictionificator.OredictionificatorItemFilter
      java_type: java.util.Collection
      type: List (Table (OredictionificatorItemFilter))
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: removeFilter
    params:
    - java_type: mekanism.common.content.oredictionificator.OredictionificatorItemFilter
      name: filter
      type: Table (OredictionificatorItemFilter)
    requires_public_security: true
    returns:
      java_type: boolean
      type: boolean
  Painting Machine:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the paintable item slot.
    methodName: getInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the pigment slot.
    methodName: getInputPigmentItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the painted item slot.
    methodName: getOutput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the pigment tank.
    methodName: getPigmentInput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the pigment tank.
    methodName: getPigmentInputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the pigment tank.
    methodName: getPigmentInputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the pigment tank.
    methodName: getPigmentInputNeeded
    returns:
      java_type: long
      type: Number (long)
  Pigment Extractor:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the input slot.
    methodName: getInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the pigment tank.
    methodName: getOutput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the pigment tank.
    methodName: getOutputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the pigment tank.
    methodName: getOutputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the pigment tank.
    methodName: getOutputNeeded
    returns:
      java_type: long
      type: Number (long)
  Pigment Mixer:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the left pigment tank.
    methodName: getLeftInput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the left pigment tank.
    methodName: getLeftInputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the left pigment tank.
    methodName: getLeftInputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the left input slot.
    methodName: getLeftInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the left pigment tank.
    methodName: getLeftInputNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the output pigment tank.
    methodName: getOutput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output pigment tank.
    methodName: getOutputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the output pigment tank.
    methodName: getOutputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output pigment tank.
    methodName: getOutputNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the right pigment tank.
    methodName: getRightInput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the right pigment tank.
    methodName: getRightInputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the right pigment tank.
    methodName: getRightInputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the right input slot.
    methodName: getRightInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the right pigment tank.
    methodName: getRightInputNeeded
    returns:
      java_type: long
      type: Number (long)
  Precision Sawmill:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the input slot.
    methodName: getInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the secondary output slot.
    methodName: getSecondaryOutput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Pressurized Reaction Chamber:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the fluid input.
    methodName: getInputFluid
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the fluid input.
    methodName: getInputFluidCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the filled percentage of the fluid input.
    methodName: getInputFluidFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the fluid input.
    methodName: getInputFluidNeeded
    returns:
      java_type: int
      type: Number (int)
  - description: Get the contents of the gas input.
    methodName: getInputGas
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the gas input.
    methodName: getInputGasCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the gas input.
    methodName: getInputGasFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the gas input.
    methodName: getInputGasNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the item input slot.
    methodName: getInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the gas output.
    methodName: getOutputGas
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the gas output.
    methodName: getOutputGasCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the gas output.
    methodName: getOutputGasFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the gas output.
    methodName: getOutputGasNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the item output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Pressurized Tube:
  - methodName: getBuffer
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - methodName: getCapacity
    returns:
      java_type: long
      type: Number (long)
  - methodName: getFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - methodName: getNeeded
    returns:
      java_type: long
      type: Number (long)
  QIO Dashboard:
  - methodName: getCraftingInput
    params:
    - java_type: int
      name: window
      type: Number (int)
    - java_type: int
      name: slot
      type: Number (int)
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getCraftingOutput
    params:
    - java_type: int
      name: window
      type: Number (int)
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  QIO Drive Array:
  - methodName: getDrive
    params:
    - java_type: int
      name: slot
      type: Number (int)
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getDriveStatus
    params:
    - java_type: int
      name: slot
      type: Number (int)
    returns:
      java_type: mekanism.common.tile.qio.TileEntityQIODriveArray$DriveStatus
      type: String (DriveStatus)
  - description: Requires a frequency to be selected
    methodName: getFrequencyItemCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Requires a frequency to be selected
    methodName: getFrequencyItemCount
    returns:
      java_type: long
      type: Number (long)
  - description: Requires a frequency to be selected
    methodName: getFrequencyItemPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Requires a frequency to be selected
    methodName: getFrequencyItemTypeCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Requires a frequency to be selected
    methodName: getFrequencyItemTypeCount
    returns:
      java_type: long
      type: Number (long)
  - description: Requires a frequency to be selected
    methodName: getFrequencyItemTypePercentage
    returns:
      java_type: double
      type: Number (double)
  - methodName: getSlotCount
    returns:
      java_type: int
      type: Number (int)
  QIO Exporter:
  - methodName: getExportWithoutFilter
    returns:
      java_type: boolean
      type: boolean
  - methodName: isRoundRobin
    returns:
      java_type: boolean
      type: boolean
  - methodName: setExportsWithoutFilter
    params:
    - java_type: boolean
      name: value
      type: boolean
    requires_public_security: true
  - methodName: setRoundRobin
    params:
    - java_type: boolean
      name: value
      type: boolean
    requires_public_security: true
  QIO Importer:
  - methodName: getImportWithoutFilter
    returns:
      java_type: boolean
      type: boolean
  - methodName: setImportsWithoutFilter
    params:
    - java_type: boolean
      name: value
      type: boolean
    requires_public_security: true
  QIO Machine:
  - description: Requires frequency to not already exist and for it to be public so
      that it can make it as the player who owns the block. Also sets the frequency
      after creation
    methodName: createFrequency
    params:
    - java_type: java.lang.String
      name: name
      type: String
    requires_public_security: true
  - description: Requires a frequency to be selected
    methodName: decrementFrequencyColor
    requires_public_security: true
  - description: Lists public frequencies
    methodName: getFrequencies
    returns:
      java_extra:
      - mekanism.common.content.qio.QIOFrequency
      java_type: java.util.Collection
      type: List (Table (QIOFrequency))
  - description: Requires a frequency to be selected
    methodName: getFrequency
    returns:
      java_type: mekanism.common.content.qio.QIOFrequency
      type: Table (QIOFrequency)
  - description: Requires a frequency to be selected
    methodName: getFrequencyColor
    returns:
      java_type: mekanism.api.text.EnumColor
      type: String (EnumColor)
  - methodName: hasFrequency
    returns:
      java_type: boolean
      type: boolean
  - description: Requires a frequency to be selected
    methodName: incrementFrequencyColor
    requires_public_security: true
  - description: Requires a public frequency to exist
    methodName: setFrequency
    params:
    - java_type: java.lang.String
      name: name
      type: String
    requires_public_security: true
  - description: Requires a frequency to be selected
    methodName: setFrequencyColor
    params:
    - java_type: mekanism.api.text.EnumColor
      name: color
      type: String (EnumColor)
    requires_public_security: true
  QIO Machine with Filter:
  - methodName: addFilter
    params:
    - java_type: mekanism.common.content.qio.filter.QIOFilter
      name: filter
      type: Table (QIOFilter)
    requires_public_security: true
    returns:
      java_type: boolean
      type: boolean
  - methodName: getFilters
    returns:
      java_extra:
      - mekanism.common.content.qio.filter.QIOFilter
      java_type: java.util.Collection
      type: List (Table (QIOFilter))
  - methodName: removeFilter
    params:
    - java_type: mekanism.common.content.qio.filter.QIOFilter
      name: filter
      type: Table (QIOFilter)
    requires_public_security: true
    returns:
      java_type: boolean
      type: boolean
  QIO Redstone Adapter:
  - methodName: clearTargetItem
    requires_public_security: true
  - methodName: getFuzzyMode
    returns:
      java_type: boolean
      type: boolean
  - methodName: getTargetItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getTriggerAmount
    returns:
      java_type: long
      type: Number (long)
  - methodName: invertSignal
    requires_public_security: true
  - methodName: isInverted
    returns:
      java_type: boolean
      type: boolean
  - methodName: setFuzzyMode
    params:
    - java_type: boolean
      name: fuzzy
      type: boolean
    requires_public_security: true
  - methodName: setSignalInverted
    params:
    - java_type: boolean
      name: inverted
      type: boolean
    requires_public_security: true
  - methodName: setTargetItem
    params:
    - java_type: net.minecraft.resources.ResourceLocation
      name: itemName
      type: String (ResourceLocation)
    requires_public_security: true
  - methodName: setTriggerAmount
    params:
    - java_type: long
      name: amount
      type: Number (long)
    requires_public_security: true
  - methodName: toggleFuzzyMode
    requires_public_security: true
  Quantum Entangloporter:
  - description: Requires frequency to not already exist and for it to be public so
      that it can make it as the player who owns the block. Also sets the frequency
      after creation
    methodName: createFrequency
    params:
    - java_type: java.lang.String
      name: name
      type: String
    requires_public_security: true
  - description: Get the contents of the chemical buffer.
    methodName: getBufferChemical
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the chemical buffer.
    methodName: getBufferChemicalCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the chemical buffer.
    methodName: getBufferChemicalFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the chemical buffer.
    methodName: getBufferChemicalNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the fluid buffer.
    methodName: getBufferFluid
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the fluid buffer.
    methodName: getBufferFluidCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the filled percentage of the fluid buffer.
    methodName: getBufferFluidFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the fluid buffer.
    methodName: getBufferFluidNeeded
    returns:
      java_type: int
      type: Number (int)
  - methodName: getBufferItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: May not be accurate if there is no frequency
    methodName: getEnvironmentalLoss
    returns:
      java_type: double
      type: Number (double)
  - description: Lists public frequencies
    methodName: getFrequencies
    returns:
      java_extra:
      - mekanism.common.content.entangloporter.InventoryFrequency
      java_type: java.util.Collection
      type: List (Table (InventoryFrequency))
  - description: Requires a frequency to be selected
    methodName: getFrequency
    returns:
      java_type: mekanism.common.content.entangloporter.InventoryFrequency
      type: Table (InventoryFrequency)
  - description: Requires a frequency to be selected
    methodName: getTemperature
    returns:
      java_type: double
      type: Number (double)
  - description: May not be accurate if there is no frequency
    methodName: getTransferLoss
    returns:
      java_type: double
      type: Number (double)
  - methodName: hasFrequency
    returns:
      java_type: boolean
      type: boolean
  - description: Requires a public frequency to exist
    methodName: setFrequency
    params:
    - java_type: java.lang.String
      name: name
      type: String
    requires_public_security: true
  Radioactive Waste Barrel:
  - description: Get the capacity of the barrel.
    methodName: getCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the barrel.
    methodName: getFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the barrel.
    methodName: getNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the barrel.
    methodName: getStored
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  Resistive Heater:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - methodName: getEnergyUsed
    returns:
      java_type: long
      type: Number (long)
  - methodName: getEnvironmentalLoss
    returns:
      java_type: double
      type: Number (double)
  - description: Get the temperature of the heater in Kelvin.
    methodName: getTemperature
    returns:
      java_type: double
      type: Number (double)
  - methodName: getTransferLoss
    returns:
      java_type: double
      type: Number (double)
  - methodName: setEnergyUsage
    params:
    - java_type: long
      name: usage
      type: Number (long)
    requires_public_security: true
  Rotary Condensentrator:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the fluid tank.
    methodName: getFluid
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the fluid tank.
    methodName: getFluidCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the filled percentage of the fluid tank.
    methodName: getFluidFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the fluid item input slot.
    methodName: getFluidItemInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the fluid item ouput slot.
    methodName: getFluidItemOutput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the fluid tank.
    methodName: getFluidNeeded
    returns:
      java_type: int
      type: Number (int)
  - description: Get the contents of the gas tank.
    methodName: getGas
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the gas tank.
    methodName: getGasCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the gas tank.
    methodName: getGasFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the gas item input slot.
    methodName: getGasItemInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the gas item output slot.
    methodName: getGasItemOutput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the gas tank.
    methodName: getGasNeeded
    returns:
      java_type: long
      type: Number (long)
  - methodName: isCondensentrating
    returns:
      java_type: boolean
      type: boolean
  - methodName: setCondensentrating
    params:
    - java_type: boolean
      name: value
      type: boolean
    requires_public_security: true
  SPS Multiblock (formed):
  - methodName: getCoils
    returns:
      java_type: int
      type: Number (int)
  - description: Get the contents of the input tank.
    methodName: getInput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the input tank.
    methodName: getInputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the input tank.
    methodName: getInputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the input tank.
    methodName: getInputNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the output tank.
    methodName: getOutput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output tank.
    methodName: getOutputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the output tank.
    methodName: getOutputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the amount needed to fill the output tank.
    methodName: getOutputNeeded
    returns:
      java_type: long
      type: Number (long)
  - methodName: getProcessRate
    returns:
      java_type: double
      type: Number (double)
  SPS Port:
  - description: true -> output, false -> input.
    methodName: getMode
    returns:
      java_type: boolean
      type: boolean
  - description: true -> output, false -> input.
    methodName: setMode
    params:
    - java_type: boolean
      name: output
      type: boolean
  Sawing Factory:
  - methodName: getSecondaryOutput
    params:
    - java_type: int
      name: process
      type: Number (int)
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Seismic Vibrator:
  - methodName: getBlockAt
    params:
    - java_type: int
      name: chunkRelativeX
      type: Number (int)
    - java_type: int
      name: y
      type: Number (int)
    - java_type: int
      name: chunkRelativeZ
      type: Number (int)
    returns:
      java_type: net.minecraft.world.level.block.state.BlockState
      type: Table (BlockState)
  - description: Get a column info, table key is the Y level
    methodName: getColumnAt
    params:
    - java_type: int
      name: chunkRelativeX
      type: Number (int)
    - java_type: int
      name: chunkRelativeZ
      type: Number (int)
    returns:
      java_extra:
      - java.lang.Integer
      - net.minecraft.world.level.block.state.BlockState
      java_type: java.util.Map
      type: Table (Number (int) => Table (BlockState))
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: isVibrating
    returns:
      java_type: boolean
      type: boolean
  Solar Generator:
  - methodName: canSeeSun
    returns:
      java_type: boolean
      type: boolean
  - description: Get the contents of the energy item slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Solar Neutron Activator:
  - methodName: canSeeSun
    returns:
      java_type: boolean
      type: boolean
  - description: Get the contents of the input tank.
    methodName: getInput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the input tank.
    methodName: getInputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the input tank.
    methodName: getInputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the input tank.
    methodName: getInputNeeded
    returns:
      java_type: long
      type: Number (long)
  - description: Get the contents of the output tank.
    methodName: getOutput
    returns:
      java_type: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output tank.
    methodName: getOutputCapacity
    returns:
      java_type: long
      type: Number (long)
  - description: Get the filled percentage of the output tank.
    methodName: getOutputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output tank.
    methodName: getOutputNeeded
    returns:
      java_type: long
      type: Number (long)
  - methodName: getPeakProductionRate
    returns:
      java_type: float
      type: Number (float)
  - methodName: getProductionRate
    returns:
      java_type: float
      type: Number (float)
  Teleporter:
  - description: Requires frequency to not already exist and for it to be public so
      that it can make it as the player who owns the block. Also sets the frequency
      after creation
    methodName: createFrequency
    params:
    - java_type: java.lang.String
      name: name
      type: String
    requires_public_security: true
  - description: Requires a frequency to be selected
    methodName: decrementFrequencyColor
    requires_public_security: true
  - description: Requires a frequency to be selected
    methodName: getActiveTeleporters
    returns:
      java_extra:
      - net.minecraft.core.GlobalPos
      java_type: java.util.Set
      type: List (Table (GlobalPos))
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Lists public frequencies
    methodName: getFrequencies
    returns:
      java_extra:
      - mekanism.common.content.teleporter.TeleporterFrequency
      java_type: java.util.Collection
      type: List (Table (TeleporterFrequency))
  - description: Requires a frequency to be selected
    methodName: getFrequency
    returns:
      java_type: mekanism.common.content.teleporter.TeleporterFrequency
      type: Table (TeleporterFrequency)
  - description: Requires a frequency to be selected
    methodName: getFrequencyColor
    returns:
      java_type: mekanism.api.text.EnumColor
      type: String (EnumColor)
  - methodName: getStatus
    returns:
      java_type: java.lang.String
      type: String
  - methodName: hasFrequency
    returns:
      java_type: boolean
      type: boolean
  - description: Requires a frequency to be selected
    methodName: incrementFrequencyColor
    requires_public_security: true
  - description: Requires a public frequency to exist
    methodName: setFrequency
    params:
    - java_type: java.lang.String
      name: name
      type: String
    requires_public_security: true
  - description: Requires a frequency to be selected
    methodName: setFrequencyColor
    params:
    - java_type: mekanism.api.text.EnumColor
      name: color
      type: String (EnumColor)
    requires_public_security: true
  Thermal Evaporation Multiblock (formed):
  - methodName: getActiveSolars
    returns:
      java_type: int
      type: Number (int)
  - methodName: getEnvironmentalLoss
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the input tank.
    methodName: getInput
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the input tank.
    methodName: getInputCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the filled percentage of the input tank.
    methodName: getInputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the input side's input slot.
    methodName: getInputItemInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the input side's output slot.
    methodName: getInputItemOutput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the input tank.
    methodName: getInputNeeded
    returns:
      java_type: int
      type: Number (int)
  - description: Get the contents of the output tank.
    methodName: getOutput
    returns:
      java_type: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the output tank.
    methodName: getOutputCapacity
    returns:
      java_type: int
      type: Number (int)
  - description: Get the filled percentage of the output tank.
    methodName: getOutputFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - description: Get the contents of the output side's input slot.
    methodName: getOutputItemInput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output side's output slot.
    methodName: getOutputItemOutput
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output tank.
    methodName: getOutputNeeded
    returns:
      java_type: int
      type: Number (int)
  - methodName: getProductionAmount
    returns:
      java_type: double
      type: Number (double)
  - methodName: getTemperature
    returns:
      java_type: double
      type: Number (double)
  Universal Cable:
  - methodName: getBuffer
    returns:
      java_type: long
      type: Number (long)
  - methodName: getCapacity
    returns:
      java_type: long
      type: Number (long)
  - methodName: getFilledPercentage
    returns:
      java_type: double
      type: Number (double)
  - methodName: getNeeded
    returns:
      java_type: long
      type: Number (long)
  Wind Generator:
  - description: Get the contents of the energy item slot.
    methodName: getEnergyItem
    returns:
      java_type: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: isBlacklistedDimension
    returns:
      java_type: boolean
      type: boolean
version: 10.7.0
---
