package org.solveme.philosophers;


public interface Fork {

    boolean isBusy();

    boolean take();

    void release();

}
