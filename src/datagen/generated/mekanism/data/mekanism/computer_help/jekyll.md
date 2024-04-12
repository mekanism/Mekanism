---
builtInTables:
  mekanism.api.chemical.ChemicalStack:
    description: An amount of Gas/Fluid/Slurry/Pigment
    fields:
      amount:
        description: The amount in mB
        javaType: int
        type: Number (int)
      name:
        description: The Chemical's registered name
        javaType: net.minecraft.world.item.Item
        type: String (Item)
    humanName: Table (ChemicalStack)
  mekanism.common.content.filter.IFilter:
    description: |-
      Common Filter properties. Use the API Global to make constructing these a little easier.
      Filters are a combination of these base properties, an ItemStack or Mod Id or Tag component, and a device specific type.
      The exception to that is an Oredictionificator filter, which does not have an item/mod/tag component.
    fields:
      enabled:
        description: Whether the filter is enabled when added to a device
        javaType: boolean
        type: boolean
      type:
        description: The type of filter in this structure
        javaType: mekanism.common.content.filter.FilterType
        type: String (FilterType)
    humanName: Table (IFilter)
  mekanism.common.content.miner.MinerFilter:
    description: A Digital Miner filter
    extends: mekanism.common.content.filter.IFilter
    fields:
      replaceTarget:
        description: The name of the item block that will be used to replace a mined
          block
        javaType: net.minecraft.world.item.Item
        type: String (Item)
      requiresReplacement:
        description: Whether the filter requires a replacement to be done before it
          will allow mining
        javaType: boolean
        type: boolean
    humanName: Table (MinerFilter)
  mekanism.common.content.miner.MinerItemStackFilter:
    description: Digital Miner filter with ItemStack filter properties
    extends: mekanism.common.content.miner.MinerFilter
    fields:
      item:
        description: The filtered item's registered name
        javaType: net.minecraft.world.item.Item
        type: String (Item)
      itemAttachments:
        description: The Attachment NBT data of the filtered item, optional
        javaType: java.lang.String
        type: String
      itemNBT:
        description: The NBT data of the filtered item, optional
        javaType: java.lang.String
        type: String
    humanName: Table (MinerItemStackFilter)
  mekanism.common.content.miner.MinerModIDFilter:
    description: Digital Miner filter with Mod Id filter properties
    extends: mekanism.common.content.miner.MinerFilter
    fields:
      modId:
        description: The mod id to filter. e.g. mekansim
        javaType: java.lang.String
        type: String
    humanName: Table (MinerModIDFilter)
  mekanism.common.content.miner.MinerTagFilter:
    description: Digital Miner filter with Tag filter properties
    extends: mekanism.common.content.miner.MinerFilter
    fields:
      tag:
        description: The tag to filter. e.g. forge:ores
        javaType: java.lang.String
        type: String
    humanName: Table (MinerTagFilter)
  mekanism.common.content.oredictionificator.OredictionificatorItemFilter:
    description: An Oredictionificator filter
    extends: mekanism.common.content.filter.IFilter
    fields:
      selected:
        description: The selected output item's registered name. Optional for adding
          a filter
        javaType: net.minecraft.world.item.Item
        type: String (Item)
      target:
        description: The target tag to match (input)
        javaType: java.lang.String
        type: String
    humanName: Table (OredictionificatorItemFilter)
  mekanism.common.content.qio.filter.QIOFilter:
    description: A Quantum Item Orchestration filter
    extends: mekanism.common.content.filter.IFilter
    humanName: Table (QIOFilter)
  mekanism.common.content.qio.filter.QIOItemStackFilter:
    description: QIO filter with ItemStack filter properties
    extends: mekanism.common.content.qio.filter.QIOFilter
    fields:
      fuzzy:
        description: Whether Fuzzy mode is enabled (checks only the item name/type)
        javaType: boolean
        type: boolean
      item:
        description: The filtered item's registered name
        javaType: net.minecraft.world.item.Item
        type: String (Item)
      itemAttachments:
        description: The Attachment NBT data of the filtered item, optional
        javaType: java.lang.String
        type: String
      itemNBT:
        description: The NBT data of the filtered item, optional
        javaType: java.lang.String
        type: String
    humanName: Table (QIOItemStackFilter)
  mekanism.common.content.qio.filter.QIOModIDFilter:
    description: QIO filter with Mod Id filter properties
    extends: mekanism.common.content.qio.filter.QIOFilter
    fields:
      modId:
        description: The mod id to filter. e.g. mekansim
        javaType: java.lang.String
        type: String
    humanName: Table (QIOModIDFilter)
  mekanism.common.content.qio.filter.QIOTagFilter:
    description: QIO filter with Tag filter properties
    extends: mekanism.common.content.qio.filter.QIOFilter
    fields:
      tag:
        description: The tag to filter. e.g. forge:ores
        javaType: java.lang.String
        type: String
    humanName: Table (QIOTagFilter)
  mekanism.common.content.transporter.SorterFilter:
    description: A Logistical Sorter filter
    extends: mekanism.common.content.filter.IFilter
    fields:
      allowDefault:
        description: Allows the filtered item to travel to the default color destination
        javaType: boolean
        type: boolean
      color:
        description: The color configured, nil if none
        javaType: mekanism.api.text.EnumColor
        type: String (EnumColor)
      max:
        description: In Size Mode, the maximum to send
        javaType: int
        type: Number (int)
      min:
        description: In Size Mode, the minimum to send
        javaType: int
        type: Number (int)
      size:
        description: If Size Mode is enabled
        javaType: boolean
        type: boolean
    humanName: Table (SorterFilter)
  mekanism.common.content.transporter.SorterItemStackFilter:
    description: Logistical Sorter filter with ItemStack filter properties
    extends: mekanism.common.content.transporter.SorterFilter
    fields:
      fuzzy:
        description: Whether Fuzzy mode is enabled (checks only the item name/type)
        javaType: boolean
        type: boolean
      item:
        description: The filtered item's registered name
        javaType: net.minecraft.world.item.Item
        type: String (Item)
      itemAttachments:
        description: The Attachment NBT data of the filtered item, optional
        javaType: java.lang.String
        type: String
      itemNBT:
        description: The NBT data of the filtered item, optional
        javaType: java.lang.String
        type: String
    humanName: Table (SorterItemStackFilter)
  mekanism.common.content.transporter.SorterModIDFilter:
    description: Logistical Sorter filter with Mod Id filter properties
    extends: mekanism.common.content.transporter.SorterFilter
    fields:
      modId:
        description: The mod id to filter. e.g. mekansim
        javaType: java.lang.String
        type: String
    humanName: Table (SorterModIDFilter)
  mekanism.common.content.transporter.SorterTagFilter:
    description: Logistical Sorter filter with Tag filter properties
    extends: mekanism.common.content.transporter.SorterFilter
    fields:
      tag:
        description: The tag to filter. e.g. forge:ores
        javaType: java.lang.String
        type: String
    humanName: Table (SorterTagFilter)
  mekanism.common.lib.frequency.Frequency:
    description: A frequency's identity
    fields:
      key:
        description: Usually the name of the frequency entered in the GUI
        javaType: java.lang.String
        type: String
      security:
        description: Whether the Frequency is public, trusted, or private
        javaType: mekanism.api.security.SecurityMode
        type: String (SecurityMode)
    humanName: Table (Frequency)
  net.minecraft.core.BlockPos:
    description: An xyz position
    fields:
      x:
        description: The x component
        javaType: int
        type: Number (int)
      y:
        description: The y component
        javaType: int
        type: Number (int)
      z:
        description: The z component
        javaType: int
        type: Number (int)
    humanName: Table (BlockPos)
  net.minecraft.core.GlobalPos:
    description: An xyz position with a dimension component
    fields:
      dimension:
        description: The dimension component
        javaType: net.minecraft.resources.ResourceLocation
        type: String (ResourceLocation)
      x:
        description: The x component
        javaType: int
        type: Number (int)
      y:
        description: The y component
        javaType: int
        type: Number (int)
      z:
        description: The z component
        javaType: int
        type: Number (int)
    humanName: Table (GlobalPos)
  net.minecraft.world.item.ItemStack:
    description: A stack of Item(s)
    fields:
      attachments:
        description: Any Attachment NBT of the item, in Command JSON format
        javaType: java.lang.String
        type: String
      count:
        description: The count of items in the stack
        javaType: int
        type: Number (int)
      name:
        description: The Item's registered name
        javaType: net.minecraft.world.item.Item
        type: String (Item)
      nbt:
        description: Any NBT of the item, in Command JSON format
        javaType: java.lang.String
        type: String
    humanName: Table (ItemStack)
  net.minecraft.world.level.block.state.BlockState:
    description: A Block State
    fields:
      block:
        description: The Block's registered name, e.g. minecraft:sand
        javaType: java.lang.String
        type: String
      state:
        description: Any state parameters will be in Table format under this key.
          Not present if there are none
        javaType: java.util.Map
        type: Table
    humanName: Table (BlockState)
  net.neoforged.neoforge.fluids.FluidStack:
    description: An amount of fluid
    fields:
      amount:
        description: The amount in mB
        javaType: int
        type: Number (int)
      name:
        description: The Fluid's registered name, e.g. minecraft:water
        javaType: net.minecraft.resources.ResourceLocation
        type: String (ResourceLocation)
      nbt:
        description: Any NBT of the fluid, in Command JSON format
        javaType: java.lang.String
        type: String
    humanName: Table (FluidStack)
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
  - GAS
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
  - GAS
  - INFUSION
  - PIGMENT
  - SLURRY
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
  - EXCESS_WASTE
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
    - javaType: mekanism.api.math.FloatingLong
      name: fe
      type: Number (FloatingLong)
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Convert Mekanism Joules to Forge Energy
    methodName: joulesToFE
    params:
    - javaType: mekanism.api.math.FloatingLong
      name: joules
      type: Number (FloatingLong)
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  'API Global: computerFilterHelper':
  - description: Create a Digital Miner Item Filter from an Item name
    methodName: createMinerItemFilter
    params:
    - javaType: net.minecraft.world.item.Item
      name: item
      type: String (Item)
    returns:
      javaType: mekanism.common.content.miner.MinerItemStackFilter
      type: Table (MinerItemStackFilter)
  - description: Create a Digital Miner Mod Id Filter from a mod id
    methodName: createMinerModIdFilter
    params:
    - javaType: java.lang.String
      name: modId
      type: String
    returns:
      javaType: mekanism.common.content.miner.MinerModIDFilter
      type: Table (MinerModIDFilter)
  - description: Create a Digital Miner Tag Filter from a Tag name
    methodName: createMinerTagFilter
    params:
    - javaType: java.lang.String
      name: tag
      type: String
    returns:
      javaType: mekanism.common.content.miner.MinerTagFilter
      type: Table (MinerTagFilter)
  - description: Create an Oredictionificator filter from a tag, without specifying
      an output item
    methodName: createOredictionificatorItemFilter
    params:
    - javaType: net.minecraft.resources.ResourceLocation
      name: filterTag
      type: String (ResourceLocation)
    returns:
      javaType: mekanism.common.content.oredictionificator.OredictionificatorItemFilter
      type: Table (OredictionificatorItemFilter)
  - description: Create an Oredictionificator filter from a tag and a selected output.
      The output is not validated.
    methodName: createOredictionificatorItemFilter
    params:
    - javaType: net.minecraft.resources.ResourceLocation
      name: filterTag
      type: String (ResourceLocation)
    - javaType: net.minecraft.world.item.Item
      name: selectedOutput
      type: String (Item)
    returns:
      javaType: mekanism.common.content.oredictionificator.OredictionificatorItemFilter
      type: Table (OredictionificatorItemFilter)
  - description: Create a QIO Item Filter structure from an Item name
    methodName: createQIOItemFilter
    params:
    - javaType: net.minecraft.world.item.Item
      name: item
      type: String (Item)
    returns:
      javaType: mekanism.common.content.qio.filter.QIOItemStackFilter
      type: Table (QIOItemStackFilter)
  - description: Create a QIO Mod Id Filter from a mod id
    methodName: createQIOModIdFilter
    params:
    - javaType: java.lang.String
      name: modId
      type: String
    returns:
      javaType: mekanism.common.content.qio.filter.QIOModIDFilter
      type: Table (QIOModIDFilter)
  - description: Create a QIO Tag Filter from a Tag name
    methodName: createQIOTagFilter
    params:
    - javaType: java.lang.String
      name: tag
      type: String
    returns:
      javaType: mekanism.common.content.qio.filter.QIOTagFilter
      type: Table (QIOTagFilter)
  - description: Create a Logistical Sorter Item Filter structure from an Item name
    methodName: createSorterItemFilter
    params:
    - javaType: net.minecraft.world.item.Item
      name: item
      type: String (Item)
    returns:
      javaType: mekanism.common.content.transporter.SorterItemStackFilter
      type: Table (SorterItemStackFilter)
  - description: Create a Logistical Sorter Mod Id Filter structure from a mod id
    methodName: createSorterModIdFilter
    params:
    - javaType: java.lang.String
      name: modId
      type: String
    returns:
      javaType: mekanism.common.content.transporter.SorterModIDFilter
      type: Table (SorterModIDFilter)
  - description: Create a Logistical Sorter Tag Filter from a tag
    methodName: createSorterTagFilter
    params:
    - javaType: java.lang.String
      name: tag
      type: String
    returns:
      javaType: mekanism.common.content.transporter.SorterTagFilter
      type: Table (SorterTagFilter)
  Antiprotonic Nucleosynthesizer:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the input gas tank.
    methodName: getInputChemical
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the input gas tank.
    methodName: getInputChemicalCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the input gas tank.
    methodName: getInputChemicalFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the input gas item slot.
    methodName: getInputChemicalItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the input gas tank.
    methodName: getInputChemicalNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the input item slot.
    methodName: getInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Bin:
  - description: Get the maximum number of items the bin can contain.
    methodName: getCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the type of item the Bin is locked to (or Air if not locked)
    methodName: getLock
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the bin.
    methodName: getStored
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: If true, the Bin is locked to a particular item type.
    methodName: isLocked
    returns:
      javaType: boolean
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
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the biofuel tank.
    methodName: getBioFuelCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the filled percentage of the biofuel tank.
    methodName: getBioFuelFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the biofuel tank.
    methodName: getBioFuelNeeded
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the energy item.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the fuel slot.
    methodName: getFuelItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Boiler Multiblock (formed):
  - description: Get the maximum possible boil rate for this Boiler, based on the
      number of Superheating Elements
    methodName: getBoilCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the rate of boiling (mB/t)
    methodName: getBoilRate
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the cooled coolant tank.
    methodName: getCooledCoolant
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the cooled coolant tank.
    methodName: getCooledCoolantCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the cooled coolant tank.
    methodName: getCooledCoolantFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the cooled coolant tank.
    methodName: getCooledCoolantNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the amount of heat lost to the environment in the last tick (Kelvin)
    methodName: getEnvironmentalLoss
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the heated coolant tank.
    methodName: getHeatedCoolant
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the heated coolant tank.
    methodName: getHeatedCoolantCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the heated coolant tank.
    methodName: getHeatedCoolantFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the heated coolant tank.
    methodName: getHeatedCoolantNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the maximum rate of boiling seen (mB/t)
    methodName: getMaxBoilRate
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the steam tank.
    methodName: getSteam
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the steam tank.
    methodName: getSteamCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the steam tank.
    methodName: getSteamFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the steam tank.
    methodName: getSteamNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: How many superheaters this Boiler has
    methodName: getSuperheaters
    returns:
      javaType: int
      type: Number (int)
  - description: Get the temperature of the boiler in Kelvin.
    methodName: getTemperature
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the water tank.
    methodName: getWater
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the water tank.
    methodName: getWaterCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the filled percentage of the water tank.
    methodName: getWaterFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the water tank.
    methodName: getWaterNeeded
    returns:
      javaType: int
      type: Number (int)
  Boiler Valve:
  - description: Toggle the current valve configuration to the previous option in
      the list
    methodName: decrementMode
  - description: Get the current configuration of this valve
    methodName: getMode
    returns:
      javaType: mekanism.common.block.attribute.AttributeStateBoilerValveMode$BoilerValveMode
      type: String (BoilerValveMode)
  - description: Toggle the current valve configuration to the next option in the
      list
    methodName: incrementMode
  - description: Change the configuration of this valve
    methodName: setMode
    params:
    - javaType: mekanism.common.block.attribute.AttributeStateBoilerValveMode$BoilerValveMode
      name: mode
      type: String (BoilerValveMode)
  Chemical Crystallizer:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the input tank.
    methodName: getInput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the input tank.
    methodName: getInputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the input tank.
    methodName: getInputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the input item slot.
    methodName: getInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the input tank.
    methodName: getInputNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the output slot.
    methodName: getOutput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Chemical Dissolution Chamber:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the gas input tank.
    methodName: getGasInput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the gas input tank.
    methodName: getGasInputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the gas input tank.
    methodName: getGasInputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the gas input tank.
    methodName: getGasInputNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the gas input item slot.
    methodName: getInputGasItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output tank.
    methodName: getOutput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output tank.
    methodName: getOutputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the output tank.
    methodName: getOutputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output tank.
    methodName: getOutputNeeded
    returns:
      javaType: long
      type: Number (long)
  Chemical Infuser:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the left input tank.
    methodName: getLeftInput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the left input tank.
    methodName: getLeftInputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the left input tank.
    methodName: getLeftInputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the left input item slot.
    methodName: getLeftInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the left input tank.
    methodName: getLeftInputNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the output (center) tank.
    methodName: getOutput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output (center) tank.
    methodName: getOutputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the output (center) tank.
    methodName: getOutputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the output item slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output (center) tank.
    methodName: getOutputNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the right input tank.
    methodName: getRightInput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the right input tank.
    methodName: getRightInputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the right input tank.
    methodName: getRightInputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the right input item slot.
    methodName: getRightInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the right input tank.
    methodName: getRightInputNeeded
    returns:
      javaType: long
      type: Number (long)
  Chemical Oxidizer:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the input slot.
    methodName: getInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output tank.
    methodName: getOutput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output tank.
    methodName: getOutputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the output tank.
    methodName: getOutputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the output item slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output tank.
    methodName: getOutputNeeded
    returns:
      javaType: long
      type: Number (long)
  Chemical Tank:
  - description: Descend the Dumping mode to the previous configuration in the list
    methodName: decrementDumpingMode
    requiresPublicSecurity: true
  - description: Get the capacity of the tank.
    methodName: getCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the drain slot.
    methodName: getDrainItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the current Dumping configuration
    methodName: getDumpingMode
    returns:
      javaType: mekanism.common.tile.TileEntityChemicalTank$GasMode
      type: String (GasMode)
  - description: Get the contents of the fill slot.
    methodName: getFillItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the filled percentage of the tank.
    methodName: getFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the tank.
    methodName: getNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the tank.
    methodName: getStored
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Advance the Dumping mode to the next configuration in the list
    methodName: incrementDumpingMode
    requiresPublicSecurity: true
  - description: Set the Dumping mode of the tank
    methodName: setDumpingMode
    params:
    - javaType: mekanism.common.tile.TileEntityChemicalTank$GasMode
      name: mode
      type: String (GasMode)
    requiresPublicSecurity: true
  Chemical Washer:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the fluid tank.
    methodName: getFluid
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the fluid tank.
    methodName: getFluidCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the filled percentage of the fluid tank.
    methodName: getFluidFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the fluid item input slot.
    methodName: getFluidItemInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the fluid item output slot.
    methodName: getFluidItemOutput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the fluid tank.
    methodName: getFluidNeeded
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the slurry item output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the input slurry tank.
    methodName: getSlurryInput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the input slurry tank.
    methodName: getSlurryInputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the input slurry tank.
    methodName: getSlurryInputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the input slurry tank.
    methodName: getSlurryInputNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the output slurry tank.
    methodName: getSlurryOutput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output slurry tank.
    methodName: getSlurryOutputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the output slurry tank.
    methodName: getSlurryOutputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the output slurry tank.
    methodName: getSlurryOutputNeeded
    returns:
      javaType: long
      type: Number (long)
  Combiner:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the main input slot.
    methodName: getMainInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the secondary input slot.
    methodName: getSecondaryInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Combining Factory:
  - description: Get the contents of the secondary input slot.
    methodName: getSecondaryInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Compressing/Injecting/Purifying Factory:
  - description: Empty the contents of the gas tank into the environment
    methodName: dumpChemical
    requiresPublicSecurity: true
  - description: Get the contents of the gas tank.
    methodName: getChemical
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the gas tank.
    methodName: getChemicalCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the gas tank.
    methodName: getChemicalFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the chemical item (extra) slot.
    methodName: getChemicalItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the gas tank.
    methodName: getChemicalNeeded
    returns:
      javaType: long
      type: Number (long)
  Compressing/Injecting/Purifying Machine:
  - description: Empty the contents of the gas tank into the environment
    methodName: dumpChemical
    requiresPublicSecurity: true
  - description: Get the contents of the gas tank.
    methodName: getChemical
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the gas tank.
    methodName: getChemicalCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the gas tank.
    methodName: getChemicalFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the secondary input slot.
    methodName: getChemicalItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the gas tank.
    methodName: getChemicalNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the input slot.
    methodName: getInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Digital Miner:
  - description: Add a new filter to the miner. Requires miner to be stopped/reset
      first
    methodName: addFilter
    params:
    - javaType: mekanism.common.content.miner.MinerFilter
      name: filter
      type: Table (MinerFilter)
    requiresPublicSecurity: true
    returns:
      javaType: boolean
      type: boolean
  - description: Remove the target for Replacement in Inverse Mode. Requires miner
      to be stopped/reset first
    methodName: clearInverseModeReplaceTarget
    requiresPublicSecurity: true
  - description: Whether Auto Eject is turned on
    methodName: getAutoEject
    returns:
      javaType: boolean
      type: boolean
  - description: Whether Auto Pull is turned on
    methodName: getAutoPull
    returns:
      javaType: boolean
      type: boolean
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the current list of Miner Filters
    methodName: getFilters
    returns:
      javaExtra:
      - mekanism.common.content.miner.MinerFilter
      javaType: java.util.Collection
      type: List (Table (MinerFilter))
  - description: Whether Inverse Mode is enabled or not
    methodName: getInverseMode
    returns:
      javaType: boolean
      type: boolean
  - description: Get the configured Replacement target item
    methodName: getInverseModeReplaceTarget
    returns:
      javaType: net.minecraft.world.item.Item
      type: String (Item)
  - description: Whether Inverse Mode Require Replacement is turned on
    methodName: getInverseModeRequiresReplacement
    returns:
      javaType: boolean
      type: boolean
  - description: Get the contents of the internal inventory slot. 0 based.
    methodName: getItemInSlot
    params:
    - javaType: int
      name: slot
      type: Number (int)
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the maximum allowable Radius value, determined from the mod's
      config
    methodName: getMaxRadius
    returns:
      javaType: int
      type: Number (int)
  - description: Gets the configured maximum Y level for mining
    methodName: getMaxY
    returns:
      javaType: int
      type: Number (int)
  - description: Gets the configured minimum Y level for mining
    methodName: getMinY
    returns:
      javaType: int
      type: Number (int)
  - description: Get the current radius configured (blocks)
    methodName: getRadius
    returns:
      javaType: int
      type: Number (int)
  - description: Whether Silk Touch mode is enabled or not
    methodName: getSilkTouch
    returns:
      javaType: boolean
      type: boolean
  - description: Get the size of the Miner's internal inventory
    methodName: getSlotCount
    returns:
      javaType: int
      type: Number (int)
  - description: Get the state of the Miner's search
    methodName: getState
    returns:
      javaType: mekanism.common.content.miner.ThreadMinerSearch$State
      type: String (State)
  - description: Get the count of block found but not yet mined
    methodName: getToMine
    returns:
      javaType: int
      type: Number (int)
  - description: Whether the miner is currently running
    methodName: isRunning
    returns:
      javaType: boolean
      type: boolean
  - description: Removes the exactly matching filter from the miner. Requires miner
      to be stopped/reset first
    methodName: removeFilter
    params:
    - javaType: mekanism.common.content.miner.MinerFilter
      name: filter
      type: Table (MinerFilter)
    requiresPublicSecurity: true
    returns:
      javaType: boolean
      type: boolean
  - description: Stop the mining process and reset the Miner to be able to change
      settings
    methodName: reset
    requiresPublicSecurity: true
  - description: Update the Auto Eject setting
    methodName: setAutoEject
    params:
    - javaType: boolean
      name: eject
      type: boolean
    requiresPublicSecurity: true
  - description: Update the Auto Pull setting
    methodName: setAutoPull
    params:
    - javaType: boolean
      name: pull
      type: boolean
    requiresPublicSecurity: true
  - description: Update the Inverse Mode setting. Requires miner to be stopped/reset
      first
    methodName: setInverseMode
    params:
    - javaType: boolean
      name: enabled
      type: boolean
    requiresPublicSecurity: true
  - description: Update the target for Replacement in Inverse Mode. Requires miner
      to be stopped/reset first
    methodName: setInverseModeReplaceTarget
    params:
    - javaType: net.minecraft.world.item.Item
      name: target
      type: String (Item)
    requiresPublicSecurity: true
  - description: Update the Inverse Mode Requires Replacement setting. Requires miner
      to be stopped/reset first
    methodName: setInverseModeRequiresReplacement
    params:
    - javaType: boolean
      name: requiresReplacement
      type: boolean
    requiresPublicSecurity: true
  - description: Update the maximum Y level for mining. Requires miner to be stopped/reset
      first
    methodName: setMaxY
    params:
    - javaType: int
      name: maxY
      type: Number (int)
    requiresPublicSecurity: true
  - description: Update the minimum Y level for mining. Requires miner to be stopped/reset
      first
    methodName: setMinY
    params:
    - javaType: int
      name: minY
      type: Number (int)
    requiresPublicSecurity: true
  - description: Update the mining radius (blocks). Requires miner to be stopped/reset
      first
    methodName: setRadius
    params:
    - javaType: int
      name: radius
      type: Number (int)
    requiresPublicSecurity: true
  - description: Update the Silk Touch setting
    methodName: setSilkTouch
    params:
    - javaType: boolean
      name: silk
      type: boolean
    requiresPublicSecurity: true
  - description: Attempt to start the mining process
    methodName: start
    requiresPublicSecurity: true
  - description: Attempt to stop the mining process
    methodName: stop
    requiresPublicSecurity: true
  Dimensional Stabilizer:
  - description: 'Sets the chunks in the specified radius to not be kept loaded. The
      chunk the Stabilizer is in is always loaded. Range: [1, 2]'
    methodName: disableChunkLoadingFor
    params:
    - javaType: int
      name: radius
      type: Number (int)
    requiresPublicSecurity: true
  - description: 'Sets the chunks in the specified radius to be loaded. The chunk
      the Stabilizer is in is always loaded. Range: [1, 2]'
    methodName: enableChunkLoadingFor
    params:
    - javaType: int
      name: radius
      type: Number (int)
    requiresPublicSecurity: true
  - description: Get the number of chunks being loaded.
    methodName: getChunksLoaded
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: 'Check if the Dimensional Stabilizer is configured to load a the
      specified relative chunk position at x,y (Stabilizer is at 0,0). Range: [-2,
      2]'
    methodName: isChunkLoadingAt
    params:
    - javaType: int
      name: x
      type: Number (int)
    - javaType: int
      name: z
      type: Number (int)
    returns:
      javaType: boolean
      type: boolean
  - description: 'Set if the Dimensional Stabilizer is configured to load a the specified
      relative position (Stabilizer is at 0,0). True = load the chunk, false = don''t
      load the chunk. Range: [-2, 2]'
    methodName: setChunkLoadingAt
    params:
    - javaType: int
      name: x
      type: Number (int)
    - javaType: int
      name: z
      type: Number (int)
    - javaType: boolean
      name: load
      type: boolean
    requiresPublicSecurity: true
  - description: 'Toggle loading the specified relative chunk at the relative x,y
      position (Stabilizer is at 0,0). Just like clicking the button in the GUI. Range:
      [-2, 2]'
    methodName: toggleChunkLoadingAt
    params:
    - javaType: int
      name: x
      type: Number (int)
    - javaType: int
      name: z
      type: Number (int)
    requiresPublicSecurity: true
  Diversion Transporter:
  - methodName: decrementMode
    params:
    - javaType: net.minecraft.core.Direction
      name: side
      type: String (Direction)
  - methodName: getMode
    params:
    - javaType: net.minecraft.core.Direction
      name: side
      type: String (Direction)
    returns:
      javaType: mekanism.common.content.network.transmitter.DiversionTransporter$DiversionControl
      type: String (DiversionControl)
  - methodName: incrementMode
    params:
    - javaType: net.minecraft.core.Direction
      name: side
      type: String (Direction)
  - methodName: setMode
    params:
    - javaType: net.minecraft.core.Direction
      name: side
      type: String (Direction)
    - javaType: mekanism.common.content.network.transmitter.DiversionTransporter$DiversionControl
      name: mode
      type: String (DiversionControl)
  Dynamic Tank Multiblock (formed):
  - methodName: decrementContainerEditMode
  - methodName: getChemicalTankCapacity
    returns:
      javaType: long
      type: Number (long)
  - methodName: getContainerEditMode
    returns:
      javaType: mekanism.common.tile.interfaces.IFluidContainerManager$ContainerEditMode
      type: String (ContainerEditMode)
  - methodName: getFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getStored
    returns:
      javaExtra:
      - mekanism.api.chemical.ChemicalStack
      - net.neoforged.neoforge.fluids.FluidStack
      javaType: com.mojang.datafixers.util.Either
      type: Table (ChemicalStack) or Table (FluidStack)
  - methodName: getTankCapacity
    returns:
      javaType: int
      type: Number (int)
  - methodName: incrementContainerEditMode
  - methodName: setContainerEditMode
    params:
    - javaType: mekanism.common.tile.interfaces.IFluidContainerManager$ContainerEditMode
      name: mode
      type: String (ContainerEditMode)
  Electric Machine:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the input slot.
    methodName: getInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Electric Pump:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the buffer tank.
    methodName: getFluid
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the buffer tank.
    methodName: getFluidCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the filled percentage of the buffer tank.
    methodName: getFluidFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the buffer tank.
    methodName: getFluidNeeded
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: reset
    requiresPublicSecurity: true
  Electrolytic Separator:
  - methodName: decrementLeftOutputDumpingMode
    requiresPublicSecurity: true
  - methodName: decrementRightOutputDumpingMode
    requiresPublicSecurity: true
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the input tank.
    methodName: getInput
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the input tank.
    methodName: getInputCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the filled percentage of the input tank.
    methodName: getInputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the input item slot.
    methodName: getInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the input tank.
    methodName: getInputNeeded
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the left output tank.
    methodName: getLeftOutput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the left output tank.
    methodName: getLeftOutputCapacity
    returns:
      javaType: long
      type: Number (long)
  - methodName: getLeftOutputDumpingMode
    returns:
      javaType: mekanism.common.tile.TileEntityChemicalTank$GasMode
      type: String (GasMode)
  - description: Get the filled percentage of the left output tank.
    methodName: getLeftOutputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the left output item slot.
    methodName: getLeftOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the left output tank.
    methodName: getLeftOutputNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the right output tank.
    methodName: getRightOutput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the right output tank.
    methodName: getRightOutputCapacity
    returns:
      javaType: long
      type: Number (long)
  - methodName: getRightOutputDumpingMode
    returns:
      javaType: mekanism.common.tile.TileEntityChemicalTank$GasMode
      type: String (GasMode)
  - description: Get the filled percentage of the right output tank.
    methodName: getRightOutputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the right output item slot.
    methodName: getRightOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the right output tank.
    methodName: getRightOutputNeeded
    returns:
      javaType: long
      type: Number (long)
  - methodName: incrementLeftOutputDumpingMode
    requiresPublicSecurity: true
  - methodName: incrementRightOutputDumpingMode
    requiresPublicSecurity: true
  - methodName: setLeftOutputDumpingMode
    params:
    - javaType: mekanism.common.tile.TileEntityChemicalTank$GasMode
      name: mode
      type: String (GasMode)
    requiresPublicSecurity: true
  - methodName: setRightOutputDumpingMode
    params:
    - javaType: mekanism.common.tile.TileEntityChemicalTank$GasMode
      name: mode
      type: String (GasMode)
    requiresPublicSecurity: true
  Energy Cube:
  - description: Get the contents of the charge slot.
    methodName: getChargeItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the discharge slot.
    methodName: getDischargeItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Factory Machine:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - methodName: getInput
    params:
    - javaType: int
      name: process
      type: Number (int)
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getOutput
    params:
    - javaType: int
      name: process
      type: Number (int)
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getRecipeProgress
    params:
    - javaType: int
      name: process
      type: Number (int)
    returns:
      javaType: int
      type: Number (int)
  - description: Total number of ticks it takes currently for the recipe to complete
    methodName: getTicksRequired
    returns:
      javaType: int
      type: Number (int)
  - methodName: isAutoSortEnabled
    returns:
      javaType: boolean
      type: boolean
  - methodName: setAutoSort
    params:
    - javaType: boolean
      name: enabled
      type: boolean
    requiresPublicSecurity: true
  Filter Wrapper:
  - methodName: getFilterType
    returns:
      javaType: mekanism.common.content.filter.FilterType
      type: String (FilterType)
  - methodName: isEnabled
    returns:
      javaType: boolean
      type: boolean
  - methodName: setEnabled
    params:
    - javaType: boolean
      name: enabled
      type: boolean
  Filter Wrapper (Digital Miner):
  - methodName: clone
    returns:
      javaType: mekanism.common.content.miner.MinerFilter
      type: Table (MinerFilter)
  - methodName: getReplaceTarget
    returns:
      javaType: net.minecraft.world.item.Item
      type: String (Item)
  - methodName: getRequiresReplacement
    returns:
      javaType: boolean
      type: boolean
  - methodName: hasBlacklistedElement
    returns:
      javaType: boolean
      type: boolean
  - methodName: setReplaceTarget
    params:
    - javaType: net.minecraft.world.item.Item
      name: value
      type: String (Item)
  - methodName: setRequiresReplacement
    params:
    - javaType: boolean
      name: value
      type: boolean
  Filter Wrapper (ItemStack):
  - methodName: getItemStack
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: setItem
    params:
    - javaType: net.minecraft.world.item.Item
      name: item
      type: String (Item)
  - methodName: setItemStack
    params:
    - javaType: net.minecraft.world.item.ItemStack
      name: stack
      type: Table (ItemStack)
  Filter Wrapper (Logistical Sorter):
  - methodName: clone
    returns:
      javaType: mekanism.common.content.transporter.SorterFilter
      type: Table (SorterFilter)
  - methodName: getAllowDefault
    returns:
      javaType: boolean
      type: boolean
  - methodName: getColor
    returns:
      javaType: mekanism.api.text.EnumColor
      type: String (EnumColor)
  - methodName: getMax
    returns:
      javaType: int
      type: Number (int)
  - methodName: getMin
    returns:
      javaType: int
      type: Number (int)
  - methodName: getSizeMode
    returns:
      javaType: boolean
      type: boolean
  - methodName: setAllowDefault
    params:
    - javaType: boolean
      name: value
      type: boolean
  - methodName: setColor
    params:
    - javaType: mekanism.api.text.EnumColor
      name: value
      type: String (EnumColor)
  - methodName: setMinMax
    params:
    - javaType: int
      name: min
      type: Number (int)
    - javaType: int
      name: max
      type: Number (int)
  - methodName: setSizeMode
    params:
    - javaType: boolean
      name: value
      type: boolean
  Filter Wrapper (Mod Id):
  - methodName: getModID
    returns:
      javaType: java.lang.String
      type: String
  - methodName: setModID
    params:
    - javaType: java.lang.String
      name: id
      type: String
  Filter Wrapper (Oredictionificator Item):
  - methodName: getSelectedOutput
    returns:
      javaType: net.minecraft.world.item.Item
      type: String (Item)
  - methodName: setSelectedOutput
    params:
    - javaType: net.minecraft.world.item.Item
      name: item
      type: String (Item)
  Filter Wrapper (Oredictionificator):
  - methodName: clone
    returns:
      javaType: mekanism.common.content.oredictionificator.OredictionificatorFilter
      type: Table (OredictionificatorFilter)
  - methodName: getFilter
    returns:
      javaType: java.lang.String
      type: String
  - methodName: setFilter
    params:
    - javaType: net.minecraft.resources.ResourceLocation
      name: tag
      type: String (ResourceLocation)
  Filter Wrapper (QIO):
  - methodName: clone
    returns:
      javaType: mekanism.common.content.qio.filter.QIOFilter
      type: Table (QIOFilter)
  Filter Wrapper (Tag):
  - methodName: getTagName
    returns:
      javaType: java.lang.String
      type: String
  - methodName: setTagName
    params:
    - javaType: java.lang.String
      name: name
      type: String
  Fission Reactor Logic Adapter:
  - methodName: getLogicMode
    returns:
      javaType: mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter$FissionReactorLogic
      type: String (FissionReactorLogic)
  - methodName: getRedstoneLogicStatus
    returns:
      javaType: mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter$RedstoneStatus
      type: String (RedstoneStatus)
  - methodName: setLogicMode
    params:
    - javaType: mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter$FissionReactorLogic
      name: logicType
      type: String (FissionReactorLogic)
  Fission Reactor Multiblock (formed):
  - description: Must be disabled, and if meltdowns are disabled must not have been
      force disabled
    methodName: activate
  - description: Actual burn rate as it may be lower if say there is not enough fuel
    methodName: getActualBurnRate
    returns:
      javaType: double
      type: Number (double)
  - methodName: getBoilEfficiency
    returns:
      javaType: double
      type: Number (double)
  - description: Configured burn rate
    methodName: getBurnRate
    returns:
      javaType: double
      type: Number (double)
  - methodName: getCoolant
    returns:
      javaExtra:
      - mekanism.api.chemical.ChemicalStack
      - net.neoforged.neoforge.fluids.FluidStack
      javaType: com.mojang.datafixers.util.Either
      type: Table (ChemicalStack) or Table (FluidStack)
  - methodName: getCoolantCapacity
    returns:
      javaType: long
      type: Number (long)
  - methodName: getCoolantFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - methodName: getCoolantNeeded
    returns:
      javaType: long
      type: Number (long)
  - methodName: getDamagePercent
    returns:
      javaType: long
      type: Number (long)
  - methodName: getEnvironmentalLoss
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the fuel tank.
    methodName: getFuel
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - methodName: getFuelAssemblies
    returns:
      javaType: int
      type: Number (int)
  - description: Get the capacity of the fuel tank.
    methodName: getFuelCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the fuel tank.
    methodName: getFuelFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the fuel tank.
    methodName: getFuelNeeded
    returns:
      javaType: long
      type: Number (long)
  - methodName: getFuelSurfaceArea
    returns:
      javaType: int
      type: Number (int)
  - methodName: getHeatCapacity
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the heated coolant.
    methodName: getHeatedCoolant
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the heated coolant.
    methodName: getHeatedCoolantCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the heated coolant.
    methodName: getHeatedCoolantFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the heated coolant.
    methodName: getHeatedCoolantNeeded
    returns:
      javaType: long
      type: Number (long)
  - methodName: getHeatingRate
    returns:
      javaType: long
      type: Number (long)
  - methodName: getMaxBurnRate
    returns:
      javaType: long
      type: Number (long)
  - description: true -> active, false -> off
    methodName: getStatus
    returns:
      javaType: boolean
      type: boolean
  - description: Get the temperature of the reactor in Kelvin.
    methodName: getTemperature
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the waste tank.
    methodName: getWaste
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the waste tank.
    methodName: getWasteCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the waste tank.
    methodName: getWasteFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the waste tank.
    methodName: getWasteNeeded
    returns:
      javaType: long
      type: Number (long)
  - methodName: isForceDisabled
    returns:
      javaType: boolean
      type: boolean
  - description: Must be enabled
    methodName: scram
  - methodName: setBurnRate
    params:
    - javaType: double
      name: rate
      type: Number (double)
  Fission Reactor Port:
  - methodName: decrementMode
  - methodName: getMode
    returns:
      javaType: mekanism.generators.common.block.attribute.AttributeStateFissionPortMode$FissionPortMode
      type: String (FissionPortMode)
  - methodName: incrementMode
  - methodName: setMode
    params:
    - javaType: mekanism.generators.common.block.attribute.AttributeStateFissionPortMode$FissionPortMode
      name: mode
      type: String (FissionPortMode)
  Fluid Tank:
  - methodName: decrementContainerEditMode
    requiresPublicSecurity: true
  - description: Get the capacity of the tank.
    methodName: getCapacity
    returns:
      javaType: int
      type: Number (int)
  - methodName: getContainerEditMode
    returns:
      javaType: mekanism.common.tile.interfaces.IFluidContainerManager$ContainerEditMode
      type: String (ContainerEditMode)
  - description: Get the filled percentage of the tank.
    methodName: getFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the tank.
    methodName: getNeeded
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the tank.
    methodName: getStored
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - methodName: incrementContainerEditMode
    requiresPublicSecurity: true
  - methodName: setContainerEditMode
    params:
    - javaType: mekanism.common.tile.interfaces.IFluidContainerManager$ContainerEditMode
      name: mode
      type: String (ContainerEditMode)
    requiresPublicSecurity: true
  Fluidic Plenisher:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the buffer tank.
    methodName: getFluid
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the buffer tank.
    methodName: getFluidCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the filled percentage of the buffer tank.
    methodName: getFluidFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the buffer tank.
    methodName: getFluidNeeded
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: reset
    requiresPublicSecurity: true
  Formulaic Assemblicator:
  - description: Requires recipe and auto mode to be disabled
    methodName: craftAvailableItems
    requiresPublicSecurity: true
  - description: Requires recipe and auto mode to be disabled
    methodName: craftSingleItem
    requiresPublicSecurity: true
  - description: Requires auto mode to be disabled
    methodName: emptyGrid
    requiresPublicSecurity: true
  - description: Requires an unencoded formula in the formula slot and a valid recipe
    methodName: encodeFormula
    requiresPublicSecurity: true
  - description: Requires auto mode to be disabled
    methodName: fillGrid
    requiresPublicSecurity: true
  - description: Requires valid encoded formula
    methodName: getAutoMode
    requiresPublicSecurity: true
    returns:
      javaType: boolean
      type: boolean
  - methodName: getCraftingInputSlot
    params:
    - javaType: int
      name: slot
      type: Number (int)
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getCraftingOutputSlot
    params:
    - javaType: int
      name: slot
      type: Number (int)
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getCraftingOutputSlots
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getExcessRemainingItems
    returns:
      javaExtra:
      - net.minecraft.world.item.ItemStack
      javaType: net.minecraft.core.NonNullList
      type: List (Table (ItemStack))
  - description: Get the contents of the formula slot.
    methodName: getFormulaItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getItemInSlot
    params:
    - javaType: int
      name: slot
      type: Number (int)
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getRecipeProgress
    returns:
      javaType: int
      type: Number (int)
  - methodName: getSlots
    returns:
      javaType: int
      type: Number (int)
  - description: Requires valid encoded formula
    methodName: getStockControl
    requiresPublicSecurity: true
    returns:
      javaType: boolean
      type: boolean
  - methodName: getTicksRequired
    returns:
      javaType: int
      type: Number (int)
  - methodName: hasRecipe
    returns:
      javaType: boolean
      type: boolean
  - methodName: hasValidFormula
    returns:
      javaType: boolean
      type: boolean
  - description: Requires valid encoded formula
    methodName: setAutoMode
    params:
    - javaType: boolean
      name: mode
      type: boolean
    requiresPublicSecurity: true
  - description: Requires valid encoded formula
    methodName: setStockControl
    params:
    - javaType: boolean
      name: mode
      type: boolean
    requiresPublicSecurity: true
  Fuelwood Heater:
  - methodName: getEnvironmentalLoss
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the fuel slot.
    methodName: getFuelItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the temperature of the heater in Kelvin.
    methodName: getTemperature
    returns:
      javaType: double
      type: Number (double)
  - methodName: getTransferLoss
    returns:
      javaType: double
      type: Number (double)
  Fusion Reactor Logic Adapter:
  - methodName: getLogicMode
    returns:
      javaType: mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter$FusionReactorLogic
      type: String (FusionReactorLogic)
  - methodName: isActiveCooledLogic
    returns:
      javaType: boolean
      type: boolean
  - methodName: setActiveCooledLogic
    params:
    - javaType: boolean
      name: active
      type: boolean
  - methodName: setLogicMode
    params:
    - javaType: mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter$FusionReactorLogic
      name: logicType
      type: String (FusionReactorLogic)
  Fusion Reactor Multiblock (formed):
  - methodName: getCaseTemperature
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the fuel tank.
    methodName: getDTFuel
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the fuel tank.
    methodName: getDTFuelCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the fuel tank.
    methodName: getDTFuelFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the fuel tank.
    methodName: getDTFuelNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the deuterium tank.
    methodName: getDeuterium
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the deuterium tank.
    methodName: getDeuteriumCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the deuterium tank.
    methodName: getDeuteriumFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the deuterium tank.
    methodName: getDeuteriumNeeded
    returns:
      javaType: long
      type: Number (long)
  - methodName: getEnvironmentalLoss
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the Hohlraum slot.
    methodName: getHohlraum
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: true -> water cooled, false -> air cooled
    methodName: getIgnitionTemperature
    params:
    - javaType: boolean
      name: active
      type: boolean
    returns:
      javaType: double
      type: Number (double)
  - methodName: getInjectionRate
    returns:
      javaType: int
      type: Number (int)
  - description: true -> water cooled, false -> air cooled
    methodName: getMaxCasingTemperature
    params:
    - javaType: boolean
      name: active
      type: boolean
    returns:
      javaType: double
      type: Number (double)
  - description: true -> water cooled, false -> air cooled
    methodName: getMaxPlasmaTemperature
    params:
    - javaType: boolean
      name: active
      type: boolean
    returns:
      javaType: double
      type: Number (double)
  - description: true -> water cooled, false -> air cooled
    methodName: getMinInjectionRate
    params:
    - javaType: boolean
      name: active
      type: boolean
    returns:
      javaType: int
      type: Number (int)
  - methodName: getPassiveGeneration
    params:
    - javaType: boolean
      name: active
      type: boolean
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - methodName: getPlasmaTemperature
    returns:
      javaType: double
      type: Number (double)
  - methodName: getProductionRate
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the steam tank.
    methodName: getSteam
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the steam tank.
    methodName: getSteamCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the steam tank.
    methodName: getSteamFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the steam tank.
    methodName: getSteamNeeded
    returns:
      javaType: long
      type: Number (long)
  - methodName: getTransferLoss
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the tritium tank.
    methodName: getTritium
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the tritium tank.
    methodName: getTritiumCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the tritium tank.
    methodName: getTritiumFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the tritium tank.
    methodName: getTritiumNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the water tank.
    methodName: getWater
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the water tank.
    methodName: getWaterCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the filled percentage of the water tank.
    methodName: getWaterFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the water tank.
    methodName: getWaterNeeded
    returns:
      javaType: int
      type: Number (int)
  - description: Checks if a reaction is occurring.
    methodName: isIgnited
    returns:
      javaType: boolean
      type: boolean
  - methodName: setInjectionRate
    params:
    - javaType: int
      name: rate
      type: Number (int)
  Fusion Reactor Port:
  - description: true -> output, false -> input
    methodName: getMode
    returns:
      javaType: boolean
      type: boolean
  - description: true -> output, false -> input
    methodName: setMode
    params:
    - javaType: boolean
      name: output
      type: boolean
  Gas Generator:
  - methodName: getBurnRate
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the energy item slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the fuel tank.
    methodName: getFuel
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the fuel tank.
    methodName: getFuelCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the fuel tank.
    methodName: getFuelFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the fuel item slot.
    methodName: getFuelItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the fuel tank.
    methodName: getFuelNeeded
    returns:
      javaType: long
      type: Number (long)
  Generator:
  - methodName: getMaxOutput
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the amount of energy produced by this generator in the last tick.
    methodName: getProductionRate
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  Generic Mekanism Machine:
  - methodName: getComparatorLevel
    restriction: COMPARATOR
    returns:
      javaType: int
      type: Number (int)
  - methodName: getDirection
    restriction: DIRECTIONAL
    returns:
      javaType: net.minecraft.core.Direction
      type: String (Direction)
  - methodName: getEnergy
    restriction: ENERGY
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - methodName: getEnergyFilledPercentage
    restriction: ENERGY
    returns:
      javaType: double
      type: Number (double)
  - methodName: getEnergyNeeded
    restriction: ENERGY
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - methodName: getMaxEnergy
    restriction: ENERGY
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - methodName: getRedstoneMode
    restriction: REDSTONE_CONTROL
    returns:
      javaType: mekanism.common.tile.interfaces.IRedstoneControl$RedstoneControl
      type: String (RedstoneControl)
  - methodName: setRedstoneMode
    params:
    - javaType: mekanism.common.tile.interfaces.IRedstoneControl$RedstoneControl
      name: type
      type: String (RedstoneControl)
    requiresPublicSecurity: true
    restriction: REDSTONE_CONTROL
  Heat Generator:
  - description: Get the contents of the energy item slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getEnvironmentalLoss
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the fuel item slot.
    methodName: getFuelItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the lava tank.
    methodName: getLava
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the lava tank.
    methodName: getLavaCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the filled percentage of the lava tank.
    methodName: getLavaFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the lava tank.
    methodName: getLavaNeeded
    returns:
      javaType: int
      type: Number (int)
  - description: Get the temperature of the generator in Kelvin.
    methodName: getTemperature
    returns:
      javaType: double
      type: Number (double)
  - methodName: getTransferLoss
    returns:
      javaType: double
      type: Number (double)
  Induction Matrix Multiblock (formed):
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getInstalledCells
    returns:
      javaType: int
      type: Number (int)
  - methodName: getInstalledProviders
    returns:
      javaType: int
      type: Number (int)
  - methodName: getLastInput
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - methodName: getLastOutput
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getTransferCap
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  Induction Matrix Port:
  - description: true -> output, false -> input.
    methodName: getMode
    returns:
      javaType: boolean
      type: boolean
  - description: true -> output, false -> input
    methodName: setMode
    params:
    - javaType: boolean
      name: output
      type: boolean
  Industrial Turbine Multiblock (formed):
  - methodName: decrementDumpingMode
  - methodName: getBlades
    returns:
      javaType: int
      type: Number (int)
  - methodName: getCoils
    returns:
      javaType: int
      type: Number (int)
  - methodName: getCondensers
    returns:
      javaType: int
      type: Number (int)
  - methodName: getDispersers
    returns:
      javaType: int
      type: Number (int)
  - methodName: getDumpingMode
    returns:
      javaType: mekanism.common.tile.TileEntityChemicalTank$GasMode
      type: String (GasMode)
  - methodName: getFlowRate
    returns:
      javaType: long
      type: Number (long)
  - methodName: getLastSteamInputRate
    returns:
      javaType: long
      type: Number (long)
  - methodName: getMaxFlowRate
    returns:
      javaType: long
      type: Number (long)
  - methodName: getMaxProduction
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - methodName: getMaxWaterOutput
    returns:
      javaType: long
      type: Number (long)
  - methodName: getProductionRate
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the steam tank.
    methodName: getSteam
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the steam tank.
    methodName: getSteamCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the steam tank.
    methodName: getSteamFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the steam tank.
    methodName: getSteamNeeded
    returns:
      javaType: long
      type: Number (long)
  - methodName: getVents
    returns:
      javaType: int
      type: Number (int)
  - methodName: incrementDumpingMode
  - methodName: setDumpingMode
    params:
    - javaType: mekanism.common.tile.TileEntityChemicalTank$GasMode
      name: mode
      type: String (GasMode)
  Isotopic Centrifuge:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the input tank.
    methodName: getInput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the input tank.
    methodName: getInputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the input tank.
    methodName: getInputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the input tank.
    methodName: getInputNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the output tank.
    methodName: getOutput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output tank.
    methodName: getOutputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the output tank.
    methodName: getOutputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output tank.
    methodName: getOutputNeeded
    returns:
      javaType: long
      type: Number (long)
  Laser:
  - methodName: getDiggingPos
    returns:
      javaType: net.minecraft.core.BlockPos
      type: Table (BlockPos)
  Laser Amplifier:
  - methodName: getDelay
    returns:
      javaType: int
      type: Number (int)
  - methodName: getMaxThreshold
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - methodName: getMinThreshold
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - methodName: getRedstoneOutputMode
    returns:
      javaType: mekanism.common.tile.laser.TileEntityLaserAmplifier$RedstoneOutput
      type: String (RedstoneOutput)
  - methodName: setDelay
    params:
    - javaType: int
      name: delay
      type: Number (int)
    requiresPublicSecurity: true
  - methodName: setMaxThreshold
    params:
    - javaType: mekanism.api.math.FloatingLong
      name: threshold
      type: Number (FloatingLong)
    requiresPublicSecurity: true
  - methodName: setMinThreshold
    params:
    - javaType: mekanism.api.math.FloatingLong
      name: threshold
      type: Number (FloatingLong)
    requiresPublicSecurity: true
  - methodName: setRedstoneOutputMode
    params:
    - javaType: mekanism.common.tile.laser.TileEntityLaserAmplifier$RedstoneOutput
      name: mode
      type: String (RedstoneOutput)
    requiresPublicSecurity: true
  Laser Tractor Beam:
  - methodName: getItemInSlot
    params:
    - javaType: int
      name: slot
      type: Number (int)
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getSlotCount
    returns:
      javaType: int
      type: Number (int)
  Logistical Sorter:
  - methodName: addFilter
    params:
    - javaType: mekanism.common.content.transporter.SorterFilter
      name: filter
      type: Table (SorterFilter)
    requiresPublicSecurity: true
    returns:
      javaType: boolean
      type: boolean
  - methodName: clearDefaultColor
    requiresPublicSecurity: true
  - methodName: decrementDefaultColor
    requiresPublicSecurity: true
  - methodName: getAutoMode
    returns:
      javaType: boolean
      type: boolean
  - methodName: getDefaultColor
    returns:
      javaType: mekanism.api.text.EnumColor
      type: String (EnumColor)
  - methodName: getFilters
    returns:
      javaExtra:
      - mekanism.common.content.transporter.SorterFilter
      javaType: java.util.Collection
      type: List (Table (SorterFilter))
  - methodName: incrementDefaultColor
    requiresPublicSecurity: true
  - methodName: isRoundRobin
    returns:
      javaType: boolean
      type: boolean
  - methodName: isSingle
    returns:
      javaType: boolean
      type: boolean
  - methodName: removeFilter
    params:
    - javaType: mekanism.common.content.transporter.SorterFilter
      name: filter
      type: Table (SorterFilter)
    requiresPublicSecurity: true
    returns:
      javaType: boolean
      type: boolean
  - methodName: setAutoMode
    params:
    - javaType: boolean
      name: value
      type: boolean
    requiresPublicSecurity: true
  - methodName: setDefaultColor
    params:
    - javaType: mekanism.api.text.EnumColor
      name: color
      type: String (EnumColor)
    requiresPublicSecurity: true
  - methodName: setRoundRobin
    params:
    - javaType: boolean
      name: value
      type: boolean
    requiresPublicSecurity: true
  - methodName: setSingle
    params:
    - javaType: boolean
      name: value
      type: boolean
    requiresPublicSecurity: true
  Machine with Ejector Component:
  - methodName: clearInputColor
    params:
    - javaType: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    requiresPublicSecurity: true
  - methodName: clearOutputColor
    requiresPublicSecurity: true
  - methodName: decrementInputColor
    params:
    - javaType: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    requiresPublicSecurity: true
  - methodName: decrementOutputColor
    requiresPublicSecurity: true
  - methodName: getInputColor
    params:
    - javaType: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    returns:
      javaType: mekanism.api.text.EnumColor
      type: String (EnumColor)
  - methodName: getOutputColor
    returns:
      javaType: mekanism.api.text.EnumColor
      type: String (EnumColor)
  - methodName: hasStrictInput
    returns:
      javaType: boolean
      type: boolean
  - methodName: incrementInputColor
    params:
    - javaType: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    requiresPublicSecurity: true
  - methodName: incrementOutputColor
    requiresPublicSecurity: true
  - methodName: setInputColor
    params:
    - javaType: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    - javaType: mekanism.api.text.EnumColor
      name: color
      type: String (EnumColor)
    requiresPublicSecurity: true
  - methodName: setOutputColor
    params:
    - javaType: mekanism.api.text.EnumColor
      name: color
      type: String (EnumColor)
    requiresPublicSecurity: true
  - methodName: setStrictInput
    params:
    - javaType: boolean
      name: strict
      type: boolean
    requiresPublicSecurity: true
  Machine with Recipe Progress:
  - methodName: getRecipeProgress
    returns:
      javaType: int
      type: Number (int)
  - methodName: getTicksRequired
    returns:
      javaType: int
      type: Number (int)
  Machine with Security Component:
  - methodName: getOwnerName
    returns:
      javaType: java.lang.String
      type: String
  - methodName: getOwnerUUID
    returns:
      javaType: java.util.UUID
      type: String (UUID)
  - methodName: getSecurityMode
    returns:
      javaType: mekanism.api.security.SecurityMode
      type: String (SecurityMode)
  Machine with Side Configuration Component:
  - methodName: canEject
    params:
    - javaType: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    returns:
      javaType: boolean
      type: boolean
  - methodName: decrementMode
    params:
    - javaType: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    - javaType: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    requiresPublicSecurity: true
  - methodName: getConfigurableTypes
    returns:
      javaExtra:
      - mekanism.common.lib.transmitter.TransmissionType
      javaType: java.util.List
      type: List (String (TransmissionType))
  - methodName: getMode
    params:
    - javaType: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    - javaType: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    requiresPublicSecurity: true
    returns:
      javaType: mekanism.common.tile.component.config.DataType
      type: String (DataType)
  - methodName: getSupportedModes
    params:
    - javaType: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    requiresPublicSecurity: true
    returns:
      javaExtra:
      - mekanism.common.tile.component.config.DataType
      javaType: java.util.Set
      type: List (String (DataType))
  - methodName: incrementMode
    params:
    - javaType: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    - javaType: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    requiresPublicSecurity: true
  - methodName: isEjecting
    params:
    - javaType: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    returns:
      javaType: boolean
      type: boolean
  - methodName: setEjecting
    params:
    - javaType: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    - javaType: boolean
      name: ejecting
      type: boolean
    requiresPublicSecurity: true
  - methodName: setMode
    params:
    - javaType: mekanism.common.lib.transmitter.TransmissionType
      name: type
      type: String (TransmissionType)
    - javaType: mekanism.api.RelativeSide
      name: side
      type: String (RelativeSide)
    - javaType: mekanism.common.tile.component.config.DataType
      name: mode
      type: String (DataType)
    requiresPublicSecurity: true
  Machine with Upgrade Component:
  - methodName: getInstalledUpgrades
    returns:
      javaExtra:
      - mekanism.api.Upgrade
      - java.lang.Integer
      javaType: java.util.Map
      type: Table (String (Upgrade) => Number (int))
  - methodName: getSupportedUpgrades
    returns:
      javaExtra:
      - mekanism.api.Upgrade
      javaType: java.util.Set
      type: List (String (Upgrade))
  Mechanical Pipe:
  - methodName: getBuffer
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - methodName: getCapacity
    returns:
      javaType: long
      type: Number (long)
  - methodName: getFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - methodName: getNeeded
    returns:
      javaType: long
      type: Number (long)
  Metallurgic Infuser:
  - methodName: dumpInfuseType
    requiresPublicSecurity: true
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the infusion buffer.
    methodName: getInfuseType
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the infusion buffer.
    methodName: getInfuseTypeCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the infusion buffer.
    methodName: getInfuseTypeFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the infusion (extra) input slot.
    methodName: getInfuseTypeItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the infusion buffer.
    methodName: getInfuseTypeNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the input slot.
    methodName: getInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Metallurgic Infuser Factory:
  - description: Empty the contents of the infusion buffer into the environment
    methodName: dumpInfuseType
    requiresPublicSecurity: true
  - description: Get the contents of the infusion buffer.
    methodName: getInfuseType
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the infusion buffer.
    methodName: getInfuseTypeCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the infusion buffer.
    methodName: getInfuseTypeFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the infusion extra input slot.
    methodName: getInfuseTypeItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the infusion buffer.
    methodName: getInfuseTypeNeeded
    returns:
      javaType: long
      type: Number (long)
  Modification Station:
  - description: Get the contents of the module holder slot (suit, tool, etc).
    methodName: getContainerItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the module slot.
    methodName: getModuleItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Multiblock:
  - methodName: isFormed
    restriction: MULTIBLOCK
    returns:
      javaType: boolean
      type: boolean
  Multiblock (formed):
  - methodName: getHeight
    returns:
      javaType: int
      type: Number (int)
  - methodName: getLength
    returns:
      javaType: int
      type: Number (int)
  - methodName: getMaxPos
    returns:
      javaType: net.minecraft.core.BlockPos
      type: Table (BlockPos)
  - methodName: getMinPos
    returns:
      javaType: net.minecraft.core.BlockPos
      type: Table (BlockPos)
  - methodName: getWidth
    returns:
      javaType: int
      type: Number (int)
  Nutritional Liquifier:
  - description: Get the contents of the fillable container slot.
    methodName: getContainerFillItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the input slot.
    methodName: getInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output tank.
    methodName: getOutput
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the output tank.
    methodName: getOutputCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the filled percentage of the output tank.
    methodName: getOutputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the filled container output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output tank.
    methodName: getOutputNeeded
    returns:
      javaType: int
      type: Number (int)
  Oredictionificator:
  - methodName: addFilter
    params:
    - javaType: mekanism.common.content.oredictionificator.OredictionificatorItemFilter
      name: filter
      type: Table (OredictionificatorItemFilter)
    requiresPublicSecurity: true
    returns:
      javaType: boolean
      type: boolean
  - methodName: getFilters
    returns:
      javaExtra:
      - mekanism.common.content.oredictionificator.OredictionificatorItemFilter
      javaType: java.util.Collection
      type: List (Table (OredictionificatorItemFilter))
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: removeFilter
    params:
    - javaType: mekanism.common.content.oredictionificator.OredictionificatorItemFilter
      name: filter
      type: Table (OredictionificatorItemFilter)
    requiresPublicSecurity: true
    returns:
      javaType: boolean
      type: boolean
  Painting Machine:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the paintable item slot.
    methodName: getInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the pigment slot.
    methodName: getInputPigmentItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the painted item slot.
    methodName: getOutput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the pigment tank.
    methodName: getPigmentInput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the pigment tank.
    methodName: getPigmentInputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the pigment tank.
    methodName: getPigmentInputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the pigment tank.
    methodName: getPigmentInputNeeded
    returns:
      javaType: long
      type: Number (long)
  Pigment Extractor:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the input slot.
    methodName: getInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the pigment tank.
    methodName: getOutput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the pigment tank.
    methodName: getOutputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the pigment tank.
    methodName: getOutputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the pigment tank.
    methodName: getOutputNeeded
    returns:
      javaType: long
      type: Number (long)
  Pigment Mixer:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the left pigment tank.
    methodName: getLeftInput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the left pigment tank.
    methodName: getLeftInputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the left pigment tank.
    methodName: getLeftInputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the left input slot.
    methodName: getLeftInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the left pigment tank.
    methodName: getLeftInputNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the output pigment tank.
    methodName: getOutput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output pigment tank.
    methodName: getOutputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the output pigment tank.
    methodName: getOutputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output pigment tank.
    methodName: getOutputNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the right pigment tank.
    methodName: getRightInput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the right pigment tank.
    methodName: getRightInputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the right pigment tank.
    methodName: getRightInputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the right input slot.
    methodName: getRightInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the right pigment tank.
    methodName: getRightInputNeeded
    returns:
      javaType: long
      type: Number (long)
  Precision Sawmill:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the input slot.
    methodName: getInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output slot.
    methodName: getOutput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the secondary output slot.
    methodName: getSecondaryOutput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Pressurized Reaction Chamber:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the fluid input.
    methodName: getInputFluid
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the fluid input.
    methodName: getInputFluidCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the filled percentage of the fluid input.
    methodName: getInputFluidFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the fluid input.
    methodName: getInputFluidNeeded
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the gas input.
    methodName: getInputGas
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the gas input.
    methodName: getInputGasCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the gas input.
    methodName: getInputGasFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the gas input.
    methodName: getInputGasNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the item input slot.
    methodName: getInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the gas output.
    methodName: getOutputGas
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the gas output.
    methodName: getOutputGasCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the gas output.
    methodName: getOutputGasFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the gas output.
    methodName: getOutputGasNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the item output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Pressurized Tube:
  - methodName: getBuffer
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - methodName: getCapacity
    returns:
      javaType: long
      type: Number (long)
  - methodName: getFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - methodName: getNeeded
    returns:
      javaType: long
      type: Number (long)
  QIO Dashboard:
  - methodName: getCraftingInput
    params:
    - javaType: int
      name: window
      type: Number (int)
    - javaType: int
      name: slot
      type: Number (int)
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getCraftingOutput
    params:
    - javaType: int
      name: window
      type: Number (int)
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  QIO Drive Array:
  - methodName: getDrive
    params:
    - javaType: int
      name: slot
      type: Number (int)
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getDriveStatus
    params:
    - javaType: int
      name: slot
      type: Number (int)
    returns:
      javaType: mekanism.common.tile.qio.TileEntityQIODriveArray$DriveStatus
      type: String (DriveStatus)
  - description: Requires a frequency to be selected
    methodName: getFrequencyItemCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Requires a frequency to be selected
    methodName: getFrequencyItemCount
    returns:
      javaType: long
      type: Number (long)
  - description: Requires a frequency to be selected
    methodName: getFrequencyItemPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Requires a frequency to be selected
    methodName: getFrequencyItemTypeCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Requires a frequency to be selected
    methodName: getFrequencyItemTypeCount
    returns:
      javaType: long
      type: Number (long)
  - description: Requires a frequency to be selected
    methodName: getFrequencyItemTypePercentage
    returns:
      javaType: double
      type: Number (double)
  - methodName: getSlotCount
    returns:
      javaType: int
      type: Number (int)
  QIO Exporter:
  - methodName: getExportWithoutFilter
    returns:
      javaType: boolean
      type: boolean
  - methodName: isRoundRobin
    returns:
      javaType: boolean
      type: boolean
  - methodName: setExportsWithoutFilter
    params:
    - javaType: boolean
      name: value
      type: boolean
    requiresPublicSecurity: true
  - methodName: setRoundRobin
    params:
    - javaType: boolean
      name: value
      type: boolean
    requiresPublicSecurity: true
  QIO Importer:
  - methodName: getImportWithoutFilter
    returns:
      javaType: boolean
      type: boolean
  - methodName: setImportsWithoutFilter
    params:
    - javaType: boolean
      name: value
      type: boolean
    requiresPublicSecurity: true
  QIO Machine:
  - description: Requires frequency to not already exist and for it to be public so
      that it can make it as the player who owns the block. Also sets the frequency
      after creation
    methodName: createFrequency
    params:
    - javaType: java.lang.String
      name: name
      type: String
    requiresPublicSecurity: true
  - description: Requires a frequency to be selected
    methodName: decrementFrequencyColor
    requiresPublicSecurity: true
  - description: Lists public frequencies
    methodName: getFrequencies
    returns:
      javaExtra:
      - mekanism.common.content.qio.QIOFrequency
      javaType: java.util.Collection
      type: List (Table (QIOFrequency))
  - description: Requires a frequency to be selected
    methodName: getFrequency
    returns:
      javaType: mekanism.common.content.qio.QIOFrequency
      type: Table (QIOFrequency)
  - description: Requires a frequency to be selected
    methodName: getFrequencyColor
    returns:
      javaType: mekanism.api.text.EnumColor
      type: String (EnumColor)
  - methodName: hasFrequency
    returns:
      javaType: boolean
      type: boolean
  - description: Requires a frequency to be selected
    methodName: incrementFrequencyColor
    requiresPublicSecurity: true
  - description: Requires a public frequency to exist
    methodName: setFrequency
    params:
    - javaType: java.lang.String
      name: name
      type: String
    requiresPublicSecurity: true
  - description: Requires a frequency to be selected
    methodName: setFrequencyColor
    params:
    - javaType: mekanism.api.text.EnumColor
      name: color
      type: String (EnumColor)
    requiresPublicSecurity: true
  QIO Machine with Filter:
  - methodName: addFilter
    params:
    - javaType: mekanism.common.content.qio.filter.QIOFilter
      name: filter
      type: Table (QIOFilter)
    requiresPublicSecurity: true
    returns:
      javaType: boolean
      type: boolean
  - methodName: getFilters
    returns:
      javaExtra:
      - mekanism.common.content.qio.filter.QIOFilter
      javaType: java.util.Collection
      type: List (Table (QIOFilter))
  - methodName: removeFilter
    params:
    - javaType: mekanism.common.content.qio.filter.QIOFilter
      name: filter
      type: Table (QIOFilter)
    requiresPublicSecurity: true
    returns:
      javaType: boolean
      type: boolean
  QIO Redstone Adapter:
  - methodName: clearTargetItem
    requiresPublicSecurity: true
  - methodName: getFuzzyMode
    returns:
      javaType: boolean
      type: boolean
  - methodName: getTargetItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: getTriggerAmount
    returns:
      javaType: long
      type: Number (long)
  - methodName: invertSignal
    requiresPublicSecurity: true
  - methodName: isInverted
    returns:
      javaType: boolean
      type: boolean
  - methodName: setFuzzyMode
    params:
    - javaType: boolean
      name: fuzzy
      type: boolean
    requiresPublicSecurity: true
  - methodName: setSignalInverted
    params:
    - javaType: boolean
      name: inverted
      type: boolean
    requiresPublicSecurity: true
  - methodName: setTargetItem
    params:
    - javaType: net.minecraft.resources.ResourceLocation
      name: itemName
      type: String (ResourceLocation)
    requiresPublicSecurity: true
  - methodName: setTriggerAmount
    params:
    - javaType: long
      name: amount
      type: Number (long)
    requiresPublicSecurity: true
  - methodName: toggleFuzzyMode
    requiresPublicSecurity: true
  Quantum Entangloporter:
  - description: Requires frequency to not already exist and for it to be public so
      that it can make it as the player who owns the block. Also sets the frequency
      after creation
    methodName: createFrequency
    params:
    - javaType: java.lang.String
      name: name
      type: String
    requiresPublicSecurity: true
  - description: Get the contents of the fluid buffer.
    methodName: getBufferFluid
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the fluid buffer.
    methodName: getBufferFluidCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the filled percentage of the fluid buffer.
    methodName: getBufferFluidFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the fluid buffer.
    methodName: getBufferFluidNeeded
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the gas buffer.
    methodName: getBufferGas
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the gas buffer.
    methodName: getBufferGasCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the gas buffer.
    methodName: getBufferGasFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the gas buffer.
    methodName: getBufferGasNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the infusion buffer.
    methodName: getBufferInfuseType
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the infusion buffer.
    methodName: getBufferInfuseTypeCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the infusion buffer.
    methodName: getBufferInfuseTypeFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the infusion buffer.
    methodName: getBufferInfuseTypeNeeded
    returns:
      javaType: long
      type: Number (long)
  - methodName: getBufferItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the pigment buffer.
    methodName: getBufferPigment
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the pigment buffer.
    methodName: getBufferPigmentCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the pigment buffer.
    methodName: getBufferPigmentFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the pigment buffer.
    methodName: getBufferPigmentNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the slurry buffer.
    methodName: getBufferSlurry
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the slurry buffer.
    methodName: getBufferSlurryCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the slurry buffer.
    methodName: getBufferSlurryFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the slurry buffer.
    methodName: getBufferSlurryNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: May not be accurate if there is no frequency
    methodName: getEnvironmentalLoss
    returns:
      javaType: double
      type: Number (double)
  - description: Lists public frequencies
    methodName: getFrequencies
    returns:
      javaExtra:
      - mekanism.common.content.entangloporter.InventoryFrequency
      javaType: java.util.Collection
      type: List (Table (InventoryFrequency))
  - description: Requires a frequency to be selected
    methodName: getFrequency
    returns:
      javaType: mekanism.common.content.entangloporter.InventoryFrequency
      type: Table (InventoryFrequency)
  - description: Requires a frequency to be selected
    methodName: getTemperature
    returns:
      javaType: double
      type: Number (double)
  - description: May not be accurate if there is no frequency
    methodName: getTransferLoss
    returns:
      javaType: double
      type: Number (double)
  - methodName: hasFrequency
    returns:
      javaType: boolean
      type: boolean
  - description: Requires a public frequency to exist
    methodName: setFrequency
    params:
    - javaType: java.lang.String
      name: name
      type: String
    requiresPublicSecurity: true
  Radioactive Waste Barrel:
  - description: Get the capacity of the barrel.
    methodName: getCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the barrel.
    methodName: getFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the barrel.
    methodName: getNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the barrel.
    methodName: getStored
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  Resistive Heater:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - methodName: getEnergyUsed
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - methodName: getEnvironmentalLoss
    returns:
      javaType: double
      type: Number (double)
  - description: Get the temperature of the heater in Kelvin.
    methodName: getTemperature
    returns:
      javaType: double
      type: Number (double)
  - methodName: getTransferLoss
    returns:
      javaType: double
      type: Number (double)
  - methodName: setEnergyUsage
    params:
    - javaType: mekanism.api.math.FloatingLong
      name: usage
      type: Number (FloatingLong)
    requiresPublicSecurity: true
  Rotary Condensentrator:
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the energy used in the last tick by the machine
    methodName: getEnergyUsage
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - description: Get the contents of the fluid tank.
    methodName: getFluid
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the fluid tank.
    methodName: getFluidCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the filled percentage of the fluid tank.
    methodName: getFluidFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the fluid item input slot.
    methodName: getFluidItemInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the fluid item ouput slot.
    methodName: getFluidItemOutput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the fluid tank.
    methodName: getFluidNeeded
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the gas tank.
    methodName: getGas
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the gas tank.
    methodName: getGasCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the gas tank.
    methodName: getGasFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the gas item input slot.
    methodName: getGasItemInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the gas item output slot.
    methodName: getGasItemOutput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the gas tank.
    methodName: getGasNeeded
    returns:
      javaType: long
      type: Number (long)
  - methodName: isCondensentrating
    returns:
      javaType: boolean
      type: boolean
  - methodName: setCondensentrating
    params:
    - javaType: boolean
      name: value
      type: boolean
    requiresPublicSecurity: true
  SPS Multiblock (formed):
  - methodName: getCoils
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the input tank.
    methodName: getInput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the input tank.
    methodName: getInputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the input tank.
    methodName: getInputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the input tank.
    methodName: getInputNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the output tank.
    methodName: getOutput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output tank.
    methodName: getOutputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the output tank.
    methodName: getOutputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the amount needed to fill the output tank.
    methodName: getOutputNeeded
    returns:
      javaType: long
      type: Number (long)
  - methodName: getProcessRate
    returns:
      javaType: double
      type: Number (double)
  SPS Port:
  - description: true -> output, false -> input.
    methodName: getMode
    returns:
      javaType: boolean
      type: boolean
  - description: true -> output, false -> input.
    methodName: setMode
    params:
    - javaType: boolean
      name: output
      type: boolean
  Sawing Factory:
  - methodName: getSecondaryOutput
    params:
    - javaType: int
      name: process
      type: Number (int)
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Seismic Vibrator:
  - methodName: getBlockAt
    params:
    - javaType: int
      name: chunkRelativeX
      type: Number (int)
    - javaType: int
      name: y
      type: Number (int)
    - javaType: int
      name: chunkRelativeZ
      type: Number (int)
    returns:
      javaType: net.minecraft.world.level.block.state.BlockState
      type: Table (BlockState)
  - description: Get a column info, table key is the Y level
    methodName: getColumnAt
    params:
    - javaType: int
      name: chunkRelativeX
      type: Number (int)
    - javaType: int
      name: chunkRelativeZ
      type: Number (int)
    returns:
      javaExtra:
      - java.lang.Integer
      - net.minecraft.world.level.block.state.BlockState
      javaType: java.util.Map
      type: Table (Number (int) => Table (BlockState))
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: isVibrating
    returns:
      javaType: boolean
      type: boolean
  Solar Generator:
  - methodName: canSeeSun
    returns:
      javaType: boolean
      type: boolean
  - description: Get the contents of the energy item slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  Solar Neutron Activator:
  - methodName: canSeeSun
    returns:
      javaType: boolean
      type: boolean
  - description: Get the contents of the input tank.
    methodName: getInput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the input tank.
    methodName: getInputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the input tank.
    methodName: getInputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the input slot.
    methodName: getInputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the input tank.
    methodName: getInputNeeded
    returns:
      javaType: long
      type: Number (long)
  - description: Get the contents of the output tank.
    methodName: getOutput
    returns:
      javaType: mekanism.api.chemical.ChemicalStack
      type: Table (ChemicalStack)
  - description: Get the capacity of the output tank.
    methodName: getOutputCapacity
    returns:
      javaType: long
      type: Number (long)
  - description: Get the filled percentage of the output tank.
    methodName: getOutputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the output slot.
    methodName: getOutputItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output tank.
    methodName: getOutputNeeded
    returns:
      javaType: long
      type: Number (long)
  - methodName: getPeakProductionRate
    returns:
      javaType: float
      type: Number (float)
  - methodName: getProductionRate
    returns:
      javaType: float
      type: Number (float)
  Teleporter:
  - description: Requires frequency to not already exist and for it to be public so
      that it can make it as the player who owns the block. Also sets the frequency
      after creation
    methodName: createFrequency
    params:
    - javaType: java.lang.String
      name: name
      type: String
    requiresPublicSecurity: true
  - description: Requires a frequency to be selected
    methodName: decrementFrequencyColor
    requiresPublicSecurity: true
  - description: Requires a frequency to be selected
    methodName: getActiveTeleporters
    returns:
      javaExtra:
      - net.minecraft.core.GlobalPos
      javaType: java.util.Set
      type: List (Table (GlobalPos))
  - description: Get the contents of the energy slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Lists public frequencies
    methodName: getFrequencies
    returns:
      javaExtra:
      - mekanism.common.content.teleporter.TeleporterFrequency
      javaType: java.util.Collection
      type: List (Table (TeleporterFrequency))
  - description: Requires a frequency to be selected
    methodName: getFrequency
    returns:
      javaType: mekanism.common.content.teleporter.TeleporterFrequency
      type: Table (TeleporterFrequency)
  - description: Requires a frequency to be selected
    methodName: getFrequencyColor
    returns:
      javaType: mekanism.api.text.EnumColor
      type: String (EnumColor)
  - methodName: getStatus
    returns:
      javaType: java.lang.String
      type: String
  - methodName: hasFrequency
    returns:
      javaType: boolean
      type: boolean
  - description: Requires a frequency to be selected
    methodName: incrementFrequencyColor
    requiresPublicSecurity: true
  - description: Requires a public frequency to exist
    methodName: setFrequency
    params:
    - javaType: java.lang.String
      name: name
      type: String
    requiresPublicSecurity: true
  - description: Requires a frequency to be selected
    methodName: setFrequencyColor
    params:
    - javaType: mekanism.api.text.EnumColor
      name: color
      type: String (EnumColor)
    requiresPublicSecurity: true
  Thermal Evaporation Multiblock (formed):
  - methodName: getActiveSolars
    returns:
      javaType: int
      type: Number (int)
  - methodName: getEnvironmentalLoss
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the input tank.
    methodName: getInput
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the input tank.
    methodName: getInputCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the filled percentage of the input tank.
    methodName: getInputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the input side's input slot.
    methodName: getInputItemInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the input side's output slot.
    methodName: getInputItemOutput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the input tank.
    methodName: getInputNeeded
    returns:
      javaType: int
      type: Number (int)
  - description: Get the contents of the output tank.
    methodName: getOutput
    returns:
      javaType: net.neoforged.neoforge.fluids.FluidStack
      type: Table (FluidStack)
  - description: Get the capacity of the output tank.
    methodName: getOutputCapacity
    returns:
      javaType: int
      type: Number (int)
  - description: Get the filled percentage of the output tank.
    methodName: getOutputFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - description: Get the contents of the output side's input slot.
    methodName: getOutputItemInput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the contents of the output side's output slot.
    methodName: getOutputItemOutput
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - description: Get the amount needed to fill the output tank.
    methodName: getOutputNeeded
    returns:
      javaType: int
      type: Number (int)
  - methodName: getProductionAmount
    returns:
      javaType: double
      type: Number (double)
  - methodName: getTemperature
    returns:
      javaType: double
      type: Number (double)
  Universal Cable:
  - methodName: getBuffer
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - methodName: getCapacity
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  - methodName: getFilledPercentage
    returns:
      javaType: double
      type: Number (double)
  - methodName: getNeeded
    returns:
      javaType: mekanism.api.math.FloatingLong
      type: Number (FloatingLong)
  Wind Generator:
  - description: Get the contents of the energy item slot.
    methodName: getEnergyItem
    returns:
      javaType: net.minecraft.world.item.ItemStack
      type: Table (ItemStack)
  - methodName: isBlacklistedDimension
    returns:
      javaType: boolean
      type: boolean
version: 10.5.19
---
