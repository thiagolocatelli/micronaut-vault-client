#!/bin/sh
docker build . -t vault-config-sample -m 2g
echo
echo
echo "To run the docker container execute:"
echo "    $ docker run -p 8080:8080 vault-config-sample"