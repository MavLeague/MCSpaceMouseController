package org.mavleague.spacemousecontroller.client;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;

public class SpaceMouseHID4JavaImpl {

    // Vendor IDs for legacy (Logitech) and modern (3DConnexion) devices
    private static final int VENDOR_ID_LOGITECH = 0x046d;
    private static final int VENDOR_ID_3DCONNEXION = 0x256f;

    // Comprehensive list of SpaceMouse product IDs
    private static final int[] PRODUCT_IDS = {
            0xc626, // SpaceNavigator
            0xc628, // SpaceMouse Pro
            0xc62b, // SpaceNavigator USB
            0xc62e, // SpaceMouse Wireless (Wired Mode)
            0xc62f, // SpaceMouse Wireless Receiver
            0xc631, // SpaceMouse Pro Wireless (Wired Mode)
            0xc632, // SpaceMouse Pro Wireless (Receiver)
            0xc635, // SpaceMouse Compact
            0xc63a, // SpaceMouse Wireless BT (Bluetooth Edition)
            0xc652  // 3DConnexion Universal Receiver
    };

    private HidDevice device;
    private HidServices hidServices;

    // Persistent state to hold values across different HID reports
    private final SpaceMouseState currentState = new SpaceMouseState();

    public boolean connect() {
        try {
            // Get HID services using the correct HidManager invocation
            hidServices = HidManager.getHidServices();

            // Array of vendor IDs to check
            int[] vendorIds = { VENDOR_ID_3DCONNEXION, VENDOR_ID_LOGITECH };

            // Search for compatible SpaceMouse devices across all Vendor IDs
            for (int vendorId : vendorIds) {
                for (int productId : PRODUCT_IDS) {
                    device = hidServices.getHidDevice(vendorId, productId, null);

                    if (device != null) {
                        // Try to open the device connection
                        if (device.open()) {
                            System.out.println("SpaceMouse connected: " + device.getProduct());
                            return true;
                        }
                    }
                }
            }

            System.err.println("No SpaceMouse device found or could not be opened.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public SpaceMouseState read() {
        if (device == null || !device.isOpen()) {
            return currentState;
        }

        try {
            // SpaceMouse HID Report Size is up to 13 bytes
            byte[] data = new byte[13];
            // device.read() returns the actual number of bytes sent by the device
            int bytesRead = device.read(data, 10);

            if (bytesRead > 0) {
                // Pass both the data array and the actual bytes read to the parser
                updateStateFromHIDData(data, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return currentState;
    }

    private void updateStateFromHIDData(byte[] data, int bytesRead) {
        // Prevent out-of-bounds errors on empty reads
        if (bytesRead < 1) return;

        int reportId = data[0] & 0xFF;

        switch (reportId) {
            case 1: // Translation Report (Older devices) OR Unified 6DOF Report (Newer devices)
                if (bytesRead >= 7) {
                    short rawX = (short) ((data[1] & 0xFF) | ((data[2] & 0xFF) << 8));
                    short rawY = (short) ((data[3] & 0xFF) | ((data[4] & 0xFF) << 8));
                    short rawZ = (short) ((data[5] & 0xFF) | ((data[6] & 0xFF) << 8));

                    currentState.x = rawX / 350.0f;
                    currentState.y = rawY / 350.0f;
                    currentState.z = rawZ / 350.0f;
                }

                // If the device sends a 13-byte report, it attaches the rotation to the same packet
                if (bytesRead >= 13) {
                    short rawPitch = (short) ((data[7] & 0xFF) | ((data[8] & 0xFF) << 8));
                    short rawRoll  = (short) ((data[9] & 0xFF) | ((data[10] & 0xFF) << 8));
                    short rawYaw   = (short) ((data[11] & 0xFF) | ((data[12] & 0xFF) << 8));

                    currentState.pitch = rawPitch / 350.0f;
                    currentState.roll = rawRoll / 350.0f;
                    currentState.yaw = rawYaw / 350.0f;
                }
                break;

            case 2: // Dedicated Rotation Report (Older devices only)
                if (bytesRead >= 7) {
                    short rawPitch = (short) ((data[1] & 0xFF) | ((data[2] & 0xFF) << 8));
                    short rawRoll  = (short) ((data[3] & 0xFF) | ((data[4] & 0xFF) << 8));
                    short rawYaw   = (short) ((data[5] & 0xFF) | ((data[6] & 0xFF) << 8));

                    currentState.pitch = rawPitch / 350.0f;
                    currentState.roll = rawRoll / 350.0f;
                    currentState.yaw = rawYaw / 350.0f;
                }
                break;

            case 3: // Button Report
                if (bytesRead >= 2) {
                    currentState.button1 = (data[1] & 0x01) != 0;
                    currentState.button2 = (data[1] & 0x02) != 0;
                }
                break;

            default:
                // Unknown report ID, safely ignore
                break;
        }
    }
    public void disconnect() {
        if (device != null && device.isOpen()) {
            device.close();
        }
        if (hidServices != null) {
            hidServices.shutdown();
        }
    }
}