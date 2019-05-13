FROM oracle/graalvm-ce:1.0.0-rc16 as graalvm
COPY . /home/app/complete
WORKDIR /home/app/complete
RUN native-image --no-server --allow-incomplete-classpath -cp target/vault-config-sample-*.jar

FROM frolvlad/alpine-glibc
EXPOSE 8080
COPY --from=graalvm /home/app/vault-config-sample .
ENTRYPOINT ["./vault-config-sample"]
