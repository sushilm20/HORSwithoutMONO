package org.firstinspires.ftc.teamcode;

/**
 * PID Controller for shooter RPM control with feedforward and exponential moving average
 */
public class PIDController {
    private double kP;
    private double kI;
    private double kD;
    private double maxRPM;
    private double emaAlpha;
    
    private double currentRPM;
    private double targetRPM;
    private int lastPosition;
    private long lastTime;
    private double rpmScale;
    
    private static final double TICKS_PER_REV = 537.6;
    
    /**
     * Constructor for PID Controller
     * @param kP Proportional gain
     * @param maxRPM Maximum RPM value
     * @param emaAlpha Exponential moving average smoothing factor
     * @param rpmScale RPM scaling factor for calibration
     */
    public PIDController(double kP, double maxRPM, double emaAlpha, double rpmScale) {
        this.kP = kP;
        this.kI = 0.0;
        this.kD = 0.0;
        this.maxRPM = maxRPM;
        this.emaAlpha = emaAlpha;
        this.rpmScale = rpmScale;
        this.currentRPM = 0.0;
        this.targetRPM = 0.0;
        this.lastPosition = 0;
        this.lastTime = System.currentTimeMillis();
    }
    
    /**
     * Update RPM measurement from encoder
     * @param currentPosition Current encoder position
     * @return Smoothed RPM value
     */
    public double updateRPM(int currentPosition) {
        long nowMs = System.currentTimeMillis();
        int deltaTicks = currentPosition - lastPosition;
        long deltaTimeMs = nowMs - lastTime;
        if (deltaTimeMs <= 0) deltaTimeMs = 1;
        
        double ticksPerSec = (deltaTicks * 1000.0) / deltaTimeMs;
        double measuredRPMRaw = (ticksPerSec / TICKS_PER_REV) * 60.0;
        double measuredRPMScaled = measuredRPMRaw * rpmScale;
        
        // EMA smoothing
        currentRPM = (1.0 - emaAlpha) * currentRPM + emaAlpha * measuredRPMScaled;
        if (currentRPM < 0.0) currentRPM = 0.0;
        
        lastPosition = currentPosition;
        lastTime = nowMs;
        
        return currentRPM;
    }
    
    /**
     * Calculate motor power using feedforward + P control
     * @return Motor power value [0.0, 1.0]
     */
    public double calculatePower() {
        double ff = targetRPM / Math.max(1.0, maxRPM);
        double error = targetRPM - currentRPM;
        double pTerm = kP * error;
        double power = ff + pTerm;
        return Math.max(0.0, Math.min(1.0, power));
    }
    
    /**
     * Check if RPM is within tolerance of target
     * @param tolerance Tolerance in RPM
     * @return true if within tolerance
     */
    public boolean isAtTarget(double tolerance) {
        return Math.abs(targetRPM - currentRPM) <= tolerance;
    }
    
    /**
     * Calibrate RPM scale based on target and measured raw RPM
     * @param rawRPM Raw measured RPM
     */
    public void calibrate(double rawRPM) {
        double safeMeasured = Math.abs(rawRPM);
        if (safeMeasured >= 1.0) {
            double candidateScale = targetRPM / rawRPM;
            if (candidateScale < 0.2) candidateScale = 0.2;
            if (candidateScale > 3.0) candidateScale = 3.0;
            rpmScale = candidateScale;
        }
    }
    
    // Getters and setters
    public double getCurrentRPM() { return currentRPM; }
    public double getTargetRPM() { return targetRPM; }
    public void setTargetRPM(double targetRPM) { this.targetRPM = targetRPM; }
    public double getRpmScale() { return rpmScale; }
    public void setRpmScale(double rpmScale) { this.rpmScale = rpmScale; }
    public void setLastPosition(int position) { this.lastPosition = position; }
    public void setLastTime(long time) { this.lastTime = time; }
}
