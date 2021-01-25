package com.masranber.bikecomputer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ControlButton extends androidx.appcompat.widget.AppCompatButton implements View.OnClickListener {

    public enum State {
        BLUETOOTH_OFF, DISCONNECTED, CONNECTING, BEGIN_TRIP, END_TRIP;
    }

    public interface OnStateChangeListener {
        void onStateChange(State currentState);
    }

    private OnStateChangeListener onStateChangeListener;
    private State currentState;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public ControlButton(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ControlButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        //setOnClickListener(this);
        setState(State.DISCONNECTED);
    }

    public void setState(State state) {
        this.currentState = state;
        setBackgroundResource(R.drawable.main_button_bg_selector_blue);
        switch(state) {
            case BLUETOOTH_OFF:
                setText("TURN ON BLUETOOTH");
                setEnabled(false);
                break;
            case DISCONNECTED:
                handler.removeCallbacksAndMessages(null);
                setText("CONNECT");
                setEnabled(true);
                break;
            case CONNECTING:
                setText("CONNECTING");

                handler.post(new Runnable() {
                    int count = 0;
                    @Override
                    public void run() {
                        count++;
                        if (count == 1)
                        {
                            setText("CONNECTING.");
                        }
                        else if (count == 2)
                        {
                            setText("CONNECTING..");
                        }
                        else if (count == 3)
                        {
                            setText("CONNECTING...");
                        }
                        if (count == 3) count = 0;
                        handler.postDelayed(this, 2 * 200);
                    }
                 });
                setEnabled(false);
                break;
            case BEGIN_TRIP:
                handler.removeCallbacksAndMessages(null);
                setText("START TRIP");
                setEnabled(true);
                break;
            case END_TRIP:
                setText("STOP TRIP");
                setEnabled(true);
                setBackgroundResource(R.drawable.main_button_bg_selector_red);
                break;
        }
    }

    public State getState() {
        return currentState;
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        this.onStateChangeListener = listener;
    }

    public void removeOnStateChangeListener() {
        this.onStateChangeListener = null;
    }

    @Override
    public void onClick(View view) {
        if(this.onStateChangeListener != null) {
            this.onStateChangeListener.onStateChange(currentState);
        }
    }
}
