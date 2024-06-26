package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.JavaUtil;
import org.firstinspires.ftc.teamcode.robot.Common.ButtonMgr;
import org.firstinspires.ftc.teamcode.robot.Common.Parts;
import org.firstinspires.ftc.teamcode.robot.Common.TelemetryMgr;
import org.firstinspires.ftc.teamcode.robot.Common.Tools.Position;
import org.firstinspires.ftc.teamcode.robot.DiscShooter.PartsDS;

@TeleOp(name = "AA_MentorTestAT01", group = "")
//@Disabled
public class MentorTestAT01 extends LinearOpMode {

   private ElapsedTime elapsedTime; // = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

   private ElapsedTime timerLoop = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
   private double timeLoop;

   private final double maxSpeed = 1;//0.2;

   //    double DriveSpeed, DriveAngle, Rotate;
   double currentError = 0;

   public Parts parts;

   @Override
   public void runOpMode() {

      parts = new PartsDS(this, Parts.robotType.GOCANUM);

      parts.useODO = false;
      parts.useSlamra = true; //true;
      parts.useNeoMatrix = true;
      parts.useAprilTag = true;
      //robot.reverseDrive = true;  // for AndyMark test
      parts.useDistanceSensors = false; //true; //false;
      //parts.fieldStartPosition = new Position (36,63,-90);
      parts.odoRobotOffset = new Position (2.25,0,0);
      parts.slamraRobotOffset = new Position(-8,-1,0);
      parts.setup();

      elapsedTime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

      parts.preInit();

      TelemetryMgr.setDebugLevel(10);
      while (!isStarted()) {
         TelemetryMgr.Message(1, ">", "Press Play to start");
         TelemetryMgr.Message(1, ">", "Robot Heading = %.1f", parts.robot.returnImuHeading(true));
         TelemetryMgr.Message(1, "Drive Type:", parts.reverseDrive ? "AndyMark" : "GobildaBot");

         parts.initLoop();

         if (parts.buttonMgr.wasTapped(1, ButtonMgr.Buttons.x))
            parts.reverseDrive = !parts.reverseDrive;
         if (parts.buttonMgr.wasTapped(2, ButtonMgr.Buttons.x))
            parts.reverseDrive = !parts.reverseDrive;

         TelemetryMgr.Update(true);
         sleep(20);
      }

      parts.preRun();

      parts.navigator.setMaxSpeed(maxSpeed);
      //navigator.setDeltaHeading();

      TelemetryMgr.setDebugLevel(10);
      if (opModeIsActive()) {
         // Put run blocks here.
         while (opModeIsActive()) {

            addTelemetryLoopStart();
            parts.runLoop();
            addTelemetryLoopEnd();
            TelemetryMgr.Update();
         }
      }

      parts.stop();

   }

   private void addTelemetryLoopStart() {
      TelemetryMgr.Message(2, "Loop time (ms)", JavaUtil.formatNumber(calculateLoopTime(), 0));
      TelemetryMgr.Message(2, "IMU raw heading", JavaUtil.formatNumber(parts.robot.returnImuHeading(),2));
//      TelemetryHandler.Message(3, "rangeL", String.format("%.01f in", parts.sensors.distL));
//      TelemetryHandler.Message(3, "rangeM", String.format("%.01f in", parts.sensors.distM));
//      TelemetryHandler.Message(3, "rangeR", String.format("%.01f in", parts.sensors.distR));
//        telemetry.addData("raw__", localizer.odoRawPose.toString(2));
//        telemetry.addData("robot", localizer.odoRobotPose.toString(2));
//        telemetry.addData("final", localizer.odoFinalPose.toString(2));
      if (parts.useODO) parts.odometry.addTeleOpTelemetry();
   }

   private void addTelemetryLoopEnd() {
      TelemetryMgr.Message(4, "r (magnitude)", parts.controls.DriveSpeed);
      TelemetryMgr.Message(4, "robotAngle", parts.controls.DriveAngle);
      TelemetryMgr.Message(4, "rotate", parts.controls.Rotate);
      TelemetryMgr.Message(4, "storedHeading", JavaUtil.formatNumber(parts.navigator.storedHeading, 2));
      TelemetryMgr.Message(4, "deltaHeading", JavaUtil.formatNumber(parts.navigator.deltaHeading, 2));
      TelemetryMgr.Message(4, "modifyHeading", JavaUtil.formatNumber(parts.navigator.modifyHeading, 2));
//      TelemetryMgr.Message(4, "IMU-Modified", JavaUtil.formatNumber(parts.robot.returnImuHeading()-parts.navigator.deltaHeading, 2));
      TelemetryMgr.Message(4, "IMU-Modified", JavaUtil.formatNumber(parts.robot.returnImuHeading() - parts.navigator.modifyHeading, 2));

//        telemetry.addData("error", JavaUtil.formatNumber(currentError, 2));
//        telemetry.addData("v0", JavaUtil.formatNumber(navigator.v0, 2));
//        telemetry.addData("v1", JavaUtil.formatNumber(navigator.v2, 2));
//        telemetry.addData("v2", JavaUtil.formatNumber(navigator.v1, 2));
//        telemetry.addData("v3", JavaUtil.formatNumber(navigator.v3, 2));
//        telemetry.addData("rot about Z", JavaUtil.formatNumber(robot.returnImuHeading(),2));
//        telemetry.addData("odo Heading", JavaUtil.formatNumber(localizer.returnOdoHeading(), 2));
//        telemetry.addData("Target X", navigator.targetX);
//        telemetry.addData("Target Y", navigator.targetY);
//        telemetry.addData("Target Rot", navigator.targetRot);
//        telemetry.addData ("OdoY", localizer.encoderY);
//        telemetry.addData ("OdoXL", localizer.encoderXL);
//        telemetry.addData ("OdoXR", localizer.encoderXR);
//        telemetry.addData ("X", JavaUtil.formatNumber(localizer.xPos, 2));
//        telemetry.addData ("Y", JavaUtil.formatNumber(localizer.yPos, 2));
      Position robo = parts.apriltag.getTagRobotPosition();
      if (robo!=null) TelemetryMgr.Message(3,String.format("robotPos XYZ %6.1f %6.1f %6.1f  (inch, inch, deg)", robo.X, robo.Y, robo.R));
      else TelemetryMgr.Message(3,"robotpos - no tag position");

   }

   // Calculate loop time for performance optimization
   private double calculateLoopTime() {
      timeLoop = timerLoop.milliseconds();
      timerLoop.reset();
      return timeLoop;
   }

}
