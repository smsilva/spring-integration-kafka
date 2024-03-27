# Prometheus

## Running a local Prometheus Instance

```bash
docker run \
  --name prometheus \
  --rm \
  --detach \
  --network host \
  --volume $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus:v2.51.0 \
    --config.file=/etc/prometheus/prometheus.yml
```

## Running a local Grafana Instance

```bash
docker run \
  --name grafana \
  --rm \
  --detach \
  --network host \
  grafana/grafana:10.3.5-ubuntu
```
