#!/bin/bash

./gradlew build -x test
java -jar build/smart-parking.jar