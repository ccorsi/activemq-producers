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
package com.fusesource.support.message.creator;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * @author Claudio Corsi
 *
 */
public class TextMessageCreator extends AbstractMessageCreator<TextMessage> {

	private Session session;
	private int cnt = 0;

	public TextMessageCreator(Session session) {
		this.session = session;
	}
	
	/* (non-Javadoc)
	 * @see com.fusesource.support.producers.MessageCreator#create()
	 */
	public TextMessage create() throws JMSException {
		// Create the message
		TextMessage message = session.createTextMessage("Message:" + cnt++);
		// Apply all chained settings
		this.apply(message);
		// Return newly created message
		return message;
	}

}
