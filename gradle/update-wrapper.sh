#!/bin/sh

DIR=$(dirname $0)

exec gradle --project-dir ${DIR}/.. wrapper --warning-mode all
