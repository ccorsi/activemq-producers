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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fusesource.support.message.creator.MessageCreator;
import com.fusesource.support.message.sender.MessageSender;

/**
 * @author Claudio Corsi
 *
 */
public class ProducerExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(ProducerExecutor.class);
	
	private volatile boolean createMessages;

	private MessageProducer producer;
	private MessageCreator creator;
	private MessageSender sender;

	/**
	 * @param producer
	 * @param creator
	 * @param sender
	 */
	public ProducerExecutor(MessageProducer producer, MessageCreator creator, MessageSender sender) {
		this.producer = producer;
		this.creator = creator;
		this.sender = sender;
	}
	
	public void execute() {
		logger.info("Producing messages");
		while(!createMessages) {
			try {
				Message message = creator.create();
				sender.send(producer, message);
			} catch(JMSException e) {
				logger.debug("Received an exception while creating and sending messages",e);
			}
		}
		try {
			producer.close();
		} catch (JMSException e) {
				logger.debug("An exception was raised when closing producer",e);
		}
		logger.info("completed producing messages");
	}

	public void stopCreatingMessages() {
		this.createMessages = true;
	}
	
}
