/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package org.raiderrobotics;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
/***************************************************************************
The two left motors are connected to victors on ports 1 and 3
The two right motors are connected for victors on ports 2 and 4
*****************************************************************************/

public class RobotMain extends IterativeRobot {
    //Arm static variables
    final static double ARM_MAX = 0.5;
    final static double ARM_JOYSTICK_MIN = 0.1;
    final static double ARM_FREEZE = 0.15;


    final static int ARCADE = 1;
    final static int TANK = 2;

    //create object references
    Joystick leftStick, rightStick;
    RobotDrive driveTrain1, driveTrain2;
    Victor victor1, victor2, victor3, victor4;
    Jaguar armJaguar;
    DigitalInput limitSwitch;
    DigitalInput armSensor;
    //the JoystickButton class does not exist in our Java FRC plugins!
    // JoystickButton stickLBtn1, stickLBtn2; 

    //global variables
    private int driveState = ARCADE;

    boolean xboxControl;
    Preferences prefs;    
    //create global objects here
    public void robotInit() {
        victor1 = new Victor(1);
        victor2 = new Victor(2);
        victor3 = new Victor(3);
        victor4 = new Victor(4);
        armJaguar = new Jaguar(10); //for the arm
        /*** do the following lines do anything? 
        victor1.enableDeadbandElimination(true);
        victor2.enableDeadbandElimination(true);
        victor3.enableDeadbandElimination(true);
        victor4.enableDeadbandElimination(true);
        armJaguar.enableDeadbandElimination(true);
        ***/
        
        driveTrain1 = new RobotDrive(victor1, victor2);
        driveTrain2 = new RobotDrive(victor3, victor4);
        
        leftStick = new Joystick(1);
        rightStick = new Joystick(2);
        //stickLBtn1 = new JoystickButton(stickL, 1);
        //stickLBtn2 = new JoystickButton(stickL, 2);
        limitSwitch = new DigitalInput(5);
        xboxControl = prefs.getBoolean("Xbox Control Mechanism",false);
        armSensor = new DigitalInput(10);
    }

    public void teleopInit() {
        Watchdog.getInstance().feed();
    }

    // called at 50Hz (every 20ms). This method must not take more than 20ms to complete!
    public void teleopPeriodic() {
        // feed the watchdog
        Watchdog.getInstance().feed();

        normalDrive();
        moveArm();
//        checkArmSensor(); //Remove for now
        
        //publicDrive();
        sendSmartDashboard();

        //check for button press to switch mode. Use two buttons to prevent bounce.
        //if (joyLeftBtn2.get()) driveState = ARCADE;
        //if (joyLeftBtn3.get()) driveState = TANK;
        //Since joystick button is not loaded ....
        boolean button2 = leftStick.getRawButton(2);
        boolean button3 = leftStick.getRawButton(3);
        if (button2) driveState = ARCADE;
        if (button3) driveState = TANK;
    }

    public void autonomousInit() {
        //chassis.setSafetyEnabled(false); // or better yet: feed the watchdog regularly
    }

    public void autonomousPeriodic() {
        sendSmartDashboard();
    }

    public void autonomousDisabled() {
        //turn off motors here
    }
    
    // drive the robot normally
    private void normalDrive() {
        if (driveState == ARCADE && xboxControl == false) {
            driveTrain1.arcadeDrive(leftStick);
            driveTrain2.arcadeDrive(leftStick);
        } else if(driveState==TANK && xboxControl == false) {
            driveTrain1.tankDrive(leftStick, rightStick);
            driveTrain2.tankDrive(leftStick, rightStick);
        }else if (driveState == ARCADE && xboxControl == true) {
            driveTrain1.arcadeDrive(leftStick.getRawAxis(2),leftStick.getRawAxis(1),true);
            driveTrain2.arcadeDrive(leftStick.getRawAxis(2),leftStick.getRawAxis(1),true);
        } else if(driveState==TANK && xboxControl == true) {
            driveTrain1.tankDrive(leftStick.getRawAxis(2), leftStick.getRawAxis(5));
            driveTrain2.tankDrive(leftStick.getRawAxis(2), leftStick.getRawAxis(5));
        }
        /*Raw Axis Index for Xbox are as follows :
			1 - LeftX
			2 - LeftY
			3 - Triggers (Each trigger = 0 to 1, axis value = right - left)
			4 - RightX
			5 - RightY
			6 - DPad Left/Right
			The code above checks which mode the robot is in, and if there is xbox control. 
			It proceeds as normal if there is no xbox control
			However, if there is xbox control, the arcade drives are made using raw double values instead of a joystick class. 
			These values are gotten using get raw axis on the left stick.
       */
    }

    private void moveArm(){
        if(driveState != TANK && rightStick.getY() > ARM_JOYSTICK_MIN){ //Check if in the correct Drive State and the joystick is not at rest
            if(!limitSwitch.get()) //If it is touching the limit switch (i.e. At it's max height)
                armJaguar.set(ARM_FREEZE); //Set it to a small current so that it doesn't fall down
            else
                armJaguar.set(rightStick.getY() >= ARM_MAX ? ARM_MAX : rightStick.getY()); //Move the arm at the speed between 0.1 and 0.5
        } else
            armJaguar.set(0.0); //Let it fall down freely
    }
    
    //Testing the sensor
    private void checkArmSensor(){
        System.out.println("Arm Sensor: "+armSensor.get());
    }
    
    // square the inputs (while preserving the sign) to increase fine control while permitting full power
    double squareInputs(double input) {
        return Math.abs(input) * input;
    }
    
    //limit values so that they are always between -1.0 and +1.0
    public double limit(double num) {
        if (num > 1.0) return 1.0;
        if (num < -1.0) return -1.0;
        return num;
    }
    
    public void sendSmartDashboard(){
        //smartDashboardSetup-CURRENTLY UNTESTED
        SmartDashboard.putDouble("joyRx", rightStick.getX());
        SmartDashboard.putDouble("joyLx", leftStick.getX());
        SmartDashboard.putDouble("joyRy", rightStick.getY());
        SmartDashboard.putDouble("joyLy", leftStick.getY());
        SmartDashboard.putBoolean("btn2",leftStick.getRawButton(2));
        SmartDashboard.putBoolean("btn3",leftStick.getRawButton(3));
        
        SmartDashboard.putDouble("vic1",victor1.get());
        SmartDashboard.putDouble("vic2",victor2.get());
        SmartDashboard.putDouble("vic3",victor3.get());
        SmartDashboard.putDouble("vic4",victor4.get());
        SmartDashboard.putDouble("jagArm",jag1.get());
        if(driveState==ARCADE){    
            SmartDashboard.putString("driveState","Arcade");
        }else{
            SmartDashboard.putString("driveState","Tank Drive");
        }
        SmartDashboard.putBoolean("limitSwitch",limitSwitch.get());
    }
        SmartDashboard.putDouble("jagArm",armJaguar.get());
}
