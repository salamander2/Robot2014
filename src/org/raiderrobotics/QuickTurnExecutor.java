package org.raiderrobotics;

import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.Joystick;

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
            {5, -90, 1}, //90 Left
            {3, -180, 1}, //180 Left
            {6, 90, -1}, //90 Right
            {4, 180, -1} //180 Right
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
        for(int i=0; i<buttons.length; i++){ //Loop through all buttons
            int id = buttons[i][0];
            System.out.print("Buton "+id+" : "+joystick.getRawButton(id));
            if (joystick.getRawButton(id)) { //Find one that's pressed

                if(buttonPressed != id) //If it id the first time button was pressed
                    gyro.reset(); //Reset Gyro for better results

                buttonPressed = id;
                System.out.println("Button pressed: "+buttonPressed);
                turn(buttons[i]);
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
            System.out.println("Rotating");
            main.driveTrain1.drive(button[2]*turnSpeed, 1);
            main.driveTrain2.drive(button[2]*turnSpeed, 1);
        } else { //If the rotation is complete
            System.out.println("not Rotating");
            complete = true;
            main.driveTrain1.drive(0, 0);
            main.driveTrain2.drive(0, 0);
        }
    }
}
