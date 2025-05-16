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
package org.ladysnake.blabber.impl.common.settings;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.impl.common.commands.SettingsSubCommand;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.EnumSet;

public class BlabberSettingsComponent implements AutoSyncedComponent {
    public static final ComponentKey<BlabberSettingsComponent> KEY = ComponentRegistry.getOrCreate(Blabber.id("settings"), BlabberSettingsComponent.class);

    public static BlabberSettingsComponent get(PlayerEntity player) {
        return player.getComponent(KEY);
    }

    private EnumSet<BlabberSetting> enabledSettings = EnumSet.noneOf(BlabberSetting.class);
    private final PlayerEntity player;

    public BlabberSettingsComponent(PlayerEntity player) {
        this.player = player;
    }

    public boolean isDebugEnabled() {
        if (this.player instanceof ServerPlayerEntity serverPlayer && !SettingsSubCommand.ALLOW_DEBUG.test(serverPlayer.getCommandSource())) {
            return false;
        }
        return !this.enabledSettings.isEmpty();
    }

    public boolean isEnabled(BlabberSetting feature) {
        return enabledSettings.contains(feature);
    }

    public void setEnabled(BlabberSetting feature, boolean debugEnabled) {
        if (debugEnabled) {
            this.enabledSettings.add(feature);
        } else {
            this.enabledSettings.remove(feature);
        }
        KEY.sync(this.player);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.player;
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        boolean enabled = this.isDebugEnabled();
        buf.writeBoolean(enabled);
        if (enabled) {
            buf.writeEnumSet(this.enabledSettings, BlabberSetting.class);
        }
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        boolean debugEnabled = buf.readBoolean();
        if (debugEnabled) {
            this.enabledSettings = buf.readEnumSet(BlabberSetting.class);
        } else {
            this.enabledSettings = EnumSet.noneOf(BlabberSetting.class);
        }
    }

    @Override
    public void readData(ReadView readView) {
        this.enabledSettings = EnumSet.noneOf(BlabberSetting.class);
        for (BlabberSetting feature : readView.getTypedListView("enabled_features", BlabberSetting.CODEC)) {
            this.enabledSettings.add(feature);
        }
    }

    @Override
    public void writeData(WriteView writeView) {
        WriteView.ListAppender<BlabberSetting> listAppender = writeView.getListAppender("enabled_features", BlabberSetting.CODEC);
        for (BlabberSetting feature : this.enabledSettings) {
            listAppender.add(feature);
        }
    }
}
