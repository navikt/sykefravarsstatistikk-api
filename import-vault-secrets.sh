#!/usr/bin/env sh

if test /var/run/secrets/nais.io/vault/vault_token;
then
    export VAULT_TOKEN=$(cat /var/run/secrets/nais.io/vault/vault_token)
    echo "Setting VAULT_TOKEN"
fi

if test -f /var/run/secrets/nais.io/altinn/x-nav-apiKey;
then
    export ALTINN_APIGW_APIKEY=$(cat /var/run/secrets/nais.io/altinn/x-nav-apiKey)
    echo "Setting ALTINN_APIGW_APIKEY"
fi
