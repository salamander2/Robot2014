package org.raiderrobotics;

import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.Joystick;

/**
 * Author: toropov023
 * Date: 09/01/2015
 */
public class QuickTurnExecutor {
    Joystick joystick;
    RobotMain main;
    Gyro gyro;

    double turnSpeed = 0.5;

    //Joystick buttons allocation
    //TODO determine the buttons
    //Joystick buttons matrix
    int[][] buttons = new int[][]{
            //Button number | Turn degree | turn for DriveTrain1 | turn for DriveTrain2
            {1, -90, -1, 1}, //90 Left
            {2, -180, -1, 1}, //180 Left
            {3, 90, 1, -1}, //90 Right
            {4, 180, 1, -1} //180 Right
    };

    //Quick turn dynamic state variables
    int buttonPressed = 0;
    boolean complete = false;

    public QuickTurnExecutor(RobotMain robotMain, Joystick joystick, Gyro gyro) {
        this.main = robotMain;
        this.joystick = joystick;
        this.gyro = gyro;
    }

    public void check() {
        for (int[] button : buttons) { //Loop through all buttons
            int id = button[0];
            if (joystick.getRawButton(id)) { //Find one that's pressed

                if(buttonPressed != id) //If first time button was pressed
                    gyro.reset(); //Reset Gyro for better results

                buttonPressed = id;
                turn(button);
                return;
            }
        }
        buttonPressed = 0;
        complete = false;
    }

    //Continue if a pressed button is found
    private void turn(int[] button){
        if(complete)
            return;

        if(button[1] - gyro.getAngle() < 0){ //If not in position yet
            main.driveTrain1.drive(button[2]*turnSpeed, 0);
            main.driveTrain2.drive(button[3]*turnSpeed, 0);
        } else { //If the rotation is complete
            complete = true;
            main.driveTrain1.drive(0, 0);
            main.driveTrain2.drive(0, 0);
        }
    }
}
