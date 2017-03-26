package ic2.api.info;

import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.MinecraftForge;

import ic2.api.event.TeBlockFinalCallEvent;
import ic2.api.item.ITeBlockSpecialItem;
import ic2.api.tile.IWrenchable;

/** First of all, yes this class is empty. It's public documentation for the actual internal interface ITeBlock. <strong>Use the internal one not this!</strong>
 *	<p>
 *	You may be asking, why would an internal interface need documenting? Well, the answer is so people can use <code>TileEntityBlock</code>.
 *	That class is the base for all IC2 tile entities, so by being able to use that you're level to IC2.
 *	Many methods are protected in there too, as they're only designed to be called by <code>BlockTileEntity</code> (which is the block all IC2 tile entities will be on),
 *	by being able to extend it means all those methods you can now extend and be confident they will be called correctly.
 *	</p>
 *	<p>
 *  The more observant might have noticed there was an event that allowed you to do basically the same thing called <code>TeBlockBakeEvent</code>.
 *  Whilst that event used to add your own class onto <code>TeBlock</code> (that wasn't ideal as you could end up using the same ID as another addon which would cause a crash),
 *  this allows you to completely implement your own version of <code>TeBlock</code>, then let IC2 take it and build the necessary structures around it.
 *  Both mean you no longer have to use <code>EnumHelper</code> and lots of reflection to mess about with <code>TeBlock</code>,
 *  just to be left with either a crash from doing it too late/wrong, or lots of code bound to IC2 (such as language keys and blockstate jsons being in IC2's assets).
 *	</p>
 *	<p>
 *	Unlike <code>TeBlockBakeEvent</code>, this gives you near total control of your tile entities:
 *	<blockquote>
 *	You can pick whether each of your tile entity items show up in creative, and the order in which they do.<br/>
 *	You can choose whether you use an <code>enum</code> or <code>class</code> for your implementation.
 *	We use (and I personally recommend) using an <code>enum</code>, as the values should never change, but in the end the choice is yours.<br/>
 *	There is a proper separation between each registered <code>ITeBlock</code> by resource location, rather than everything being inserted into a single <code>enum</code>.
 *	You also receive your own instance of <code>BlockTileEntity</code> and <code>ItemBlockTileEntity</code> rather than having to use IC2's own one.<br/>
 *	All the language keys and blockstate file are in your own assets (which you can, and in fact have to, pick).
 *	</blockquote>
 *	It is a vast improvement over <code>TeBlockBakeEvent</code>, which is probably important as it's gone now ;)
 *	</p>
 *	<br/>
 *	The event allows you to properly register your own version of <code>TeBlock</code> in a way that will be as fully supported as is physically possible.
 *	It's not in the actual API itself as it's heavily dependent on internal classes, so you have this instead.
 *	This is also probably the most documented thing in the entire IC2 API. <small>I'm too kind really. ;)</small>
 *	</p>
 *	<hr>
 *	<p>
 *	So that's all well and good, but what you really want is an example of how to do it properly. Good idea, let's go.
 *	<blockquote><pre><code>
 *import net.minecraftforge.common.MinecraftForge;
 *import net.minecraftforge.fml.common.Mod;
 *import net.minecraftforge.fml.common.Mod.EventHandler;
 *import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
 *import net.minecraftforge.fml.common.event.FMLInitializationEvent;
 *
 *{@literal @}Mod(modId="ExampleIC2Addon")
 *public class ExampleIC2Addon {
 *	public ExampleIC2Addon() {
 *		//You have to register before IC2 reaches Pre-Initialization, which unless you specifically load before it is unlikely you'll achieve.
 *		//Especially if you're an addon, you'll want to be loading after IC2 not before!
 *		//But, your mod's constructor will definitely be called before then. The issue is it's called more than once.
 *		//If you try registering the same tile entity ID twice it will crash (more on that later), so you'll have to use a singleton instance.
 *		//An enum works well for this, but anything will work as long as it's registered to the event bus once, and only once.
 *		MinecraftForge.EVENT_BUS.register(TileEntityRegisterer.INSTANCE);
 *	}
 *
 *	{@literal @}EventHandler
 *	public void preInit(FMLPreInitializationEvent event) {
 *		MyTeBlock.registerTileEntities();
 *	}
 *
 *	{@literal @}EventHandler
 *	public void preInit(FMLInitializationEvent event) {
 *		MyTeBlock.buildDummies();
 *	}
 *}
 *
 *import ic2.api.event.TeBlockFinalCallEvent;
 *import ic2.core.block.TeBlockRegistry;
 *import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
 *
 *public enum TileEntityRegisterer {
 *	INSTANCE;
 *
 *	{@literal @}SubscribeEvent
 *	public void register(TeBlockFinalCallEvent event) {
 *		TeBlockRegistry.addAll(MyTeBlock.class, MyTeBlock.IDENTITY);
 *	}
 *}
 *
 *import ic2.core.block.BlockTileEntity;
 *import ic2.core.block.ITeBlock;
 *import ic2.core.block.TileEntityBlock;
 *import ic2.core.item.block.ItemBlockTileEntity;
 *import ic2.core.ref.TeBlock.DefaultDrop;
 *import ic2.core.ref.TeBlock.HarvestTool;
 *import ic2.core.ref.TeBlock.ITePlaceHandler;
 *import ic2.core.util.Util;
 *import net.minecraft.block.material.Material;
 *import net.minecraft.creativetab.CreativeTabs;
 *import net.minecraft.item.EnumRarity;
 *import net.minecraft.item.ItemStack;
 *import net.minecraft.tileentity.TileEntity;
 *import net.minecraft.util.EnumFacing;
 *import net.minecraft.util.ResourceLocation;
 *
 *public enum MyTeBlock implements ITeBlock {
 *	my_machine_name(MyMachineTileEntity.class, 0, false, Util.noFacings, false, HarvestTool.Pickaxe, DefaultDrop.Machine, 5, 10, EnumRarity.UNCOMMON, Material.IRON);
 *
 *	private MyTeBlock(Class<? extends TileEntityBlock> teClass, int ID,
 *			boolean hasActive, Set{@literal <}EnumFacing{@literal >} possibleFacings, boolean canBeWrenched,
 *			HarvestTool tool, DefaultDrop drop,
 *			float hardness, float explosionResistance, EnumRarity rarity, Material material) {
 *		this.teClass = teClass;
 *		this.ID = ID;
 *		this.hasActive = hasActive;
 *		this.possibleFacings = possibleFacings;
 *		this.canBeWrenched = canBeWrenched;
 *		this.tool = tool;
 *		this.drop = drop;
 *		this.hardness = hardness;
 *		this.explosionResistance = explosionResistance;
 *		this.rarity = rarity;
 *		this.material = material;
 *	}
 *
 *	{@literal @}Override
 *	public boolean hasItem() {
 *		return teClass != null && itemMeta != -1;
 *	}
 *
 *	{@literal @}Override
 *	public String getName() {
 *		return name();
 *	}
 *
 *	{@literal @}Override
 *	public ResourceLocation getIdentifier() {
 *		return IDENTITY;
 *	}
 *
 *	{@literal @}Override
 *	public Class<? extends TileEntityBlock> getTeClass() {
 *		return teClass;
 *	}
 *
 *	{@literal @}Override
 *	public boolean hasActive() {
 *		return hasActive;
 *	}
 *
 *	{@literal @}Override
 *	public int getId() {
 *		return ID;
 *	}
 *
 *	{@literal @}Override
 *	public float getHardness() {
 *		return hardness;
 *	}
 *
 *	{@literal @}Override
 *	public HarvestTool getHarvestTool() {
 *		return tool;
 *	}
 *
 *	{@literal @}Override
 *	public DefaultDrop getDefaultDrop() {
 *		return drop;
 *	}
 *
 *	{@literal @}Override
 *	public float getExplosionResistance() {
 *		return explosionResistance;
 *	}
 *
 *	{@literal @}Override
 *	public boolean allowWrenchRotating() {
 *		return canBeWrenched;
 *	}
 *
 *	{@literal @}Override
 *	public Set{@literal <}EnumFacing{@literal >} getSupportedFacings() {
 *		return possibleFacings;
 *	}
 *
 *	{@literal @}Override
 *	public EnumRarity getRarity() {
 *		return rarity;
 *	}
 *
 *	{@literal @}Override
 *	public Material getMaterial() {
 *		return material;
 *	}
 *
 *	{@literal @}Override
 *	public void addSubBlocks(List{@literal <}ItemStack{@literal >} list, BlockTileEntity block, ItemBlockTileEntity item, CreativeTabs tab) {
 *		for (MyTeBlock block : values()) {
 *			if (type.hasItem()) {
 *				list.add(block.getItemStack(type));
 *			}
 *		}
 *	}
 *
 *	{@literal @}Override
 *	public void setPlaceHandler(ITePlaceHandler handler) {
 *		this.placeHandler = handler;
 *	}
 *
 *	{@literal @}Override
 *	public ITePlaceHandler getPlaceHandler() {
 *		return placeHandler;
 *	}
 *
 *	public static void registerTileEntities() {
 *		for (MyTeBlock block : values()) {
 *			if (block.teClass != null) TileEntity.addMapping(block.teClass, IDENTITY.getResourceDomain() + ':' + block.getName());
 *		}
 *	}
 *
 *	public static void buildDummies() {
 *		for (MyTeBlock block : values()) {
 *			//System.out.printf("Registering %s (with teClass %s)%n", block.getName(), block.teClass);
 *			if (block.teClass != null) {
 *				try {
 *					block.dummyTe = block.teClass.newInstance();
 *				} catch (Exception e) {
 *					e.printStackTrace();
 *				}
 *			}
 *		}
 *	}
 *
 *	{@literal @}Override
 *	public TileEntityBlock getDummyTe() {
 *		return dummyTe;
 *	}
 *
 *	private final Class<? extends TileEntityBlock> teClass;
 *	private final int ID;
 *	private final boolean hasActive;
 *	private final Set{@literal <}EnumFacing{@literal >} possibleFacings;
 *	private final boolean canBeWrenched;
 *	private final HarvestTool tool;
 *	private final DefaultDrop drop;
 *	private final float hardness;
 *	private final float explosionResistance;
 *	private final EnumRarity rarity;
 *	private final Material material;
 *	private TileEntityBlock dummyTe;
 *	private ITePlaceHandler placeHandler;
 *
 *	public static final ResourceLocation IDENTITY = new ResourceLocation("ExampleIC2Addon", "machines");
 *}
 *  </code></pre></blockquote>
 *  And that's it, you now have <code>my_machine_name</code> registered with the <code>MyMachineTileEntity</code> tile entity class,
 *  the language key to translate it's name being ExampleIC2Addon.machines.my_machine_name and
 *  the blockstate being in <code>machines.json</code> file in your mod's assets.
 *  </p>
 *  <hr>
 *  <p>
 *  But hang on, what are all the parameters of our new <code>enum</code> and what does <code>ITeBlock</code> do with them? What are dummy tile entities for?
 *  Don't worry, it's quite simple (once you know it at least).
 *	</p>
 *	<p>
 *	First we'll start on what {@link TeBlockFinalCallEvent} is. It's posted on the {@link MinecraftForge#EVENT_BUS} just before the blocks are built,
 *	after which point any further attempts to register <code>ITeBlock</code>s will fail, as we can't inject them late due to the model generating.
 *	It is best to hook into this to register your <code>ITeBlock</code>s, as it will be guaranteed to be posted at the right time.
 *	<br/><br/>
 *	In terms of <code>ITeBlock</code>, there's quite a bit of information it needs in order to function. We'll go through each parameter (or getter method most cases too) in turn:
 *	<ul>
 *		<li><b>name</b>: The name the new tile entity. This is important as the localisation and resource names depend on it, as well as some network syncing code.</li>
 *		<li><b>teClass</b>: The class of your tile entity, it must extend <code>TileEntityBlock</code> otherwise it would miss critical methods to work.
 *							If extending that is a problem then using <code>ITeBlock</code> isn't what you should be doing.</li>
 *		<li><b>itemMeta</b>: The ID of the new instance. This is utterly critical, as it's the thing used to detect which <code>ITeBlock</code> an item is.
 *							That's very important as the tile entity class is worked out by the ID when a block's being placed in <code>ItemBlockTileEntity</code>.
 *							Changing this will cause everyone's items to change and there is no stopping that if you do.
 *							The ID you pick should start at 0, and go up to Short.MAX_VALUE.
 *							Use -1 if the tile entity doesn't have an item form (or is placed using an item other than <code>ItemBlockTileEntity</code>).</li>
 *		<li><b>identifier</b>: The resource location associated with the <code>ITeBlock</code>.
 *								The language keys, translations and internal mappings all use this, so it's best to store it as a static final variable and use that.
 *		<li><b>hasActive</b>: If the tile entity has an active state too, which is based off whether <code>TileEntityBlock#getActive()</code> returns <code>true</code> or not.
 *							The models for that will be at name_active, you can check <code>te.json</code> for an example if you wish.</li>
 *		<li><b>supportedFacings</b>: A set of <code>EnumFacing</code>s that the block can face, used for rotating by the wrench for example.
 *									IC2 has some useful constants relating to that in <code>Util</code> (allFacings, horizontalFacings, downSideFacings and noFacings).</li>
 *		<li><b>allowWrenchRotating</b>: Whether the tile entity can be rotated with a wrench (for {@link IWrenchable}).</li>
 *		<li><b>harvestTool</b>: The tool used to harvest the tile entity, currently only None, Pickaxe, Shovel and Axe.
 *								More can be added on request, but support has to be specifically added, so there's no nice way via <code>EnumHelper</code> unfortunately.</li>
 *		<li><b>defaultDrop</b>: The item(s) that will drop when the block is broken using the correct tool. Currently only Self, None, Generator, Machine, and AdvMachine.
 *								Like <code>harvestTool</code>, more can be added upon request.</li>
 *		<li><b>hardness</b>: The hardness the block should have with the tile entity, IC2 normally uses 5.</li>
 *		<li><b>explosionResistance</b>: The explosionResistance the block should have with the tile entity, IC2 normally uses 10.</li>
 *		<li><b>rarity</b>: The <code>EnumRarity</code> that the item will have for the tile entity.</li>
 *		<li><b>material</b>: The {@link Material} that the block representing the tile entity should have. Maximum of 16 different ones per ITeBlock registration.</li>
 *		<li><b>placeHandler</b>: Custom placement logic, used for things like Reactor Chambers within IC2 itself.
 *								Returning null for this uses the default logic (placing like a normal block).</li>
 *		<li><b>dummyTe</b>: An instance of the tile entity never registered to the world (don't try doing that) used for getting additional information about the Tile Entity.
 *							Mostly an internal thing, only exposed in case your Tile Entity uses a special constructor.</li>
 *
 *		<li><b>addSubBlocks</b>: Called by <code>BlockTileEntity</code> to add all the <code>ITeBlock</code>s you want to the creative menu.
 *								{@link BlockTileEntity#getItemStack(ITeBlock)} will be a useful method to get the item stack, a block instance is passed for that reason.
 *								This is only called once on a random <code>ITeBlock</code> for the resource location, because they are internally stored in a {@link Set} so has no order.
 *								Will probably be changed to a static method if/when the move is made to only Java 8 support.
 *	</ul>
 *	<strong>IMPORTANT THINGS TO NOTE</strong>
 *		<dl>
 *			<dt>Getting your instance of <code>BlockTileEntity</code> and <code>ItemBlockTileEntity</code></dt>
 *			<dd>For <code>BlockTileEntity</code>, use {@link TeBlockRegistry#get(ResourceLocation)}.<br/>
 *				For <code>ItemBlockTileEntity</code>, use {@link BlockTileEntity#getItem()}.</dd>
 *			<dt>Using a custom item model for your <code>ITeBlock</code></dt>
 *			<dd>Implement <code>ITeBlockSpecialItem</code> along with <code>ITeBlock</code>, then the item model will be what's provided rather than the block model.</dd>
 *		</dl>
 *		More will be added to this list if questions keep re-occurring, or are about a suitably important topic.
 *	</p>
 *  <hr>
 *	<p>
 *	There, that should be everything you need to utilise <code>ITeBlock</code> to add your own tile entities using IC2's very flexible framework.
 *	I'll be happy to answer any questions you have on it, your best bet is to ask me (Chocohead) on <a href="http://forum.industrial-craft.net">the IC2 forums</a>,
 *	although if you post in the support section other people might be able to help before I see your post.
 *	I'll certainly take suggestions too, this is a first version of <code>ITeBlock</code>, so I'm sure there could be improvements made.
 *	<br/>
 *	<small>Goodness I spent too long on this again...</small>
 *	</p>
 *
 *
 *  @see {@link TeBlockFinalCallEvent} for the event you'll need to subscribe to.
 *	@see {@link ic2.core.ref.TeBlock} for how all the IC2 implements {@link ITeBlock}.
 *	@see {@link ic2.core.ref.TeBlock.HarvestTool} for the currently available tools (you'll need this for adding new tile entities).
 *	@see {@link ic2.core.ref.TeBlock.DefaultDrop} for the currently available drops (you'll need this for adding new tile entities too).
 *	@see {@link ic2.core.ref.TeBlock.ITePlaceHandler} for making your own block placement handler (example in {@link ic2.core.item.ItemHandlers#reactorChamberPlace}).
 *	@see {@link ic2.core.util.Util} for the useful facings you might want for <code>supportedFacings</code>.
 *	@see {@link ic2.core.block.TileEntityBlock} for the base tile entity.
 *	@see {@link ic2.core.block.BlockTileEntity} for the base block (you'll never need to construct this yourself).
 *	@see {@link ic2.core.item.block.ItemBlockTileEntity} for the base item  (you'll never need to construct this yourself either).
 *	@see {@link ITeBlockSpecialItem} for the interface used to have a custom item location.
 *
 *
 *	@since IC2 2.6.114
 *	@version 1.2
 *
 *  @author Chocohead
 */
public interface ITeBlock {
}