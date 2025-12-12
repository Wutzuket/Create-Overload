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

public class BurnrateScrollValueBehaviour extends ScrollValueBehaviour {
    public BurnrateScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
        super(label, be, slot);
        this.withFormatter((v) -> String.valueOf(v));
    }

    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        ImmutableList<Component> rows = ImmutableList.of(Component.translatable("\ud83d\udd25").withStyle(ChatFormatting.BOLD));
        ValueSettingsFormatter formatter = new ValueSettingsFormatter(this::formatSettings);
        return new ValueSettingsBoard(this.label, 100, 10, rows, formatter);
    }

    public void setValueSettings(Player player, ValueSettingsBehaviour.ValueSettings valueSetting, boolean ctrlHeld) {
        int value = Math.max(0, Math.min(100, valueSetting.value()));
        if (!valueSetting.equals(this.getValueSettings())) {
            this.playFeedbackSound(this);
        }

        this.setValue(value);
    }

    public ValueSettingsBehaviour.ValueSettings getValueSettings() {
        return new ValueSettingsBehaviour.ValueSettings(0, Math.max(0, Math.min(100, this.value)));
    }

    public MutableComponent formatSettings(ValueSettingsBehaviour.ValueSettings settings) {
        return CreateLang.number((double)Math.max(0, Math.min(100, settings.value()))).component();
    }

    public String getClipboardKey() {
        return "Burnrate";
    }
}
