package com.example.algorithm.ControlCenter;

import android.content.Context;

public interface Controller {
    int HEAD_UP = 0 ;
    int HEAD_DOWN = 1;
    int HEAD_LEFT = 2 ;
    int HEAD_RIGHT = 3;
    int HEAD_LEAN_LEFT = 4;
    int HEAD_LEAN_RIGHT = 5;
    int HEAD_UP_TWICE = 6 ;
    int HEAD_DOWN_TWICE = 7 ;
    int HEAD_LEFT_TWICE = 8 ;
    int HEAD_RIGHT_TWICE = 9 ;
    int HEAD_LEFT_RIGHT = 10 ;
    int HEAD_RIGHT_LEFT = 11 ;
    int BODY_DOWNSTAIRS = 0 ;
    int BODY_UPSTAIRS = 1 ;
    int BODY_STILL = 2 ;
    int BODY_WALK = 3;
    String []HEAD = {"Up","DOWN","LEFT",
            "RIGHT","LEAN_LEFT","LEAN_RIGHT",
            "UP_TWICE","DOWN_TWICE","LEFT_TWICE",
            "RIGHT_TWICE","LEFT_RIGHT","RIGHT_LEFT",} ;
    String []BODY ={"DOWNSTAIRS","UPSTAIRS","STILL","WALK","jog"} ;

    /**
     * input the actionCode of the head action and return head string
     * @param actionCode  Controller.[actionCode]
     * @return headString
     */
    static String headString(int actionCode){
        return HEAD[actionCode] ;
    }

    /**
     * input the actionCode of the head action and return head string
     * @param actionCode  Controller.[actionCode]
     * @return bodyString
     */
    static String bodyString(int actionCode){
        return BODY[actionCode] ;
    }

    /**
     *
     */
    void control(int headCode,int bodyCode) ;
}
