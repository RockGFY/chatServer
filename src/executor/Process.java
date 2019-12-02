package executor;

import handler.IHandlerRouter;
import queue.Bundle;

public class Process
        implements Runnable {

    private IHandlerRouter handler;
    private Bundle bundle;

    Process(IHandlerRouter handler, Bundle bundle) {
        this.handler = handler;
        this.bundle = bundle;
    }

    @Override
    public void run() {

        try {
            handler.route(bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
