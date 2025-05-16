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
package org.ladysnake.blabber.impl.client.illustrations;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.storage.NbtReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.impl.common.illustrations.entity.DialogueIllustrationNbtEntity;

public class NbtEntityIllustrationRenderer extends EntityIllustrationRenderer<DialogueIllustrationNbtEntity> {
    public NbtEntityIllustrationRenderer(DialogueIllustrationNbtEntity illustration) {
        super(illustration);
    }

    @Override
    protected @Nullable LivingEntity getRenderedEntity(World world) {
        EntityType<?> entityType = Registries.ENTITY_TYPE.getOptionalValue(illustration.id()).orElse(null);
        if (entityType == null) return null;

        if (entityType.create(world, SpawnReason.COMMAND) instanceof LivingEntity living) {
            this.illustration.data().ifPresent(nbt -> loadEntityData(world, living, nbt));
            living.lastBodyYaw = living.bodyYaw = 0.0f;
            living.lastHeadYaw = living.headYaw = 0.0f;
            return living;
        }

        return null;
    }

    public static void loadEntityData(World world, LivingEntity entity, NbtCompound nbt) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(entity.getErrorReporterContext(), Blabber.LOGGER)) {
            entity.readData(NbtReadView.create(logging, world.getRegistryManager(), nbt));
        }
    }
}
