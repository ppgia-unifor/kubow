# locust

A distributed load generation and testing tool.

## Getting started

The file `locustfile-cfg.yaml` is a ConfigMap to hold the locust class (aka: locustfile). 
Edit this file with the application specific load generation. [See the locust official documentation](https://docs.locust.io/en/stable/writing-a-locustfile.html).

The file `attacked-host-cfg.yaml` holds the environment variable with the url of attacked host. Edit this by your needs.
