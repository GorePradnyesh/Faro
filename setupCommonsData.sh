#!/bin/bash

BACKEND_DATA_DIR=Backend/src/main/java/com/zik/faro/data
FRONTEND_DATA_DIR=Frontend/app/src/main/java/com/zik/faro

if [ ! -d "$FRONTEND_DATA_DIR/data" ]; then
    echo "    Creating symlink to $BACKEND_DATA_DIR for use by Frontend"
    ln -s $PWD/$BACKEND_DATA_DIR $FRONTEND_DATA_DIR
fi
