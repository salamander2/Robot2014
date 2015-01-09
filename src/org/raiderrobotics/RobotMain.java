/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package org.raiderrobotics;

import edu.wpi.first.wpilibj.*;

/**
 * ************************************************************************
 * The two left motors are connected to victors on ports 1 and 3
 * The two right motors are connected for victors on ports 2 and 4
 * ***************************************************************************
 */

public class RobotMain extends IterativeRobot {
    //Arm static variables
    final static double ARM_JOYSTICK_MAX = 0.5;
    final static double ARM_JOYSTICK_MIN = 0.1;
    final static double ARM_FREEZE = 0.15;

    //Game state variables
    final static int ARCADE = 1;
    final static int TANK = 2;

    //create object references
    Joystick leftStick, rightStick; //The two joystick
    RobotDrive driveTrain1, driveTrain2; //Robot drive train
    Victor victor1, victor2, victor3, victor4; //Chassis drive Victors
    Jaguar armJaguar; //Arm jaguar
    DigitalInput limitSwitch; //Limit switch on the arm
    DigitalInput armSensor; //Arm motion sensor

    //global variables
    private int driveState = ARCADE;

    //Joystick buttons allocation
    //TODO determine the buttons
    int arcadeSwitchButton = 0;
    int tankSwitchButton = 0;

    //Quick turn class
    QuickTurnExecutor quickTurnExecutor;

    //create global objects here
    public void robotInit() {
        victor1 = new Victor(1);
        victor2 = new Victor(2);
        victor3 = new Victor(3);
        victor4 = new Victor(4);
        armJaguar = new Jaguar(10);

        driveTrain1 = new RobotDrive(victor2, victor1);
        driveTrain2 = new RobotDrive(victor4, victor3);

        leftStick = new Joystick(2);
        rightStick = new Joystick(1);
        limitSwitch = new DigitalInput(5);
        armSensor = new DigitalInput(10);

        //TODO determine on what stick that is (left or right)
        quickTurnExecutor = new QuickTurnExecutor(this, rightStick, new Gyro(new AnalogChannel(2)));
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

        //Check for button press to switch mode. Use two buttons to prevent bounce.
        if (leftStick.getRawButton(arcadeSwitchButton)) driveState = ARCADE;
        if (leftStick.getRawButton(tankSwitchButton)) driveState = TANK;
    }

    public void autonomousInit() {
        //chassis.setSafetyEnabled(false); // or better yet: feed the watchdog regularly
    }

    public void autonomousPeriodic() {

    }

    public void autonomousDisabled() {
        //turn off motors here
    }

    //Drive the robot normally
    private void normalDrive() {
        if (driveState == ARCADE) {
            driveTrain1.arcadeDrive(leftStick);
            driveTrain2.arcadeDrive(leftStick);
        } else {
            driveTrain1.tankDrive(leftStick, rightStick);
            driveTrain2.tankDrive(leftStick, rightStick);
        }

        //Check for quick turns (Add this to future publicDrive() if desired)
        quickTurnExecutor.check();
    }

    private void moveArm() {
        if (driveState != TANK && rightStick.getY() > ARM_JOYSTICK_MIN) { //Check if in the correct Drive State and the joystick is not at rest
            if (!limitSwitch.get()) //If it is touching the limit switch (i.e. At it's max height)
                armJaguar.set(ARM_FREEZE); //Set it to a small current so that it doesn't fall down
            else
                armJaguar.set(rightStick.getY() >= ARM_JOYSTICK_MAX ? ARM_JOYSTICK_MAX : rightStick.getY()); //Move the arm at the speed between 0.1 and 0.5
        } else
            armJaguar.set(0.0); //Let it fall down freely
    }

    /**
     * Testing the sensor
     *
     * @deprecated The sensor is not used in any way, might consider the removal
     */
    @Deprecated
    private void checkArmSensor() {
        System.out.println("Arm Sensor: " + armSensor.get());
    }

    //Square the inputs (while preserving the sign) to increase fine control while permitting full power
    double squareInputs(double input) {
        return Math.abs(input) * input;
    }

    //Limit values so that they are always between -1.0 and +1.0
    public double limit(double num) {
        return num > 1.0 ? 1.0 : (num < -1.0 ? -1.0 : num);
    }
}
