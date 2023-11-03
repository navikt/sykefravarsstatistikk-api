package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetEksportPerKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetMetadata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetSykefravær
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.LegacyEksporteringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaClient
import org.springframework.stereotype.Component
import java.util.function.Consumer
import java.util.stream.Collector
import java.util.stream.Collectors

@Component
@Deprecated("Slettes når Salesforce-teamet har gått over til eksport per kategori")
object LegacyEksporteringServiceUtils {
    const val OPPDATER_VIRKSOMHETER_SOM_ER_EKSPORTERT_BATCH_STØRRELSE = 1000
    const val EKSPORT_BATCH_STØRRELSE = 10000
    fun filterByKvartal(
        årstallOgKvartal: ÅrstallOgKvartal,
        sykefraværsstatistikkVirksomhetUtenVarighet: List<SykefraværsstatistikkVirksomhetUtenVarighet>
    ): List<SykefraværsstatistikkVirksomhetUtenVarighet> {
        return sykefraværsstatistikkVirksomhetUtenVarighet.stream()
            .filter { (årstall, kvartal): SykefraværsstatistikkVirksomhetUtenVarighet ->
                (årstall == årstallOgKvartal.årstall
                        && kvartal == årstallOgKvartal.kvartal)
            }
            .collect(Collectors.toList())
    }

    fun hentSisteKvartalIBeregningen(
        umaskertSykefraværsstatistikkSiste4Kvartaler: List<UmaskertSykefraværForEttKvartal>,
        årstallOgKvartal: ÅrstallOgKvartal
    ): UmaskertSykefraværForEttKvartal? {
        return umaskertSykefraværsstatistikkSiste4Kvartaler.stream()
            .filter { u: UmaskertSykefraværForEttKvartal? -> u!!.årstallOgKvartal == årstallOgKvartal }
            .findFirst()
            .orElse(null)
    }

    fun mapToSykefraværsstatistikkLand(
        umaskertSykefraværForEttKvartal: UmaskertSykefraværForEttKvartal
    ): SykefraværsstatistikkLand {
        return SykefraværsstatistikkLand(
            umaskertSykefraværForEttKvartal.Årstall,
            umaskertSykefraværForEttKvartal.kvartal,
            umaskertSykefraværForEttKvartal.antallPersoner,
            umaskertSykefraværForEttKvartal.dagsverkTeller,
            umaskertSykefraværForEttKvartal.dagsverkNevner
        )
    }

    fun toMap(
        sykefraværsstatistikkVirksomhetUtenVarighet: List<SykefraværsstatistikkVirksomhetUtenVarighet>
    ): Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> {
        val map: MutableMap<String, SykefraværsstatistikkVirksomhetUtenVarighet> = HashMap()
        sykefraværsstatistikkVirksomhetUtenVarighet.forEach(Consumer { sf: SykefraværsstatistikkVirksomhetUtenVarighet ->
            map[sf.orgnr] = sf
        })
        return map
    }

    fun getVirksomhetMetadataHashMap(
        virksomhetMetadataListe: List<VirksomhetMetadata>
    ): Map<String, VirksomhetMetadata> {
        val virksomhetMetadataHashMap = HashMap<String, VirksomhetMetadata>()
        virksomhetMetadataListe.forEach(Consumer { v: VirksomhetMetadata -> virksomhetMetadataHashMap[v.orgnr] = v })
        return virksomhetMetadataHashMap
    }

    fun getVirksomheterMetadataFraSubset(
        virksomhetMetadataHashMap: Map<String, VirksomhetMetadata>,
        subset: List<VirksomhetEksportPerKvartal>
    ): List<VirksomhetMetadata> {
        val virksomheterMetadata: MutableList<VirksomhetMetadata?> = ArrayList()
        subset.forEach(
            Consumer { v: VirksomhetEksportPerKvartal ->
                if (virksomhetMetadataHashMap.containsKey(v.getOrgnr())) {
                    virksomheterMetadata.add(virksomhetMetadataHashMap[v.getOrgnr()])
                }
            })
        return virksomheterMetadata.filterNotNull()
    }

    fun getVirksomhetSykefravær(
        virksomhetMetadata: VirksomhetMetadata,
        sykefraværsstatistikkVirksomhetUtenVarighet: Map<String, SykefraværsstatistikkVirksomhetUtenVarighet>
    ): VirksomhetSykefravær {
        val sfStatistikk = sykefraværsstatistikkVirksomhetUtenVarighet[virksomhetMetadata.orgnr]
        return VirksomhetSykefravær(
            virksomhetMetadata.orgnr,
            virksomhetMetadata.navn,
            ÅrstallOgKvartal(virksomhetMetadata.årstall, virksomhetMetadata.kvartal),
            sfStatistikk?.tapteDagsverk,
            sfStatistikk?.muligeDagsverk,
            sfStatistikk?.antallPersoner ?: 0
        )
    }

    fun getSykefraværMedKategoriForLand(
        årstallOgKvartal: ÅrstallOgKvartal, sykefraværsstatistikkLand: SykefraværsstatistikkLand
    ): SykefraværMedKategori {
        return SykefraværMedKategori(
            Statistikkategori.LAND,
            "NO",
            årstallOgKvartal,
            sykefraværsstatistikkLand.tapteDagsverk,
            sykefraværsstatistikkLand.muligeDagsverk,
            sykefraværsstatistikkLand.antallPersoner
        )
    }

    fun getSykefraværMedKategoriForSektor(
        virksomhetMetadata: VirksomhetMetadata,
        sykefraværsstatistikkSektor: List<SykefraværsstatistikkSektor>
    ): SykefraværMedKategori {
        val (_, _, sektorkode, antallPersoner, tapteDagsverk, muligeDagsverk) = sykefraværsstatistikkSektor.stream()
            .filter { (årstall, kvartal, sektorkode): SykefraværsstatistikkSektor -> sektorkode == virksomhetMetadata.sektor.sektorkode && årstall == virksomhetMetadata.årstall && kvartal == virksomhetMetadata.kvartal }
            .collect(
                toSingleton(
                    SykefraværsstatistikkSektor(
                        virksomhetMetadata.årstall,
                        virksomhetMetadata.kvartal,
                        virksomhetMetadata.sektor.sektorkode,
                        0,
                        null,
                        null
                    )
                )
            )
        return SykefraværMedKategori(
            Statistikkategori.SEKTOR,
            sektorkode,
            ÅrstallOgKvartal(virksomhetMetadata.årstall, virksomhetMetadata.kvartal),
            tapteDagsverk,
            muligeDagsverk,
            antallPersoner
        )
    }

    fun getSykefraværMedKategoriNæringForVirksomhet(
        virksomhetMetadata: VirksomhetMetadata,
        sykefraværsstatistikkForNæring: List<SykefraværsstatistikkForNæring>
    ): SykefraværMedKategori {
        val (_, _, næringkode, antallPersoner, tapteDagsverk, muligeDagsverk) = sykefraværsstatistikkForNæring.stream()
            .filter { (årstall, kvartal, næringkode): SykefraværsstatistikkForNæring -> næringkode == virksomhetMetadata.primærnæring && årstall == virksomhetMetadata.årstall && kvartal == virksomhetMetadata.kvartal }
            .collect(
                toSingleton(
                    SykefraværsstatistikkForNæring(
                        virksomhetMetadata.årstall,
                        virksomhetMetadata.kvartal,
                        virksomhetMetadata.primærnæring,
                        0,
                        null,
                        null
                    )
                )
            )
        return SykefraværMedKategori(
            Statistikkategori.NÆRING,
            næringkode,
            ÅrstallOgKvartal(virksomhetMetadata.årstall, virksomhetMetadata.kvartal),
            tapteDagsverk,
            muligeDagsverk,
            antallPersoner
        )
    }

    fun getSykefraværMedKategoriForNæring5Siffer(
        virksomhetMetadata: VirksomhetMetadata,
        sykefraværsstatistikkForNæringskodeList: List<SykefraværsstatistikkForNæringskode>
    ): List<SykefraværMedKategori> {
        val filteredList = getSykefraværsstatistikkNæring5Siffers(
            virksomhetMetadata, sykefraværsstatistikkForNæringskodeList
        )
        val resultatList: MutableList<SykefraværMedKategori> = ArrayList()
        filteredList.forEach(
            Consumer { (_, _, næringkode5siffer, antallPersoner, tapteDagsverk, muligeDagsverk): SykefraværsstatistikkForNæringskode ->
                resultatList.add(
                    SykefraværMedKategori(
                        Statistikkategori.NÆRINGSKODE,
                        næringkode5siffer,
                        ÅrstallOgKvartal(
                            virksomhetMetadata.årstall, virksomhetMetadata.kvartal
                        ),
                        tapteDagsverk,
                        muligeDagsverk,
                        antallPersoner
                    )
                )
            })
        return resultatList
    }

    fun getSykefraværsstatistikkNæring5Siffers(
        virksomhetMetadata: VirksomhetMetadata,
        sykefraværsstatistikkForNæringskodeList: List<SykefraværsstatistikkForNæringskode>
    ): List<SykefraværsstatistikkForNæringskode> {
        return sykefraværsstatistikkForNæringskodeList.stream()
            .filter { (årstall, kvartal, næringkode5siffer): SykefraværsstatistikkForNæringskode ->
                virksomhetMetadata.næringOgNæringskode5siffer.stream()
                    .anyMatch { (femsifferIdentifikator): Næringskode ->
                        (næringkode5siffer
                                == femsifferIdentifikator)
                    } && årstall == virksomhetMetadata.årstall && kvartal == virksomhetMetadata.kvartal
            }
            .collect(Collectors.toList())
    }

    fun cleanUpEtterBatch(legacyEksporteringRepository: LegacyEksporteringRepository) {
        legacyEksporteringRepository.oppdaterAlleVirksomheterIEksportTabellSomErBekrreftetEksportert()
        legacyEksporteringRepository.slettVirksomheterBekreftetEksportert()
    }

    fun leggTilOrgnrIEksporterteVirksomheterListaOglagreIDbNårListaErFull(
        orgnr: String,
        årstallOgKvartal: ÅrstallOgKvartal,
        virksomheterSomSkalFlaggesSomEksportert: MutableList<String>,
        legacyEksporteringRepository: LegacyEksporteringRepository,
        kafkaClient: KafkaClient
    ): Int {
        virksomheterSomSkalFlaggesSomEksportert.add(orgnr)
        return if (virksomheterSomSkalFlaggesSomEksportert.size
            == OPPDATER_VIRKSOMHETER_SOM_ER_EKSPORTERT_BATCH_STØRRELSE
        ) {
            lagreEksporterteVirksomheterOgNullstillLista(
                årstallOgKvartal,
                virksomheterSomSkalFlaggesSomEksportert,
                legacyEksporteringRepository,
                kafkaClient
            )
        } else {
            0
        }
    }

    fun lagreEksporterteVirksomheterOgNullstillLista(
        årstallOgKvartal: ÅrstallOgKvartal,
        virksomheterSomSkalFlaggesSomEksportert: MutableList<String>,
        legacyEksporteringRepository: LegacyEksporteringRepository,
        kafkaClient: KafkaClient
    ): Int {
        val antallSomSkalOppdateres = virksomheterSomSkalFlaggesSomEksportert.size
        val startWriteToDB = System.nanoTime()
        legacyEksporteringRepository.batchOpprettVirksomheterBekreftetEksportert(
            virksomheterSomSkalFlaggesSomEksportert, årstallOgKvartal
        )
        virksomheterSomSkalFlaggesSomEksportert.clear()
        val stopWriteToDB = System.nanoTime()
        kafkaClient.addDBOppdateringProcessingTime(startWriteToDB, stopWriteToDB)
        return antallSomSkalOppdateres
    }

    fun getListeAvVirksomhetEksportPerKvartal(
        årstallOgKvartal: ÅrstallOgKvartal?,
        legacyEksporteringRepository: LegacyEksporteringRepository
    ): List<VirksomhetEksportPerKvartal> {
        val virksomhetEksportPerKvartal = legacyEksporteringRepository.hentVirksomhetEksportPerKvartal(
            årstallOgKvartal!!
        )
        val virksomhetEksportPerKvartalStream =
            virksomhetEksportPerKvartal.stream().filter { v: VirksomhetEksportPerKvartal -> !v.eksportert() }
        return virksomhetEksportPerKvartalStream.collect(Collectors.toList())
    }

    private fun <T> toSingleton(emptySykefraværsstatistikk: T): Collector<T, *, T> {
        return Collectors.collectingAndThen(
            Collectors.toList()
        ) { list ->
            if (list.size != 1) {
                emptySykefraværsstatistikk
            } else {
                list[0]
            }
        }
    }
}
