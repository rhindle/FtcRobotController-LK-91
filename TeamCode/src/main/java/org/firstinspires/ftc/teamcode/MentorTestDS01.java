package org.firstinspires.ftc.teamcode;

import android.annotation.SuppressLint;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.JavaUtil;
import org.firstinspires.ftc.teamcode.RobotParts.Common.ButtonMgr;
import org.firstinspires.ftc.teamcode.RobotParts.Common.Parts;
import org.firstinspires.ftc.teamcode.RobotParts.Common.TelemetryMgr;
import org.firstinspires.ftc.teamcode.RobotParts.Common.TelemetryMgr.Category;
import org.firstinspires.ftc.teamcode.RobotParts.DiscShooter.PartsDS;
import org.firstinspires.ftc.teamcode.Tools.DataTypes.Position;
import org.firstinspires.ftc.teamcode.Tools.Functions;

@TeleOp(name = "AA_MentorTest_DS01", group = "")
//@Disabled
public class MentorTestDS01 extends LinearOpMode {

   public Parts parts;

   @Override
   public void runOpMode() {

      parts = new PartsDS(this);

      parts.useODO = false;
      parts.useSlamra = true;
      parts.useNeoMatrix = true;
      parts.useAprilTag = true;
      parts.useDrivetrainEncoders = true;
      parts.reverseDrive = false;
      parts.useDistanceSensors = false;
      parts.odoRobotOffset = new Position (2.25,0,0);  // if this is inherent to the robot, should it be in PartsDS?
      parts.slamraRobotOffset = new Position(-8,-1,0);
      parts.speedMaximum = 1;

      parts.setup();
      parts.preInit();

      TelemetryMgr.setDebugLevel(10);
//      TelemetryMgr.enableCategories(new TelemetryMgr.Category[] {Category.AUTODRIVE});
      TelemetryMgr.enableAllCategories();

      /* Init Loop */
      while (!isStarted()) {
         TelemetryMgr.message(Category.MANDATORY, ">", "Press Play to start");
         TelemetryMgr.message(Category.MANDATORY, ">", "Robot Heading = %.1f", parts.imuMgr.returnImuHeadingRaw(true));
         TelemetryMgr.message(Category.MANDATORY, "Drive Type:", parts.reverseDrive ? "AndyMark" : "GobildaBot");

         parts.initLoop();

         if (parts.buttonMgr.wasTapped(1, ButtonMgr.Buttons.x))
            parts.reverseDrive = !parts.reverseDrive;
         if (parts.buttonMgr.wasTapped(2, ButtonMgr.Buttons.x))
            parts.reverseDrive = !parts.reverseDrive;

         TelemetryMgr.Update(true);
         sleep(20);
      }

      parts.preRun();

      /* Run Loop */
      if (opModeIsActive()) {
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
      TelemetryMgr.message(Category.BASIC, "Loop time (ms)", JavaUtil.formatNumber(Functions.calculateLoopTime(), 0));
      TelemetryMgr.message(Category.BASIC, "IMU raw heading", JavaUtil.formatNumber(parts.imuMgr.returnImuHeadingRaw(),2));
      if (parts.useODO) parts.odometry.addTeleOpTelemetry();
   }

   @SuppressLint("DefaultLocale")
   private void addTelemetryLoopEnd() {
      TelemetryMgr.message(Category.CONTROLS, "r (magnitude)", parts.controls.driveData.driveSpeed);
      TelemetryMgr.message(Category.CONTROLS, "robotAngle", parts.controls.driveData.driveAngle);
      TelemetryMgr.message(Category.CONTROLS, "rotate", parts.controls.driveData.rotate);
      TelemetryMgr.message(Category.USERDRIVE, "storedHeading", JavaUtil.formatNumber(parts.userDrive.storedHeading, 2));
      TelemetryMgr.message(Category.USERDRIVE, "deltaHeading", JavaUtil.formatNumber(parts.userDrive.deltaHeading, 2));
      TelemetryMgr.message(Category.IMU, "IMU-Modified", JavaUtil.formatNumber(parts.imuMgr.returnImuRobotHeading(),2));
      Position robo = parts.dsApriltag.getTagRobotPosition();
      if (robo!=null) TelemetryMgr.message(Category.APRILTAG, String.format("robotPos XYZ %6.1f %6.1f %6.1f  (inch, inch, deg)", robo.X, robo.Y, robo.R));
      else TelemetryMgr.message(Category.APRILTAG,"robotpos - no tag position");
   }
}
