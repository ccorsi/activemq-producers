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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * @author Claudio Corsi
 *
 */
public abstract class DestinationFactory<D extends Destination> {

	private Session session;

	public DestinationFactory(Session session) {
		this.session = session;
	}
	
	protected Session getSession() {
		return this.session;
	}

	
	/**
	 * This method will create a jms destination
	 * 
	 * @param name The particular destination being created by the sub-class
	 * 
	 * @return The newly created destination instance
	 * @throws JMSException 
	 */
	public abstract D createDestination(String name) throws JMSException;
	
}
