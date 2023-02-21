package com.example.algorithm.Bluetooth;

public class HeadAndBody {
    public static final int HEAD_UP = 0 ;
    public static final int HEAD_DOWN = 1;
    public static final int HEAD_LEFT = 2 ;
    public static final int HEAD_RIGHT = 3;
    public static final int HEAD_LEAN_LEFT = 4;
    public static final int HEAD_LEAN_RIGHT = 5;
    public static final int HEAD_UP_TWICE = 6 ;
    public static final int HEAD_DOWN_TWICE = 7 ;
    public static final int HEAD_LEFT_TWICE = 8 ;
    public static final int HEAD_RIGHT_TWICE = 9 ;
    public static final int HEAD_LEFT_RIGHT = 10 ;
    public static final int HEAD_RIGHT_LEFT = 11 ;
    public static final int BODY_DOWNSTAIRS = 0 ;
    public static final int BODY_UPSTAIRS = 1 ;
    public static final int BODY_STILL = 2 ;
    public static final int BODY_WALK = 3;
    private static final String []HEAD = {"Up","DOWN","LEFT",
                                        "RIGHT","LEAN_LEFT","LEAN_RIGHT",
                                        "UP_TWICE","DOWN_TWICE","LEFT_TWICE",
                                        "RIGHT_TWICE","LEFT_RIGHT","RIGHT_LEFT",} ;
    public static final String []BODY ={"DOWNSTAIRS","UPSTAIRS","STILL","WALK","jog"} ;

    /**
     * This function is to return the head motion name of this code.
     * @param actionCode HeadAndBody's class member
     * @return the name of this action code
     */
    public static String getHeadAction(int actionCode){
        return HEAD[actionCode];
    }
    /**
     * This function is to return the body motion condition name of this code.
     * @param actionCode HeadAndBody's class member
     * @return the name of this action code
     */
    public static String getBodyAction(int actionCode){
        return BODY[actionCode];

    }
}
