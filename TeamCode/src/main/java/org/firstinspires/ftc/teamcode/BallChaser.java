package org.firstinspires.ftc.teamcode;

import android.graphics.Color;
import android.util.Size;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.SortOrder;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.opencv.Circle;
import org.firstinspires.ftc.vision.opencv.ColorBlobLocatorProcessor;
import org.firstinspires.ftc.vision.opencv.ColorRange;
import org.firstinspires.ftc.vision.opencv.ImageRegion;

import java.util.List;

/**
 * BallChaser - Autonomous Ball Detection and Chasing OpMode
 * 
 * This OpMode automatically detects and chases green or purple balls using a webcam.
 * It uses ColorBlobLocatorProcessor to identify balls and simple PID-like control
 * to drive towards them.
 */
@TeleOp(name = "Ball Chaser", group = "Autonomous")
public class BallChaser extends LinearOpMode {
    
    // Drive motors
    private DcMotor frontLeftDrive, backLeftDrive, frontRightDrive, backRightDrive;
    
    // Vision processors for color detection
    public ColorBlobLocatorProcessor purpleLocator, greenLocator;
    
    // PID-like control parameters
    public double Pr = 0.003;   // Proportional gain for rotation
    public double Py = 0.02;    // Proportional gain for forward/backward movement
    public double Px = 0.002;   // Proportional gain for strafing
    
    // Camera and blob detection parameters
    public double camCenter = 160;           // Center of camera in pixels (for 320px width)
    public double blobRadGoal = 50;          // Target blob radius (how close to approach)
    public double INTAKE_ACTIVATION_RADIUS = 45;  // Radius at which intake would activate
    
    // Control state variables for PID-like behavior
    private double rotGoal = 0;
    private double xGoal = 0;
    private double yGoal = 0;

    @Override
    public void runOpMode() {
        // Initialize hardware
        initializeHardware();
        
        // Initialize vision processing
        initializeVision();
        
        // Set up telemetry for better debugging
        telemetry.setMsTransmissionInterval(100);
        telemetry.addData("Status", "Initialized");
        telemetry.update();
        
        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        
        // Run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            // Process vision and update drive
            processBallDetection();
            
            // Update telemetry
            telemetry.update();
            
            // Small sleep to prevent CPU overload
            sleep(10);
        }
    }
    
    /**
     * Initialize all hardware components
     */
    private void initializeHardware() {
        // Map drive motors
        frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeft");
        backLeftDrive = hardwareMap.get(DcMotor.class, "backLeft");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRight");
        backRightDrive = hardwareMap.get(DcMotor.class, "backRight");
        
        // Set motor directions for mecanum drive
        frontLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        backLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);
        
        // Set zero power behavior
        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }
    
    /**
     * Initialize vision processing with color blob locators
     */
    private void initializeVision() {
        // Create PURPLE color blob locator
        purpleLocator = new ColorBlobLocatorProcessor.Builder()
                .setTargetColorRange(ColorRange.ARTIFACT_PURPLE)
                .setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)
                .setRoi(ImageRegion.entireFrame())
                .setDrawContours(true)
                .setBoxFitColor(0)
                .setCircleFitColor(Color.rgb(255, 255, 0))
                .setBlurSize(5)
                .setDilateSize(15)
                .setErodeSize(15)
                .setMorphOperationType(ColorBlobLocatorProcessor.MorphOperationType.CLOSING)
                .build();
        
        // Create GREEN color blob locator
        greenLocator = new ColorBlobLocatorProcessor.Builder()
                .setTargetColorRange(ColorRange.ARTIFACT_GREEN)
                .setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)
                .setRoi(ImageRegion.entireFrame())
                .setDrawContours(true)
                .setBoxFitColor(0)
                .setCircleFitColor(Color.rgb(255, 255, 0))
                .setBlurSize(5)
                .setDilateSize(15)
                .setErodeSize(15)
                .setMorphOperationType(ColorBlobLocatorProcessor.MorphOperationType.CLOSING)
                .build();
        
        // Build vision portal with both processors
        VisionPortal portal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessors(greenLocator, purpleLocator)
                .setCameraResolution(new Size(320, 240))
                .enableLiveView(true)
                .build();
    }
    
    /**
     * Process ball detection and control robot movement
     */
    private void processBallDetection() {
        // Get blobs from both processors
        List<ColorBlobLocatorProcessor.Blob> blobs = purpleLocator.getBlobs();
        blobs.addAll(greenLocator.getBlobs());
        
        // Filter blobs to remove noise
        ColorBlobLocatorProcessor.Util.filterByCriteria(
                ColorBlobLocatorProcessor.BlobCriteria.BY_CONTOUR_AREA,
                100, 20000, blobs);
        
        ColorBlobLocatorProcessor.Util.filterByCriteria(
                ColorBlobLocatorProcessor.BlobCriteria.BY_CIRCULARITY,
                0.5, 1, blobs);
        
        // Sort blobs by size (largest first)
        ColorBlobLocatorProcessor.Util.sortByCriteria(
                ColorBlobLocatorProcessor.BlobCriteria.BY_CONTOUR_AREA,
                SortOrder.DESCENDING, blobs);
        
        // Display blob information
        telemetry.addLine("Circularity Radius Center");
        for (ColorBlobLocatorProcessor.Blob b : blobs) {
            Circle circleFit = b.getCircle();
            telemetry.addLine(String.format("%5.3f      %3d     (%3d,%3d)",
                    b.getCircularity(), (int) circleFit.getRadius(), 
                    (int) circleFit.getX(), (int) circleFit.getY()));
        }
        
        // Control robot based on detected blobs
        if (!blobs.isEmpty()) {
            // Get the largest blob
            ColorBlobLocatorProcessor.Blob targetBlob = blobs.get(0);
            Circle targetCircle = targetBlob.getCircle();
            
            // Set control goals
            rotGoal = camCenter;
            xGoal = camCenter;
            yGoal = blobRadGoal;
            
            // Calculate control outputs using simple proportional control
            double rotError = targetCircle.getX() - rotGoal;
            double xError = targetCircle.getX() - xGoal;
            double yError = yGoal - targetCircle.getRadius();
            
            double rotPower = Pr * rotError;
            double xPower = Px * xError;
            double yPower = Py * yError;
            
            // Apply control to drive
            updateDrive(yPower, xPower, rotPower);
            
            telemetry.addData("Target Found", "Yes");
            telemetry.addData("Target X", (int) targetCircle.getX());
            telemetry.addData("Target Y", (int) targetCircle.getY());
            telemetry.addData("Target Radius", (int) targetCircle.getRadius());
        } else {
            // No blobs detected - drive slowly forward to search
            rotGoal = 0;
            yGoal = 0;
            xGoal = 0;
            updateDrive(0.4, 0.0, 0.0);
            telemetry.addData("Target Found", "No - Searching");
        }
    }
    
    /**
     * Update drive motors with mecanum drive kinematics
     * @param axial Forward/backward movement (-1 to 1)
     * @param lateral Left/right strafing (-1 to 1)
     * @param yaw Rotation (-1 to 1)
     */
    private void updateDrive(double axial, double lateral, double yaw) {
        // Calculate individual motor powers for mecanum drive
        double frontLeftPower = axial + lateral + yaw;
        double frontRightPower = axial - lateral - yaw;
        double backLeftPower = axial - lateral + yaw;
        double backRightPower = axial + lateral - yaw;
        
        // Normalize powers to keep them within [-1, 1]
        double max = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
        max = Math.max(max, Math.abs(backLeftPower));
        max = Math.max(max, Math.abs(backRightPower));
        
        if (max > 1.0) {
            frontLeftPower /= max;
            frontRightPower /= max;
            backLeftPower /= max;
            backRightPower /= max;
        }
        
        // Set motor powers
        frontLeftDrive.setPower(frontLeftPower);
        frontRightDrive.setPower(frontRightPower);
        backLeftDrive.setPower(backLeftPower);
        backRightDrive.setPower(backRightPower);
        
        // Telemetry for debugging
        telemetry.addData("Drive Powers", "FL: %.2f, FR: %.2f, BL: %.2f, BR: %.2f",
                frontLeftPower, frontRightPower, backLeftPower, backRightPower);
    }
}
