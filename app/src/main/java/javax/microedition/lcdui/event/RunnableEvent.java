/*
 * Copyright 2012 Kulikov Dmitriy
 * Copyright 2017-2018 Nikita Shakarun
 * Copyright 2020-2023 Yury Kharchenko
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

import javax.microedition.util.ArrayStack;
import javax.microedition.util.ContextHolder;

import ru.playsoftware.j2meloader.R;

public class RunnableEvent extends Event {
	private static final ArrayStack<RunnableEvent> recycled = new ArrayStack<>();
	private static int queued;

	private Runnable runnable;

	public static Event getInstance(Runnable runnable) {
		RunnableEvent instance = recycled.pop();

		if (instance == null) {
			instance = new RunnableEvent();
		}

		instance.runnable = runnable;

		return instance;
	}

	@Override
	public void process() {
		runnable.run();
	}

	@Override
	public void recycle() {
		runnable = null;
		recycled.push(this);
	}

	@Override
	public void enterQueue() {
		if (++queued > 50 && EventQueue.isImmediate()) {
			EventQueue.setImmediate(false);
			ContextHolder.getActivity().toast(R.string.msg_immediate_mode_disabled);
		}
	}

	@Override
	public void leaveQueue() {
		queued--;
	}

	@Override
	public boolean placeableAfter(Event event) {
		return true;
	}
}