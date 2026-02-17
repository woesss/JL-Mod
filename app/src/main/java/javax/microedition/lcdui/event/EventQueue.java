/*
 * Copyright 2012 Kulikov Dmitriy
 * Copyright 2017 Nikita Shakarun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.microedition.lcdui.event;

import javax.microedition.util.LinkedList;

/**
 * The event queue. A really complicated thing.
 */
public class EventQueue implements Runnable {
	private static boolean immediate;

	private final LinkedList<Event> queue = new LinkedList<>();
	private final Object waiter = new Object();
	private final Object interlock = new Object();
	private final Object callbackLock = new Object();

	private boolean enabled;
	private Thread thread;

	/**
	 * Enable immediate processing mode.
	 * <p>
	 * In this mode event are processed as soon as they arrive,
	 * without queue (violates serialization principle).
	 * <p>
	 * You can try to turn on this mode if every frame counts,
	 * but the midlet behavior will be unpredictable.
	 *
	 * @param value true if the immediate processing mode shoud be enabled
	 */
	public static void setImmediate(boolean value) {
		immediate = value;
	}

	public static boolean isImmediate() {
		return immediate;
	}

	/**
	 * Add event to the queue.
	 * <p>
	 * If the immediate processing mode is enabled,
	 * the event is processed here,
	 * in this case there is no queue at all
	 * <p>
	 * If an event has been added to the queue,
	 * its enterQueue() method is called.
	 *
	 * @param event the added event
	 */
	public void postEvent(Event event) {
		if (immediate) { // the immediate processing mode is enabled
			event.enterQueue();
			synchronized (callbackLock) {
				event.run(); // process event on the spot
			}
			return;      // and nothing to do here
		}

		synchronized (queue) {
			if (queue.isEmpty() || event.placeableAfter(queue.getLast())) {
				queue.addLast(event);
				event.enterQueue();
			} else {
				event.recycle();
				return;
			}
		}

		synchronized (waiter) {
			waiter.notify();
		}
	}

	/**
	 * Check if there is anything in the queue.
	 *
	 * @return true, if the queue is empty
	 */
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	/**
	 * Clear the queue.
	 */
	public void clear() {
		synchronized (queue) {
			queue.clear();
		}
	}

	/**
	 * Start the event loop.
	 * Repeated calls to this method are ignored.
	 */
	public void startProcessing() {
		enabled = true;

		if (thread == null) {
			thread = new Thread(this, "MIDletEventQueue");
			thread.start();
		}
	}

	/**
	 * Stop the event loop.
	 * This method is blocked until the loop is completely stopped.
	 */
	public void stopProcessing() {
		enabled = false;

		synchronized (waiter) {
			waiter.notifyAll();
		}

		synchronized (interlock) {
			thread = null;
		}
	}

	/**
	 * Here is the main event loop.
	 */
	@Override
	public void run() {
		synchronized (interlock) {
			while (enabled) {
				Event event = null;
				synchronized (queue) {
					if (!queue.isEmpty()) {
						event = queue.removeFirst();
					}
				}

				if (event != null) {
					synchronized (callbackLock) {
						event.run();
					}
				} else {
					synchronized (waiter) {
						boolean empty;
						synchronized (queue) {
							empty = queue.isEmpty();
						}
						if (empty && enabled) {
							try {
								waiter.wait();
							} catch (InterruptedException ie) {
								ie.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	public void serviceRepaints(Event paintEvent) {
		if (immediate) {
			return;
		}

		synchronized (callbackLock) {
			paintEvent.process();
		}
	}
}
