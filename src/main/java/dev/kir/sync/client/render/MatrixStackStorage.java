package dev.kir.sync.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.util.math.MatrixStack;

public final class MatrixStackStorage {
    private static MatrixStack modelMatrixStack;

    public static void saveModelMatrixStack(MatrixStack matrixStack) {
        modelMatrixStack = matrixStack;
    }

    public static MatrixStack getModelMatrixStack() {
        return modelMatrixStack;
    }

    public void onStart(WorldRenderContext context) {
        ;
    }
}