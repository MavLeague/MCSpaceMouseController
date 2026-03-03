package org.mavleague.spacemousecontroller.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.mavleague.spacemousecontroller.config.SpaceMouseConfig;

public class SpacemousecontrollerClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("spacemousecontroller");

    // Made static so we only ever have ONE central connection to the hardware
    private static SpaceMouseHID4JavaImpl spaceMouseHandler;
    private static volatile SpaceMouseState currentState = new SpaceMouseState();
    private static volatile boolean isRunning = true;

    // Stores the current connection status to display it in the menu
    private static String connectedDeviceName = "Not Connected";

    public static SpaceMouseState getCurrentState() {
        return currentState;
    }

    public static String getConnectedDeviceName() {
        return connectedDeviceName;
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing SpaceMouseController...");

        // Initial connection attempt on startup
        reconnectSpaceMouse();
        SpaceMouseConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            float deadzone = 0.15f;

            client.options.forwardKey.setPressed(currentState.y < -deadzone);
            client.options.backKey.setPressed(currentState.y > deadzone);

            client.options.leftKey.setPressed(currentState.x < -deadzone);
            client.options.rightKey.setPressed(currentState.x > deadzone);

            boolean isPullingUp = currentState.z < -deadzone;
            client.options.jumpKey.setPressed(isPullingUp);

            boolean isPushingDown = currentState.z > deadzone;
            client.options.sneakKey.setPressed(isPushingDown);

            handleButtonAction(client, SpaceMouseConfig.button1Action, currentState.button1);
            handleButtonAction(client, SpaceMouseConfig.button2Action, currentState.button2);

        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            LOGGER.info("Shutting down SpaceMouse connection...");
            isRunning = false;
            if (spaceMouseHandler != null) {
                spaceMouseHandler.disconnect();
            }
        });
    }

    private void handleButtonAction(MinecraftClient client, SpaceMouseButtonAction action, boolean isPressed) {
        if (action == SpaceMouseButtonAction.NONE) return;

        switch (action) {
            case JUMP   -> client.options.jumpKey.setPressed(isPressed);
            case SNEAK  -> client.options.sneakKey.setPressed(isPressed);
            case ATTACK -> client.options.attackKey.setPressed(isPressed);
            case USE    -> client.options.useKey.setPressed(isPressed);
            case SPRINT ->  client.options.sprintKey.setPressed(isPressed);
        }
    }

    /**
     * Safely disconnects the old device and attempts to reconnect.
     * This avoids running multiple polling threads simultaneously.
     */
    public static void reconnectSpaceMouse() {
        LOGGER.info("Attempting to connect/reconnect SpaceMouse...");

        // Temporarily stop the old thread and disconnect
        isRunning = false;
        if (spaceMouseHandler != null) {
            spaceMouseHandler.disconnect();
        }

        // Create a fresh handler
        spaceMouseHandler = new SpaceMouseHID4JavaImpl();

        if (spaceMouseHandler.connect()) {
            LOGGER.info("SpaceMouse successfully connected!");
            connectedDeviceName = "Connected";

            // Restart the polling thread
            isRunning = true;
            startPollingThread();
        } else {
            LOGGER.warn("Could not connect to SpaceMouse.");
            connectedDeviceName = "Not Connected";
        }
    }

    private static void startPollingThread() {
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