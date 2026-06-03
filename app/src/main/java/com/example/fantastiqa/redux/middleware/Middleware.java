package com.example.fantastiqa.redux.middleware;

import com.example.fantastiqa.redux.Action;
import com.example.fantastiqa.redux.Store;

/**
 * Base interface for middleware in the Redux pipeline.
 * Middleware can intercept actions for logging, validation, async operations, etc.
 */
public interface Middleware {
    /**
     * Called for each action dispatched to the store
     */
    void process(Store store, Next next, Action action);

    /**
     * Represents the next middleware or reducer in the chain
     */
    interface Next {
        void dispatch(Action action);
    }
}
