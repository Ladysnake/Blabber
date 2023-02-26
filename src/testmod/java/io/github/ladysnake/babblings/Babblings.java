/*
 * Blabber
 * Copyright (C) 2022-2023 Ladysnake
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
package io.github.ladysnake.babblings;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.ladysnake.blabber.impl.common.BlabberRegistrar;
import io.github.ladysnake.blabber.impl.common.DialogueTemplate;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.util.Objects;

public final class Babblings implements ModInitializer {
    @Override
    public void onInitialize() {
        DynamicRegistrySetupCallback.EVENT.register(registryView -> registryView.getOptional(BlabberRegistrar.DIALOGUE_REGISTRY_KEY).ifPresent(dialogueRegistry -> {
            Gson gson = new Gson();
            JsonElement remnantChoice = gson.fromJson(new InputStreamReader(Objects.requireNonNull(Babblings.class.getResourceAsStream("/data/babblings/blabber_dialogues/remnant_choice.json"))), JsonObject.class);
            Registry.register(dialogueRegistry, new Identifier("babblings:remnant_choice_builtin"), DialogueTemplate.CODEC.parse(JsonOps.INSTANCE, remnantChoice).result().orElseThrow());
        }));
    }
}
