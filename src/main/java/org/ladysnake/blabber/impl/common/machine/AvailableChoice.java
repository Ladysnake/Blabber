package org.ladysnake.blabber.impl.common.machine;

import net.minecraft.text.Text;

import java.util.Optional;

public record AvailableChoice(int originalChoiceIndex, Text text, Optional<Text> unavailabilityMessage) {}
