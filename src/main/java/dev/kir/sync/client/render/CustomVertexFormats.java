package dev.kir.sync.client.render;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;

@Environment(EnvType.CLIENT)
public final class CustomVertexFormats extends VertexFormats {
    // TODO: Padding went poof :despair:
    public static final VertexFormat POSITION_COLOR_OVERLAY_LIGHT_NORMAL = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("Color", VertexFormatElement.COLOR).add("UV1", VertexFormatElement.UV_1).add("UV2", VertexFormatElement.UV_2).add("Normal", VertexFormatElement.NORMAL).build();
}