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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * @author Claudio Corsi
 *
 */
public class MessageProducerFactory {
	
	private Session session;
	private Destination destination;

	public MessageProducerFactory(Session session, Destination destination) {
		this.session = session;
		this.destination = destination;
	}

	/**
	 * @return
	 * @throws JMSException 
	 */
	public MessageProducer create() throws JMSException {
		return session.createProducer(destination);
	}

}
