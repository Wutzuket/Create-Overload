package de.wutzuket.create_overdrive.index;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid.PotionFluidType;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.tterrag.registrate.builders.FluidBuilder.FluidTypeFactory;
import com.tterrag.registrate.util.entry.FluidEntry;

import de.wutzuket.create_overdrive.Main;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry.InteractionInformation;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

@SuppressWarnings("removal")
public class CPAFluids {
	private static final CreateRegistrate REGISTRATE = Main.REGISTRATE;

    public static final FluidEntry<BaseFlowingFluid.Flowing> FUSIONREACTORFUEL =
            REGISTRATE.standardFluid("fusion_reactor_fuel",
                            SolidRenderedPlaceableFluidType.create(0x622020, () -> 1f / 32f))
                    .lang("Fusion Reactor Fuel")
                    .properties(b -> b.viscosity(1500).density(1400).temperature(350))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2).tickRate(25).slopeFindDistance(3).explosionResistance(100f))
                    .source(BaseFlowingFluid.Source::new)
                    .block()
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
                    .build()
                    .register();

	// Load this class

	public static void register() {
	}

	public static void registerFluidInteractions() {

		FluidInteractionRegistry.addInteraction(NeoForgeMod.LAVA_TYPE.value(), new InteractionInformation(
                FUSIONREACTORFUEL.get().getFluidType(),
			fluidState -> {
				if (fluidState.isSource()) {
					return Blocks.OBSIDIAN.defaultBlockState();
				} else {
					return AllPaletteStoneTypes.SCORIA.getBaseBlock()
						.get()
						.defaultBlockState();
				}
			}
		));

		// Auch die "umgekehrte" Richtung registrieren: unser Fuel fließt in Lava
		FluidInteractionRegistry.addInteraction(FUSIONREACTORFUEL.get().getFluidType(), new InteractionInformation(
                NeoForgeMod.LAVA_TYPE.value(),
			fluidState -> {
				if (fluidState.isSource()) {
					return Blocks.OBSIDIAN.defaultBlockState();
				} else {
					return AllPaletteStoneTypes.SCORIA.getBaseBlock()
						.get()
						.defaultBlockState();
				}
			}
		));
	}

	@Nullable
	public static BlockState getLavaInteraction(FluidState fluidState) {
		Fluid fluid = fluidState.getType();
		// Quelle UND Flusszustand vergleichen
		if (fluid.isSame(FUSIONREACTORFUEL.get()) || fluid.isSame(FUSIONREACTORFUEL.getSource()))
			return AllPaletteStoneTypes.SCORIA.getBaseBlock()
				.get()
				.defaultBlockState();
		return null;
	}

	private static final DispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();
	private static final DispenseItemBehavior DISPENSE_FLUID = new DefaultDispenseItemBehavior(){
			@Override
			protected ItemStack execute(BlockSource pSource, ItemStack pStack) {
				DispensibleContainerItem dispensibleContainerItem = (DispensibleContainerItem) pStack.getItem();
				BlockPos pos = pSource.pos().relative(pSource.state().getValue(DispenserBlock.FACING));
				Level level = pSource.level();
				if (dispensibleContainerItem.emptyContents(null, level, pos, null, pStack)) {
					return new ItemStack(Items.BUCKET);
				}
				return DEFAULT.dispense(pSource, pStack);
			}
		};

	private static void registerFluidDispenseBehavior(BucketItem bucket) {
		DispenserBlock.registerBehavior(bucket, DISPENSE_FLUID);
	}

	public static abstract class TintedFluidType extends FluidType {

		protected static final int NO_TINT = 0xffffffff;
		private final ResourceLocation stillTexture;
		private final ResourceLocation flowingTexture;

		public TintedFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
			super(properties);
			this.stillTexture = stillTexture;
			this.flowingTexture = flowingTexture;
		}

		@Override
		public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
			consumer.accept(new IClientFluidTypeExtensions() {
				private final ResourceLocation WATER_STILL = ResourceLocation.fromNamespaceAndPath("minecraft","block/water_still");
				private final ResourceLocation WATER_FLOW = ResourceLocation.fromNamespaceAndPath("minecraft","block/water_flow");
				private boolean loggedMissing = false;

				private boolean resourceExists(ResourceLocation rl) {
					try {
						// Client-only API
						var rm = net.minecraft.client.Minecraft.getInstance().getResourceManager();
						Optional<?> opt = (Optional<?>) rm.getClass().getMethod("getResource", ResourceLocation.class).invoke(rm, rl);
						return opt.isPresent();
					} catch (Throwable ignored) {
						return false;
					}
				}

				private ResourceLocation resolve(ResourceLocation original, boolean flowing) {
					// 1. original
					if (resourceExists(original)) return original;
					// 2. alternate Ordner fluid->block
					String path = original.getPath();
					if (path.startsWith("fluid/")) {
						ResourceLocation alt = ResourceLocation.fromNamespaceAndPath(original.getNamespace(), "block/" + path.substring(6));
						if (resourceExists(alt)) {
							if (!loggedMissing) {
								Create.LOGGER.warn("[create_overdrive] Fluid-Textur nicht unter " + original + " gefunden, nutze Alternative " + alt);
							}
							loggedMissing = true;
							return alt;
						}
					}
					// 3. Fallback Wasser
					if (!loggedMissing) {
						Create.LOGGER.warn("[create_overdrive] Fluid-Textur weder unter " + original + " noch block/... vorhanden – fallback Wasser. PNG fehlt?");
						loggedMissing = true;
					}
					return flowing ? WATER_FLOW : WATER_STILL;
				}

				@Override
				public ResourceLocation getStillTexture() {
					return resolve(stillTexture, false);
				}

				@Override
				public ResourceLocation getFlowingTexture() {
					return resolve(flowingTexture, true);
				}

				@Override
				public int getTintColor(FluidStack stack) {
					return TintedFluidType.this.getTintColor(stack);
				}

				@Override
				public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
					return TintedFluidType.this.getTintColor(state, getter, pos);
				}

				@Override
				public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level,
														int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
					Vector3f customFogColor = TintedFluidType.this.getCustomFogColor();
					return customFogColor == null ? fluidFogColor : customFogColor;
				}

				@Override
				public void modifyFogRender(Camera camera, FogMode mode, float renderDistance, float partialTick,
											float nearDistance, float farDistance, FogShape shape) {
					float modifier = TintedFluidType.this.getFogDistanceModifier();
					float baseWaterFog = 96.0f;
					if (modifier != 1f) {
						RenderSystem.setShaderFogShape(FogShape.CYLINDER);
						RenderSystem.setShaderFogStart(-8);
						RenderSystem.setShaderFogEnd(baseWaterFog * modifier);
					}
				}

			});
		}

		protected abstract int getTintColor(FluidStack stack);

		protected abstract int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos);

		protected Vector3f getCustomFogColor() {
			return null;
		}

		protected float getFogDistanceModifier() {
			return 1f;
		}

		// Verhindert, dass das Fluid in "ultraWarm"-Dimensionen (z. B. Nether) sofort verdampft
		@Override
		public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
			return false;
		}
	}

	private static class SolidRenderedPlaceableFluidType extends TintedFluidType {
		// Erzwinge dauerhaft Wasser-Texturen
		private static final boolean TEST_USE_WATER_TEXTURES = true;
		private Vector3f fogColor;
		private Supplier<Float> fogDistance;

		public static FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
			final ResourceLocation resolvedStill = TEST_USE_WATER_TEXTURES
				? ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still")
				: stillTexture;
			final ResourceLocation resolvedFlow = TEST_USE_WATER_TEXTURES
				? ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_flow")
				: flowingTexture;

			return (p, s, f) -> {
				SolidRenderedPlaceableFluidType fluidType = new SolidRenderedPlaceableFluidType(p, resolvedStill, resolvedFlow);
				fluidType.fogColor = new Color(fogColor, false).asVectorF();
				fluidType.fogDistance = fogDistance;
				// Debug-Prüfung entfällt – echte Prüfung nun in initializeClient
				return fluidType;
			};
		}

		public static FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance) {
			return create(fogColor, fogDistance,
				ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still"),
				ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_flow"));
		}

		private SolidRenderedPlaceableFluidType(Properties properties, ResourceLocation stillTexture,
												ResourceLocation flowingTexture) {
			super(properties, stillTexture, flowingTexture);
		}

		// Fester Tint (ARGB). Anpassbar per setFusionReactorFuelTint. Kein Farbwechsel mehr.
		public static int FUSION_REACTOR_FUEL_TINT = 0xFFff8f1f;

		public static void setFusionReactorFuelTint(int argb) { FUSION_REACTOR_FUEL_TINT = argb; }

		@Override
		protected int getTintColor(FluidStack stack) {
			return FUSION_REACTOR_FUEL_TINT;
		}

		@Override
		public int getTintColor(FluidState state, BlockAndTintGetter world, BlockPos pos) {
			return FUSION_REACTOR_FUEL_TINT;
		}

		@Override
		protected Vector3f getCustomFogColor() {
			return fogColor;
		}

		@Override
		protected float getFogDistanceModifier() {
			return fogDistance.get();
		}

	}

}
