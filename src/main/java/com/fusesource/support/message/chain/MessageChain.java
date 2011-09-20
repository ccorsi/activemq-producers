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
package com.fusesource.support.message.chain;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author Claudio Corsi
 *
 */
public abstract class MessageChain {

	private MessageChain next;

	public abstract void apply(Message message) throws JMSException;
	
	public final MessageChain next() {
		return this.next;
	}
	
	public final void chain(MessageChain next) {
		if (this.next == null) {
			this.next = next;
		} else {
			next.chain(this.next);
			this.next = next;
		}
	}
	
}
