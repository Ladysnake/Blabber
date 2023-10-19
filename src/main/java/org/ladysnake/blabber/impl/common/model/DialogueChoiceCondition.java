package org.ladysnake.blabber.impl.common.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record DialogueChoiceCondition(Identifier predicate, UnavailableAction whenUnavailable) {
    public static final Codec<DialogueChoiceCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("predicate").forGetter(DialogueChoiceCondition::predicate),
            UnavailableAction.CODEC.fieldOf("when_unavailable").forGetter(DialogueChoiceCondition::whenUnavailable)
    ).apply(instance, DialogueChoiceCondition::new));
}
