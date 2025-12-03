package org.firstinspires.ftc.teamcode;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@Autonomous(name = "Pedro 12 ball", group = "Autonomous")
@Configurable // Panels
public class PedroAutonomous extends OpMode {

    private TelemetryManager panelsTelemetry; // Panels Telemetry instance
    public Follower follower; // Pedro Pathing follower instance
    private int pathState; // Current autonomous path state (state machine)
    private Paths paths; // Paths defined in the Paths class
    private ElapsedTime waitTimer; // Timer for waiting after Path1

    // Pedro Pathing Shoot Position (within ±5 grid units of 45, 100 in x/y, heading 12.5)
    private static final Pose PEDRO_SHOOT_POSITION = new Pose(45, 100, Math.toRadians(12.5));

    @Override
    public void init() {
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(72, 8, Math.toRadians(90)));

        paths = new Paths(follower); // Build paths

        waitTimer = new ElapsedTime(); // Timer for waiting
        panelsTelemetry.debug("Status", "Initialized");
        panelsTelemetry.update(telemetry);
        pathState = 0; // Start state machine at 0
    }

    @Override
    public void loop() {
        follower.update(); // Update Pedro Pathing

        pathState = autonomousPathUpdate(); // Update autonomous state machine

        // Log values to Panels and Driver Station
        panelsTelemetry.debug("Path State", pathState);
        panelsTelemetry.debug("X", follower.getPose().getX());
        panelsTelemetry.debug("Y", follower.getPose().getY());
        panelsTelemetry.debug("Heading", follower.getPose().getHeading());
        panelsTelemetry.update(telemetry);
    }

    public int autonomousPathUpdate() {
        // Autonomous state machine
        switch (pathState) {
            case 0:
                follower.followPath(paths.Path1);
                pathState++;
                break;

            case 1:
                if (!follower.isBusy()) {
                    // Finished Path1 -- start wait timer
                    waitTimer.reset();
                    pathState++;
                }
                break;

            case 2:
                // Waiting for 5 seconds
                if (waitTimer.seconds() >= 5.0) {
                    follower.followPath(paths.Path2);
                    pathState++;
                }
                break;

            case 3:
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path3);
                    pathState++;
                }
                break;

            case 4:
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path4);
                    pathState++;
                }
                break;

            case 5:
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path5);
                    pathState++;
                }
                break;

            case 6:
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path6);
                    pathState++;
                }
                break;

            case 7:
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path7);
                    pathState++;
                }
                break;

            case 8:
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path8);
                    pathState++;
                }
                break;

            case 9:
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path9);
                    pathState++;
                }
                break;

            case 10:
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path10);
                    pathState++;
                }
                break;

            default:
                // Finished paths, optionally add stopping code here
                break;
        }

        return pathState;
    }

    public static class Paths {

        public PathChain Path1;
        public PathChain Path2;
        public PathChain Path3;
        public PathChain Path4;
        public PathChain Path5;
        public PathChain Path6;
        public PathChain Path7;
        public PathChain Path8;
        public PathChain Path9;
        public PathChain Path10;

        public Paths(Follower follower) {
            Path1 = follower
                .pathBuilder()
                .addPath(
                    new BezierLine(new Pose(25.274, 125.122), new Pose(65.681, 84.403))
                )
                .setLinearHeadingInterpolation(
                    Math.toRadians(142.5),
                    Math.toRadians(180)
                )
                .build();

            Path2 = follower
                .pathBuilder()
                .addPath(
                    new BezierLine(new Pose(65.681, 84.403), new Pose(18.468, 84.104))
                )
                .setTangentHeadingInterpolation()
                .build();
            Path2.setVelocityConstraint(0.3); // SLOW for Path2

            Path3 = follower
                .pathBuilder()
                .addPath(
                    new BezierLine(new Pose(18.468, 84.104), PEDRO_SHOOT_POSITION)
                )
                .setLinearHeadingInterpolation(
                    Math.toRadians(180),
                    Math.toRadians(142.5)
                )
                .build();
            Path3.setVelocityConstraint(0.5); // SLOW DOWN for Path3

            Path4 = follower
                .pathBuilder()
                .addPath(
                    new BezierLine(PEDRO_SHOOT_POSITION, new Pose(44.423, 59.896))
                )
                .setLinearHeadingInterpolation(
                    Math.toRadians(142.5),
                    Math.toRadians(180)
                )
                .build();

            Path5 = follower
                .pathBuilder()
                .addPath(
                    new BezierLine(new Pose(44.423, 59.896), new Pose(18.468, 59.646))
                )
                .setTangentHeadingInterpolation()
                .build();
            Path5.setVelocityConstraint(0.3); // SLOW for Path5

            Path6 = follower
                .pathBuilder()
                .addPath(
                    new BezierLine(new Pose(18.468, 59.646), PEDRO_SHOOT_POSITION)
                )
                .setLinearHeadingInterpolation(
                    Math.toRadians(180),
                    Math.toRadians(142.5)
                )
                .build();

            Path7 = follower
                .pathBuilder()
                .addPath(
                    new BezierLine(PEDRO_SHOOT_POSITION, new Pose(43.924, 35.438))
                )
                .setLinearHeadingInterpolation(
                    Math.toRadians(142.5),
                    Math.toRadians(180)
                )
                .build();

            Path8 = follower
                .pathBuilder()
                .addPath(
                    new BezierLine(new Pose(43.924, 35.438), new Pose(18.718, 35.438))
                )
                .setTangentHeadingInterpolation()
                .build();
            Path8.setVelocityConstraint(0.3); // SLOW for Path8

            Path9 = follower
                .pathBuilder()
                .addPath(
                    new BezierLine(new Pose(18.718, 35.438), PEDRO_SHOOT_POSITION)
                )
                .setLinearHeadingInterpolation(
                    Math.toRadians(180),
                    Math.toRadians(142.5)
                )
                .build();

            Path10 = follower
                .pathBuilder()
                .addPath(
                    new BezierLine(PEDRO_SHOOT_POSITION, new Pose(44.672, 134.017))
                )
                .setLinearHeadingInterpolation(
                    Math.toRadians(142.5),
                    Math.toRadians(90)
                )
                .build();
        }
    }
}
