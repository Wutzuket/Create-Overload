package de.wutzuket.create_overdrive.ponder;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import de.wutzuket.create_overdrive.blocks.Ionator.IonatorBlockEntity;
import net.createmod.catnip.data.IntAttached;
import net.createmod.catnip.math.Pointing;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;


public class CPAPonderScenes {
    public CPAPonderScenes(){

    }

    public static void particleAcceleratorCoreScene(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("particle_accelerator_core", "Building and Using the Particle Accelerator Core");
        scene.configureBasePlate(0, 0, 11);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos corePos = util.grid().at(5, 1, 5);
        BlockPos inputPos = util.grid().at(0, 1, 5);
        BlockPos outputPos = util.grid().at(5, 1, 0);

        // Zeige den Particle Accelerator Core
        scene.world().showSection(util.select().position(corePos), Direction.DOWN);
        scene.idle(10);

        // Zeige die umliegende Struktur
        scene.world().showSection(util.select().fromTo(0, 1, 0, 10, 2, 10), Direction.DOWN);
        scene.idle(20);

        // Zeige Text über den Core
        scene.overlay().showText(60)
                .text("This is the Particle Accelerator Core. It is the heart of the machine.")
                .placeNearTarget()
                .pointAt(util.vector().topOf(corePos));
        scene.idle(70);

        // Zeige den Input-Block
        scene.world().showSection(util.select().position(inputPos), Direction.DOWN);
        scene.overlay().showText(60)
                .text("This is the Input Block. Materials are inserted here.")
                .placeNearTarget()
                .pointAt(util.vector().topOf(inputPos));
        scene.idle(70);

        // Zeige den Output-Block
        scene.world().showSection(util.select().position(outputPos), Direction.DOWN);
        scene.overlay().showText(60)
                .text("This is the Output Block. Results are output here.")
                .placeNearTarget()
                .pointAt(util.vector().topOf(outputPos));
        scene.idle(70);

        // Zeige die Aktivierung
        scene.overlay().showText(80)
                .text("Build a circular structure using Brass Casings.")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(corePos));
        scene.idle(90);

        // Zeige die Aktivierung
        scene.overlay().showText(80)
                .text("Activate the core by building the structure correctly.")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(corePos));
        scene.idle(90);

        // Zeige Partikel-Effekte bei erfolgreicher Aktivierung
        scene.overlay().showText(100)
                .text("If the structure is correct, the core will activate and start working.")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(corePos));
        scene.world().showSection(util.select().fromTo(0, 1, 0, 10, 2, 10), Direction.UP);
        scene.idle(100);

        // Markiere die Szene als abgeschlossen
        scene.markAsFinished();
    }

    public static void ionize(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("ionator", "Processing Items with the Ionator");
        scene.configureBasePlate(0, 0, 5);
        scene.world().setBlock(util.grid().at(1, 1, 2), AllBlocks.ANDESITE_CASING.getDefaultState(), false);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(1, 4, 3, 1, 1, 5), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(1, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(1, 2, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(1, 4, 2), Direction.SOUTH);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(3, 1, 1, 1, 1, 1), Direction.SOUTH);
        scene.world().showSection(util.select().fromTo(3, 1, 5, 3, 1, 2), Direction.SOUTH);
        scene.idle(20);

        BlockPos basin = util.grid().at(1, 2, 2);
        BlockPos pressPos = util.grid().at(1, 4, 2);
        Vec3 basinSide = util.vector().blockSurface(basin, Direction.WEST);

        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        ItemStack copper = new ItemStack(Items.COPPER_INGOT);
        ItemStack gold = new ItemStack(Items.GOLD_NUGGET);

        scene.overlay().showText(60)
                .pointAt(basinSide)
                .placeNearTarget()
                .attachKeyFrame()
                .text("With a Ionator and Basin, some Crafting Recipes can be automated");
        scene.idle(40);

        scene.overlay().showControls(util.vector().topOf(basin), Pointing.LEFT, 30).withItem(iron);
        scene.overlay().showControls(util.vector().topOf(basin), Pointing.RIGHT, 30).withItem(copper);
        scene.idle(30);
        Class<IonatorBlockEntity> type = IonatorBlockEntity.class;
        scene.world().modifyBlockEntity(pressPos, type, IonatorBlockEntity::startProcessingBasin);
        scene.world().createItemOnBeltLike(basin, Direction.UP, copper);
        scene.world().createItemOnBeltLike(basin, Direction.UP, iron);
        scene.idle(80);
        scene.world().modifyBlockEntityNBT(util.select().position(basin), BasinBlockEntity.class, nbt -> {
            nbt.put("VisualizedItems",
                    NBTHelper.writeCompoundList(ImmutableList.of(IntAttached.with(1, gold)), ia -> (CompoundTag) ia.getValue().saveOptional(scene.world().getHolderLookupProvider())));
        });
        scene.idle(4);
        scene.world().createItemOnBelt(util.grid().at(1, 1, 1), Direction.UP, gold);
        scene.idle(30);



        scene.rotateCameraY(-30);
        scene.idle(10);
        scene.world().setBlock(util.grid().at(1, 1, 2), AllBlocks.BLAZE_BURNER.getDefaultState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), true);
        scene.idle(10);

        scene.overlay().showText(80)
                .pointAt(basinSide.subtract(0, 1, 0))
                .placeNearTarget()
                .text("Some of those recipes may require the heat of a Blaze Burner");
        scene.idle(40);

        scene.rotateCameraY(30);

        scene.idle(60);
        Vec3 filterPos = util.vector().of(1, 2.75f, 2.5f);
        scene.overlay().showFilterSlotInput(filterPos, Direction.WEST, 100);
        scene.overlay().showText(100)
                .pointAt(filterPos)
                .placeNearTarget()
                .attachKeyFrame()
                .text("The filter slot can be used in case two recipes are conflicting.");
        scene.idle(80);
    }

    public static void fusionReactorCore(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("fusion_reactor", "Building and Using the Fusion Reactor");
        scene.configureBasePlate(0, 0, 11);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos corePos = util.grid().at(5, 3, 5);
        BlockPos inputPos = util.grid().at(0, 2, 4);

        scene.world().showSection(util.select().position(corePos), Direction.DOWN);
        scene.idle(10);

        for (int i = -2; i <= 3; i++) {
            scene.world().showSection(util.select().position(corePos.getX() + i, corePos.getY() - 1, corePos.getZ() + 3), Direction.DOWN);
            scene.world().showSection(util.select().position(corePos.getX() - i, corePos.getY() - 1, corePos.getZ() - 3), Direction.DOWN);
            scene.world().showSection(util.select().position(corePos.getX() + 3, corePos.getY() - 1, corePos.getZ() - i), Direction.DOWN);
            scene.world().showSection(util.select().position(corePos.getX() - 3, corePos.getY() - 1, corePos.getZ() + i), Direction.DOWN);
        }

        scene.overlay().showText(60)
                .pointAt(util.vector().topOf(corePos))
                .placeNearTarget()
                .attachKeyFrame()
                .text("The Fusion Reactor is build by multiple layers");
        scene.idle(10);

        // Äußerer Ring
        for (int i = -3; i <= 4; i++) {
            scene.world().showSection(util.select().position(corePos.getX() + i, corePos.getY(), corePos.getZ() + 4), Direction.DOWN);
            scene.world().showSection(util.select().position(corePos.getX() - i, corePos.getY(), corePos.getZ() - 4), Direction.DOWN);
            scene.world().showSection(util.select().position(corePos.getX() + 4, corePos.getY(), corePos.getZ() - i), Direction.DOWN);
            scene.world().showSection(util.select().position(corePos.getX() - 4, corePos.getY(), corePos.getZ() + i), Direction.DOWN);
        }

        // Innerer Ring
        for (int i = -1; i <= 2; i++) {
            scene.world().showSection(util.select().position(corePos.getX() + i, corePos.getY(), corePos.getZ() + 2), Direction.DOWN);
            scene.world().showSection(util.select().position(corePos.getX() - i, corePos.getY(), corePos.getZ() - 2), Direction.DOWN);
            scene.world().showSection(util.select().position(corePos.getX() + 2, corePos.getY(), corePos.getZ() - i), Direction.DOWN);
            scene.world().showSection(util.select().position(corePos.getX() - 2, corePos.getY(), corePos.getZ() + i), Direction.DOWN);
        }

        // Mitte vom inneren Ring
        scene.world().showSection(util.select().position(corePos.getX() + 1, corePos.getY(), corePos.getZ()), Direction.DOWN);
        scene.world().showSection(util.select().position(corePos.getX() - 1, corePos.getY(), corePos.getZ()), Direction.DOWN);
        scene.world().showSection(util.select().position(corePos.getX(), corePos.getY(), corePos.getZ() - 1), Direction.DOWN);
        scene.world().showSection(util.select().position(corePos.getX(), corePos.getY(), corePos.getZ() + 1), Direction.DOWN);

        scene.idle(10);

        for (int i = -2; i <= 3; i++) {
            scene.world().showSection(util.select().position(corePos.getX() + i, corePos.getY() + 1, corePos.getZ() + 3), Direction.DOWN);
            scene.world().showSection(util.select().position(corePos.getX() - i, corePos.getY() + 1, corePos.getZ() - 3), Direction.DOWN);
            scene.world().showSection(util.select().position(corePos.getX() + 3, corePos.getY() + 1, corePos.getZ() - i), Direction.DOWN);
            scene.world().showSection(util.select().position(corePos.getX() - 3, corePos.getY() + 1, corePos.getZ() + i), Direction.DOWN);
        }

        scene.overlay().showText(40)
                .pointAt(util.vector().topOf(inputPos))
                .placeNearTarget()
                .attachKeyFrame()
                .text("Through the Input block, you insert Fusion Fuel");

        scene.idle(40);

        scene.world().showSection(util.select().position(corePos.above(1)), Direction.DOWN);
        scene.world().showSection(util.select().position(corePos.above(2)), Direction.DOWN);

        scene.idle(40);

        scene.overlay().showText(60)
                .pointAt(util.vector().topOf(corePos))
                .placeNearTarget()
                .text("At the bottom of the core you can adjust the Burn Rate");

        scene.idle(20);

        scene.world().modifyBlockEntityNBT(util.select().position(corePos.above(2)), StressGaugeBlockEntity.class,
                nbt -> nbt.putFloat("Value", .25f));

        scene.overlay().showText(40)
                .pointAt(util.vector().topOf(corePos.above(2)))
                .placeNearTarget()
                .attachKeyFrame()
                .text("The higher the Burn Rate, the more stress is generated");

        scene.idle(40);

    }

    public static void rotator(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("rotator", "Building and Using the Rotator");
        scene.configureBasePlate(0, 0, 8);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos corePos = util.grid().at(1, 3, 2);
        BlockPos inputPos = util.grid().at(5, 3, 2);
        BlockPos outputPos = util.grid().at(2, 4, 1);

        scene.world().showSection(util.select().position(corePos), Direction.DOWN);
        scene.idle(10);

        int cos = 1;
        int sin = 0;

        Vec3 blockSurface = util.vector().blockSurface(corePos, Direction.WEST)
                .add(0, 0, 0);
        scene.overlay().showFilterSlotInput(blockSurface, Direction.WEST, 80);
        scene.overlay().showControls(blockSurface, Pointing.DOWN, 60).rightClick();
        scene.idle(20);

        scene.overlay().showText(40)
                .text("In the controller you can adjust the max stress")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(blockSurface);

        scene.idle(40);

        //Layer 1
        for (int i = 0; i <= 1; i++) {
            scene.world().showSection(
                    util.select().position(
                            corePos.getX(),
                            corePos.getY() + i,
                            corePos.getZ() + (-1 * cos)
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX(),
                            corePos.getY() - i,
                            corePos.getZ() + cos
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX(),
                            corePos.getY() - 1,
                            corePos.getZ() + (-i * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos + i * sin),
                            corePos.getY() + 1,
                            corePos.getZ() + (i * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );
        }



        scene.overlay().showText(40)
                .pointAt(util.vector().topOf(corePos))
                .placeNearTarget()
                .attachKeyFrame()
                .text("The Rotator is build by multiple layers");
        scene.idle(40);

        //Layer 2
        for (int i = 0; i <= 1; i++) {
            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos - 1 * sin) + 1,
                            corePos.getY() + i,
                            corePos.getZ() + (-1 * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos + 1 * sin) + 1,
                            corePos.getY() - i,
                            corePos.getZ() + (1 * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos - i * sin) + 1,
                            corePos.getY() - 1,
                            corePos.getZ() + (-i * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos + i * sin) + 1,
                            corePos.getY() + 1,
                            corePos.getZ() + (i * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );
        }
        scene.world().showSection(util.select().position(corePos.offset(1, 0, 0)), Direction.DOWN);

        scene.idle(10);

        for (int i = 0; i <= 1; i++) {
            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos - 1 * sin) + 2,
                            corePos.getY() + i,
                            corePos.getZ() + (-1 * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos + 1 * sin) + 2,
                            corePos.getY() - i,
                            corePos.getZ() + (1 * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos - i * sin) + 2,
                            corePos.getY() - 1,
                            corePos.getZ() + (-i * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos + i * sin) + 2,
                            corePos.getY() + 1,
                            corePos.getZ() + (i * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );
        }
        scene.world().showSection(util.select().position(corePos.offset(2, 0, 0)), Direction.DOWN);

        scene.idle(10);

        scene.overlay().showText(40)
                .pointAt(util.vector().topOf(corePos.offset(2,1,0)))
                .placeNearTarget()
                .attachKeyFrame()
                .text("You can stack this layer up to 10 times to increase the generation");

        scene.rotateCameraY(90);

        scene.idle(40);

        for (int i = 0; i <= 1; i++) {
            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + 3,
                            corePos.getY() + i,
                            corePos.getZ() + (-1 * cos)
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos + 1 * sin) + 3,
                            corePos.getY() - i,
                            corePos.getZ() + (1 * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos - i * sin) + 3,
                            corePos.getY() - 1,
                            corePos.getZ() + (-i * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos + i * sin) + 3,
                            corePos.getY() + 1,
                            corePos.getZ() + (i * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );
        }
        scene.world().showSection(util.select().position(corePos.offset(3, 0, 0)), Direction.DOWN);

        scene.idle(10);

        for (int i = 0; i <= 1; i++) {
            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos - 1 * sin) + 4,
                            corePos.getY() + i,
                            corePos.getZ() + (-1 * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos + 1 * sin) + 4,
                            corePos.getY() - i,
                            corePos.getZ() + (1 * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos - i * sin) + 4,
                            corePos.getY() - 1,
                            corePos.getZ() + (-i * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );

            scene.world().showSection(
                    util.select().position(
                            corePos.getX() + (0 * cos + i * sin) + 4,
                            corePos.getY() + 1,
                            corePos.getZ() + (i * cos + 0 * sin)
                    ),
                    Direction.DOWN
            );
        }
        scene.world().showSection(util.select().position(corePos.offset(4, 0, 0)), Direction.DOWN);

        scene.idle(10);

        scene.overlay().showText(40)
                .pointAt(util.vector().centerOf(inputPos))
                .placeNearTarget()
                .attachKeyFrame()
                .text("Here comes the power from");

        scene.idle(40);


        scene.overlay().showText(40)
                .pointAt(util.vector().topOf(outputPos))
                .placeNearTarget()
                .text("You can connect any cable from other mods to extract the FE");

        scene.idle(40);

    }


}
