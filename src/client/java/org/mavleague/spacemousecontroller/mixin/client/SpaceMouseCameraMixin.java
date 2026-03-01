package org.mavleague.spacemousecontroller.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.mavleague.spacemousecontroller.client.SpaceMouseState;
import org.mavleague.spacemousecontroller.client.SpacemousecontrollerClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects code into the vanilla Minecraft Mouse handler.
 * This runs every single frame, ensuring buttery smooth camera movement.
 */
@Mixin(Mouse.class)
public class SpaceMouseCameraMixin {

    private long lastFrameTime = System.nanoTime();

    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void injectSpaceMouseCamera(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Only apply camera movement if the player is actually in the game and unpaused
        if (client.player == null || client.isPaused()) {
            lastFrameTime = System.nanoTime();
            return;
        }

        // Calculate Delta-Time to ensure movement speed is independent of framerate
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0f;
        lastFrameTime = currentTime;

        // Retrieve the latest threaded hardware state
        SpaceMouseState state = SpacemousecontrollerClient.getCurrentState();

        float lookSensitivity = 250.0f;
        float deadzone = 0.15f;

        // Apply Pitch (Looking up/down)
        if (Math.abs(state.pitch) > deadzone) {
            float currentPitch = client.player.getPitch();
            float pitchChange = state.pitch * lookSensitivity * deltaTime;

            // Clamp pitch to prevent the camera from flipping upside down
            float newPitch = Math.max(-90.0f, Math.min(90.0f, currentPitch + pitchChange));
            client.player.setPitch(newPitch);
        }

        // Apply Yaw (Looking left/right)
        if (Math.abs(state.yaw) > deadzone) {
            float yawChange = state.yaw * lookSensitivity * deltaTime;
            client.player.setYaw(client.player.getYaw() + yawChange);
        }
    }
}