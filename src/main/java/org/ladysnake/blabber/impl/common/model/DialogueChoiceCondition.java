package org.ladysnake.blabber.impl.common.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record DialogueChoiceCondition(Identifier predicate, UnavailableAction whenUnavailable) {
    public static Codec<DialogueChoiceCondition> codec(Codec<Text> textCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("predicate").forGetter(DialogueChoiceCondition::predicate),
                UnavailableAction.codec(textCodec).fieldOf("when_unavailable").forGetter(DialogueChoiceCondition::whenUnavailable)
        ).apply(instance, DialogueChoiceCondition::new));
    }
}
