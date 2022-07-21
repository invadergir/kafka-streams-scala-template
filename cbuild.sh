#!/bin/bash

set -x
set -e
podman build -t kstemplate:latest . && echo Success.
