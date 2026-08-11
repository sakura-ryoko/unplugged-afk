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

import com.sakuraryoko.unplugged_afk.api.state.PosState;

/**
 * Gson TypeAdapter for {@link PosState}.
 *
 * <p>Java records have {@code final} fields, which Gson versions before 2.10
 * (e.g. 2.8.9 bundled with Minecraft 1.19.2) cannot deserialize reflectively.
 * This adapter lets the config load on every supported Gson version (2.8.9+).
 */
@ApiStatus.Internal
public class PosStateAdapter extends TypeAdapter<PosState>
{
	@Override
	public void write(JsonWriter out, PosState value) throws IOException
	{
		if (value == null)
		{
			out.nullValue();
			return;
		}

		out.beginObject();
		out.name("location").value(value.location());
		out.name("x").value(value.x());
		out.name("y").value(value.y());
		out.name("z").value(value.z());
		out.name("yaw").value(value.yaw());
		out.name("pitch").value(value.pitch());
		out.endObject();
	}

	@Override
	public PosState read(JsonReader in) throws IOException
	{
		if (in.peek() == JsonToken.NULL)
		{
			in.nextNull();
			return null;
		}

		String location = "minecraft:overworld";
		int x = 0;
		int y = 0;
		int z = 0;
		float yaw = 0.0f;
		float pitch = 0.0f;

		in.beginObject();
		while (in.hasNext())
		{
			String name = in.nextName();

			switch (name)
			{
				case "location" -> location = in.nextString();
				case "x" -> x = in.nextInt();
				case "y" -> y = in.nextInt();
				case "z" -> z = in.nextInt();
				case "yaw" -> yaw = (float) in.nextDouble();
				case "pitch" -> pitch = (float) in.nextDouble();
				default -> in.skipValue();
			}
		}
		in.endObject();

		return new PosState(location, x, y, z, yaw, pitch);
	}
}
