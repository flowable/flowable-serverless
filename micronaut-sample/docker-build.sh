./mvnw package
docker build . -t my-app
docker run --network host my-app
