package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name="Simple Hors Brain", group="Linear OpMode")
public class horsBaseTest extends LinearOpMode {

    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor frontLeftDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor frontRightDrive = null;
    private DcMotor backRightDrive = null;
    private DcMotor shooter = null;
    private DcMotor turret = null;

    private double shooterPower = 1.0;
    private boolean shooterOn = true;//ikd
    private boolean yPressedLast = false;

    private int turretTarget = 0;
    private final int TICKS_PER_INCREMENT = 28;

    @Override
    public void runOpMode() {

        frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeft");
        backLeftDrive = hardwareMap.get(DcMotor.class, "backLeft");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRight");
        backRightDrive = hardwareMap.get(DcMotor.class, "backRight");
        shooter = hardwareMap.get(DcMotor.class, "shooter");
        turret = hardwareMap.get(DcMotor.class, "turret");

        frontLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        backLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);
        shooter.setDirection(DcMotor.Direction.FORWARD);
        turret.setDirection(DcMotor.Direction.FORWARD);

        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turret.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        telemetry.addData("Status", "Initialized");
        telemetry.speak("six seven six seven six seven six seven six seven six seven ");
        telemetry.update();

        waitForStart();
        runtime.reset();

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

            // Shooter toggle with Y button
            if (gamepad1.y && !yPressedLast) {
                shooterOn = !shooterOn;
                yPressedLast = true;
            } else if (!gamepad1.y) {
                yPressedLast = false;
            }

            // Shooter power adjustment with D-pad (only if shooter is on)
            if (shooterOn) {
                if (gamepad1.dpad_right && shooterPower < 1.0) {
                    shooterPower += 0.05;
                    shooterPower = Math.min(shooterPower, 1.0);
                    sleep(200);
                } else if (gamepad1.dpad_left && shooterPower > 0.0) {
                    shooterPower -= 0.05;
                    shooterPower = Math.max(shooterPower, 0.0);
                    sleep(200);
                }
            }

            // Turret movement with bumpers
            if (gamepad2.right_bumper) {
                turretTarget += TICKS_PER_INCREMENT;
                turret.setTargetPosition(turretTarget);
                turret.setPower(0.5);
                sleep(200);
            } else if (gamepad2.left_bumper) {
                turretTarget -= TICKS_PER_INCREMENT;
                turret.setTargetPosition(turretTarget);
                turret.setPower(0.5);
                sleep(200);
            }

            frontLeftDrive.setPower(frontLeftPower);
            frontRightDrive.setPower(frontRightPower);
            backLeftDrive.setPower(backLeftPower);
            backRightDrive.setPower(backRightPower);
            shooter.setPower(shooterOn ? shooterPower : 0.0);

            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("Front left/right", "%4.2f, %4.2f", frontLeftPower, frontRightPower);
            telemetry.addData("Back  left/right", "%4.2f, %4.2f", backLeftPower, backRightPower);
            telemetry.addData("Shooter Power", "%4.2f", shooterOn ? shooterPower : 0.0);
            telemetry.addData("Shooter State", shooterOn ? "ON" : "OFF");
            telemetry.addData("Turret Target", turretTarget);
            telemetry.addData("Turret Position", turret.getCurrentPosition());
            telemetry.update();
        }
    }
}
