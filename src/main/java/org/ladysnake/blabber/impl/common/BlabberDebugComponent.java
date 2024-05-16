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

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.impl.common.commands.SettingsSubCommand;

public class BlabberDebugComponent implements AutoSyncedComponent {
    public static final ComponentKey<BlabberDebugComponent> KEY = ComponentRegistry.getOrCreate(Blabber.id("debug"), BlabberDebugComponent.class);

    public static BlabberDebugComponent get(PlayerEntity player) {
        return player.getComponent(KEY);
    }

    private boolean debugEnabled;
    private final PlayerEntity player;

    public BlabberDebugComponent(PlayerEntity player) {
        this.player = player;
    }

    public boolean debugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
        KEY.sync(this.player);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.player;
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeBoolean(this.debugEnabled() && SettingsSubCommand.ALLOW_DEBUG.test(recipient.getCommandSource()));
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.debugEnabled = buf.readBoolean();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.debugEnabled = tag.getBoolean("enabled");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("enabled", this.debugEnabled);
    }
}
