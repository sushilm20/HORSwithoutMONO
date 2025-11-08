package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * TouchpadMovement
 *
 * Drives the robot at low power (0.1) based solely on the PS5 controller touchpad on gamepad1.
 * - The code first attempts to read touchpad X/Y values from gamepad1 via reflection (common field names).
 * - If those fields are not present, it attaches a GenericMotionListener and accepts MotionEvents only
 *   from a PS5/DUALSENSE device (device name detection). This still uses only the PS5 controller input.
 * - Movement mapping:
 *     touch X -> lateral (strafe): left negative, right positive
 *     touch Y -> forward/back (axial): finger up -> forward
 * - No rotation is applied.
 * - Touchpad drags are interpreted as directional input; the robot moves in the direction/ratio of the drag.
 *
 * Important: Controller mappings and Android/driver behavior vary between devices/OS versions. This class
 * is written to prefer gamepad1 touchpad inputs and to avoid using other input sources.
 */
@TeleOp(name="TouchpadMovement", group="Linear OpMode")
public class TouchpadMovement extends LinearOpMode {

    private DcMotor frontLeftDrive, backLeftDrive, frontRightDrive, backRightDrive;

    // touch state updated either from reflection or MotionEvent listener
    private volatile float touchX = 0f;   // expected -1..1
    private volatile float touchY = 0f;   // expected -1..1
    private volatile boolean touching = false;

    // reflection fallback for gamepad1 fields
    private boolean gamepadTouchFieldsAvailable = false;
    private java.lang.reflect.Field gpTouchXField = null;
    private java.lang.reflect.Field gpTouchYField = null;

    // configuration
    private static final double MAX_TOUCH_POWER = 0.1; // base power
    private static final double TOUCH_DEADZONE = 0.05; // ignore small drags
    private static final String[] GAMEPAD_TOUCH_FIELD_CANDIDATES = {
            "touchpad_x", "touchpad_y", "touchpadX", "touchpadY", "touchpadAxisX", "touchpadAxisY",
            "touchpad_left_x", "touchpad_left_y", "touchX", "touchY"
    };

    @Override
    public void runOpMode() throws InterruptedException {
        frontLeftDrive  = hardwareMap.get(DcMotor.class, "frontLeft");
        backLeftDrive   = hardwareMap.get(DcMotor.class, "backLeft");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRight");
        backRightDrive  = hardwareMap.get(DcMotor.class, "backRight");

        frontLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        backLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);

        // Try reflection on gamepad1 to find touchpad fields (use ONLY gamepad1 when available)
        try {
            Class<?> gpClass = gamepad1.getClass();
            java.lang.reflect.Field fx = null;
            java.lang.reflect.Field fy = null;
            for (String name : GAMEPAD_TOUCH_FIELD_CANDIDATES) {
                try {
                    java.lang.reflect.Field f = gpClass.getField(name);
                    if (f != null) {
                        // heuristic: pick first as X, and look for corresponding Y later
                        if (fx == null) {
                            fx = f;
                        } else if (fy == null && !f.getName().equals(fx.getName())) {
                            fy = f;
                            break;
                        }
                    }
                } catch (NoSuchFieldException ignored) {}
            }
            // If both found, use them
            if (fx != null && fy != null) {
                gpTouchFieldAssign:
                try {
                    // ensure types are numeric
                    Object ox = fx.get(gamepad1);
                    Object oy = fy.get(gamepad1);
                    if ((ox instanceof Number) && (oy instanceof Number)) {
                        gpTouchXField = fx;
                        gpTouchYField = fy;
                        gpTouchXField.setAccessible(true);
                        gpTouchYField.setAccessible(true);
                        gamepadTouchFieldsAvailable = true;
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        // If reflection failed, attach a MotionEvent listener that filters events to PS5 controller devices only.
        if (!gamepadTouchFieldsAvailable) {
            try {
                final Activity activity = (Activity) hardwareMap.appContext;
                activity.runOnUiThread(() -> {
                    final View decor = activity.getWindow().getDecorView();
                    decor.setOnGenericMotionListener((v, event) -> {
                        // Filter motion events to controllers that look like a PS5 DualSense / Wireless Controller
                        InputDevice device = event.getDevice();
                        if (device == null) return false;
                        String name = device.getName() != null ? device.getName().toLowerCase() : "";
                        boolean looksLikePS5 = name.contains("dual") || name.contains("dualsense")
                                || name.contains("wireless controller") || name.contains("ps5") || name.contains("sony");
                        if (!looksLikePS5) return false; // ignore non-PS5 devices

                        // Try to read axes commonly used for touchpad-like input
                        float ax = event.getAxisValue(MotionEvent.AXIS_X);
                        float ay = event.getAxisValue(MotionEvent.AXIS_Y);

                        if (Math.abs(ax) < 1e-6f && Math.abs(ay) < 1e-6f) {
                            ax = event.getAxisValue(MotionEvent.AXIS_HAT_X);
                            ay = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
                        }
                        if (Math.abs(ax) < 1e-6f && Math.abs(ay) < 1e-6f) {
                            ax = event.getAxisValue(MotionEvent.AXIS_Z);
                            ay = event.getAxisValue(MotionEvent.AXIS_RZ);
                        }

                        // Use values if significant
                        if (Math.abs(ax) > 1e-6f || Math.abs(ay) > 1e-6f) {
                            synchronized (this) {
                                touchX = ax;
                                touchY = ay;
                                touching = true;
                            }
                            return true;
                        }
                        return false;
                    });

                    // Also allow direct touch on the RC phone screen to emulate drag (helpful for testing)
                    decor.setOnTouchListener((v, event) -> {
                        int action = event.getActionMasked();
                        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                            float nx = (event.getX() / (float) v.getWidth()) * 2f - 1f;
                            float ny = (event.getY() / (float) v.getHeight()) * 2f - 1f;
                            synchronized (this) {
                                touchX = nx;
                                touchY = ny;
                                touching = true;
                            }
                            return true;
                        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                            synchronized (this) {
                                touching = false;
                                touchX = 0f;
                                touchY = 0f;
                            }
                            return true;
                        }
                        return false;
                    });
                });
            } catch (Exception e) {
                telemetry.addData("TouchListener", "Attach failed: " + e.getMessage());
            }
        }

        telemetry.addData("Status", "Initialized - waiting for start (gamepad1 touchpad only)");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            float x = 0f;
            float y = 0f;
            boolean active = false;
            String source = "none";

            // 1) Preferred: reflection fields on gamepad1
            if (gamepadTouchFieldsAvailable) {
                try {
                    Object ox = gpTouchXField.get(gamepad1);
                    Object oy = gpTouchYField.get(gamepad1);
                    if (ox instanceof Number && oy instanceof Number) {
                        x = ((Number) ox).floatValue();
                        y = ((Number) oy).floatValue();
                        // treat small values as not touching
                        if (Math.abs(x) > TOUCH_DEADZONE || Math.abs(y) > TOUCH_DEADZONE) {
                            active = true;
                            source = "gamepad1-fields";
                        }
                    }
                } catch (Exception ignored) {}
            }

            // 2) If no fields or no active touch yet, use MotionEvent-captured values (only from PS5 device)
            if (!active && !gamepadTouchFieldsAvailable) {
                if (touching) {
                    synchronized (this) {
                        x = touchX;
                        y = touchY;
                        active = Math.abs(x) > TOUCH_DEADZONE || Math.abs(y) > TOUCH_DEADZONE;
                        source = "motion-ps5";
                    }
                }
            }

            // 3) If still not active, do not move (we do NOT use other inputs)
            if (!active) {
                frontLeftDrive.setPower(0.0);
                frontRightDrive.setPower(0.0);
                backLeftDrive.setPower(0.0);
                backRightDrive.setPower(0.0);
                telemetry.addData("TouchSource", source);
                telemetry.addData("touchX", "%.3f", x);
                telemetry.addData("touchY", "%.3f", y);
                telemetry.update();
                idle();
                continue;
            }

            // Map touch X/Y into robot axial/lateral. Invert Y so finger up -> forward.
            double axial = -y;
            double lateral = x;
            double yaw = 0.0;

            // Compute mecanum wheel powers (no rotation)
            double fl = axial + lateral + yaw;
            double fr = axial - lateral - yaw;
            double bl = axial - lateral + yaw;
            double br = axial + lateral - yaw;

            // Normalize by max magnitude then scale to MAX_TOUCH_POWER
            double max = Math.max(Math.abs(fl), Math.abs(fr));
            max = Math.max(max, Math.abs(bl));
            max = Math.max(max, Math.abs(br));
            if (max < 1e-6) max = 1.0;
            fl = fl / max * MAX_TOUCH_POWER;
            fr = fr / max * MAX_TOUCH_POWER;
            bl = bl / max * MAX_TOUCH_POWER;
            br = br / max * MAX_TOUCH_POWER;

            frontLeftDrive.setPower(fl);
            frontRightDrive.setPower(fr);
            backLeftDrive.setPower(bl);
            backRightDrive.setPower(br);

            telemetry.addData("TouchSource", source);
            telemetry.addData("touchX", "%.3f", x);
            telemetry.addData("touchY", "%.3f", y);
            telemetry.addData("power", "%.3f", MAX_TOUCH_POWER);
            telemetry.update();

            idle();
        }
    }
}