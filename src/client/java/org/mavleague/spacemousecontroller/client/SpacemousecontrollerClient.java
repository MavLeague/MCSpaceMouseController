package org.mavleague.spacemousecontroller.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpacemousecontrollerClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("spacemousecontroller");
    private SpaceMouseHID4JavaImpl spaceMouseHandler;

    // Made static so the Mixin can access the state from anywhere
    private static volatile SpaceMouseState currentState = new SpaceMouseState();
    private volatile boolean isRunning = true;

    // Getter method for the Mixin
    public static SpaceMouseState getCurrentState() {
        return currentState;
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing SpaceMouseController...");

        spaceMouseHandler = new SpaceMouseHID4JavaImpl();

        if (spaceMouseHandler.connect()) {
            LOGGER.info("SpaceMouse successfully connected!");
            startPollingThread();
        } else {
            LOGGER.warn("Could not connect to SpaceMouse.");
        }

        // Tick Event: Only handles walking, jumping, and sneaking (20 times per second)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            float deadzone = 0.15f;

            // Simulating WASD keys
            client.options.forwardKey.setPressed(currentState.y < -deadzone);
            client.options.backKey.setPressed(currentState.y > deadzone);

            client.options.leftKey.setPressed(currentState.x < -deadzone);
            client.options.rightKey.setPressed(currentState.x > deadzone);

            // Jumping and Sneaking
            boolean isPullingUp = currentState.z < -deadzone;
            client.options.jumpKey.setPressed(isPullingUp);

            boolean isPushingDown = currentState.z > deadzone;
            client.options.sneakKey.setPressed(isPushingDown);

            // Right and Left Click (Simulating KeyBindings)
            // By passing the boolean directly, it perfectly handles both single clicks and holding the button
            client.options.attackKey.setPressed(currentState.button1);
            client.options.useKey.setPressed(currentState.button2);
        });

        // Disconnect hook
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            LOGGER.info("Shutting down SpaceMouse connection...");
            isRunning = false;
            if (spaceMouseHandler != null) {
                spaceMouseHandler.disconnect();
            }
        });
    }

    private void startPollingThread() {
        Thread pollingThread = new Thread(() -> {
            while (isRunning && spaceMouseHandler != null) {
                SpaceMouseState newState = spaceMouseHandler.read();
                if (newState != null) {
                    currentState = newState;
                }

                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "SpaceMouse-Input-Thread");

        pollingThread.setDaemon(true);
        pollingThread.start();
    }
}