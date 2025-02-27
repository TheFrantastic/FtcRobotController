package ftc.teamcode.FreightFrenzy;

import com.qualcomm.hardware.lynx.LynxDcMotorController;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.MovingStatistics;

import net.frogbots.ftcopmodetunercommon.opmode.TunableLinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.Globals;
import org.firstinspires.ftc.teamcode.TrackingWheelIntegrator;
import org.firstinspires.ftc.teamcode.control.AcceleratedGain;
import org.firstinspires.ftc.teamcode.robotComponents.drivebase.SkyStoneDriveBase;
import org.firstinspires.ftc.teamcode.trajectory.AUTOLiftPreLoad;
import org.firstinspires.ftc.teamcode.trajectory.AutoLiftDownWithWait;
import org.firstinspires.ftc.teamcode.trajectory.AutoLiftUp;
import org.firstinspires.ftc.teamcode.trajectory.AutoPlaceAndStow;
import org.firstinspires.ftc.teamcode.trajectory.AutoResetOdo;
import org.firstinspires.ftc.teamcode.trajectory.AutoResetOdoWarehouse;
import org.firstinspires.ftc.teamcode.trajectory.AutoTransfer;
import org.firstinspires.ftc.teamcode.trajectory.DropMineral;
import org.firstinspires.ftc.teamcode.trajectory.IntakeOnSM;
import org.firstinspires.ftc.teamcode.trajectory.LiftPreLoad;
import org.firstinspires.ftc.teamcode.trajectory.NewStrafeToWallRed;
import org.firstinspires.ftc.teamcode.trajectory.ResetOdo;
import org.firstinspires.ftc.teamcode.trajectory.StateMPointApproach;
import org.firstinspires.ftc.teamcode.trajectory.StateMTrajectory;
import org.firstinspires.ftc.teamcode.trajectory.TurretSMAUTO;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;

import ftc.teamcode.DropPositions;

import static org.firstinspires.ftc.teamcode.Globals.ARML;
import static org.firstinspires.ftc.teamcode.Globals.ARMR;
import static org.firstinspires.ftc.teamcode.Globals.HEXCLAW;
import static org.firstinspires.ftc.teamcode.Globals.RotationI;
import static org.firstinspires.ftc.teamcode.Globals.TRANSFERGoing;
import static org.firstinspires.ftc.teamcode.Globals.TSELift;
import static org.firstinspires.ftc.teamcode.Globals.Turret;

@Autonomous(preselectTeleOp="FrogTeleOpFFred")
public class FreightFrenzyMainAutoRedStateM extends LinearOpMode {
        MovingStatistics movingStatistics = new MovingStatistics(300);

        long startLoop = 0;


        double GapHeading;
        double GapY1;
        double GapY2;
        double GapY3;
        AutoLiftUp AutoLiftUp = new AutoLiftUp();
        AUTOLiftPreLoad AUTOLiftPreLoad = new AUTOLiftPreLoad();
        AutoPlaceAndStow AutoPlaceAndStow = new AutoPlaceAndStow();
        AutoTransfer AutoTransfer = new AutoTransfer();
        AutoLiftDownWithWait AutoLiftDownWithWait = new AutoLiftDownWithWait();
        AutoResetOdo AutoResetOdo = new AutoResetOdo();
        AutoResetOdoWarehouse AutoResetOdoWarehouse = new AutoResetOdoWarehouse();
        DropMineral DropMineral = new DropMineral();
        TurretSMAUTO TurretSMAUTO = new TurretSMAUTO();
        double cmToInch = 0;
        public double inch = 0;
        //OpenCvCamera phoneCam;
        public static boolean RingStack;
        double Kp=2; //1.2
        double Ki=.01; //.008
        double Kd=2.5; //1
        //OpenCvCamera phoneCam;
        OpenCvCamera WebCam;




        //public static double TurretTARGET;
        //public static boolean TurretTurn = true;
        double integral;
        double error;
        double turnPower;
        boolean MotorGo;
        double derivative;
        double lastError;
        double RealPot;
        double targetPosition;
        double GAPAdjustment;


        TrackingWheelIntegrator trackingWheelIntegrator = new TrackingWheelIntegrator();

        static LynxDcMotorController ctrl;
        LynxModule module;

        SkyStoneDriveBase skyStoneDriveBase;

        StateMTrajectory trajectory;

        StateMTrajectory FirstMovment;
        StateMTrajectory HubToHouse;
        StateMTrajectory HouseToHub;
        StateMTrajectory HubToHouseFinal;
        StateMTrajectory LiftUp;
        StateMTrajectory LiftUpPreLoad;
        StateMTrajectory LiftDown;
        StateMTrajectory Xfer;


    DropPositions position = DropPositions.B;

        @Override
        public void runOpMode() throws InterruptedException
        {

            trackingWheelIntegrator = new TrackingWheelIntegrator();

            //Globals.FrontSonar = hardwareMap.get(MaxSonarI2CXL.class, "FrontDistance");
            module = (LynxModule) hardwareMap.get(LynxModule.class, "Expansion Hub 3");
            ctrl = hardwareMap.get(LynxDcMotorController.class, "Expansion Hub 3");
            Globals.HEXCLAW = hardwareMap.get(Servo.class, "HEXCLAW");
            TSELift = hardwareMap.get(Servo.class, "TSELift");
            Globals.ARML = hardwareMap.get(Servo.class, "ARML");
            Globals.ARMR = hardwareMap.get(Servo.class, "ARMR");
            Globals.RotationI = hardwareMap.get(Servo.class, "RotationI");
            Globals.FL = hardwareMap.get(DcMotorEx.class, "FL");
            Globals.FR = hardwareMap.get(DcMotorEx.class, "FR");
            Globals.RR = hardwareMap.get(DcMotorEx.class, "RR");
            Globals.RL = hardwareMap.get(DcMotorEx.class, "RL");
            Globals.DUCKwheel = hardwareMap.get(CRServo.class, "DUCKwheel");
            Globals.Lift = hardwareMap.get(DcMotorEx.class, "Lift");
            Globals.Intake = hardwareMap.get(DcMotor.class, "Intake");
            Globals.FrightDetector = hardwareMap.get(com.qualcomm.robotcore.hardware.DistanceSensor.class, "REVDS");
            Globals.leftTW = hardwareMap.get(DcMotorEx.class, "Intake"); // this is also left Tracking wheel
            Globals.rightTW = hardwareMap.get(DcMotorEx.class, "TSEMotor");
            Globals.backTW = hardwareMap.get(DcMotorEx.class, "Turret");
            Globals.Turret = hardwareMap.get(DcMotorEx.class, "Turret");
            Globals.potentiometer = hardwareMap.get(AnalogInput.class, "Potentiometer");
            //Globals.FrontDS = hardwareMap.get(com.qualcomm.robotcore.hardware.DistanceSensor.class, "FrontDistance");
            Globals.LiftLimit = hardwareMap.get(TouchSensor.class, "LiftLimit");

            skyStoneDriveBase = new SkyStoneDriveBase();
            skyStoneDriveBase.init(hardwareMap);
            skyStoneDriveBase.resetEncoders();
            skyStoneDriveBase.enableBrake(true);
            skyStoneDriveBase.enablePID();
            Globals.robot=skyStoneDriveBase;
            Globals.driveBase=skyStoneDriveBase;

            Turret.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
            Turret.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);

            Globals.Lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            Globals.Lift.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            Globals.Lift.setTargetPosition(1);
            Globals.Lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            Globals.Lift.setDirection(DcMotorSimple.Direction.REVERSE);


            //Globals.RightSonar = hardwareMap.get(MaxSonarI2CXL.class, "RightSonar");
            //Globals.LeftSonar = hardwareMap.get(MaxSonarI2CXL.class, "LeftSonar");
            Globals.opMode = this;
            Globals.trackingWheelIntegrator = trackingWheelIntegrator;
            Globals.odoModule = module;

            int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
            WebcamName WebCam = hardwareMap.get(WebcamName.class, "FrogVision");
            OpenCvCamera OpenWebCam = OpenCvCameraFactory.getInstance().createWebcam(WebCam, cameraMonitorViewId);


            TSEDetectionRed TSEPipline = new TSEDetectionRed();
            OpenWebCam.setPipeline(TSEPipline);
            OpenWebCam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
            {
                @Override
                public void onOpened()
                {
                    OpenWebCam.setViewportRenderingPolicy(OpenCvCamera.ViewportRenderingPolicy.OPTIMIZE_VIEW);
                    OpenWebCam.startStreaming(320, 240, OpenCvCameraRotation.SIDEWAYS_LEFT);

                }

                @Override
                public void onError(int errorCode) {

                }
            });

            //Globals.RingDetector = hardwareMap.get(com.qualcomm.robotcore.hardware.DistanceSensor.class, "REVCS");

            telemetry.setMsTransmissionInterval(20);
            //cmToInch = Globals.LeftSonar.getDistanceSync();
            //inch = cmToInch/2.54;
            //telemetry.addData("Dist inch", -inch+2);
            telemetry.addData("Position", TSEPipline.GetPosition());
            telemetry.update();

            Globals.inch = inch;

            trackingWheelIntegrator.setFirstTrackingVal(0,70);

            //TurretTARGET=1.29;
            GapHeading = -270;
            GapY1 = -.5;
            GapY2 = -.5;
            GapY3 = -.5;
            Globals.FirstMoving = true;


            clearEnc();
            position = DropPositions.C;

            while (!isStopRequested() && !isStarted()) {
                position = TSEPipline.GetPosition(); //DropPositions.C;
                telemetry.addData("Position", TSEPipline.GetPosition());
                telemetry.update();

                HEXCLAW.setPosition(0);
                ARML.setPosition(.6);
                ARMR.setPosition(.4);
                RotationI.setPosition(.6);
            }
            if (position == DropPositions.A) { //Lvl 1
                LiftPreLoad.Lvl = 1;
                Globals.LOWERARM = true;
            }
            if (position == DropPositions.B) { // Lvl 2
                LiftPreLoad.Lvl = 80;
                Globals.LOWERARM = false;
            }
            if (position == DropPositions.C) { //Lvl 3
                LiftPreLoad.Lvl = 340;
                Globals.LOWERARM = false;
            }
            Globals.HEXCLAW.setPosition(0);

            buildTrajectory();


            while (opModeIsActive()) {
                TSELift.setPosition(.5);
                Globals.TurretPos = Globals.currentVoltage;
                RealPot = Globals.TurretPos;

//                Kp = getDouble("Kp");
//                Ki = getDouble("Ki");
//                Kd = getDouble("Kd");
//                TurretTARGET = getDouble("Targetposition");
//                TurretTurn = getBoolean("MotorGo");

                if (Globals.TurretPos == Globals.TurretTARGET) {
                    Globals.TurretTurn = false;
                    Turret.setPower(0);
                }
                if (Globals.TurretTurn) {

                    if (error < 0 && lastError > 0 || error > 0 && lastError < 0) {
                        integral = 0;
                    }
                    error = Globals.TurretTARGET - RealPot;
                    integral = integral + error;
                    derivative = error - lastError;
                    turnPower = (Kp * (error) + Ki * (integral) + Kd * (derivative));
                    if (Math.abs(turnPower) > 1) {
                        integral = integral - error;
                    }
                    Globals.turnPower = turnPower;
                    if ((RealPot > 2.8 && turnPower < 0) || (RealPot < .34 && turnPower > 0)) {
                        Globals.TurretTurn = false;
                        Turret.setPower(0);
                    }
                    if (Globals.TurretTurn) {
                        Turret.setPower(turnPower);
                    }
                    lastError = error;

                    //Turret.setPower(turnPower);
                }
                else {
                    integral = 0;
                    Turret.setPower(0);
                }

                if (Globals.LiftLimit.isPressed()) {
                    Globals.Lift.setPower(0);
                    Globals.Lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                }

                Globals.updateTracking();
                if (Globals.Cycle > Globals.ResetCycle) {
                    HubToHouse.reset();
                    HouseToHub.reset();
                    AutoTransfer.reset();
                    AutoLiftDownWithWait.reset();
                    AutoResetOdo.reset();
                    AutoResetOdoWarehouse.reset();
                    Globals.ResetCycle = Globals.ResetCycle +1;
                }

                if (Globals.FirstMoving) {
                    Globals.Cycle = 1;
                    Globals.TurretTARGET = 2;
                    Globals.ResetCycle = 2;
                    FirstMovment.followInteration();
                    AUTOLiftPreLoad.runIteration(); // NEEDS TO BE RED
                    //TurretSMAUTO.runIteration();
                }

                if (Globals.Cycle == 2 && Globals.FrightDistance > 80 && !Globals.WeHaveTheGoods) {
                    Globals.TurretTurn = true;
                    Globals.TurretTARGET =1.29;
                    //GAPAdjustment = +1;
                    HubToHouse.followInteration();
                    AutoLiftDownWithWait.runIteration();
                    //AutoPlaceAndStow.runIteration();
                }
                if (!Globals.FirstMoving && Globals.FrightDistance < 80) {
                    Globals.WeHaveTheGoods = true;
                    Globals.MineralInClaw = true;
                }
                if(!Globals.FirstMoving && Globals.WeHaveTheGoods) {

                    HouseToHub.followInteration();
                    AutoTransfer.runIteration();
                    if (!TRANSFERGoing) {
                        //HEXCLAW.setPosition(0);
                        Globals.ARML.setPosition(.2);
                        Globals.ARMR.setPosition(.8);
                        Globals.Intake.setPower(0);
                        Globals.LiftTarget = 330;
                        //Globals.LiftLevel = 3;
                        Globals.Lift.setTargetPosition(330);
                        Globals.Lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        Globals.Lift.setPower(1);
                        if (Globals.LiftPos >200) {
                            Globals.TurretTurn = true;
                            Globals.TurretTARGET  = 2;
                        }
                    }

                }
                if (Globals.Cycle == 3 && Globals.FrightDistance > 80 && !Globals.WeHaveTheGoods) {
                    GapY1 = +1;
                    Globals.TurretTurn = true;
                    Globals.TurretTARGET =1.29;
                    HubToHouse.followInteration();
                    AutoLiftDownWithWait.runIteration();
                }
                if (Globals.Cycle == 4 && Globals.FrightDistance > 80 && !Globals.WeHaveTheGoods) {

                    Globals.TurretTurn = true;
                    Globals.TurretTARGET =1.29;
                    HubToHouse.followInteration();
                    AutoLiftDownWithWait.runIteration();
                }
                /*if (Globals.Cycle == 5 && Globals.FrightDistance > 80 && !Globals.WeHaveTheGoods) {

                    Globals.TurretTurn = true;
                    Globals.TurretTARGET =1.29;
                    HubToHouse.followInteration();
                    AutoLiftDownWithWait.runIteration();
                }

                 */




            }
            /*
            while (FirstMoving) {
                Globals.updateTracking();
                FirstMovment.followInteration();
                AUTOLiftPreLoad.runIteration();
            }

            while (!FirstMoving && Globals.FrightDistance > 80) {
                Globals.updateTracking();
                HubToHouse.followInteration();
                AutoPlaceAndStow.runIteration();
            }
            if (Globals.FrightDistance > 80) {
                WeHaveTheGoods = true;
            }

            while (WeHaveTheGoods) {
                Globals.updateTracking();
                HouseToHub.followInteration();
                AutoTransfer.runIteration();
                if (Globals.TRANSFERGoing = false) {
                    AutoLiftUp.runIteration();
                }
            }


             */


        }
        public static void clearEnc()        {
            ctrl.setMotorMode(3, DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            ctrl.setMotorMode(1, DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            ctrl.setMotorMode(2, DcMotor.RunMode.STOP_AND_RESET_ENCODER);


            ctrl.setMotorMode(3, DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            ctrl.setMotorMode(1, DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            ctrl.setMotorMode(2, DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        }

    public void buildTrajectory() {



        FirstMovment = new StateMTrajectory.Builder()

                .addMovement(new StateMPointApproach.Builder() //Shipping Hub movment
                        .setTargetPosition(-23.25,64)
                        .setMaxPower(.4)
                        .setXyGain(.06)
                        .setTargetHeading(0)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(1)
                        .setMovementThresh(4)
                        .setHeadingThreshold(2)
                        .stopMotorsOnDone(true)
                        .build())
               .build();


        HubToHouse = new StateMTrajectory.Builder()


                .addMovement(new StateMPointApproach.Builder() //To Gap
                        .setTargetPosition(-1,75)
                        .setMaxPower(.4)
                        .setXyGain(.06)
                        .setTargetHeading(0)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(1)
                        .setMovementThresh(5)
                        .setHeadingThreshold(2)
                        .stopMotorsOnDone(true)
                        .build())
                //.addMovement(new NewStrafeToWallRed())
                //.addMovement(new AutoResetOdo())
                .addMovement(new IntakeOnSM())
                .addMovement(new StateMPointApproach.Builder() //through Gap
                        .setTargetPosition(1,94)
                        .setMaxPower(.6)
                        .setXyGain(.06)
                        .setTargetHeading(0)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(1)
                        .setMovementThresh(8)
                        .setHeadingThreshold(5)
                        .stopMotorsOnDone(false)
                        .build())
                .addMovement(new StateMPointApproach.Builder() //To Mineral
                        .setTargetPosition(1,120)
                        .setMaxPower(.2)
                        .setXyGain(.06)
                        .setTargetHeading(0)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(1)
                        .setMovementThresh(6)
                        .setHeadingThreshold(7)
                        .stopMotorsOnDone(true)
                        .build())
                .build();


        HouseToHub = new StateMTrajectory.Builder()

                .addMovement(new StateMPointApproach.Builder() //Start OUT
                        .setTargetPosition(-1,94)
                        .setMaxPower(.5)
                        .setXyGain(.04)
                        .setTargetHeading(0)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(1)
                        .setMovementThresh(6)
                        .setHeadingThreshold(5)
                        .stopMotorsOnDone(true)
                        .build())
                //.addMovement(new NewStrafeToWallRed())
                //.addMovement(new AutoResetOdoWarehouse())
                .addMovement(new StateMPointApproach.Builder() //Out
                        .setTargetPosition(-1,64)
                        .setMaxPower(.7)
                        .setXyGain(.06)
                        .setTargetHeading(0)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(1)
                        .setMovementThresh(8)
                        .setHeadingThreshold(2)
                        .stopMotorsOnDone(true)
                        .build())
                .addMovement(new StateMPointApproach.Builder() //Shipping Hub movment
                        .setTargetPosition(-23.5,62)
                        .setMaxPower(.45)
                        .setXyGain(.05)
                        .setTargetHeading(0)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(1)
                        .setMovementThresh(2)
                        .setHeadingThreshold(2)
                        .stopMotorsOnDone(true)
                        .build())
                .addMovement(new DropMineral())


                .build();

        /*
        trajectory = new StateMTrajectory.Builder()


                .addMovement(new StateMPointApproach.Builder() //Shipping Hub movment
                        .setTargetPosition(-59,20)
                        .setMaxPower(1)
                        .setXyGain(.06)
                        .setTargetHeading(-200)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(1)
                        .setMovementThresh(3)
                        .setHeadingThreshold(5)
                        .stopMotorsOnDone(true)
                        .build())
           //Lift PreloAd
                .addMovement(new StateMPointApproach.Builder() //Shipping Hub movment
                        .setTargetPosition(-57,22)
                        .setMaxPower(0.5)
                        .setXyGain(.06)
                        .setTargetHeading(-200)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.8)
                        .setMovementThresh(1)
                        .setHeadingThreshold(2)
                        .stopMotorsOnDone(true)
                        .build())
                //.addMovement(new PreLoadPlaceMineral()) // PLACE ONE
               // .addMovement(new LiftDown())

                .addMovement(new StateMPointApproach.Builder() //ENTER FOR TWO
                        .setTargetPosition(-74,GapY1)
                        .setMaxPower(0.6)
                        .setXyGain(.06)
                        .setTargetHeading(GapHeading)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.5)
                        .setMovementThresh(3)
                        .setHeadingThreshold(1)
                        .stopMotorsOnDone(true)
                        .build())
                //.addMovement(new FFIntakeOn())

                .addMovement(new StateMPointApproach.Builder()// MOVE INTO FOR TWO
                        .setTargetPosition(-100,GapY1)
                        .setMaxPower(.6)
                        .setXyGain(.06)
                        .setTargetHeading(GapHeading+2)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.6)
                        .setMovementThresh(.5)
                        .setHeadingThreshold(2)
                        .stopMotorsOnDone(false)
                        .build())
                .addMovement(new StateMPointApproach.Builder()// MOVE SLOWLY FOR PICKUP
                        .setTargetPosition(-106,GapY1)
                        .setMaxPower(0.25)
                        .setXyGain(.06)
                        .setTargetHeading(GapHeading+2)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.6)
                        .setMovementThresh(.5)
                        .setHeadingThreshold(2)
                        .stopMotorsOnDone(true)
                        .build())
                //.addMovement(new IntakeOff())
                .addMovement(new StateMPointApproach.Builder()// BACKOUT
                        .setTargetPosition(-100,GapY1)
                        .setMaxPower(0.4)
                        .setXyGain(.06)
                        .setTargetHeading(GapHeading+2)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.5)
                        .setMovementThresh(.5)
                        .setHeadingThreshold(1)
                        .stopMotorsOnDone(false)
                        .build())
                //.addMovement(new Transfer())
                .addMovement(new StateMPointApproach.Builder()// ALL OUT
                        .setTargetPosition(-74,GapY1)
                        .setMaxPower(0.5)
                        .setXyGain(.06)
                        .setTargetHeading(-270)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.5)
                        .setMovementThresh(.5)
                        .setHeadingThreshold(1)
                        .stopMotorsOnDone(false)
                        .build())
                //.addMovement(new LiftUp())
                .addMovement(new StateMPointApproach.Builder()// GO TO HUB
                        .setTargetPosition(-57,18)
                        .setMaxPower(1)
                        .setXyGain(.06)
                        .setTargetHeading(-200)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(1)
                        .setMovementThresh(3)
                        .setHeadingThreshold(5)
                        .stopMotorsOnDone(true)
                        .build())
                .addMovement(new StateMPointApproach.Builder()// CLOSER TO HUB SLOWLY
                        .setTargetPosition(-55,20)
                        .setMaxPower(0.4)
                        .setXyGain(.06)
                        .setTargetHeading(-200)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.8)
                        .setMovementThresh(1)
                        .setHeadingThreshold(2)
                        .stopMotorsOnDone(true)
                        .build())
                .addMovement(new StateMPointApproach.Builder() //ENTER FOR THREE
                        .setTargetPosition(-74,GapY1)
                        .setMaxPower(0.6)
                        .setXyGain(.06)
                        .setTargetHeading(GapHeading)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.5)
                        .setMovementThresh(3)
                        .setHeadingThreshold(1)
                        .stopMotorsOnDone(true)
                        .build())
                //.addMovement(new FFIntakeOn())
                .addMovement(new StateMPointApproach.Builder()// MOVE INTO FOR THREE
                        .setTargetPosition(-100,GapY1)
                        .setMaxPower(.6)
                        .setXyGain(.06)
                        .setTargetHeading(GapHeading+2)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.6)
                        .setMovementThresh(.5)
                        .setHeadingThreshold(2)
                        .stopMotorsOnDone(false)
                        .build())
                .addMovement(new StateMPointApproach.Builder()// MOVE SLOWLY FOR PICKUP
                        .setTargetPosition(-106,GapY1)
                        .setMaxPower(0.25)
                        .setXyGain(.06)
                        .setTargetHeading(GapHeading+2)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.6)
                        .setMovementThresh(.5)
                        .setHeadingThreshold(2)
                        .stopMotorsOnDone(true)
                        .build())
                //.addMovement(new IntakeOff())
                .addMovement(new StateMPointApproach.Builder()// BACKOUT
                        .setTargetPosition(-100,GapY1)
                        .setMaxPower(0.4)
                        .setXyGain(.06)
                        .setTargetHeading(GapHeading+2)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.5)
                        .setMovementThresh(.5)
                        .setHeadingThreshold(1)
                        .stopMotorsOnDone(false)
                        .build())
                //.addMovement(new Transfer())
                .addMovement(new StateMPointApproach.Builder()// ALL OUT
                        .setTargetPosition(-74,GapY1)
                        .setMaxPower(0.5)
                        .setXyGain(.06)
                        .setTargetHeading(-270)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.5)
                        .setMovementThresh(.5)
                        .setHeadingThreshold(1)
                        .stopMotorsOnDone(false)
                        .build())
                //.addMovement(new LiftUp())
                .addMovement(new StateMPointApproach.Builder()// GO TO HUB
                        .setTargetPosition(-57,18)
                        .setMaxPower(1)
                        .setXyGain(.06)
                        .setTargetHeading(-200)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(1)
                        .setMovementThresh(3)
                        .setHeadingThreshold(5)
                        .stopMotorsOnDone(true)
                        .build())
                .addMovement(new StateMPointApproach.Builder()// CLOSER TO HUB SLOWLY
                        .setTargetPosition(-55,20)
                        .setMaxPower(0.4)
                        .setXyGain(.06)
                        .setTargetHeading(-200)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.8)
                        .setMovementThresh(1)
                        .setHeadingThreshold(2)
                        .stopMotorsOnDone(true)
                        .build())
                .addMovement(new StateMPointApproach.Builder() //ENTER FOR PARK
                        .setTargetPosition(-74,GapY1)
                        .setMaxPower(0.8)
                        .setXyGain(.06)
                        .setTargetHeading(GapHeading)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.5)
                        .setMovementThresh(3)
                        .setHeadingThreshold(1)
                        .stopMotorsOnDone(true)
                        .build())
                //.addMovement(new FFIntakeOn())
                .addMovement(new StateMPointApproach.Builder()// MOVE INTO FOR PARK
                        .setTargetPosition(-100,GapY1)
                        .setMaxPower(.6)
                        .setXyGain(.06)
                        .setTargetHeading(GapHeading+2)
                        .setHeadingDynamicGain(new AcceleratedGain(.012, 0.0004))
                        .setMaxTurnPower(0.6)
                        .setMovementThresh(.5)
                        .setHeadingThreshold(2)
                        .stopMotorsOnDone(false)
                        .build())



                .build();

         */
    }


}



/*.addMovement(new StateMWaypoint.Builder() // First Stright movment to Target position
                        .setTargetPosition(25, 37)
                        .setTargetHeading(-40)
                        .setSpeed(1)
                        .setTransThreshMethod(StateMWaypoint.TranslationThreshMethod.Y_ONLY)
                        .setMovementThresh(1)
                        .setHeadingThreshold(0)
                        .build())

 */
