package org.firstinspires.ftc.teamcode;

/**
 * State machine manager for robot operations
 * Handles transitions between different robot states
 */
public class RobotStateMachine {
    private RobotState currentState;
    private RobotMode currentMode;
    private ClawState clawState;
    
    // Mode-specific parameters
    public static final double TURRET_SPEED_CLOSE = 0.3;
    public static final double TURRET_SPEED_FAR   = 0.3;
    public static final double RIGHT_HOOD_CLOSE   = 0.12;
    public static final double RIGHT_HOOD_FAR     = 0.24;
    public static final double TARGET_RPM_CLOSE   = 90.0;
    public static final double TARGET_RPM_FAR     = 140.0;
    
    // Shooter control state
    private boolean shooterOn;
    
    // Timing for non-blocking operations
    private long clawActionStartMs;
    private static final long CLAW_CLOSE_MS = 500L;
    
    // Hood positions
    private double leftHoodPosition;
    private double rightHoodPosition;
    private long lastLeftHoodAdjustMs;
    private long lastRightHoodAdjustMs;
    private static final long HOOD_ADJUST_DEBOUNCE_MS = 120L;
    
    // Saved state for left trigger override
    private double savedTargetRPMBeforeLeftTrigger;
    private boolean savedShooterOnBeforeLeftTrigger;
    
    public RobotStateMachine() {
        this.currentState = RobotState.IDLE;
        this.currentMode = RobotMode.CLOSE;
        this.clawState = ClawState.IDLE;
        this.shooterOn = true;
        this.leftHoodPosition = 0.12;
        this.rightHoodPosition = RIGHT_HOOD_CLOSE;
        this.savedTargetRPMBeforeLeftTrigger = -1.0;
        this.savedShooterOnBeforeLeftTrigger = false;
    }
    
    /**
     * Toggle between Far and Close modes
     */
    public void toggleMode() {
        if (currentMode == RobotMode.CLOSE) {
            currentMode = RobotMode.FAR;
            rightHoodPosition = RIGHT_HOOD_FAR;
        } else {
            currentMode = RobotMode.CLOSE;
            rightHoodPosition = RIGHT_HOOD_CLOSE;
        }
        shooterOn = true;
    }
    
    /**
     * Get target RPM based on current mode
     */
    public double getTargetRPMForMode() {
        return (currentMode == RobotMode.FAR) ? TARGET_RPM_FAR : TARGET_RPM_CLOSE;
    }
    
    /**
     * Get turret speed based on current mode
     */
    public double getTurretSpeedForMode() {
        return (currentMode == RobotMode.FAR) ? TURRET_SPEED_FAR : TURRET_SPEED_CLOSE;
    }
    
    /**
     * Start claw closing action
     */
    public void startClawAction(long currentTimeMs) {
        clawState = ClawState.CLOSING;
        clawActionStartMs = currentTimeMs;
    }
    
    /**
     * Update claw state machine
     */
    public void updateClawState(long currentTimeMs) {
        if (clawState == ClawState.CLOSING && currentTimeMs >= clawActionStartMs + CLAW_CLOSE_MS) {
            clawState = ClawState.OPENING;
        }
    }
    
    /**
     * Adjust left hood position
     */
    public boolean adjustLeftHood(boolean increase, long currentTimeMs) {
        if (currentTimeMs - lastLeftHoodAdjustMs >= HOOD_ADJUST_DEBOUNCE_MS) {
            lastLeftHoodAdjustMs = currentTimeMs;
            if (increase) {
                leftHoodPosition += 0.025;
                if (leftHoodPosition > 0.45) leftHoodPosition = 0.45;
            } else {
                leftHoodPosition -= 0.025;
                if (leftHoodPosition < 0.12) leftHoodPosition = 0.12;
            }
            return true;
        }
        return false;
    }
    
    /**
     * Adjust right hood position
     */
    public boolean adjustRightHood(boolean increase, long currentTimeMs) {
        if (currentTimeMs - lastRightHoodAdjustMs >= HOOD_ADJUST_DEBOUNCE_MS) {
            lastRightHoodAdjustMs = currentTimeMs;
            if (increase) {
                rightHoodPosition += 0.01;
                if (rightHoodPosition > 0.45) rightHoodPosition = 0.45;
            } else {
                rightHoodPosition -= 0.01;
                if (rightHoodPosition < 0.12) rightHoodPosition = 0.12;
            }
            return true;
        }
        return false;
    }
    
    /**
     * Save shooter state for left trigger override
     */
    public void saveShooterState(double targetRPM, boolean shooterOn) {
        this.savedTargetRPMBeforeLeftTrigger = targetRPM;
        this.savedShooterOnBeforeLeftTrigger = shooterOn;
    }
    
    /**
     * Restore shooter state after left trigger override
     */
    public double restoreTargetRPM() {
        double restored = savedTargetRPMBeforeLeftTrigger;
        savedTargetRPMBeforeLeftTrigger = -1.0;
        return restored;
    }
    
    /**
     * Check if shooter state was saved
     */
    public boolean hasSavedShooterState() {
        return savedTargetRPMBeforeLeftTrigger >= 0.0;
    }
    
    // Getters and setters
    public RobotState getCurrentState() { return currentState; }
    public void setCurrentState(RobotState state) { this.currentState = state; }
    public RobotMode getCurrentMode() { return currentMode; }
    public ClawState getClawState() { return clawState; }
    public void setClawState(ClawState state) { this.clawState = state; }
    public boolean isShooterOn() { return shooterOn; }
    public void setShooterOn(boolean on) { this.shooterOn = on; }
    public void toggleShooter() { this.shooterOn = !this.shooterOn; }
    public double getLeftHoodPosition() { return leftHoodPosition; }
    public double getRightHoodPosition() { return rightHoodPosition; }
    public void setRightHoodPosition(double position) { this.rightHoodPosition = position; }
    public boolean getSavedShooterOnBeforeLeftTrigger() { return savedShooterOnBeforeLeftTrigger; }
}
