package org.ladysnake.blabber.impl.common.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.Text;
import org.ladysnake.blabber.impl.common.FailingOptionalFieldCodec;

import java.util.Optional;

public record UnavailableAction(UnavailableDisplay display, Optional<Text> message) {
    public static Codec<UnavailableAction> codec(Codec<Text> textCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
                UnavailableDisplay.CODEC.fieldOf("display").forGetter(UnavailableAction::display),
                FailingOptionalFieldCodec.of("message", textCodec).forGetter(UnavailableAction::message)
        ).apply(instance, UnavailableAction::new));
    }
}
