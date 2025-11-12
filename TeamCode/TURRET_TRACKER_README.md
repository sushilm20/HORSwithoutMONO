# AprilTag Turret Tracker

## Overview
This implementation provides automatic turret tracking using AprilTag detection. The turret will automatically center itself on AprilTag 22 using webcam1.

## Files Created

### 1. TurretTracker.java
A reusable class that manages turret tracking logic:
- Handles motor control with encoder limits (-400 to 400)
- Calculates position error from camera center
- Implements proportional control for smooth tracking
- Provides safety features and limit checking

### 2. AprilTagTurretTracker.java
The main OpMode for turret tracking:
- Initializes webcam1 for AprilTag detection
- Tracks AprilTag ID 22
- Continuously updates turret position
- Provides comprehensive telemetry

## Hardware Requirements

### Robot Configuration
The following hardware must be configured in the Robot Controller app:

1. **Webcam**: Name must be "webcam1"
   - Type: Webcam
   - Connected via USB

2. **Motor**: Name must be "turret"
   - Type: DC Motor
   - Connected to Control Hub or Expansion Hub
   - Direction: FORWARD (can be adjusted in code if needed)

## Usage

### Running the OpMode

1. **Configure Hardware**:
   - Ensure webcam1 is properly connected and configured
   - Ensure turret motor is properly connected and configured
   - Verify motor encoder cable is connected

2. **Select OpMode**:
   - On Driver Station, select "AprilTag Turret Tracker" from TeleOp list
   - Press START when ready

3. **Operation**:
   - Turret will automatically search for AprilTag 22
   - When tag is detected, turret moves to center it in view
   - Turret stops when tag is centered (within tolerance)
   - Press DPAD_DOWN on gamepad1 to manually stop turret

### Controls
- **DPAD_DOWN**: Manual stop (pauses automatic tracking)

## Technical Details

### Tracking Algorithm
1. AprilTag processor detects tags in camera view
2. System finds AprilTag 22 from detected tags
3. Calculates X-axis error: `error = tag.center.x - camera.center.x`
4. Moves turret left/right to minimize error
5. Stops when error < tolerance (50 pixels)

### Safety Features
- **Encoder Limits**: Turret stops at -400 and 400 positions
- **Brake Mode**: Motor holds position when stopped
- **Limit Checking**: Prevents movement beyond safe range
- **Manual Override**: Can stop tracking with DPAD_DOWN

### Tunable Parameters

In `TurretTracker.java`:
```java
// Adjust these values based on your setup
private static final int TURRET_MIN_LIMIT = -400;      // Minimum encoder position
private static final int TURRET_MAX_LIMIT = 400;       // Maximum encoder position
private static final double CENTER_TOLERANCE = 50.0;    // Pixels from center
private static final double TURRET_SPEED = 0.3;         // Motor power (0.0 - 1.0)
private static final double CAMERA_CENTER_X = 320.0;    // Camera resolution center
```

### Camera Resolution
The code assumes a 640x480 camera resolution (center at X=320). If using different resolution:
1. Determine your camera's width in pixels
2. Update `CAMERA_CENTER_X` to `width / 2`

Example for 1280x720:
```java
private static final double CAMERA_CENTER_X = 640.0;
```

## Telemetry Display

When running, the OpMode displays:
- Number of AprilTags detected
- Current turret encoder position
- Whether turret is within safe limits
- Target tag information (if found):
  - Tag ID
  - X and Y position in pixels
  - Tag name (if in library)
- Tracking status and mode

## Troubleshooting

### Turret Not Moving
1. Check motor is configured as "turret" in hardware config
2. Verify motor encoder cable is connected
3. Check that AprilTag 22 is visible to camera
4. Ensure turret hasn't reached encoder limits

### Tag Not Detected
1. Verify webcam1 is properly connected
2. Check lighting conditions (AprilTags need good lighting)
3. Ensure tag is within camera view
4. Verify using correct AprilTag ID (22)
5. Check tag is from correct library (DECODE season)

### Turret Moves in Wrong Direction
- If turret moves opposite to expected direction, change motor direction in code:
  ```java
  turretMotor.setDirection(DcMotor.Direction.REVERSE);
  ```

### Turret Oscillates
- If turret moves back and forth constantly:
  1. Increase `CENTER_TOLERANCE` in TurretTracker.java
  2. Decrease `TURRET_SPEED` for smoother movement

### Limits Too Restrictive/Permissive
- Adjust `TURRET_MIN_LIMIT` and `TURRET_MAX_LIMIT` based on your physical setup
- Test limits carefully to avoid mechanical damage

## Advanced Usage

### Tracking Different AprilTag
To track a different AprilTag ID:
```java
private static final int TARGET_TAG_ID = 22;  // Change to desired ID
```

### Using Different Camera
To use a different webcam:
```java
// In initializeAprilTag() method
hardwareMap.get(WebcamName.class, "webcam1")  // Change "webcam1" to your camera name
```

### Adjusting Tracking Speed
For faster/slower tracking, modify in TurretTracker.java:
```java
private static final double TURRET_SPEED = 0.3;  // Increase for faster, decrease for slower
```

### Integration with Existing Code
To integrate TurretTracker into an existing OpMode:

```java
// 1. Declare variables
private TurretTracker turretTracker;
private AprilTagProcessor aprilTag;
private VisionPortal visionPortal;

// 2. Initialize in runOpMode()
aprilTag = AprilTagProcessor.easyCreateWithDefaults();
visionPortal = VisionPortal.easyCreateWithDefaults(
    hardwareMap.get(WebcamName.class, "webcam1"), aprilTag);
turretTracker = new TurretTracker(
    hardwareMap.get(DcMotor.class, "turret"), 22);

// 3. In main loop
AprilTagDetection detection = findTargetTag();  // Implement this method
turretTracker.updateTurret(detection);
```

## Performance Notes
- Update rate: ~50 Hz (20ms sleep between updates)
- Detection latency: Typically 30-100ms depending on lighting
- Movement smoothness depends on motor controller and gearing

## Future Enhancements
Possible improvements for this implementation:
1. Add PID control for smoother tracking
2. Implement predictive tracking for moving targets
3. Add auto-calibration for camera center
4. Support multiple simultaneous tag tracking
5. Add distance-based speed adjustment
6. Implement field-centric positioning using tag location data
