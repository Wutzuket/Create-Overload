package de.wutzuket.create_overdrive.ponder;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
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
        scene.title("particle_accelerator_core", "Aufbau und Verwendung des Particle Accelerator Core");
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

        ItemStack blue = new ItemStack(Items.BLUE_DYE);
        ItemStack red = new ItemStack(Items.RED_DYE);
        ItemStack purple = new ItemStack(Items.PURPLE_DYE);

        scene.overlay().showText(60)
                .pointAt(basinSide)
                .placeNearTarget()
                .attachKeyFrame()
                .text("With a Ionator and Basin, some Crafting Recipes can be automated");
        scene.idle(40);

        scene.overlay().showControls(util.vector().topOf(basin), Pointing.LEFT, 30).withItem(blue);
        scene.overlay().showControls(util.vector().topOf(basin), Pointing.RIGHT, 30).withItem(red);
        scene.idle(30);
        Class<IonatorBlockEntity> type = IonatorBlockEntity.class;
        scene.world().modifyBlockEntity(pressPos, type, pte -> pte.startProcessingBasin());
        scene.world().createItemOnBeltLike(basin, Direction.UP, red);
        scene.world().createItemOnBeltLike(basin, Direction.UP, blue);
        scene.idle(80);
        scene.world().modifyBlockEntityNBT(util.select().position(basin), BasinBlockEntity.class, nbt -> {
            nbt.put("VisualizedItems",
                    NBTHelper.writeCompoundList(ImmutableList.of(IntAttached.with(1, purple)), ia -> (CompoundTag) ia.getValue().saveOptional(scene.world().getHolderLookupProvider())));
        });
        scene.idle(4);
        scene.world().createItemOnBelt(util.grid().at(1, 1, 1), Direction.UP, purple);
        scene.idle(30);

        scene.overlay().showText(80)
                .pointAt(basinSide)
                .placeNearTarget()
                .attachKeyFrame()
                .text("Available recipes include any Shapeless Crafting Recipe, plus a couple extra ones");
        scene.idle(80);

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
}
