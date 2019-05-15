#!/bin/sh

docker build -f Dockerfile-native -t vault-config-sample-native -m 4g .

echo
echo
echo "To run the docker container execute:"
echo "    $ docker run -p 8080:8080 vault-config-sample-native"