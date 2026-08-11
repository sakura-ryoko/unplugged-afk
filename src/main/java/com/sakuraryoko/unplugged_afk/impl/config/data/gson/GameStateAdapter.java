/*
 * This file is part of the Unplugged-AFK project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Sakura-Ryoko and contributors
 *
 * Unplugged-AFK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Unplugged-AFK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Unplugged-AFK.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sakuraryoko.unplugged_afk.impl.config.data.gson;

import java.io.IOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.unplugged_afk.api.state.GameState;

/**
 * Gson TypeAdapter for {@link GameState}.
 *
 * <p>Java records have {@code final} fields, which Gson versions before 2.10
 * (e.g. 2.8.9 bundled with Minecraft 1.19.2) cannot deserialize reflectively.
 * This adapter lets the config load on every supported Gson version (2.8.9+).
 */
@ApiStatus.Internal
public class GameStateAdapter extends TypeAdapter<GameState>
{
	@Override
	public void write(JsonWriter out, GameState value) throws IOException
	{
		if (value == null)
		{
			out.nullValue();
			return;
		}

		out.beginObject();
		out.name("gameMode").value(value.gameMode());
		out.name("flying").value(value.flying());
		out.endObject();
	}

	@Override
	public GameState read(JsonReader in) throws IOException
	{
		if (in.peek() == JsonToken.NULL)
		{
			in.nextNull();
			return null;
		}

		String gameMode = "survival";
		boolean flying = false;

		in.beginObject();
		while (in.hasNext())
		{
			String name = in.nextName();

			switch (name)
			{
				case "gameMode" -> gameMode = in.nextString();
				case "flying" -> flying = in.nextBoolean();
				default -> in.skipValue();
			}
		}
		in.endObject();

		return new GameState(gameMode, flying);
	}
}
