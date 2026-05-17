/*
 * Blabber
 * Copyright (C) 2022-2026 Ladysnake
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
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Set;

public record InterlocutorPropertiesLootCondition(EntityPredicate predicate) implements LootItemCondition {
    public static final MapCodec<InterlocutorPropertiesLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntityPredicate.CODEC.fieldOf("predicate").forGetter(InterlocutorPropertiesLootCondition::predicate)
    ).apply(instance, InterlocutorPropertiesLootCondition::new));

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getOptionalParameter(LootContext.EntityTarget.THIS.contextParam());
        Vec3 origin = lootContext.getOptionalParameter(LootContextParams.ORIGIN);
        Optional<Entity> interlocutor = PlayerDialogueTracker.KEY.maybeGet(entity).flatMap(PlayerDialogueTracker::getInterlocutor);
        return interlocutor.isPresent() && this.predicate.matches(lootContext.getLevel(), origin, interlocutor.get());
    }
}
