package com.example.fantastiqa.redux.middleware;

import com.example.fantastiqa.redux.Action;
import com.example.fantastiqa.redux.Store;
import android.util.Log;

/**
 * Logging middleware that logs all dispatched actions and state changes
 */
public class LoggingMiddleware implements Middleware {
    private static final String TAG = "Redux";

    @Override
    public void process(Store store, Next next, Action action) {
        Log.d(TAG, "Action dispatched: " + action.getType());
        Log.d(TAG, "Current state: " + store.getState().toString());
        
        // Call next middleware/reducer
        next.dispatch(action);
        
        Log.d(TAG, "New state: " + store.getState().toString());
    }
}
