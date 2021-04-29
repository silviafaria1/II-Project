FROM alpine:3.11.5

RUN apk update && apk add postgresql

RUN mkdir /db/ /run/postgresql -p && chown postgres /db/ /run/postgresql/

USER postgres

COPY ["./init-mes-db.sh", "mes_db.sql", "/"]

VOLUME {"/db"}

EXPOSE 5432

CMD ["/init-mes-db.sh"]
