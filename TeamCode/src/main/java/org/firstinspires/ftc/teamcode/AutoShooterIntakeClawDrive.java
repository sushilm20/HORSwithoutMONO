package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@Autonomous(name="Auto Shooter Intake Claw Drive", group="Linear OpMode")
public class AutoShooterIntakeClawDrive extends LinearOpMode {

    private DcMotor shooter, intakeMotor;
    private DcMotor frontLeftDrive, backLeftDrive, frontRightDrive, backRightDrive;
    private Servo leftCompressionServo, rightCompressionServo, clawServo;
    private Servo leftHoodServo, rightHoodServo;

    @Override
    public void runOpMode() {

        // Initialize hardware
        shooter = hardwareMap.get(DcMotor.class, "shooter");
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeft");
        backLeftDrive = hardwareMap.get(DcMotor.class, "backLeft");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRight");
        backRightDrive = hardwareMap.get(DcMotor.class, "backRight");
        leftCompressionServo = hardwareMap.get(Servo.class, "leftCompressionServo");
        rightCompressionServo = hardwareMap.get(Servo.class, "rightCompressionServo");
        clawServo = hardwareMap.get(Servo.class, "clawServo");
        leftHoodServo = hardwareMap.get(Servo.class, "leftHoodServo");
        rightHoodServo = hardwareMap.get(Servo.class, "rightHoodServo");

        // Set motor directions
        shooter.setDirection(DcMotor.Direction.REVERSE);
        intakeMotor.setDirection(DcMotor.Direction.REVERSE);
        frontLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        backLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);

        // Initial positions
        clawServo.setPosition(0.63); // Closed
        leftCompressionServo.setPosition(0.5); // Neutral
        rightCompressionServo.setPosition(0.5); // Neutral
        leftHoodServo.setPosition(0.35); // Hood set
        rightHoodServo.setPosition(0.35); // Hood set

        telemetry.addData("Status", "Ready to run");
        telemetry.update();

        waitForStart();

        frontLeftDrive.setPower(0.5);
        backLeftDrive.setPower(0.5);
        frontRightDrive.setPower(0.5);
        backRightDrive.setPower(0.5);
        sleep(100);





        // Step 1: Shooter ON for 3 seconds
        shooter.setPower(0.8);
        sleep(3000);

        // Step 2: Intake + Compression ON for 5 seconds (shooter continues)
        intakeMotor.setPower(1.0);
        leftCompressionServo.setPosition(1.0);  // Forward
        rightCompressionServo.setPosition(0.0); // Reverse
        sleep(5000);

        // Step 3: Stop intake + compression
        intakeMotor.setPower(0.0);
        leftCompressionServo.setPosition(0.5);
        rightCompressionServo.setPosition(0.5);

        // Step 4: Claw open then close
        clawServo.setPosition(0.2); // Open
        sleep(500);                // Wait 0.5 second
        clawServo.setPosition(0.63); // Close

        // Step 5: Drive forward for 1 second
        telemetry.addData("Drive", "Moving forward");
        telemetry.update();

        frontLeftDrive.setPower(0.5);
        backLeftDrive.setPower(0.5);
        frontRightDrive.setPower(0.5);
        backRightDrive.setPower(0.5);
        sleep(1000);

        frontLeftDrive.setPower(0.0);
        backLeftDrive.setPower(0.0);
        frontRightDrive.setPower(0.0);
        backRightDrive.setPower(0.0);

        // Step 6: Stop shooter
        shooter.setPower(0.0);

        telemetry.addData("Status", "Sequence Complete");
        telemetry.update();
    }
}
