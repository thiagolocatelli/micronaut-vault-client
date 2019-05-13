FROM oracle/graalvm-ce:1.0.0-rc15 as graalvm

#RUN gu install native-image

COPY . /home/app/vault-config-sample
WORKDIR /home/app/vault-config-sample
RUN native-image -DXmx1024m --no-server -cp target/vault-config-sample-*.jar

FROM frolvlad/alpine-glibc
EXPOSE 8080
COPY --from=graalvm /home/app/vault-config-sample .
ENTRYPOINT ["./vault-config-sample"]
