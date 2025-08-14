package de.wutzuket.create_overdrive.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

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
}
