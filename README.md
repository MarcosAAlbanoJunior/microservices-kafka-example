# microservices-kafka-example
docker-compose -f docker-compose.yml --env-file environment.env up

kafka-console-consumer.sh --topic product-created-events-topic --bootstrap-server localhost:9094 --property print.key=true