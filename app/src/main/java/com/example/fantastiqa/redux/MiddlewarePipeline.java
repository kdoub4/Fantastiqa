package com.example.fantastiqa.redux;

import com.example.fantastiqa.redux.middleware.Middleware;

import java.util.ArrayList;
import java.util.List;

/**
 * Middleware pipeline for processing actions.
 * Middleware can intercept, log, validate, or transform actions before they reach the reducer.
 * 
 * This follows the Chain of Responsibility pattern.
 */
public class MiddlewarePipeline {
    private final List<Middleware> middlewares;

    public MiddlewarePipeline() {
        this.middlewares = new ArrayList<>();
    }

    /**
     * Add a middleware to the pipeline
     */
    public MiddlewarePipeline use(Middleware middleware) {
        middlewares.add(middleware);
        return this;
    }

    /**
     * Execute the middleware pipeline with a store
     */
    public Middleware build() {
        return (store, next, action) -> {
            if (middlewares.isEmpty()) {
                next.dispatch(action);
                return;
            }
            executeMiddleware(0, store, next, action);
        };
    }

    private void executeMiddleware(int index, Store store, Middleware.Next next, Action action) {
        if (index >= middlewares.size()) {
            next.dispatch(action);
            return;
        }

        Middleware middleware = middlewares.get(index);
        Middleware.Next chainNext = (a) -> executeMiddleware(index + 1, store, next, a);
        middleware.process(store, chainNext, action);
    }
}
