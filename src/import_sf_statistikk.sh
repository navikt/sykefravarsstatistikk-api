#!/usr/bin/env sh

# Usage: import_sf_statistikk.sh
#   -- følg instruksjoner på skjerm
# Importer sykefraværsstatistikk til sykefravarsstatistikk-api DB

# Input / lesing av parametre
read -p 'Årstall: ' aarstall
read -p 'Kvartal: ' kvartal
read -p 'Stat [tillatt: land, sektor, naring, virksomhet] -- "blank" betyr alle statistikk: ' statistikk
read -sp 'Auth bearer: ' bearer
echo " "
echo " "

# Denne funsksjonen henter statistikk via endepunktet i API-et
import_stat() {
      QUERY="curl --insecure -X POST \"https://arbeidsgiver.nais.preprod.local/sykefravarsstatistikk-api/provisjonering/import/$1/$2/$3\" -H 'Authorization: Bearer $bearer'"
      echo " "
      echo " > Import sykefraværsstatistikk $1 for årstall $2 and kvartal $3"
      eval ${QUERY}
}

# Main
#-----
echo "Import starter for årstall '$aarstall' og kvartal '$kvartal'"
if [ -z "$statistikk" ]; then
      echo "Importerer alle stats (input parameter 'statistikk' er blank)"
      import_stat land $aarstall $kvartal
      import_stat sektor $aarstall $kvartal
      import_stat naring $aarstall $kvartal
      import_stat virksomhet $aarstall $kvartal
else
      import_stat $statistikk $aarstall $kvartal
fi
echo " "
echo " Import er ferdig"