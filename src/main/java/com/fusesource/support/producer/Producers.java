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

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.JMSException;
import javax.jms.MessageProducer;

import com.fusesource.support.message.creator.MessageCreator;
import com.fusesource.support.message.creator.MessageCreatorFactory;
import com.fusesource.support.message.sender.MessageSender;
import com.fusesource.support.message.sender.MessageSenderFactory;

/**
 * @author Claudio Corsi
 * 
 */
public class Producers {

	// FIXME: Replace the current defaults with a property or configuration
	// entries.
	private int nbrProducers = 1;
	private int nbrThreads = 10;

	private MessageProducerFactory messageProducerFactory;
	private MessageCreatorFactory messageCreatorFactory;
	private MessageSenderFactory messageSenderFactory;

	Producers(MessageProducerFactory messageProducerFactory,
			MessageCreatorFactory messageCreatorFactory,
			MessageSenderFactory messageSenderFactory) {
		this.messageProducerFactory = messageProducerFactory;
		this.messageCreatorFactory = messageCreatorFactory;
		this.messageSenderFactory = messageSenderFactory;

	}

	public void execute() {

		ExecutorService service = Executors.newFixedThreadPool(nbrThreads);
		Collection<Callable<Object>> callables = new LinkedList<Callable<Object>>();

		for (int cnt = 0; cnt < nbrProducers; cnt++) {
			try {
				MessageProducer producer = messageProducerFactory.create();
				MessageCreator creator = messageCreatorFactory.create();
				MessageSender sender = messageSenderFactory.create();
				ProducerExecutor producerExecutor = new ProducerExecutor(
						producer, creator, sender);

				callables.add(Executors.callable(new Runnable() {
					private ProducerExecutor executor;

					public Runnable setExecutor(ProducerExecutor executor) {
						this.executor = executor;
						return this;
					}

					@Override
					public void run() {
						this.executor.execute();
					}
				}.setExecutor(producerExecutor)));
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}

		// FIXME: We should somehow get the shutdown process to be determine by
		// the calling thread.
		service.shutdown();

	}

}
