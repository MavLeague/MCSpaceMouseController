package org.mavleague.spacemousecontroller.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;
import org.mavleague.spacemousecontroller.config.SpaceMouseConfig;

import java.util.Arrays;

//import java.util.ArrayList;

/**
 * Integrates the configuration screen directly into Mod Menu.
 */
public class SpacemousecontrollerModMenu implements ModMenuApi {

    // Temporary variable to hold the toggle state for the reconnect action
    private boolean triggerReconnect = false;

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parentScreen)
                    .setTitle(Text.literal("SpaceMouse Controller Settings"));

            ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // Sensitivity Slider
            int lookSensitivityInt = (int) SpaceMouseConfig.lookSensitivity;
            general.addEntry(entryBuilder.startIntSlider(Text.literal("Look Sensitivity"), lookSensitivityInt, 1, 500)
                    .setDefaultValue(150)
                    .setSaveConsumer(newValue -> SpaceMouseConfig.lookSensitivity = newValue)
                    .build());

            // Pitch Inversion Toggle
            general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Invert Pitch (Up/Down)"), SpaceMouseConfig.invertPitch)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> SpaceMouseConfig.invertPitch = newValue)
                    .build());

            // Yaw Inversion Toggle
            general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Invert Yaw (Left/Right)"), SpaceMouseConfig.invertYaw)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> SpaceMouseConfig.invertYaw = newValue)
                    .build());

            // Button Action 1
            general.addEntry(entryBuilder.startEnumSelector(Text.literal("Button 1 Action:"), SpaceMouseButtonAction.class, SpaceMouseConfig.button1Action)
                    .setDefaultValue(SpaceMouseConfig.button1Action)
                    .setSaveConsumer(newValue -> SpaceMouseConfig.button1Action = newValue)
                    .setTooltip(Text.literal(Arrays.toString(SpaceMouseButtonAction.values())))
                    .build());

            // Button Action 2
            general.addEntry(entryBuilder.startEnumSelector(Text.literal("Button 2 Action:"), SpaceMouseButtonAction.class, SpaceMouseConfig.button2Action)
                    .setDefaultValue(SpaceMouseConfig.button2Action)
                    .setSaveConsumer(newValue -> SpaceMouseConfig.button2Action = newValue)
                    .setTooltip(Text.literal(Arrays.toString(SpaceMouseButtonAction.values())))
                    .build());

            // Display the currently connected device using a text description (non-editable)
            String deviceStatus = "Status: " + SpacemousecontrollerClient.getConnectedDeviceName();
            general.addEntry(entryBuilder.startTextDescription(Text.literal(deviceStatus))
                    .build());

            // Workaround for a "Button": A toggle that executes an action when you click "Save & Quit"
            general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Force Reconnect on Save"), triggerReconnect)
                    .setDefaultValue(false)
                    .setTooltip(Text.literal("Set this to YES and click Save & Quit to restart the USB connection."))
                    .setSaveConsumer(newValue -> triggerReconnect = newValue)
                    .build());

            // Save the data and optionally trigger the reconnect
            builder.setSavingRunnable(() -> {
                SpaceMouseConfig.save();

                // If the user toggled the switch to true, trigger the reconnection logic in the main client
                if (triggerReconnect) {
                    SpacemousecontrollerClient.reconnectSpaceMouse();
                    triggerReconnect = false; // Reset it for the next time the menu opens
                }
            });

            return builder.build();
        };
    }
}