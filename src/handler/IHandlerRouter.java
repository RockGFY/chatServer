package handler;

import queue.Bundle;

import java.lang.reflect.InvocationTargetException;

public interface IHandlerRouter {

    /**
     * Route the task to the desired handler based on the client request.
     * @param bundle The client request.
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    void route(Bundle bundle) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException;

}
