package de.wutzuket.create_overdrive.util;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import de.wutzuket.create_overdrive.blocks.RotatorController.RotatorControllerBlockEntity;

public class StressScrollValueBehaviour extends ScrollValueBehaviour {

    private final SmartBlockEntity host;

    public StressScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
        super(label, be, slot);
        this.host = be;
    }

    private int getDynamicMax() {
        if (host instanceof RotatorControllerBlockEntity rc)
            return Math.max(0, rc.getStressMax());
        return 100;
    }

    // Skalierungsfaktor so wählen, dass maximal 100 Anzeigeeinträge entstehen
    private int getScaleFactor() {
        int dynamicMax = getDynamicMax();
        if (dynamicMax <= 100) return 1;
        return (int) Math.ceil((double) dynamicMax / 100.0);
    }

    // Maximale angezeigte Zahl (nach Skalierung), kann 0 sein
    private int getDisplayedMax() {
        int dynamicMax = getDynamicMax();
        int scale = getScaleFactor();
        if (scale <= 0) return 0;
        return (int) Math.ceil((double) dynamicMax / scale);
    }

    // Mapping: angezeigter Wert -> tatsächlicher Wert
    private int displayedToActual(int displayed) {
        int actual = displayed * getScaleFactor();
        return Math.max(0, Math.min(getDynamicMax(), actual));
    }

    // Mapping: tatsächlicher Wert -> angezeigter Wert
    private int actualToDisplayed(int actual) {
        int scale = getScaleFactor();
        if (scale <= 0) return 0;
        return Math.max(0, Math.min(getDisplayedMax(), (int) Math.round((double) actual / scale)));
    }

    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        ImmutableList<Component> rows = ImmutableList.of(Component.translatable("create_overdrive.gui.power.stress").withStyle(ChatFormatting.BOLD));
        ValueSettingsFormatter formatter = new ValueSettingsFormatter(this::formatSettings);
        int displayedMax = getDisplayedMax();
        int milestone = Math.max(1, displayedMax / 10);
        return new ValueSettingsBoard(this.label, displayedMax, milestone, rows, formatter);
    }

    public void setValueSettings(Player player, ValueSettingsBehaviour.ValueSettings valueSetting, boolean ctrlHeld) {
        int displayed = Math.max(0, Math.min(getDisplayedMax(), valueSetting.value()));
        int actual = displayedToActual(displayed);

        // Feedback wenn sich die angezeigten Einstellungen geändert haben
        if (!valueSetting.equals(this.getValueSettings())) {
            this.playFeedbackSound(this);
        }

        this.setValue(actual);
    }

    public ValueSettingsBehaviour.ValueSettings getValueSettings() {
        int displayedMax = getDisplayedMax();
        int displayed = actualToDisplayed(this.value);
        return new ValueSettingsBehaviour.ValueSettings(0, Math.max(0, Math.min(displayedMax, displayed)));
    }

    public MutableComponent formatSettings(ValueSettingsBehaviour.ValueSettings settings) {
        // Beim Anzeigen die tatsächliche (skalierte) Zahl formatieren
        int displayed = Math.max(0, settings.value());
        int actual = displayedToActual(displayed);
        return CreateLang.number(Math.max(0, actual)).component();
    }

    public String getClipboardKey() {
        return "stress";
    }
}
