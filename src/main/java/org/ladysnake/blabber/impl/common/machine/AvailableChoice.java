package org.ladysnake.blabber.impl.common.machine;

import net.minecraft.text.Text;

import java.util.Optional;

public record AvailableChoice(int originalChoiceIndex, Text text, Optional<Text> unavailabilityMessage) {
    public static final AvailableChoice ESCAPE_HATCH = new AvailableChoice(-1, Text.translatable("blabber:dialogue.escape_hatch"), Optional.empty());
}
