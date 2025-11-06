package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name="HORS BRAIN", group="Linear OpMode")
public class horsBaseTest extends LinearOpMode {

    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor frontLeftDrive, backLeftDrive, frontRightDrive, backRightDrive;
    private DcMotor shooter, turret, intakeMotor;
    private Servo clawServo, leftCompressionServo, rightCompressionServo;
    private Servo leftHoodServo, rightHoodServo;

    private boolean shooterOn = false;
    private boolean dpadDownPressedLast = false;
    private boolean dpadRightPressedLast = false;
    private boolean dpadLeftPressedLast = false;
    private double hoodServoPosition = 0.12;

    private static final double TICKS_PER_REV = 537.6;
    private double shooterRPM = 0.0;
    private int lastShooterPosition = 0;
    private long lastShooterTime = 0;

    private double targetRPM = 3000.0;
    private double kP = 0.0003;

    @Override
    public void runOpMode() {

        frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeft");
        backLeftDrive = hardwareMap.get(DcMotor.class, "backLeft");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRight");
        backRightDrive = hardwareMap.get(DcMotor.class, "backRight");
        shooter = hardwareMap.get(DcMotor.class, "shooter");
        turret = hardwareMap.get(DcMotor.class, "turret");
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        clawServo = hardwareMap.get(Servo.class, "clawServo");
        leftCompressionServo = hardwareMap.get(Servo.class, "leftCompressionServo");
        rightCompressionServo = hardwareMap.get(Servo.class, "rightCompressionServo");
        leftHoodServo = hardwareMap.get(Servo.class, "leftHoodServo");
        rightHoodServo = hardwareMap.get(Servo.class, "rightHoodServo");

        frontLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        backLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);
        shooter.setDirection(DcMotor.Direction.REVERSE);
        turret.setDirection(DcMotor.Direction.FORWARD);
        intakeMotor.setDirection(DcMotor.Direction.REVERSE);

        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turret.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        clawServo.setPosition(0.63);
        leftCompressionServo.setPosition(0.5);
        rightCompressionServo.setPosition(0.5);
        leftHoodServo.setPosition(hoodServoPosition);
        rightHoodServo.setPosition(hoodServoPosition);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();
        runtime.reset();

        lastShooterPosition = shooter.getCurrentPosition();
        lastShooterTime = System.currentTimeMillis();

        while (opModeIsActive()) {
            double axial   = -gamepad1.left_stick_y;
            double lateral = -gamepad1.left_stick_x;
            double yaw     = -gamepad1.right_stick_x;

            double frontLeftPower  = axial + lateral + yaw;
            double frontRightPower = axial - lateral - yaw;
            double backLeftPower   = axial - lateral + yaw;
            double backRightPower  = axial + lateral - yaw;

            double max = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
            max = Math.max(max, Math.abs(backLeftPower));
            max = Math.max(max, Math.abs(backRightPower));

            if (max > 1.0) {
                frontLeftPower  /= max;
                frontRightPower /= max;
                backLeftPower   /= max;
                backRightPower  /= max;
            }

            double driveSpeed = (gamepad1.left_trigger > 0.1) ? 1.0 : 0.4;

            // Shooter toggle
            if (gamepad1.dpad_down && !dpadDownPressedLast) {
                shooterOn = !shooterOn;
                dpadDownPressedLast = true;
            } else if (!gamepad1.dpad_down) {
                dpadDownPressedLast = false;
            }

            // RPM adjustment
            if (gamepad1.dpad_right && !dpadRightPressedLast) {
                targetRPM = Math.min(targetRPM + 100, 6000);
                dpadRightPressedLast = true;
            } else if (!gamepad1.dpad_right) {
                dpadRightPressedLast = false;
            }

            if (gamepad1.dpad_left && !dpadLeftPressedLast) {
                targetRPM = Math.max(targetRPM - 100, 0);
                dpadLeftPressedLast = true;
            } else if (!gamepad1.dpad_left) {
                dpadLeftPressedLast = false;
            }

            // Shooter RPM calculation
            int currentPosition = shooter.getCurrentPosition();
            long currentTime = System.currentTimeMillis();

            int deltaTicks = currentPosition - lastShooterPosition;
            long deltaTime = currentTime - lastShooterTime;

            if (deltaTime > 0) {
                double deltaMinutes = deltaTime / 60000.0;
                shooterRPM = (deltaTicks / TICKS_PER_REV) / deltaMinutes;
            }

            lastShooterPosition = currentPosition;
            lastShooterTime = currentTime;

            // Shooter power control
            double shooterPower = kP * (targetRPM - shooterRPM);
            shooterPower = Math.max(0.0, Math.min(shooterPower, 1.0));
            shooter.setPower(shooterOn ? shooterPower : 0.0);

            // Turret control
            if (gamepad1.right_bumper) {
                turret.setPower(0.2);
            } else if (gamepad1.left_bumper) {
                turret.setPower(-0.2);
            } else {
                turret.setPower(0.0);
            }

            // Intake + compression
            if (gamepad1.right_trigger > 0.1) {
                intakeMotor.setPower(1.0);
                leftCompressionServo.setPosition(1.0);
                rightCompressionServo.setPosition(0.0);
            } else {
                intakeMotor.setPower(0.0);
                leftCompressionServo.setPosition(0.5);
                rightCompressionServo.setPosition(0.5);
            }

            // Claw toggle
            if (gamepad1.x) {
                clawServo.setPosition(0.2);
                sleep(500);
                clawServo.setPosition(0.63);
            }

            // Hood servo control
            if (gamepad1.a && hoodServoPosition < 0.48) {
                hoodServoPosition += 0.025;
                hoodServoPosition = Math.min(hoodServoPosition, 0.48);
                sleep(200);
            } else if (gamepad1.b && hoodServoPosition > 0.12) {
                hoodServoPosition -= 0.025;
                hoodServoPosition = Math.max(hoodServoPosition, 0.12);
                sleep(200);
            }

            leftHoodServo.setPosition(hoodServoPosition);
            rightHoodServo.setPosition(hoodServoPosition);

            frontLeftDrive.setPower(frontLeftPower * driveSpeed);
            frontRightDrive.setPower(frontRightPower * driveSpeed);
            backLeftDrive.setPower(backLeftPower * driveSpeed);
            backRightDrive.setPower(backRightPower * driveSpeed);

            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("Shooter RPM", "%4.2f", shooterOn ? shooterRPM : 0.0);
            telemetry.addData("Target RPM", "%4.0f", targetRPM);
            telemetry.addData("Shooter State", shooterOn ? "ON" : "OFF");
            telemetry.addData("Turret Encoder", turret.getCurrentPosition());
            telemetry.addData("Claw", "%4.2f", clawServo.getPosition());
            telemetry.addData("Left Compression", "%4.2f", leftCompressionServo.getPosition());
            telemetry.addData("Right Compression", "%4.2f", rightCompressionServo.getPosition());
            telemetry.addData("Hood Servo", "%4.2f", hoodServoPosition);
            telemetry.update();
        }
    }
}
