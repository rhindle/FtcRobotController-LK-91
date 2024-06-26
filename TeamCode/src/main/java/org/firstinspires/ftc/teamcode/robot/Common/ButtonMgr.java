package org.firstinspires.ftc.teamcode.robot.Common;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Gamepad;

public class ButtonMgr {

    public LinearOpMode opMode;
    public Gamepad gamepad1;
    public Gamepad gamepad2;

    public ControlData[] controlData;

    int tapTime = 500;

    ////////////////
    //constructors//
    ////////////////
    public ButtonMgr(LinearOpMode opMode){
        construct(opMode);
    }

    void construct(LinearOpMode opMode){
        this.opMode = opMode;
        this.gamepad1 = opMode.gamepad1;
        this.gamepad2 = opMode.gamepad2;

        //allocate for # objects based on GPbuttons enum
        controlData = new ControlData[Buttons.values().length * 2];
        //create objects and assign index numbers
        for (int i = 0; i < Buttons.values().length * 2; i++) {
            controlData[i] = new ControlData();
            controlData[i].initData(i);
        }
    }

    public void runLoop() {
        updateAll();
    }

    public void updateAll()
    {
        for (ControlData i : controlData) {
            i.update();
        }
    }

    public boolean wasPressed(cButton ctrlButton) {
        return wasPressed(ctrlButton.controller, ctrlButton.button);
    }
    public boolean wasPressed(int controller, Buttons button) {
        return controlData[getIndex(controller, button)].wasPressed;
    }

    public boolean wasReleased(cButton ctrlButton) {
        return wasReleased(ctrlButton.controller, ctrlButton.button);
    }
    public boolean wasReleased(int controller, Buttons button) {
        return controlData[getIndex(controller, button)].wasReleased;
    }

    public boolean wasTapped(cButton ctrlButton) {
        return wasTapped(ctrlButton.controller, ctrlButton.button);
    }
    public boolean wasTapped(int controller, Buttons button) {
        return controlData[getIndex(controller, button)].wasTapped;
    }

    public boolean isHeld(cButton ctrlButton) {
        return isHeld(ctrlButton.controller, ctrlButton.button);
    }
    public boolean isHeld(int controller, Buttons button) {
        return controlData[getIndex(controller, button)].isHeld;
    }

    public boolean isPressed(cButton ctrlButton) {
        return isPressed(ctrlButton.controller, ctrlButton.button);
    }
    public boolean isPressed(int controller, Buttons button) {
        return controlData[getIndex(controller, button)].lastStatus;
    }

    public boolean wasSingleTapped(cButton ctrlButton) {
        return wasSingleTapped(ctrlButton.controller, ctrlButton.button);
    }
    public boolean wasSingleTapped(int controller, Buttons button) {
        return controlData[getIndex(controller, button)].wasSingleTapped;
    }

    public boolean wasDoubleTapped(cButton ctrlButton) {
        return wasDoubleTapped(ctrlButton.controller, ctrlButton.button);
    }
    public boolean wasDoubleTapped(int controller, Buttons button) {
        return controlData[getIndex(controller, button)].wasDoubleTapped;
    }

    public boolean wasHeld(cButton ctrlButton) {
        return wasHeld(ctrlButton.controller, ctrlButton.button);
    }
    public boolean wasHeld(int controller, Buttons button) {
        return controlData[getIndex(controller, button)].wasHeld;
    }

    public boolean isSingleTapHeld(cButton ctrlButton) {
        return isSingleTapHeld(ctrlButton.controller, ctrlButton.button);
    }
    public boolean isSingleTapHeld(int controller, Buttons button) {
        return controlData[getIndex(controller, button)].isSingleTapHeld;
    }

    int getIndex(int controller, Buttons button){
        //This converts an index of 0-27 based on the controller 1-2 and button 0-13
        if (controller < 1 || controller > 2) controller = 0; else controller--;
        return controller * Buttons.values().length + button.ordinal();
    }

    ControlData getAllData(int controller, Buttons button) {
        return controlData[getIndex(controller, button)];
    }

    public enum Buttons {  //must match what is in getReading's switch block
        dpad_up,
        dpad_down,
        dpad_left,
        dpad_right,
        a,
        b,
        x,
        y,
        start,
        back,
        left_bumper,
        right_bumper,
        left_stick_button,
        right_stick_button;
    }

    public static class cButton {
        int controller;
        Buttons button;
        public cButton(int controller, Buttons button){
            this.controller = controller;
            this.button = button;
        }
    }

    class ControlData {
        int index;
        Buttons name;
        boolean lastStatus;
        long lastTime;
        boolean wasTapped;
        boolean isHeld;
        boolean wasHeld;
        boolean wasPressed;
        boolean wasReleased;
        boolean wasSingleTapped;
        boolean wasDoubleTapped;
        boolean isSingleTapHeld;
        char tapEventCounter;

        public void initData(int index)  //object
        {
            this.index = index;
            name = Buttons.values()[index % Buttons.values().length];
            lastStatus = false;
            lastTime = System.currentTimeMillis();
            wasTapped = false;
            isHeld = false;
            wasHeld = false;
            wasPressed = false;
            wasReleased = false;
            wasSingleTapped = false;
            wasDoubleTapped = false;
            isSingleTapHeld = false;
            tapEventCounter = 0;
        }

        boolean getReading(int index)
        {
            Gamepad gpad;
            if (index >= Buttons.values().length) {
                index -= Buttons.values().length;
                gpad = gamepad2;
            } else {
                gpad = gamepad1;
            }
            switch (Buttons.values()[index]) {
                //must match the elements in the GPbuttons enum
                case dpad_up:             return gpad.dpad_up;
                case dpad_down:           return gpad.dpad_down;
                case dpad_left:           return gpad.dpad_left;
                case dpad_right:          return gpad.dpad_right;
                case a:                   return gpad.a;
                case b:                   return gpad.b;
                case x:                   return gpad.x;
                case y:                   return gpad.y;
                case start:               return gpad.start;
                case back:                return gpad.back;
                case left_bumper:         return gpad.left_bumper;
                case right_bumper:        return gpad.right_bumper;
                case left_stick_button:   return gpad.left_stick_button;
                case right_stick_button:  return gpad.right_stick_button;
                default:                  return false;  //something bad happened
            }
        }

        public void update()
        {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastTime;
            boolean currentState = getReading(index);

            // clear all transient events
            wasPressed = false;
            wasReleased = false;
            wasHeld = false;
            wasTapped = false;
            wasSingleTapped = false;
            wasDoubleTapped = false;
            isHeld = false;

            if (!lastStatus && currentState) {   // change from not pressed to pressed
                wasPressed = true;               // this will last for one loop!
                if (deltaTime < tapTime) {       // indicates released for < tapTime
                    if (tapEventCounter != 0) tapEventCounter++;
                } else {
                    tapEventCounter = 0;
                }
                lastTime = currentTime;          // reset the time
            } else {
                //nothing
            }
            if (lastStatus && !currentState) {   // change from pressed to not pressed
                wasReleased = true;              // this will last for one loop!
                isSingleTapHeld = false;
                if (deltaTime < tapTime) {
                    wasTapped = true;
                    tapEventCounter++;
                    if (tapEventCounter == 3) {
                        wasDoubleTapped = true;
                        tapEventCounter = 0;
                    }
                } else {
                    wasHeld = true;
                    tapEventCounter = 0;
                }
                lastTime = currentTime;          // reset the time
            } else {
                //nothing
            }
            if (lastStatus && currentState) {    // still held
                if (deltaTime >= tapTime) {
                    isHeld = true;
                    if (tapEventCounter == 2) {
                        isSingleTapHeld = true;  // will reset when released
                    }
                    tapEventCounter = 0;
                }
            }
            if (!lastStatus && !currentState) {  // still not held
                if (deltaTime >= tapTime) {
                    if (tapEventCounter == 1) {
                        wasSingleTapped = true;
                    }
                    tapEventCounter = 0;
                }
            }
            lastStatus = currentState;
        }
    }
}
