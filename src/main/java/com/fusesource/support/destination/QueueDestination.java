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
package com.fusesource.support.destination;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;

/**
 * @author Claudio Corsi
 *
 */
public class QueueDestination extends DestinationFactory<Queue> {

	private boolean temporary;

	/**
	 * @param session
	 */
	public QueueDestination(Session session, boolean temporary) {
		super(session);
		this.temporary = temporary;
	}

	/* (non-Javadoc)
	 * @see com.fusesource.support.destinations.DestinationFactory#createDestination()
	 */
	@Override
	public Queue createDestination(String name) throws JMSException {
		return this.temporary ? getSession().createTemporaryQueue() : getSession().createQueue(name);
	}

}
