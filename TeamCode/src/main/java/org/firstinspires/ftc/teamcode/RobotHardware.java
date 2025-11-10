package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Hardware abstraction class for the robot
 * Encapsulates all hardware components and their initialization
 */
public class RobotHardware {
    // Drive motors
    private DcMotor frontLeftDrive;
    private DcMotor backLeftDrive;
    private DcMotor frontRightDrive;
    private DcMotor backRightDrive;
    
    // Mechanism motors
    private DcMotor shooter;
    private DcMotor turret;
    private DcMotor intakeMotor;
    
    // Servos
    private Servo clawServo;
    private Servo leftCompressionServo;
    private Servo rightCompressionServo;
    private Servo leftHoodServo;
    private Servo rightHoodServo;
    
    // Hardware limits
    public static final int TURRET_MIN_POS = -500;
    public static final int TURRET_MAX_POS = 500;
    
    /**
     * Initialize all hardware components
     * @param hardwareMap Hardware map from OpMode
     */
    public void init(HardwareMap hardwareMap) {
        // Initialize drive motors
        frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeft");
        backLeftDrive = hardwareMap.get(DcMotor.class, "backLeft");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRight");
        backRightDrive = hardwareMap.get(DcMotor.class, "backRight");
        
        // Initialize mechanism motors
        shooter = hardwareMap.get(DcMotor.class, "shooter");
        turret = hardwareMap.get(DcMotor.class, "turret");
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        
        // Initialize servos
        clawServo = hardwareMap.get(Servo.class, "clawServo");
        leftCompressionServo = hardwareMap.get(Servo.class, "leftCompressionServo");
        rightCompressionServo = hardwareMap.get(Servo.class, "rightCompressionServo");
        leftHoodServo = hardwareMap.get(Servo.class, "leftHoodServo");
        rightHoodServo = hardwareMap.get(Servo.class, "rightHoodServo");
        
        // Set motor directions
        frontLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        backLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);
        shooter.setDirection(DcMotor.Direction.REVERSE);
        turret.setDirection(DcMotor.Direction.FORWARD);
        intakeMotor.setDirection(DcMotor.Direction.REVERSE);
        
        // Configure motor modes
        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turret.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        
        // Set initial servo positions
        clawServo.setPosition(0.63);
        leftCompressionServo.setPosition(0.5);
        rightCompressionServo.setPosition(0.5);
    }
    
    /**
     * Set drive motor powers with mecanum drive kinematics
     * @param axial Forward/backward movement
     * @param lateral Left/right movement
     * @param yaw Rotation
     * @param speedMultiplier Speed multiplier [0.0, 1.0]
     */
    public void setDrivePowers(double axial, double lateral, double yaw, double speedMultiplier) {
        double frontLeftPower  = axial + lateral + yaw;
        double frontRightPower = axial - lateral - yaw;
        double backLeftPower   = axial - lateral + yaw;
        double backRightPower  = axial + lateral - yaw;
        
        // Normalize powers
        double max = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
        max = Math.max(max, Math.abs(backLeftPower));
        max = Math.max(max, Math.abs(backRightPower));
        
        if (max > 1.0) {
            frontLeftPower  /= max;
            frontRightPower /= max;
            backLeftPower   /= max;
            backRightPower  /= max;
        }
        
        frontLeftDrive.setPower(frontLeftPower * speedMultiplier);
        frontRightDrive.setPower(frontRightPower * speedMultiplier);
        backLeftDrive.setPower(backLeftPower * speedMultiplier);
        backRightDrive.setPower(backRightPower * speedMultiplier);
    }
    
    // Getters for all hardware components
    public DcMotor getFrontLeftDrive() { return frontLeftDrive; }
    public DcMotor getBackLeftDrive() { return backLeftDrive; }
    public DcMotor getFrontRightDrive() { return frontRightDrive; }
    public DcMotor getBackRightDrive() { return backRightDrive; }
    public DcMotor getShooter() { return shooter; }
    public DcMotor getTurret() { return turret; }
    public DcMotor getIntakeMotor() { return intakeMotor; }
    public Servo getClawServo() { return clawServo; }
    public Servo getLeftCompressionServo() { return leftCompressionServo; }
    public Servo getRightCompressionServo() { return rightCompressionServo; }
    public Servo getLeftHoodServo() { return leftHoodServo; }
    public Servo getRightHoodServo() { return rightHoodServo; }
}
