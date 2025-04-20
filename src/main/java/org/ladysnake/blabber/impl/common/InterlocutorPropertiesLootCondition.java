/*
 * Blabber
 * Copyright (C) 2022-2025 Ladysnake
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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.Set;

public record InterlocutorPropertiesLootCondition(EntityPredicate predicate) implements LootCondition {
    public static final MapCodec<InterlocutorPropertiesLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntityPredicate.CODEC.fieldOf("predicate").forGetter(InterlocutorPropertiesLootCondition::predicate)
    ).apply(instance, InterlocutorPropertiesLootCondition::new));
    public static final LootConditionType TYPE = new LootConditionType(CODEC);

    @Override
    public LootConditionType getType() {
        return TYPE;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
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
