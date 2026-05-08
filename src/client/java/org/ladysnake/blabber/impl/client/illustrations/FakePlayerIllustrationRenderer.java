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
package org.ladysnake.blabber.impl.client.illustrations;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.impl.common.illustrations.entity.DialogueIllustrationFakePlayer;
import org.ladysnake.blabber.impl.mixin.PlayerEntityAccessor;
import org.ladysnake.blabber.impl.mixin.client.AbstractClientPlayerAccessor;

public class FakePlayerIllustrationRenderer extends EntityIllustrationRenderer<DialogueIllustrationFakePlayer> {
    public FakePlayerIllustrationRenderer(DialogueIllustrationFakePlayer illustration) {
        super(illustration);
    }

    @SuppressWarnings("UnreachableCode")
    @Environment(EnvType.CLIENT)
    @Override
    protected @Nullable LivingEntity getRenderedEntity(Level world) {
        GameProfile profile = this.illustration.profile();
        RemotePlayer fakePlayer = new RemotePlayer((ClientLevel) world, profile);
        this.illustration.data().ifPresent(nbt -> NbtEntityIllustrationRenderer.loadEntityData(world, fakePlayer, nbt));
        ((AbstractClientPlayerAccessor) fakePlayer).setPlayerInfo(new PlayerInfo(profile, false));
        fakePlayer.yBodyRotO = fakePlayer.yBodyRot = 0.0f;
        fakePlayer.yHeadRotO = fakePlayer.yHeadRot = 0.0f;
        DialogueIllustrationFakePlayer.PlayerModelOptions playerModelOptions = this.illustration.modelOptionsOrDefault();
        fakePlayer.getEntityData().set(PlayerEntityAccessor.getPlayerModelParts(), playerModelOptions.packVisibleParts());
        fakePlayer.setMainArm(playerModelOptions.mainHand());
        return fakePlayer;
    }
}
