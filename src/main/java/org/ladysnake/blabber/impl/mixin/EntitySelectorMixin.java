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
package org.ladysnake.blabber.impl.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.ladysnake.blabber.impl.common.BlabberEntitySelectorExt;
import org.ladysnake.blabber.impl.common.PlayerDialogueTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin implements BlabberEntitySelectorExt {
    @Unique
    private boolean blabber$interlocutorSelector;

    @Inject(method = "getEntities(Lnet/minecraft/server/command/ServerCommandSource;)Ljava/util/List;", at = @At(value = "FIELD", target = "Lnet/minecraft/command/EntitySelector;senderOnly:Z"), cancellable = true)
    private void replaceSelf(ServerCommandSource source, CallbackInfoReturnable<List<? extends Entity>> cir) throws CommandSyntaxException {
        if (this.blabber$interlocutorSelector) {
            cir.setReturnValue(source.getPlayerOrThrow().getComponent(PlayerDialogueTracker.KEY).getInterlocutor().map(List::of).orElse(List.of()));
        }
    }

    @Override
    public void blabber$setInterlocutorSelector(boolean selectInterlocutor) {
        this.blabber$interlocutorSelector = selectInterlocutor;
    }
}
