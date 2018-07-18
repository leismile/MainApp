package com.ubtechinc.alpha.appmanager;

/**
 * Created by ubt on 2017/11/27.
 */

public abstract class StateMachineBase<T> {
    protected T currentState;
    protected abstract void init();
    protected abstract void stateChange(T oldState,T newState); //退出旧状态
    private StateChangeListener listener;

    protected StateMachineBase() {
        init();
    }
    public synchronized void setState(T newState) {
        T oldState = currentState;
        stateChange(oldState,newState);
        if (listener != null) {
            listener.onStateChanged(oldState,newState);
        }
        currentState = newState;
    }

    public synchronized T getCurrentState() {
        return currentState;
    }

    public void setStateChangeListener(StateChangeListener listener) {
        this.listener = listener;
    }

    public interface StateChangeListener<T> {
        void onStateChanged(T oldState, T newState);
    }


}
