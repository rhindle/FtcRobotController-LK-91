package org.firstinspires.ftc.teamcode.robot.Common;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.robot.Common.Tools.Functions;
import org.firstinspires.ftc.teamcode.robot.Common.Tools.Position;

public class Odometry {

   public Parts parts;
   Telemetry telemetry;

   public DcMotorEx odoXL, odoY, odoXR;
   public byte odoXLdir, odoYdir, odoXRdir;
   public long encoderY, encoderXL, encoderXR;
   long encoderY0, encoderXL0, encoderXR0;
   double odoHeading, odoHeading0;
   double imuHeading, imuHeading0;
   public double globalHeading, globalHeading0;
   public double yPos, xPos;
   public boolean useFusedHeading = true;

//   public Position odoRobotOffset = new Position (2.25,0,0);          // map odo to robot (so it holds turn position better)
//   public Position odoFieldStart = new Position (-36,63,-90);  // field start position [blue right slot]
   public Position odoRobotOffset = new Position();                     // map odo to robot (so it holds turn position better)
   public Position odoFieldStart = new Position();                      // field start position [blue right slot]

   Position odoRawPose = new Position (0,0,0);                 // original calculation of position before transforms applied
   Position odoRobotPose = new Position ();                             // odo mapped to robot position (minor change)
   Position odoFinalPose = new Position ();                             // odo mapped to field
   Position odoFieldOffset = new Position ();                           // transform from initial position (more relevant for slamra!)
   public Position robotPosition; // = odoFieldStart.clone();

   private static final double eTicksPerInch = 82300 / 48.0;
   private static final double eTicksPerRotate = 169619;
   //2022-12-22 Measured 10 rot: 846684,850800; 845875,851775; 845127,845543; 848073,850867
   //                            169748.4		169765		169067		169894		==>  169618.6

   public Odometry(Parts parts){
      construct(parts);
   }

   void construct(Parts parts){
      this.parts = parts;
      this.telemetry = parts.opMode.telemetry;
   }

   public void init() {
      robotPosition = odoFieldStart.clone();
      parts.robotPosition = robotPosition.clone();
      if (!parts.useODO) {
         robotPosition = new Position(0,0, robotPosition.R);
         parts.robotPosition = robotPosition.clone();
         return;
      }
      configureEncoders();

      encoderY0 = odoY.getCurrentPosition() * (long)odoYdir;
      encoderXL0 = odoXL.getCurrentPosition() * (long)odoXLdir;
      encoderXR0 = odoXR.getCurrentPosition() * (long)odoXRdir;
      imuHeading0 = parts.robot.returnImuHeading(true);
      odoHeading0 = getOdoHeading();
      globalHeading0 = imuHeading0;

      // odo start position is 0,0,0; imu should also read 0.  odoRawPose is already 0,0,0
      updateOdoRobotPose();
      setOdoFinalPose();
      setOdoFieldOffset();
   }

   public void loop() {
      if (!parts.useODO) {
         imuHeading = parts.robot.returnImuHeading();
         globalHeading = imuHeading;
         robotPosition.R = globalHeading;
         parts.robotPosition = robotPosition.clone();
         return;
      }

      /* Update encoder readings */
      encoderY = odoY.getCurrentPosition() * (long)odoYdir;
      encoderXL = odoXL.getCurrentPosition() * (long)odoXLdir;
      encoderXR = odoXR.getCurrentPosition() * (long)odoXRdir;

      /* Update heading */
      imuHeading = parts.robot.returnImuHeading();
      odoHeading = getOdoHeading();
      globalHeading = fusedHeading();

      /* Calculate position */
      updateXY();
      odoRawPose = new Position(xPos, yPos, globalHeading);
      updateOdoRobotPose();
      setOdoFinalPose();

      /* Update robot position */
      robotPosition = odoFinalPose.clone();
      parts.robotPosition = robotPosition.clone();

   }

   private double fusedHeading() {
      /* Don't fuse if the flag isn't set */
      if (!useFusedHeading) return imuHeading;
      /* Use imuHeading only if it's settled */
      if (Math.abs(Functions.normalizeAngle(imuHeading - imuHeading0)) < 0.5) return imuHeading;
      /* Otherwise fuse it with odoHeading data */
      return Functions.normalizeAngle(globalHeading0 + (odoHeading - odoHeading0));
   }

   public void toggleUseFusedHeading() {
      useFusedHeading = !useFusedHeading;
   }

   /* Get heading from the odometry ... accuracy varies :-(  */
   private double getOdoHeading() {
      double diffX;
      diffX = encoderXR - encoderXL;
      diffX = diffX % eTicksPerRotate;
      diffX = diffX / eTicksPerRotate * 360;
      return Functions.normalizeAngle(diffX);
   }

   public double returnOdoHeading() {
      return odoHeading;
   }

   public double returnGlobalHeading() {
      return globalHeading;
   }

   /* Get XY position data from odometry wheels */
   private void updateXY () {
      // this function could use some cleanup!
      double deltaEncX, deltaEncY;
      double myHeading;

      //deltaEncX = (encoderXL - encoderXL0) / eTicksPerInch;  //for single encoder; no heading would be possible
      deltaEncX = (encoderXL + encoderXR - encoderXL0 - encoderXR0) / 2.0 / eTicksPerInch;
      deltaEncY = (encoderY - encoderY0) / eTicksPerInch;

      myHeading = getAvgHeading(globalHeading0, globalHeading);

      telemetry.addData ("My Average Heading", myHeading);

      xPos = xPos + deltaEncX * Math.cos(Math.toRadians(myHeading));
      yPos = yPos + deltaEncX * Math.sin(Math.toRadians(myHeading));

      xPos = xPos + deltaEncY * Math.sin(Math.toRadians(myHeading));
      yPos = yPos - deltaEncY * Math.cos(Math.toRadians(myHeading));

      /* Store current values for next loop */
      encoderXL0 = encoderXL;
      encoderY0 = encoderY;
      encoderXR0 = encoderXR;
      imuHeading0 = imuHeading;
      odoHeading0 = odoHeading;
      globalHeading0 = globalHeading;
   }

   /* Calculate average of two headings */
   public double getAvgHeading (double firstHeading, double secondHeading) {
      double robotHeading;
      /* Find the difference between them; based on sampling rate, assume large values wrapped */
      robotHeading = Functions.normalizeAngle(secondHeading - firstHeading);
      robotHeading /= 2;
      robotHeading += firstHeading;
      return Functions.normalizeAngle(robotHeading);
   }

   void updateOdoRobotPose() {
      //pos1 = odoRawPose, pos2 = odoRobotOffset
      odoRobotPose = transformPosition(odoRawPose, odoRobotOffset);
   }

   void setOdoFinalPose() {
      //pos1 = odoFieldOffset, pos2 = odoRobotPose
      odoFinalPose = transformPosition(odoFieldOffset, odoRobotPose);
      odoFinalPose.normalize();
   }

   void setOdoFieldOffset() {
      Position fS = odoFieldStart;
      Position rP = odoRobotPose;
      double offsetR = fS.R - rP.R;
      odoFieldOffset = new Position (
              fS.X - (rP.X*Math.cos(Math.toRadians(offsetR)) - rP.Y*Math.sin(Math.toRadians(offsetR))),
              fS.Y - (rP.X*Math.sin(Math.toRadians(offsetR)) + rP.Y*Math.cos(Math.toRadians(offsetR))),
              offsetR *1
      );
   }

   Position transformPosition(Position pos1, Position pos2) {
      return new Position(
              pos1.X + (pos2.X*Math.cos(Math.toRadians(pos1.R)) - pos2.Y*Math.sin(Math.toRadians(pos1.R))),
              pos1.Y + (pos2.X*Math.sin(Math.toRadians(pos1.R)) + pos2.Y*Math.cos(Math.toRadians(pos1.R))),
              pos1.R + pos2.R
      );
   }

   public void addTeleOpTelemetry() {
      telemetry.addData("raw__", odoRawPose.toString(2));
      telemetry.addData("robot", odoRobotPose.toString(2));
      telemetry.addData("final", odoFinalPose.toString(2));
   }

   public void configureEncoders() {
//      /* this is separated so it can be overridden */
//      odoY = parts.robot.motor0B;
//      odoXR = parts.robot.motor1B;
//      odoXL = parts.robot.motor2B;
//      odoY.setDirection(DcMotorEx.Direction.FORWARD);
//      odoXL.setDirection(DcMotorEx.Direction.REVERSE);
//      odoXR.setDirection(DcMotorEx.Direction.REVERSE);
//      odoY.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
//      odoXL.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
//      odoXR.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
//      /* dir should be -1 or 1 and is to account for the port being used elsewhere
//         and needing to be set in direction opposite to what odometry needs */
//      odoYdir = 1;
//      odoXRdir = 1;
//      odoXLdir = 1;
   }
}