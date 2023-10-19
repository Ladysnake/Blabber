package org.ladysnake.blabber.impl.common.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public record UnavailableAction(UnavailableDisplay display, Optional<Text> message) {
    public static final Codec<UnavailableAction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UnavailableDisplay.CODEC.fieldOf("display").forGetter(UnavailableAction::display),
            Codecs.createStrictOptionalFieldCodec(Codecs.TEXT, "message").forGetter(UnavailableAction::message)
    ).apply(instance, UnavailableAction::new));
}
