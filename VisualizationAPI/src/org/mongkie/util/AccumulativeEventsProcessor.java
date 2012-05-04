/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.util;

import org.openide.util.Exceptions;

/**
 * This thread is used for processing a lot of events in a short period of time.
 * <p/>
 * It takes care to only process the last event once when a lot of events come
 * within the given time interval
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class AccumulativeEventsProcessor extends Thread {

    private Runnable process;
    private final int checkInterval;
    private volatile boolean more = false;
    private boolean processing = false;

    /**
     * Construct a new <b>accumulative events processor</b> with a runnable object
     * to process events.
     * <br/>
     * It will run the process once when last event come within default time interval (100 ms).
     * This constructor has the same effect as
     * <code>AccumulativeEventsProcessor(process, 100)</code>.
     *
     * @param process a runnable object to process events
     */
    public AccumulativeEventsProcessor(Runnable process) {
        this(process, 100);
    }

    /**
     * Construct a new <b>accumulative events processor</b> with a runnable
     * object to process events and time interval to accumulate events.
     * <br/>
     * It will run the process once when last event come within the given time interval.
     *
     * @param process       a runnable object to process events
     * @param checkInterval milli seconds to accumulate coming events to be processed at once
     */
    public AccumulativeEventsProcessor(Runnable process, int checkInterval) {
        this.process = process;
        this.checkInterval = checkInterval;
    }

    @Override
    public void run() {
        try {
            do {
                more = false;
                Thread.sleep(checkInterval);
            } while (more);
            processing = true;
            process.run();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Test if this thread is accumulating coming events.
     *
     * @return true if accumulating, otherwise false
     */
    public boolean isAccumulating() {
        return isAlive() && !processing;
    }

    /**
     * Test if already running the process after finishing accumulation.
     *
     * @return true if already running the process, otherwise false
     */
    public boolean isProcessing() {
        return isAlive() && processing;
    }

    /**
     * Attach a coming event to this thread.
     */
    public void eventAttended() {
        if (!isAccumulating()) {
            throw new IllegalStateException("Can not attach an event because not accumulating.");
        }
        this.more = true;
    }

    /**
     * Attach a coming event with a new runnable object to process the event.
     *
     * @param process a new runnable object to process the event
     */
    public synchronized void eventAttended(Runnable process) {
        eventAttended();
        if (isProcessing()) {
            throw new IllegalStateException("Can not attach an event with a new process because already processing.");
        }
        this.process = process;
    }
}
