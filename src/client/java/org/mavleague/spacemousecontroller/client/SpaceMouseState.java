package org.mavleague.spacemousecontroller.client;

/**
 * Data class to store the current physical state of the SpaceMouse.
 * It holds the translation axes, rotation axes, and button press states.
 */
public class SpaceMouseState {

    // Translation values for the 3D axes
    public float x;
    public float y;
    public float z;

    // Rotation values (6DOF)
    public float pitch; // Rotation around X-axis
    public float roll;  // Rotation around Y-axis
    public float yaw;   // Rotation around Z-axis

    // States for the two main buttons
    public boolean button1;
    public boolean button2;

    /**
     * Default constructor initializing all values to zero/false.
     */
    public SpaceMouseState() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;

        this.pitch = 0.0f;
        this.roll = 0.0f;
        this.yaw = 0.0f;

        this.button1 = false;
        this.button2 = false;
    }
}