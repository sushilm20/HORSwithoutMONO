package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="ComplexityHORS", group="Linear OpMode")
public class complexityHORS extends LinearOpMode {

    // Hardware and control components
    private RobotHardware hardware;
    private RobotStateMachine stateMachine;
    private PIDController pidController;

    // Constants
    private static final double MAX_RPM = 200;
    private static final double TARGET_TOLERANCE_RPM = 5.0;
    private static final long RUMBLE_DURATION_MS = 1000L;

    // Runtime variables
    private double targetRPM = MAX_RPM;
    private boolean yPressedLast = false;

    // Debounce state for gamepad buttons
    private boolean dpadDownLast = false;
    private boolean dpadLeftLast = false;
    private boolean dpadRightLast = false;
    private boolean xPressedLast = false;
    private boolean leftTriggerActiveLast = false;
    private boolean touchpadPressedLast = false;

    // Rumble state
    private boolean atTargetLast = false;
    private boolean rumbling = false;
    private long rumbleEndTimeMs = 0L;

    @Override
    public void runOpMode() {
        // Initialize hardware and control components
        hardware = new RobotHardware();
        hardware.init(hardwareMap);
        
        stateMachine = new RobotStateMachine();
        pidController = new PIDController(0.0003, MAX_RPM, 0.15, 0.78);
        
        // Set initial servo positions
        hardware.getLeftHoodServo().setPosition(stateMachine.getLeftHoodPosition());
        hardware.getRightHoodServo().setPosition(stateMachine.getRightHoodPosition());
        
        // Set initial target RPM based on default mode (CLOSE)
        targetRPM = stateMachine.getTargetRPMForMode();
        pidController.setTargetRPM(targetRPM);
        
        telemetry.addData("Status", "Initialized (mode = CLOSE)");
        telemetry.update();

        // Initialize PID controller timing
        pidController.setLastPosition(hardware.getShooter().getCurrentPosition());
        pidController.setLastTime(System.currentTimeMillis());

        waitForStart();

        while (opModeIsActive()) {
            long nowMs = System.currentTimeMillis();

            // Handle mode toggle (touchpad tap on gamepad1)
            handleModeToggle();

            // Drive control
            handleDrive();

            // Dpad and toggle handling for both gamepads
            handleDpadControls();

            // Update RPM measurement and shooter control
            updateShooterControl(nowMs);

            // Handle rumble feedback
            handleRumble(nowMs);

            // Turret control
            handleTurret();

            // Intake and compression control
            handleIntake();

            // Claw control
            handleClaw(nowMs);

            // Hood servo adjustments
            handleHoodAdjustments(nowMs);

            // Update telemetry
            updateTelemetry();
        }
    }

    /**
     * Handle mode toggle between Far and Close
     */
    private void handleModeToggle() {
        boolean touchpadNow = false;
        try {
            touchpadNow = gamepad1.touchpad;
        } catch (Throwable t) {
            touchpadNow = (gamepad1.left_stick_button && gamepad1.right_stick_button);
        }

        if (touchpadNow && !touchpadPressedLast) {
            stateMachine.toggleMode();
            targetRPM = stateMachine.getTargetRPMForMode();
            pidController.setTargetRPM(targetRPM);
            hardware.getRightHoodServo().setPosition(stateMachine.getRightHoodPosition());
        }
        touchpadPressedLast = touchpadNow;
    }

    /**
     * Handle drive control with mecanum drive
     */
    private void handleDrive() {
        double axial   = -gamepad1.left_stick_y;
        double lateral = -gamepad1.left_stick_x;
        double yaw     = -gamepad1.right_stick_x;
        
        hardware.setDrivePowers(axial, lateral, yaw, 1.0);
    }

    /**
     * Handle dpad controls for shooter settings
     */
    private void handleDpadControls() {
        boolean dpadDownNow = gamepad1.dpad_down || gamepad2.dpad_down;
        if (dpadDownNow && !dpadDownLast) {
            stateMachine.toggleShooter();
        }
        dpadDownLast = dpadDownNow;

        boolean dpadLeftNow = gamepad1.dpad_left || gamepad2.dpad_left;
        if (dpadLeftNow && !dpadLeftLast) {
            targetRPM = Math.max(0.0, targetRPM - 10.0);
            pidController.setTargetRPM(targetRPM);
        }
        dpadLeftLast = dpadLeftNow;

        boolean dpadRightNow = gamepad1.dpad_right || gamepad2.dpad_right;
        if (dpadRightNow && !dpadRightLast) {
            targetRPM = Math.min(MAX_RPM, targetRPM + 10.0);
            pidController.setTargetRPM(targetRPM);
        }
        dpadRightLast = dpadRightNow;
    }

    /**
     * Update shooter RPM measurement and motor control
     */
    private void updateShooterControl(long nowMs) {
        int currentPosition = hardware.getShooter().getCurrentPosition();
        pidController.updateRPM(currentPosition);

        // Quick calibration with Y button
        boolean yNow = gamepad1.y || gamepad2.y;
        if (yNow && !yPressedLast) {
            pidController.calibrate(pidController.getCurrentRPM() / pidController.getRpmScale());
        }
        yPressedLast = yNow;

        // Calculate and apply shooter power
        double shooterPower = pidController.calculatePower();
        hardware.getShooter().setPower(stateMachine.isShooterOn() ? shooterPower : 0.0);
    }

    /**
     * Handle controller rumble when shooter reaches target RPM
     */
    private void handleRumble(long nowMs) {
        boolean atTargetNow = pidController.isAtTarget(TARGET_TOLERANCE_RPM);
        if (atTargetNow && !atTargetLast) {
            rumbling = true;
            rumbleEndTimeMs = nowMs + RUMBLE_DURATION_MS;
            try { gamepad1.rumble((int) RUMBLE_DURATION_MS); } catch (Throwable ignored) {}
            try { gamepad2.rumble((int) RUMBLE_DURATION_MS); } catch (Throwable ignored) {}
        }
        atTargetLast = atTargetNow;

        if (rumbling && nowMs > rumbleEndTimeMs) {
            rumbling = false;
        }
    }

    /**
     * Handle turret control with encoder limits
     */
    private void handleTurret() {
        int turretPos = hardware.getTurret().getCurrentPosition();
        double turretPower = 0.0;
        double turretSpeed = stateMachine.getTurretSpeedForMode();

        if (gamepad1.right_bumper || gamepad2.left_stick_x > 0.2) {
            if (turretPos < RobotHardware.TURRET_MAX_POS) turretPower = turretSpeed;
        } else if (gamepad1.left_bumper || gamepad2.left_stick_x < -0.2) {
            if (turretPos > RobotHardware.TURRET_MIN_POS) turretPower = -turretSpeed;
        }
        hardware.getTurret().setPower(turretPower);
    }

    /**
     * Handle intake and compression servos
     */
    private void handleIntake() {
        boolean leftTriggerNow = gamepad1.left_trigger > 0.1;

        if (leftTriggerNow) {
            if (!leftTriggerActiveLast) {
                stateMachine.saveShooterState(targetRPM, stateMachine.isShooterOn());
                targetRPM = 40.0;
                pidController.setTargetRPM(targetRPM);
                stateMachine.setShooterOn(true);
            }

            hardware.getIntakeMotor().setPower(-1.0);
            hardware.getLeftCompressionServo().setPosition(0.0);
            hardware.getRightCompressionServo().setPosition(1.0);
        } else {
            if (leftTriggerActiveLast) {
                if (stateMachine.hasSavedShooterState()) {
                    targetRPM = stateMachine.restoreTargetRPM();
                    pidController.setTargetRPM(targetRPM);
                }
                stateMachine.setShooterOn(stateMachine.getSavedShooterOnBeforeLeftTrigger());
            }

            if ((gamepad1.right_trigger > 0.1) || (gamepad2.right_trigger > 0.1)) {
                hardware.getIntakeMotor().setPower(1.0);
                hardware.getLeftCompressionServo().setPosition(1.0);
                hardware.getRightCompressionServo().setPosition(0.0);
            } else {
                hardware.getIntakeMotor().setPower(0.0);
                hardware.getLeftCompressionServo().setPosition(0.5);
                hardware.getRightCompressionServo().setPosition(0.5);
            }
        }

        leftTriggerActiveLast = leftTriggerNow;
    }

    /**
     * Handle claw control with timed phases
     */
    private void handleClaw(long nowMs) {
        boolean xNow = gamepad1.x || gamepad2.x;
        if (xNow && !xPressedLast) {
            hardware.getClawServo().setPosition(0.2);
            stateMachine.startClawAction(nowMs);
        }
        xPressedLast = xNow;

        stateMachine.updateClawState(nowMs);
        if (stateMachine.getClawState() == ClawState.OPENING) {
            hardware.getClawServo().setPosition(0.63);
            stateMachine.setClawState(ClawState.IDLE);
        }
    }

    /**
     * Handle hood servo adjustments
     */
    private void handleHoodAdjustments(long nowMs) {
        // Left hood control with gamepad1 A/B
        if (gamepad1.a) {
            if (stateMachine.adjustLeftHood(true, nowMs)) {
                hardware.getLeftHoodServo().setPosition(stateMachine.getLeftHoodPosition());
            }
        }
        if (gamepad1.b) {
            if (stateMachine.adjustLeftHood(false, nowMs)) {
                hardware.getLeftHoodServo().setPosition(stateMachine.getLeftHoodPosition());
            }
        }

        // Right hood control with gamepad2 right stick Y
        if (gamepad2.right_stick_y < -0.2) {
            if (stateMachine.adjustRightHood(true, nowMs)) {
                hardware.getRightHoodServo().setPosition(stateMachine.getRightHoodPosition());
            }
        } else if (gamepad2.right_stick_y > 0.2) {
            if (stateMachine.adjustRightHood(false, nowMs)) {
                hardware.getRightHoodServo().setPosition(stateMachine.getRightHoodPosition());
            }
        }
    }

    /**
     * Update telemetry display
     */
    private void updateTelemetry() {
        telemetry.addData("Status", "Running");
        telemetry.addData("Mode", stateMachine.getCurrentMode());
        telemetry.addData("Target RPM", "%.0f", targetRPM);
        telemetry.addData("Measured RPM Scaled", "%.2f", pidController.getCurrentRPM());
        telemetry.addData("Smoothed RPM (used)", "%.2f", pidController.getCurrentRPM());
        telemetry.addData("Shooter Power", "%.3f", 
            stateMachine.isShooterOn() ? pidController.calculatePower() : 0.0);
        telemetry.addData("Turret Encoder", hardware.getTurret().getCurrentPosition());
        telemetry.addData("Right Hood", "%.3f", stateMachine.getRightHoodPosition());
        telemetry.addData("LeftTriggerOverride", leftTriggerActiveLast);
        telemetry.update();
    }
}