package com.gamecube.displaycontrol;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

public class OnClickMaker{
    public static View.OnClickListener getOnClick(){
         return new View.OnClickListener(){
              public void onClick(View v){
                  boolean on = ((ToggleButton) v).isChecked();
                  if (on)
                  	v.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
                  else
                	v.getBackground().setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
              }
          };
    }
}

