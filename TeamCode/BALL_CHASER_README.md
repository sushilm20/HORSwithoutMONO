# Ball Chaser OpMode Documentation

## Overview
The `BallChaser` class is an autonomous OpMode that automatically detects and chases green or purple balls using webcam vision processing. It implements the same features and structure as the ArtifactFetcher example from the problem statement but adapted to work with the standard FTC SDK available in this repository.

## Features

### Ball Detection
- **Dual Color Detection**: Simultaneously detects both ARTIFACT_PURPLE and ARTIFACT_GREEN colored balls
- **Webcam Integration**: Uses "Webcam 1" for vision processing
- **Advanced Filtering**: 
  - Filters by contour area (100-20,000 pixels) to remove noise
  - Filters by circularity (0.5-1.0) to ensure round objects
  - Sorts by size to prioritize the largest ball

### Robot Control
- **Mecanum Drive**: Full 4-wheel mecanum drive support with strafing capability
- **PID-like Control**: Proportional control for smooth movement
  - Rotation control (Pr = 0.003) - Centers the ball horizontally
  - Forward/Backward control (Py = 0.02) - Approaches to optimal distance
  - Strafing control (Px = 0.002) - Fine-tunes horizontal positioning
- **Search Behavior**: When no balls are detected, drives forward slowly to search

### Telemetry
Real-time feedback includes:
- List of all detected blobs with circularity, radius, and center position
- Target status (found/searching)
- Target position and size
- Individual motor powers for debugging

## Hardware Requirements

### Motors
The OpMode expects the following motor names in the hardware configuration:
- `frontLeft` - Front left drive motor
- `backLeft` - Back left drive motor
- `frontRight` - Front right drive motor
- `backRight` - Back right drive motor

### Camera
- `Webcam 1` - USB webcam connected to the Control Hub or Robot Controller

## Configuration Parameters

You can tune the following parameters to adjust robot behavior:

```java
public double Pr = 0.003;   // Rotation gain - increase for faster turning
public double Py = 0.02;    // Forward gain - increase to approach faster
public double Px = 0.002;   // Strafe gain - increase for more aggressive centering

public double camCenter = 160;      // Camera center (should be width/2)
public double blobRadGoal = 50;     // Target distance (larger = closer)
public double INTAKE_ACTIVATION_RADIUS = 45;  // Reserved for future intake integration
```

## How It Works

1. **Initialization**: Sets up drive motors and vision processors for both purple and green balls
2. **Detection Loop**:
   - Captures camera frames
   - Processes both purple and green color ranges
   - Filters and sorts detected blobs
3. **Control Logic**:
   - If balls detected: Tracks the largest ball, adjusting rotation to center it and driving forward to reach target distance
   - If no balls: Drives forward slowly to search
4. **Drive Control**: Calculates mecanum drive motor powers and applies them with proper normalization

## Usage

1. Connect your webcam to the Control Hub and name it "Webcam 1" in the configuration
2. Ensure your drive motors are configured with the correct names
3. Select "Ball Chaser" from the TeleOp menu on the Driver Station
4. Press INIT to initialize the vision system
5. Press PLAY to start autonomous ball chasing
6. The robot will automatically detect and chase the nearest (largest) ball
7. Press STOP to end the OpMode

## Troubleshooting

### Robot doesn't move
- Check that all four motors are properly configured and connected
- Verify motor directions match your robot's configuration
- Check telemetry for motor power values

### Balls not detected
- Ensure webcam is properly connected and named "Webcam 1"
- Check lighting conditions - bright, even lighting works best
- Verify ball colors match ARTIFACT_PURPLE or ARTIFACT_GREEN ranges
- Review telemetry to see if any blobs are being detected

### Robot moves erratically
- Reduce PID gains (Pr, Py, Px) for smoother control
- Check that motor directions are correct for your robot
- Ensure the camera is securely mounted and not vibrating

### Performance issues
- The camera runs at 320x240 resolution for fast processing
- Loop time is limited to 10ms minimum to prevent CPU overload
- Telemetry updates every 100ms for efficiency

## Integration with Intake System

The code includes a placeholder constant `INTAKE_ACTIVATION_RADIUS` for future integration with an intake mechanism. To add intake control:

1. Add intake motor/servo to hardware initialization
2. Check blob radius in the control loop
3. Activate intake when `targetCircle.getRadius() > INTAKE_ACTIVATION_RADIUS`

Example:
```java
if (targetCircle.getRadius() > INTAKE_ACTIVATION_RADIUS) {
    // Activate intake
    intakeMotor.setPower(0.7);
} else {
    // Stop intake
    intakeMotor.setPower(0.0);
}
```

## Technical Notes

- Uses FTC SDK's `VisionPortal` and `ColorBlobLocatorProcessor` for vision processing
- Implements standard mecanum drive kinematics
- Motor powers are normalized to prevent exceeding [-1, 1] range
- All blob operations use FTC SDK utilities for filtering and sorting
- Compatible with FTC SDK version 9.0+

## Credits

Based on the ArtifactFetcher concept with adaptations for the standard FTC SDK and this robot's hardware configuration.
