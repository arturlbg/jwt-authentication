FROM clojure:lein-2.11.2-bullseye-slim

WORKDIR /usr/src/app

COPY project.clj .

RUN lein deps

COPY . .

EXPOSE 3000 4000

CMD ["lein", "run"]