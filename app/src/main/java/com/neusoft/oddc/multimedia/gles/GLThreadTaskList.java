package com.neusoft.oddc.multimedia.gles;

import java.util.LinkedList;

public class GLThreadTaskList extends LinkedList<Runnable> {

    public void runOnDraw(final Runnable runnable) {
        synchronized (this) {
            this.addLast(runnable);
        }
    }

    public void runPendingOnDrawTasks() {
        while (!this.isEmpty()) {
            this.removeFirst().run();
        }
    }
}
