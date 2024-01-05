/*
 * Blabber
 * Copyright (C) 2022-2024 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package org.ladysnake.blabber.impl.common;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.Set;

public record InterlocutorPropertiesLootCondition(EntityPredicate predicate) implements LootCondition {
    public static final Codec<InterlocutorPropertiesLootCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codecs.JSON_ELEMENT.xmap(EntityPredicate::fromJson, EntityPredicate::toJson)
                           .fieldOf("predicate")
                           .forGetter(InterlocutorPropertiesLootCondition::predicate)
    ).apply(instance, InterlocutorPropertiesLootCondition::new));

    public static final LootConditionType TYPE =
        new LootConditionType(new JsonSerializer<InterlocutorPropertiesLootCondition>() {
            @Override
            public void toJson(
                final JsonObject json,
                final InterlocutorPropertiesLootCondition object,
                final JsonSerializationContext context
            ) {
                json.asMap().putAll(CODEC.encodeStart(JsonOps.INSTANCE, object)
                                         .result()
                                         .orElseThrow()
                                         .getAsJsonObject()
                                         .asMap());
            }

            @Override
            public InterlocutorPropertiesLootCondition fromJson(
                final JsonObject json,
                final JsonDeserializationContext context
            ) {
                return CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();
            }
        });

    @Override
    public LootConditionType getType() {
        return TYPE;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Set.of(LootContextParameters.ORIGIN);
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.get(LootContext.EntityTarget.THIS.getParameter());
        Vec3d origin = lootContext.get(LootContextParameters.ORIGIN);
        Optional<Entity> interlocutor = PlayerDialogueTracker.KEY.maybeGet(entity).flatMap(PlayerDialogueTracker::getInterlocutor);
        return interlocutor.isPresent() && this.predicate.test(lootContext.getWorld(), origin, interlocutor.get());
    }
}
