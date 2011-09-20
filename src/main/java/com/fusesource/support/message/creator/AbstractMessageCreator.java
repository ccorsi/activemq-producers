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
import javax.jms.Message;

import com.fusesource.support.message.chain.MessageChain;
import com.fusesource.support.message.chain.NoopMessageChain;

/**
 * @author Claudio Corsi
 *
 */
public abstract class AbstractMessageCreator implements MessageCreator {

	private MessageChain chain = new NoopMessageChain();

	/* (non-Javadoc)
	 * @see com.fusesource.support.producers.MessageCreator#add(com.fusesource.support.message.chains.MessageChain)
	 */
	@Override
	public void add(MessageChain chain) {
		this.chain.chain(chain);
	}
	
	/**
	 * @param message
	 * @throws JMSException
	 */
	protected void apply(Message message) throws JMSException {
		MessageChain current = this.chain;
		while(current != null) {
			current.apply(message);
			current = current.next();
		}
	}

}
