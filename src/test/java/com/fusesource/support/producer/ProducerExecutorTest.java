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

import java.io.IOException;
import java.util.Random;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.activemq.brokerservice.runner.HubSpokeBrokerServiceSpawner;
import com.fusesource.support.message.chain.IntegerMessageChain;
import com.fusesource.support.message.chain.MessageChain;
import com.fusesource.support.message.chain.StringMessageChain;
import com.fusesource.support.message.creator.MessageCreator;
import com.fusesource.support.message.creator.TextMessageCreator;
import com.fusesource.support.message.sender.SimpleMessageSender;


/**
 * @author Claudio Corsi
 *
 */
public class ProducerExecutorTest {
	
	private ActiveMQConnectionFactory factory;
	private Connection connection;
	private HubSpokeBrokerServiceSpawner spawner;

	@Before
	public void setup() throws JMSException, IOException, InterruptedException {
                System.out.println("java.class.path=" + System.getProperty("java.class.path"));
                spawner = new HubSpokeBrokerServiceSpawner() {
                        private int currentPort;
                        private int hubPort;
                        private int hubId;

                        @Override
                        public String getSpokeTemplatePrefix() {
                            return "spoke-activemq";
                        }

                        @Override
                        public String getHubTemplatePrefix() {
                            return "hub-activemq";
                        }

                        @Override
                        public void populateProperties(TYPE type, Properties props, int idx) {
                            switch(type) {
                            case HUB:
                                populateHubProperties(props, idx);
                                break;
                            case SPOKE:
                                populateSpokeProperties(props, idx);
                                break;
                            }
                        }

                        @Override
                        protected void preCreateSpawners() {
                            currentPort = getAmqPort();
                        }

                        private void populateHubProperties(Properties props, int idx) {
                            hubPort = currentPort++;
                            hubId = idx;
                            props.setProperty("hub.suffix.name",String.valueOf(idx));
                            props.setProperty("kahadb.dir","hub");
                            props.setProperty("kahadb.prefix",String.valueOf(idx));
                            props.setProperty("hub.hostname","localhost");
                            props.setProperty("hub.port.number",String.valueOf(hubPort));
                        }

                        private void populateSpokeProperties(Properties props, int idx) {
                            props.setProperty("spoke.suffix.name",hubId + "-" + idx);
                            props.setProperty("hub.hostname","localhost");
                            props.setProperty("hub.port.number",String.valueOf(hubPort));
                            props.setProperty("kahadb.dir","spoke");
                            props.setProperty("kahadb.prefix",hubId + "-" + idx);
                            props.setProperty("spoke.hostname","localhost");
                            props.setProperty("spoke.port.number",String.valueOf(currentPort++));
                        }

                        {
                            setAmqPort(61616);
                            setNumberOfHubs(1);
                            setNumberOfSpokesPerHub(1);
                        }
                };
		spawner.execute();
		System.out.println("SLEEPING FOR 10 seconds");
		Thread.sleep(10000);
		System.out.println("COMPLETED WAIT");
		// We have to wait for the activemq broker to start.
		factory = new ActiveMQConnectionFactory();
		factory.setBrokerURL("tcp://localhost:61616");
		connection = factory.createConnection();
		connection.start();
	}

	@After
	public void teardown() throws IOException, JMSException {
		if (spawner != null)
			spawner.stopBrokers();
		if (connection != null)
			connection.close();
	}
	
	@Test
	public void execute() throws JMSException, InterruptedException {
		ActiveMQConnectionFactory spokeFactory = new ActiveMQConnectionFactory();
		spokeFactory.setBrokerURL("tcp://localhost:61617");
		Connection spokeConnection = spokeFactory.createConnection();
		spokeConnection.start();
		Session spokeSession = spokeConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination spokeDestination = spokeSession.createQueue("simple");
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createQueue("simple");
		MessageConsumer consumer = spokeSession.createConsumer(spokeDestination);
		consumer.setMessageListener(new MessageListener() {

			public void onMessage(Message message) {
				System.out.println(Thread.currentThread().getName() + " - RECEIVED MESSAGE: " + message);
			}
		});

		int destinationUnits[] = {
				3039,
				3139,
				3239,
				3339,
				3439,
				3539,
		};
		
		ThreadGroup threadGroup = new ThreadGroup("ProducerExecuterThreadGroup");
		ProducerExecutor producerExecutors[] = new ProducerExecutor[destinationUnits.length];
		int idx = 0;
		for (int destinationUnit : destinationUnits) {
			MessageCreator messageCreator = new TextMessageCreator(session);
			messageCreator
					.add(new IntegerMessageChain("DestinationUnit", destinationUnit));
			messageCreator.add(new StringMessageChain("DestinationDept", "DC"));
			messageCreator.add(new StringMessageChain("OriginDept", "ISMerch"));
			messageCreator.add(new IntegerMessageChain("OriginUnit", 1001));
			messageCreator.add(new StringMessageChain("MessageType", "PO"));
			if (idx % 2 == 0) {
				messageCreator.add(new MessageChain() {

					private Random rand = new Random();

					@Override
					public void apply(Message message) throws JMSException {
						try {
							long millis = rand.nextInt(5000) + 1;
							System.out
									.println(Thread.currentThread().getName() + " - Sleeping for " + millis + "ms.");
							Thread.sleep(millis);
						} catch (InterruptedException ie) {
							// Do nothing....
						}
					}

				});
			}

			producerExecutors[idx] = new ProducerExecutor(
					new MessageProducerFactory(session, destination).create(),
					messageCreator, new SimpleMessageSender());

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

			}.setProducerExecutor(producerExecutors[idx++])) {
				{
					setDaemon(true);
					start();
				}
			};
		}
		
		try {
			System.out.println("WAITING FOR 100 seconds prior to sending a message");
			Thread.sleep(100000);
		} finally {
			for (ProducerExecutor producerExecutor : producerExecutors) {
				producerExecutor.stopCreatingMessages();
			}
			System.out.println("STOP SENDING MESSAGES");
		}
		// Stop consuming messages....
		consumer.close();
		Thread[] threads = new Thread[destinationUnits.length];
		threadGroup.enumerate(threads, false);
		for(Thread thread : threads) {
			if (thread == null) return; // No more active threads included in the list, just return
			try {
				System.out.println("Waiting for thread: " + thread.getName() + " to exit");
				// Wait until the thread dies prior to exiting test...
				thread.join();
			} catch( InterruptedException ie) {
			}
		}
		System.out.println("We are done.");
	}

	@Test
	public void ProducerExecutorManagerTest() throws JMSException, InterruptedException {
		ActiveMQConnectionFactory spokeFactory = new ActiveMQConnectionFactory();
		spokeFactory.setBrokerURL("tcp://localhost:61617");
		Connection spokeConnection = spokeFactory.createConnection();
		spokeConnection.start();
		Session spokeSession = spokeConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination spokeDestination = spokeSession.createQueue("simple");
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createQueue("simple");
		MessageConsumer consumer = spokeSession.createConsumer(spokeDestination);
		consumer.setMessageListener(new MessageListener() {

			public void onMessage(Message message) {
				System.out.println(Thread.currentThread().getName() + " - RECEIVED MESSAGE: " + message);
			}
		});

		int destinationUnits[] = {
				3039,
				3139,
				3239,
				3339,
				3439,
				3539,
		};
		
		ProducerExecutorManager manager = new ProducerExecutorManager();
		for (int destinationUnit : destinationUnits) {
			MessageCreator messageCreator = new TextMessageCreator(session);
			messageCreator
					.add(new IntegerMessageChain("DestinationUnit", destinationUnit));
			messageCreator.add(new StringMessageChain("DestinationDept", "DC"));
			messageCreator.add(new StringMessageChain("OriginDept", "ISMerch"));
			messageCreator.add(new IntegerMessageChain("OriginUnit", 1001));
			messageCreator.add(new StringMessageChain("MessageType", "PO"));

			ProducerExecutor executor = new ProducerExecutor(
					new MessageProducerFactory(session, destination).create(),
					messageCreator, new SimpleMessageSender());

			manager.addExecutor(executor);
		}
		
		try {
			manager.start();
			System.out.println("WAITING FOR 100 seconds prior to sending a message");
			Thread.sleep(100000);
		} finally {
			System.out.println("STOPPING PRODUCER EXECUTORS AND WAIT UNTIL THEY ARE DONE");
			manager.stop(true);
			System.out.println("STOP SENDING MESSAGES");
		}
		// Stop consuming messages....
		consumer.close();
		System.out.println("We are done.");
	}
	
}
