# TurretTracker Integration with complexityHORS

## Overview
This document describes the integration of the TurretTracker AprilTag tracking system into the complexityHORS TeleOp mode.

## Changes Made

### 1. Removed Manual Turret Controls
The following manual controls were removed from the turret control section:
- **gamepad1.left_bumper**: Previously moved turret left
- **gamepad1.right_bumper**: Previously moved turret right  
- **gamepad2.left_stick_x**: Previously controlled turret rotation

### 2. Added AprilTag Vision Components
Added the following components to complexityHORS:
```java
private AprilTagProcessor aprilTag;
private VisionPortal visionPortal;
private TurretTracker turretTracker;
private static final int TARGET_APRILTAG_ID = 22;
```

### 3. Tracking Toggle Control
Added gamepad2 touchpad toggle for enabling/disabling turret tracking:
- **Default State**: Tracking is disabled when the OpMode starts
- **Toggle Method**: Press gamepad2 touchpad to toggle tracking on/off
- **Fallback**: For non-PS5 controllers, simultaneous press of both stick buttons acts as touchpad
- **State Variable**: `turretTrackingEnabled` tracks the current state

### 4. State Machine Implementation
The turret now operates as a state machine:
- **When Tracking Enabled**: TurretTracker automatically searches for and tracks AprilTag 22
- **When Tracking Disabled**: Turret motor is stopped (calls `turretTracker.stop()`)

### 5. Initialization
During OpMode initialization:
```java
// Initialize AprilTag detection and TurretTracker
initializeAprilTag();
turretTracker = new TurretTracker(turret, TARGET_APRILTAG_ID);
```

### 6. Main Loop Integration
In the main loop:
```java
String turretStatus;
if (turretTrackingEnabled) {
    // Tracking is enabled - use TurretTracker state machine
    AprilTagDetection targetDetection = findTargetAprilTag();
    turretStatus = turretTracker.updateTurret(targetDetection);
} else {
    // Tracking is disabled - stop turret
    turretTracker.stop();
    turretStatus = "Tracking Disabled";
}
```

### 7. Enhanced Telemetry
Added telemetry for tracking status:
```java
telemetry.addData("Turret Tracking", turretTrackingEnabled ? "ENABLED" : "DISABLED");
telemetry.addData("Turret Status", turretStatus);
```

### 8. Cleanup
Added proper cleanup for vision portal on OpMode exit:
```java
// Clean up vision portal on exit
if (visionPortal != null) {
    visionPortal.close();
}
```

## Hardware Requirements

### Required Hardware Configuration
1. **webcam1**: USB webcam configured in Robot Controller
2. **turret**: DC Motor with encoder configured in Robot Controller

## Usage Instructions

### Controls
- **Gamepad2 Touchpad**: Toggle turret tracking on/off (press to toggle)
  - Alternative: Press both stick buttons simultaneously for non-PS5 controllers

### Operation
1. Start the "ComplexityHORS" OpMode
2. Position the robot so webcam1 can see AprilTag 22
3. Press gamepad2 touchpad to enable tracking
4. The turret will automatically center on AprilTag 22
5. Press gamepad2 touchpad again to disable tracking

### Telemetry Display
The Driver Station will show:
- **Turret Tracking**: ENABLED or DISABLED
- **Turret Status**: Current tracking status from TurretTracker
  - "Tracking Disabled" when tracking is off
  - "Searching for AprilTag 22" when no tag is detected
  - "Centered on AprilTag 22" when tag is centered
  - Tracking error and power information when actively tracking

## Tuning Parameters
All tuning parameters remain in the TurretTracker class:
- `TURRET_MIN_LIMIT`: Minimum encoder position (-400)
- `TURRET_MAX_LIMIT`: Maximum encoder position (400)
- `CENTER_TOLERANCE`: Centering tolerance in pixels (50.0)
- `TURRET_SPEED`: Motor power for tracking (0.3)
- `CAMERA_CENTER_X`: Camera center X coordinate (320.0)

To adjust tracking behavior, modify the constants in `TurretTracker.java`.

## Benefits of This Integration
1. **Automatic Tracking**: Turret automatically tracks AprilTag without manual control
2. **Toggle Control**: Easy on/off control via gamepad2 touchpad
3. **Maintained Separation**: Tuning parameters stay in separate TurretTracker class
4. **State Machine**: Clean state-based implementation
5. **Telemetry**: Clear feedback on tracking status
6. **Resource Management**: Proper cleanup of vision resources

## Testing Notes
- Ensure webcam1 is properly connected before starting
- Test in various lighting conditions for reliable AprilTag detection
- Verify turret encoder limits are appropriate for your mechanical setup
- Test the touchpad toggle functionality with your controller
