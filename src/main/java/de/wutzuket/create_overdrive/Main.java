//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.wutzuket.create_overdrive;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import de.wutzuket.create_overdrive.blocks.AcceleratorInput.AcceleratorinputBlock;
import de.wutzuket.create_overdrive.blocks.AcceleratorInput.AcceleratorinputBlockEntity;
import de.wutzuket.create_overdrive.blocks.AcceleratorOutput.AcceleratorOutputBlock;
import de.wutzuket.create_overdrive.blocks.AcceleratorOutput.AcceleratorOutputBlockEntity;
import de.wutzuket.create_overdrive.blocks.FusionReactor.FusionReactorCoreBlock;
import de.wutzuket.create_overdrive.blocks.ParticleAccelerator.AcceleratorStructure;
import de.wutzuket.create_overdrive.blocks.ParticleAccelerator.ParticleAcceleratorCoreBlock;
import de.wutzuket.create_overdrive.blocks.FusionReactor.Casing;
import de.wutzuket.create_overdrive.config.Config;
import de.wutzuket.create_overdrive.index.CPABlockEntities;
import de.wutzuket.create_overdrive.index.CPABlocks;
import de.wutzuket.create_overdrive.particles.ModParticleTypes;
import de.wutzuket.create_overdrive.recipe.ModRecipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod("create_overdrive")
public class Main {
    public static final String MODID = "create_overdrive";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("create_overdrive");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("create_overdrive");
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS;
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS;
    public static final DeferredBlock<Block> PARTICLE_ACCELERATOR_CORE;
    public static final DeferredItem<BlockItem> PARTICLE_ACCELERATOR_CORE_ITEM;
    public static final DeferredBlock<Block> PARTICLE_ACCELERATOR_INPUT;
    public static final DeferredItem<BlockItem> PARTICLE_ACCELERATOR_INPUT_ITEM;
    public static final DeferredBlock<Block> PARTICLE_ACCELERATOR_OUTPUT;
    public static final DeferredItem<BlockItem> PARTICLE_ACCELERATOR_OUTPUT_ITEM;
    public static final DeferredBlock<Block> FUSION_REACTOR_CASING;
    public static final DeferredItem<BlockItem> FUSION_REACTOR_CASING_ITEM;
    public static final DeferredBlock<Block> FUSION_REACTOR_CORE;
    public static final DeferredItem<BlockItem> FUSION_REACTOR_CORE_ITEM;
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PARTICLE_ACCELERATOR_TAB;
    public static int RADIUS;
    public static int BlockCountAccelerator;
    public static final CreateRegistrate REGISTRATE;

    public Main(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(Main::addCreative);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        REGISTRATE.registerEventListeners(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(Type.COMMON, Config.SPEC);
        updateConfigValues();
        CPABlockEntities.register();
        ModParticleTypes.register(modEventBus);
        ModRecipes.register(modEventBus);
        modEventBus.addListener(this::registerCapabilities);
    }

    public static void updateConfigValues() {
        RADIUS = Config.radius;
        BlockCountAccelerator = AcceleratorStructure.calc_Blocks();
        LOGGER.info("Config updated: radius={}, blockCount={}", RADIUS, BlockCountAccelerator);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tabKey = event.getTabKey();
        if (tabKey.location().equals(PARTICLE_ACCELERATOR_TAB.getId())) {
            event.accept(PARTICLE_ACCELERATOR_CORE);
            event.accept(CPABlocks.ACCELERATOR_OUTPUT);
            event.accept(CPABlocks.ACCELERATOR_INPUT);
            event.accept((CPABlocks.FUSION_REACTOR_CASING));
            event.accept((CPABlocks.FUSION_REACTOR_CORE));
        }

    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        AcceleratorinputBlockEntity.registerCapabilities(event);
        AcceleratorOutputBlockEntity.registerCapabilities(event);
    }

    static {
        RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "create_overdrive");
        CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "create_overdrive");
        PARTICLE_ACCELERATOR_CORE = BLOCKS.register("particle_accelerator_core", () -> new ParticleAcceleratorCoreBlock(Properties.of().mapColor(MapColor.METAL).strength(3.5F)));
        PARTICLE_ACCELERATOR_CORE_ITEM = ITEMS.registerSimpleBlockItem("particle_accelerator_core", PARTICLE_ACCELERATOR_CORE);
        PARTICLE_ACCELERATOR_INPUT = BLOCKS.register("particle_accelerator_input", () -> new AcceleratorinputBlock(Properties.of().mapColor(MapColor.METAL).strength(3.5F)));
        PARTICLE_ACCELERATOR_INPUT_ITEM = ITEMS.registerSimpleBlockItem("particle_accelerator_input", PARTICLE_ACCELERATOR_INPUT);
        PARTICLE_ACCELERATOR_OUTPUT = BLOCKS.register("particle_accelerator_output", () -> new AcceleratorOutputBlock(Properties.of().mapColor(MapColor.METAL).strength(3.5F)));
        PARTICLE_ACCELERATOR_OUTPUT_ITEM = ITEMS.registerSimpleBlockItem("particle_accelerator_output", PARTICLE_ACCELERATOR_OUTPUT);
        FUSION_REACTOR_CASING = BLOCKS.register("fusion_reactor_casing", () -> new Casing(Properties.of().mapColor(MapColor.METAL).strength(3.5F)));
        FUSION_REACTOR_CASING_ITEM = ITEMS.registerSimpleBlockItem("fusion_reactor_casing", FUSION_REACTOR_CASING);
        FUSION_REACTOR_CORE = BLOCKS.register("fusion_reactor_core", () -> new FusionReactorCoreBlock(Properties.of().mapColor(MapColor.METAL).strength(3.5F)));
        FUSION_REACTOR_CORE_ITEM = ITEMS.registerSimpleBlockItem("fusion_reactor_core", FUSION_REACTOR_CORE);
        PARTICLE_ACCELERATOR_TAB = CREATIVE_MODE_TABS.register("create_overdrive_tab", () -> CreativeModeTab.builder().icon(() -> new ItemStack((ItemLike)PARTICLE_ACCELERATOR_CORE.get())).title(Component.translatable("itemGroup.create_overdrive_tab")).build());
        RADIUS = 10;
        REGISTRATE = CreateRegistrate.create("create_overdrive");
    }
}
