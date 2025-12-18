package de.wutzuket.create_overdrive;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import de.wutzuket.create_overdrive.blocks.AcceleratorInput.AcceleratorinputBlockEntity;
import de.wutzuket.create_overdrive.blocks.AcceleratorOutput.AcceleratorOutputBlockEntity;
import de.wutzuket.create_overdrive.blocks.FusionReactorInput.FusionReactorInputBlockEntity;
import de.wutzuket.create_overdrive.blocks.ParticleAccelerator.AcceleratorStructure;
import de.wutzuket.create_overdrive.blocks.RotatorController.RotatorControllerBlockEntity;
import de.wutzuket.create_overdrive.blocks.RotatorInput.RotatorInputBlockEntity;
import de.wutzuket.create_overdrive.blocks.RotatorOutput.RotatorOutputBlockEntity;
import de.wutzuket.create_overdrive.config.Config;
import de.wutzuket.create_overdrive.index.CPABlockEntities;
import de.wutzuket.create_overdrive.index.CPABlocks;
import de.wutzuket.create_overdrive.index.CPAFluids;
import de.wutzuket.create_overdrive.ponder.CPAPonderPlugin;
import de.wutzuket.create_overdrive.recipe.ModRecipes;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod("create_overdrive")
public class Main {
    public static final String MODID = "create_overdrive";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(Main.MODID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );;


    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("create_overdrive");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("create_overdrive");

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS;
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS;
    public static final DeferredItem<Item> STELLAR_STEEL_ALLOY;

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PARTICLE_ACCELERATOR_TAB;
    public static int RADIUS;
    public static int BlockCountAccelerator;

    public Main(IEventBus modEventBus, ModContainer modContainer) {
        // Registrierung zuerst
        REGISTRATE.registerEventListeners(modEventBus);
        CPABlockEntities.register();
        CPAFluids.register();
        CPABlocks.register();
        CREATIVE_MODE_TABS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        ModRecipes.register(modEventBus);
        modEventBus.addListener(this::registerCapabilities);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(Type.COMMON, Config.SPEC);
        updateConfigValues();
        // Listener zuletzt registrieren
        modEventBus.addListener(Main::addCreative);
        modEventBus.addListener(this::doClientStuff);
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
            event.accept(CPABlocks.PARTICLE_ACCELERATOR_CORE);
            event.accept(CPABlocks.ACCELERATOR_OUTPUT);
            event.accept(CPABlocks.ACCELERATOR_INPUT);
            event.accept((CPABlocks.FUSION_REACTOR_CASING));
            event.accept((CPABlocks.FUSION_REACTOR_CORE));
            event.accept((CPABlocks.FUSION_REACTOR_INPUT));
            event.accept(CPABlocks.IONATOR);
            event.accept(CPAFluids.FUSIONREACTORFUEL.getBucket().get());
            event.accept(STELLAR_STEEL_ALLOY);
            event.accept(CPABlocks.ROTATOR_CASING);
        }

    }

    public void doClientStuff(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CPAPonderPlugin());

        RenderType cutout = RenderType.cutoutMipped();

        ItemBlockRenderTypes.setRenderLayer(CPABlocks.PARTICLE_ACCELERATOR_CORE.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(CPABlocks.ROTATOR_INPUT.get(), cutout);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        AcceleratorinputBlockEntity.registerCapabilities(event);
        AcceleratorOutputBlockEntity.registerCapabilities(event);
        FusionReactorInputBlockEntity.registerCapabilities(event);
        RotatorInputBlockEntity.registerCapabilities(event);
        RotatorOutputBlockEntity.registerCapabilities(event);
    }

    static {
        RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "create_overdrive");
        CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "create_overdrive");

        STELLAR_STEEL_ALLOY = ITEMS.registerSimpleItem("stellar_steel_alloy");
        PARTICLE_ACCELERATOR_TAB = CREATIVE_MODE_TABS.register("create_overdrive_tab", () -> net.minecraft.world.item.CreativeModeTab.builder().icon(() -> new net.minecraft.world.item.ItemStack(CPABlocks.PARTICLE_ACCELERATOR_CORE.get())).title(net.minecraft.network.chat.Component.translatable("itemGroup.create_overdrive_tab")).build());
        RADIUS = 10;
    }


}
