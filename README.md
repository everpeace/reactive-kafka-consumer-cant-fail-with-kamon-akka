# Problem
The simple [reactive kafka](http://doc.akka.io/docs/akka-stream-kafka/current/home.html) stream below is expected to fail soon when kafka broker(`localhost:19092`) is down because of consecutive `WakeupException` happens.

```scala
  val consumerSetting = ConsumerSettings(system, new ByteArrayDeserializer, new ByteArrayDeserializer)
    .withBootstrapServers("localhost:19092")
    .withGroupId("dummy-group-id")
  val done = Consumer
    .atMostOnceSource(consumerSetting, Subscriptions.topics("topic-no-exist"))
    .toMat(Sink.ignore)(Keep.right)
    .run()
```

I faced an issue that if [Kamon](https://kamon.io/) is enabled, more precisely, `-javaagent:/path/to/org.aspectj.aspectjweaver.jar` was given and `io.kamon.kamon-akka-2.4_2.11-{verson}.jar` existed in classpath, above simple stream never fails until some signal (SIGINT(Ctrl-C) or similar) was received.

# Versions
- `reactive-kafka=0.16`
- `akka-{actor|stream}=2.4.18`
- `kamon-{core|akka-2.4}=0.6.7`
- `aspectjweaver=1.8.10` (on which kamon-akka depends)

# How to reproduce
1. clone this repo: `git clone https://github.com/everpeace/reactive-kafka-consumer-cant-fail-with-kamon-akka.git`
1. package it as docker container image: `sbt docker:publishLocal`
    - I would like to reproduce this issue in a pure java application to avoid any issues which come from build tools or IDE's environment.
1. run it __without__ Kamon: `docker run -e KAMON=no reactive-kafka-cant-stop-with-kamon-akka:0.1.0-SNAPSHOT`
    -  you will see stream successfuly fail and jvm will shutdown too.
    - refer to [appendix](#without_kamon) for detailed log
1. run it __with__ Kamon: `docker run -e KAMON=yes reactive-kafka-cant-stop-with-kamon-akka:0.1.0-SNAPSHOT`
    - you will see stream never fail, but you will only see KafkaConsumerActor stopped, stream will be still running until some signal(Ctrl-C or similar) was received.
    - refer to [appendix](#with_kamon) for detailed log

# Attaching Debugger to the container
the container image(`reactive-kafka-cant-stop-with-kamon-akka:0.1.0-SNAPSHOT`) exposes 4000 to remote debugging.  So you can run the container with `$DEBUG=true` like below:

```
docker run -e KAMON=yes -e DEBUG=true -p 4000:4000 reactive-kafka-cant-stop-with-kamon-akka:0.1.0-SNAPSHOT
```

then you can attach remote debugger `locahost:4000`.


# Appendix: Logs

<a name="without_kamon" />

## "run without kamon" => this successfully shutdown
```
$ docker run -e KAMON=no reactive-kafka-cant-stop-with-kamon-akka:0.1.0-SNAPSHOT
[DEBUG] [08/09/2017 06:31:48.486] [main] [EventStream(akka://ReactiveKafkaCantStopWithKamonAkka)] logger log1-Logging$DefaultLogger started
[DEBUG] [08/09/2017 06:31:48.487] [main] [EventStream(akka://ReactiveKafkaCantStopWithKamonAkka)] Default Loggers started
[DEBUG] [08/09/2017 06:31:48.489] [main] [EventStream] unsubscribing StandardOutLogger from all channels
[DEBUG] [08/09/2017 06:31:48.490] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [akka://ReactiveKafkaCantStopWithKamonAkka/system] now supervising Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/deadLetterListener#-143150190]
[DEBUG] [08/09/2017 06:31:48.491] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-2] [EventStream] subscribing Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/deadLetterListener#-143150190] to channel class akka.actor.DeadLetter
[DEBUG] [08/09/2017 06:31:48.492] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-2] [akka://ReactiveKafkaCantStopWithKamonAkka/system/deadLetterListener] started (akka.event.DeadLetterListener@403ee1a2)
[DEBUG] [08/09/2017 06:31:48.493] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStreamUnsubscriber] registering unsubscriber with akka.event.EventStream@5e825c1
[DEBUG] [08/09/2017 06:31:48.494] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStream] initialized unsubscriber to: Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/eventStreamUnsubscriber-1#-1277614560], registering 2 initial subscribers with it
[DEBUG] [08/09/2017 06:31:48.496] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [akka://ReactiveKafkaCantStopWithKamonAkka/system/eventStreamUnsubscriber-1] started (akka.event.EventStreamUnsubscriber@850737a)
[DEBUG] [08/09/2017 06:31:48.500] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStreamUnsubscriber] watching Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/log1-Logging$DefaultLogger#1877454579] in order to unsubscribe from EventStream when it terminates
[DEBUG] [08/09/2017 06:31:48.500] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-2] [akka://ReactiveKafkaCantStopWithKamonAkka/system] now supervising Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/eventStreamUnsubscriber-1#-1277614560]
[DEBUG] [08/09/2017 06:31:48.502] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStreamUnsubscriber] watching Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/deadLetterListener#-143150190] in order to unsubscribe from EventStream when it terminates
[DEBUG] [08/09/2017 06:31:48.502] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-7] [akka://ReactiveKafkaCantStopWithKamonAkka/system/log1-Logging$DefaultLogger] now watched by Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/eventStreamUnsubscriber-1#-1277614560]
[DEBUG] [08/09/2017 06:31:48.502] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-6] [akka://ReactiveKafkaCantStopWithKamonAkka/system/deadLetterListener] now watched by Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/eventStreamUnsubscriber-1#-1277614560]
[DEBUG] [08/09/2017 06:31:48.516] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [akka://ReactiveKafkaCantStopWithKamonAkka/user] now supervising Actor[akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0#-73505809]
[DEBUG] [08/09/2017 06:31:48.517] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-7] [akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0] started (akka.stream.impl.StreamSupervisor@62025d0d)
[DEBUG] [08/09/2017 06:31:48.633] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0] now supervising Actor[akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0/flow-0-0-unknown-operation#348505180]
[DEBUG] [08/09/2017 06:31:48.652] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-6] [akka://ReactiveKafkaCantStopWithKamonAkka/system] now supervising Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1#1508741721]
[DEBUG] [08/09/2017 06:31:48.663] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-7] [akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0/flow-0-0-unknown-operation] started (akka.stream.impl.fusing.ActorGraphInterpreter@6aff0a66)
[INFO] [2017-08-09 06:31:48,845] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-8] [o.a.k.c.consumer.ConsumerConfig] - ConsumerConfig values:
	auto.commit.interval.ms = 5000
	auto.offset.reset = latest
	bootstrap.servers = [localhost:19092]
	check.crcs = true
	client.id =
	connections.max.idle.ms = 540000
	enable.auto.commit = false
	exclude.internal.topics = true
	fetch.max.bytes = 52428800
	fetch.max.wait.ms = 500
	fetch.min.bytes = 1
	group.id = dummy-group-id
	heartbeat.interval.ms = 3000
	interceptor.classes = null
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	max.partition.fetch.bytes = 1048576
	max.poll.interval.ms = 300000
	max.poll.records = 500
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.recording.level = INFO
	metrics.sample.window.ms = 30000
	partition.assignment.strategy = [class org.apache.kafka.clients.consumer.RangeAssignor]
	receive.buffer.bytes = 65536
	reconnect.backoff.ms = 50
	request.timeout.ms = 305000
	retry.backoff.ms = 100
	sasl.jaas.config = null
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	session.timeout.ms = 10000
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer

[INFO] [2017-08-09 06:31:48,984] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-8] [o.a.kafka.common.utils.AppInfoParser] - Kafka version : 0.10.2.1
[INFO] [2017-08-09 06:31:48,984] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-8] [o.a.kafka.common.utils.AppInfoParser] - Kafka commitId : e89bffd6b2eff799
[DEBUG] [08/09/2017 06:31:48.985] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-8] [akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1] started (akka.kafka.KafkaConsumerActor@5fff97e2)
[DEBUG] [08/09/2017 06:31:48.986] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-8] [akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1] now watched by Actor[akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0/$$a#-876175647]
[DEBUG] [08/09/2017 06:31:48.989] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-8] [akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1] received handled message Subscribe(Set(topic-no-exist),akka.kafka.KafkaConsumerActor$$anon$1@2653466b) from Actor[akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0/$$a#-876175647]
[DEBUG] [08/09/2017 06:31:49.058] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-10] [akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1] received handled message Poll(akka.kafka.KafkaConsumerActor@5fff97e2,true) from Actor[akka://ReactiveKafkaCantStopWithKamonAkka/deadLetters]
[ERROR] [08/09/2017 06:31:50.078] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-10] [akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1] WakeupException limit exceeded, stopping.
[DEBUG] [08/09/2017 06:31:50.088] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-10] [akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1] stopped
[INFO] [08/09/2017 06:31:50.091] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-6] [akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1] Message [akka.kafka.KafkaConsumerActor$Internal$Stop$] from Actor[akka://ReactiveKafkaCantStopWithKamonAkka/deadLetters] to Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1#1508741721] was not delivered. [1] dead letters encountered. This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.
[DEBUG] [08/09/2017 06:31:50.095] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-7] [akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0/flow-0-0-unknown-operation] stopped
[ERROR] [2017-08-09 06:31:50,097] [ForkJoinPool-2-worker-7] [Main$] - consumer stream ended up with failure by
java.lang.Exception: Consumer actor terminated
	at akka.kafka.internal.SingleSourceLogic$$anonfun$preStart$1.apply(SingleSourceLogic.scala:54)
	at akka.kafka.internal.SingleSourceLogic$$anonfun$preStart$1.apply(SingleSourceLogic.scala:40)
	at akka.stream.stage.GraphStageLogic$StageActor.internalReceive(GraphStage.scala:166)
	at akka.stream.stage.GraphStageLogic$StageActor$$anonfun$1.apply(GraphStage.scala:133)
	at akka.stream.stage.GraphStageLogic$StageActor$$anonfun$1.apply(GraphStage.scala:133)
	at akka.stream.impl.fusing.GraphInterpreter.runAsyncInput(GraphInterpreter.scala:691)
	at akka.stream.impl.fusing.GraphInterpreterShell.receive(ActorGraphInterpreter.scala:419)
	at akka.stream.impl.fusing.ActorGraphInterpreter.akka$stream$impl$fusing$ActorGraphInterpreter$$processEvent(ActorGraphInterpreter.scala:603)
	at akka.stream.impl.fusing.ActorGraphInterpreter$$anonfun$receive$1.applyOrElse(ActorGraphInterpreter.scala:618)
	at akka.actor.Actor$class.aroundReceive(Actor.scala:502)
	at akka.stream.impl.fusing.ActorGraphInterpreter.aroundReceive(ActorGraphInterpreter.scala:529)
	at akka.actor.ActorCell.receiveMessage(ActorCell.scala:526)
	at akka.actor.ActorCell.invoke(ActorCell.scala:495)
	at akka.dispatch.Mailbox.processMailbox(Mailbox.scala:257)
	at akka.dispatch.Mailbox.run(Mailbox.scala:224)
	at akka.dispatch.Mailbox.exec(Mailbox.scala:234)
	at scala.concurrent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
	at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339)
	at scala.concurrent.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
	at scala.concurrent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)
[INFO] [2017-08-09 06:31:50,098] [shutdownHook1] [Main$] - Shutdown hook started.
[DEBUG] [08/09/2017 06:31:50.104] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-6] [akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0] stopped
[DEBUG] [08/09/2017 06:31:50.104] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-7] [akka://ReactiveKafkaCantStopWithKamonAkka/user] stopping
[DEBUG] [08/09/2017 06:31:50.106] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-7] [akka://ReactiveKafkaCantStopWithKamonAkka/user] stopped
[DEBUG] [08/09/2017 06:31:50.107] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [akka://ReactiveKafkaCantStopWithKamonAkka/system] received AutoReceiveMessage Envelope(Terminated(Actor[akka://ReactiveKafkaCantStopWithKamonAkka/user]),Actor[akka://ReactiveKafkaCantStopWithKamonAkka/user])
[DEBUG] [08/09/2017 06:31:50.108] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStream] subscribing StandardOutLogger to channel class akka.event.Logging$Error
[DEBUG] [08/09/2017 06:31:50.108] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-6] [EventStreamUnsubscriber] watching StandardOutLogger in order to unsubscribe from EventStream when it terminates
[DEBUG] [08/09/2017 06:31:50.108] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStream] subscribing StandardOutLogger to channel class akka.event.Logging$Warning
[DEBUG] [08/09/2017 06:31:50.108] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-6] [EventStreamUnsubscriber] watching StandardOutLogger in order to unsubscribe from EventStream when it terminates
[DEBUG] [08/09/2017 06:31:50.109] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStream] shutting down: StandardOutLogger started
[INFO] [2017-08-09 06:31:50,129] [shutdownHook1] [Main$] - Shutdown hook finished.
```

<a name="with_kamon" />

## run with Kamon => stream never fail and run forever
```
docker run -e KAMON=yes reactive-kafka-cant-stop-with-kamon-akka:0.1.0-SNAPSHOT
[INFO] [2017-08-09 06:33:08,177] [main] [kamon.Kamon$Instance] - Initializing Kamon...
[DEBUG] [08/09/2017 06:33:10.102] [main] [EventStream(akka://ReactiveKafkaCantStopWithKamonAkka)] logger log1-Logging$DefaultLogger started
[DEBUG] [08/09/2017 06:33:10.102] [main] [EventStream(akka://ReactiveKafkaCantStopWithKamonAkka)] Default Loggers started
[DEBUG] [08/09/2017 06:33:10.102] [main] [EventStream] unsubscribing StandardOutLogger from all channels
[DEBUG] [08/09/2017 06:33:10.104] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [akka://ReactiveKafkaCantStopWithKamonAkka/system] now supervising Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/deadLetterListener#775688788]
[DEBUG] [08/09/2017 06:33:10.104] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStream] subscribing Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/deadLetterListener#775688788] to channel class akka.actor.DeadLetter
[DEBUG] [08/09/2017 06:33:10.105] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [akka://ReactiveKafkaCantStopWithKamonAkka/system/deadLetterListener] started (akka.event.DeadLetterListener@10a491d5)
[DEBUG] [08/09/2017 06:33:10.115] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [akka://ReactiveKafkaCantStopWithKamonAkka/system] now supervising Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/eventStreamUnsubscriber-2#1665139875]
[DEBUG] [08/09/2017 06:33:10.115] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStreamUnsubscriber] registering unsubscriber with akka.event.EventStream@149b0719
[DEBUG] [08/09/2017 06:33:10.115] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStream] initialized unsubscriber to: Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/eventStreamUnsubscriber-2#1665139875], registering 2 initial subscribers with it
[DEBUG] [08/09/2017 06:33:10.116] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [akka://ReactiveKafkaCantStopWithKamonAkka/system/eventStreamUnsubscriber-2] started (akka.event.EventStreamUnsubscriber@51999bee)
[DEBUG] [08/09/2017 06:33:10.116] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStreamUnsubscriber] watching Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/log1-Logging$DefaultLogger#-527417040] in order to unsubscribe from EventStream when it terminates
[DEBUG] [08/09/2017 06:33:10.116] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStreamUnsubscriber] watching Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/deadLetterListener#775688788] in order to unsubscribe from EventStream when it terminates
[DEBUG] [08/09/2017 06:33:10.116] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-2] [akka://ReactiveKafkaCantStopWithKamonAkka/system/deadLetterListener] now watched by Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/eventStreamUnsubscriber-2#1665139875]
[DEBUG] [08/09/2017 06:33:10.121] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-4] [akka://ReactiveKafkaCantStopWithKamonAkka/system/log1-Logging$DefaultLogger] now watched by Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/eventStreamUnsubscriber-2#1665139875]
[DEBUG] [08/09/2017 06:33:10.202] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-5] [akka://ReactiveKafkaCantStopWithKamonAkka/user] now supervising Actor[akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0#-841935840]
[DEBUG] [08/09/2017 06:33:10.209] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-4] [akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0] started (akka.stream.impl.StreamSupervisor@39d54d69)
[DEBUG] [08/09/2017 06:33:10.585] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-4] [akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0] now supervising Actor[akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0/flow-0-0-unknown-operation#1323267554]
[DEBUG] [08/09/2017 06:33:10.699] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-4] [akka://ReactiveKafkaCantStopWithKamonAkka/system] now supervising Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1#1223392053]
[DEBUG] [08/09/2017 06:33:10.726] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-5] [akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0/flow-0-0-unknown-operation] started (akka.stream.impl.fusing.ActorGraphInterpreter@2e871c48)
[INFO] [2017-08-09 06:33:10,751] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-6] [o.a.k.c.consumer.ConsumerConfig] - ConsumerConfig values:
	auto.commit.interval.ms = 5000
	auto.offset.reset = latest
	bootstrap.servers = [localhost:19092]
	check.crcs = true
	client.id =
	connections.max.idle.ms = 540000
	enable.auto.commit = false
	exclude.internal.topics = true
	fetch.max.bytes = 52428800
	fetch.max.wait.ms = 500
	fetch.min.bytes = 1
	group.id = dummy-group-id
	heartbeat.interval.ms = 3000
	interceptor.classes = null
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	max.partition.fetch.bytes = 1048576
	max.poll.interval.ms = 300000
	max.poll.records = 500
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.recording.level = INFO
	metrics.sample.window.ms = 30000
	partition.assignment.strategy = [class org.apache.kafka.clients.consumer.RangeAssignor]
	receive.buffer.bytes = 65536
	reconnect.backoff.ms = 50
	request.timeout.ms = 305000
	retry.backoff.ms = 100
	sasl.jaas.config = null
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	session.timeout.ms = 10000
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer

[INFO] [2017-08-09 06:33:10,904] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-6] [o.a.kafka.common.utils.AppInfoParser] - Kafka version : 0.10.2.1
[INFO] [2017-08-09 06:33:10,904] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-6] [o.a.kafka.common.utils.AppInfoParser] - Kafka commitId : e89bffd6b2eff799
[DEBUG] [08/09/2017 06:33:10.905] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-6] [akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1] started (akka.kafka.KafkaConsumerActor@22b9f271)
[DEBUG] [08/09/2017 06:33:10.914] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-6] [akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1] received handled message Subscribe(Set(topic-no-exist),akka.kafka.KafkaConsumerActor$$anon$1@c27218) from Actor[akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0/$$a#1680143175]
[DEBUG] [08/09/2017 06:33:10.988] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-8] [akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1] received handled message Poll(akka.kafka.KafkaConsumerActor@22b9f271,true) from Actor[akka://ReactiveKafkaCantStopWithKamonAkka/deadLetters]
[ERROR] [08/09/2017 06:33:12.016] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-8] [akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1] WakeupException limit exceeded, stopping.
[DEBUG] [08/09/2017 06:33:12.052] [ReactiveKafkaCantStopWithKamonAkka-akka.kafka.default-dispatcher-8] [akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1] stopped

# .... jvm fun forever until signal was received....

^C[INFO] [2017-08-09 06:33:43,295] [shutdownHook1] [Main$] - Shutdown hook started.
[DEBUG] [08/09/2017 06:33:43.304] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0] stopping
[DEBUG] [08/09/2017 06:33:43.304] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-5] [akka://ReactiveKafkaCantStopWithKamonAkka/user] stopping
[INFO] [08/09/2017 06:33:43.333] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-5] [akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1] Message [akka.kafka.KafkaConsumerActor$Internal$Stop$] from Actor[akka://ReactiveKafkaCantStopWithKamonAkka/deadLetters] to Actor[akka://ReactiveKafkaCantStopWithKamonAkka/system/kafka-consumer-1#1223392053] was not delivered. [1] dead letters encountered. This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.
[DEBUG] [08/09/2017 06:33:43.348] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-5] [akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0] stopped
[DEBUG] [08/09/2017 06:33:43.348] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-2] [akka://ReactiveKafkaCantStopWithKamonAkka/user/StreamSupervisor-0/flow-0-0-unknown-operation] stopped
[DEBUG] [08/09/2017 06:33:43.357] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-10] [akka://ReactiveKafkaCantStopWithKamonAkka/user] stopped
[DEBUG] [08/09/2017 06:33:43.363] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [akka://ReactiveKafkaCantStopWithKamonAkka/system] received AutoReceiveMessage Envelope(Terminated(Actor[akka://ReactiveKafkaCantStopWithKamonAkka/user]),Actor[akka://ReactiveKafkaCantStopWithKamonAkka/user])
[DEBUG] [08/09/2017 06:33:43.369] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStream] subscribing StandardOutLogger to channel class akka.event.Logging$Error
[DEBUG] [08/09/2017 06:33:43.370] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-10] [EventStreamUnsubscriber] watching StandardOutLogger in order to unsubscribe from EventStream when it terminates
[DEBUG] [08/09/2017 06:33:43.370] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStream] subscribing StandardOutLogger to channel class akka.event.Logging$Warning
[DEBUG] [08/09/2017 06:33:43.371] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-2] [EventStreamUnsubscriber] watching StandardOutLogger in order to unsubscribe from EventStream when it terminates
[DEBUG] [08/09/2017 06:33:43.371] [ReactiveKafkaCantStopWithKamonAkka-akka.actor.default-dispatcher-3] [EventStream] shutting down: StandardOutLogger started
[INFO] [2017-08-09 06:33:43,436] [shutdownHook1] [Main$] - Shutdown hook finished.
```