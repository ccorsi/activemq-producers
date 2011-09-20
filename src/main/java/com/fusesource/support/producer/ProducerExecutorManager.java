/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fusesource.support.producer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Claudio Corsi
 *
 */
public class ProducerExecutorManager {
	
	private static final AtomicInteger id = new AtomicInteger();
	
	private Set<ProducerExecutor> executors = new HashSet<ProducerExecutor>();
	private boolean started;
	private ThreadGroup threadGroup;
	

	/**
	 * @param executor
	 */
	private void startProducerExecutor(ProducerExecutor executor) {
		
		threadGroup = new ThreadGroup("ProducerExecuterThreadGroup:" + id.getAndIncrement());
		
		new Thread(threadGroup, new Runnable() {

			private ProducerExecutor producerExecutor;

			public void run() {
				producerExecutor.execute();
			}

			public Runnable setProducerExecutor(
					ProducerExecutor producerExecutor) {
				this.producerExecutor = producerExecutor;
				return this;
			}

		}.setProducerExecutor(executor)) {
			{
				setDaemon(true);
				setName("ProducerExecutorThread:" + getName());
				start();
			}
		};
		
	}

	public void addExecutor(ProducerExecutor executor) {
		synchronized(executors) {
			executors.add(executor);
			if(started) {
				startProducerExecutor(executor);
			}
		}
	}
	
	public boolean removeExecutor(ProducerExecutor executor) {
		boolean result;
		
		synchronized(executors) {
			result = executors.remove(executor);
		}
		
		// TODO: Should we wait until these have completed????
		executor.stopCreatingMessages();

		return result;
	}
	
	public void stop(boolean waitForProducers) {
		int size;
		synchronized(executors) {
			if(!started) return;
			
			for( ProducerExecutor executor : executors ) {
				executor.stopCreatingMessages();
			}
			size = executors.size();
			started = false;
		}
		
		if(waitForProducers) {
			Thread[] threads = new Thread[size + 1];
			int result = threadGroup.enumerate(threads);
			for (Thread thread : threads) {
				if (thread != null) {
					try {
						thread.join();
					} catch (InterruptedException e) {
					}
				}
			}
			if (result == size + 1) {
				// We've possibly missed some threads....
				// .... TODO: Add logging statement that can be used to see this situation....
			}
		}
	}

	public void start() {
		synchronized(executors) {
			if (started) return;
			
			for(ProducerExecutor executor : executors) {
				startProducerExecutor(executor);
			}
			started = true;
		}
	}
}
