package com.masranber.bikecomputer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Observable<T> {

    public interface Observer<T> {
        void onChange(T value);
    }

    private T value;
    private final Set<Observer<T>> observers = ConcurrentHashMap.newKeySet();

    Handler mainHandler = new Handler(Looper.getMainLooper());

    public Observable() {
    }

    public Observable(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        notifyObservers();
    }

    public void postValue(final T value) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                setValue(value);
            }
        });
    }

    public void observe(Observer<T> observer) {
        observers.add(observer);
        Log.i("OBSERVER", "Has "+observers.size()+" observers");
        if(value != null) observer.onChange(value);
    }

    public void removeObserver(Observer<T> observer) {
        observers.remove(observer);
    }

    public void removeAllObservers() {
        observers.clear();
    }

    private void notifyObservers() {
        Log.i("OBSERVER", "Notifying "+observers.size()+" observers");
        for(Observer<T> observer : observers) {
            observer.onChange(this.value);
        }
    }
}
